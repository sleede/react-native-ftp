package com.rnftp;

import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class RNFtpModule extends ReactContextBaseJavaModule {
    private final ReactApplicationContext reactContext;

    private FTPClient client;

    public RNFtpModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "RNFtp";
    }

    @ReactMethod
    public void connect(final ReadableMap config, final Promise promise){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String hostname = config.hasKey("hostname") ? config.getString("hostname") : "";
                String username = config.hasKey("username") ? config.getString("username") : "anonymous";
                String password = config.hasKey("password") ? config.getString("password") : "anonymous@";
                int timeout = config.hasKey("timeout") ? config.getInt("timeout") : 0;
                String systemType = config.hasKey("systemType") ? config.getString("systemType") : null;

                if (hostname == "") {
                    promise.reject("ERROR","Expected hostname.");
                } else {
                    try {
                        client = new FTPClient();

                        if (systemType != null) {
                            FTPClientConfig ftpConfig = new FTPClientConfig(systemType);
                            client.configure(ftpConfig);
                        }

                        String[] address = hostname.split(":");
                        client.setDefaultTimeout(timeout);
                        client.setConnectTimeout(timeout);
                        if (address.length == 2) {
                            String host = address[0];
                            int port = Integer.parseInt(address[1]);
                            client.connect(host, port);
                        } else {
                            client.connect(hostname);
                        }
                        client.enterLocalPassiveMode();
                        Boolean isLogin = client.login(username, password);

                        // 自动检测并修复非标准的 Win32NT 系统类型
                        if (systemType == null && isLogin) {
                            try {
                                String remoteSystem = client.getSystemType();
                                if (remoteSystem != null && remoteSystem.toUpperCase().contains("WIN32NT")) {
                                    // 这是一个已知的兼容性问题：服务器报告 Win32NT 但通常输出 Unix 格式列表
                                    FTPClientConfig autoConfig = new FTPClientConfig(FTPClientConfig.SYST_UNIX);
                                    client.configure(autoConfig);
                                }
                            } catch (IOException e) {
                                // 忽略检测错误，避免干扰主流程
                            }
                        }

                        promise.resolve(isLogin);
                    } catch (Exception e) {
                        promise.reject("ERROR",e.getMessage());
                    }
                }
            }
        }).start();
    }

    @ReactMethod
    public void list(final String path, final Promise promise){
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (path == null) {
                    promise.reject("ERROR", "Expected path.");
                } else {
                    try {
                        FTPFile[] files = client.listFiles(path);

                        WritableArray fileMaps = Arguments.createArray();

                        for (FTPFile file : files) {
                            WritableMap fileMap = Arguments.createMap();

                            Calendar modifiedDate = file.getTimestamp();
                            TimeZone tz = TimeZone.getTimeZone("CET");
                            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
                            df.setTimeZone(tz);
                            String modifiedDateString = df.format(modifiedDate.getTime());

                            fileMap.putString("name", file.getName());
                            fileMap.putInt("type", file.getType());
                            fileMap.putDouble("size", file.getSize());
                            fileMap.putString("modifiedDate", modifiedDateString);

                            fileMaps.pushMap(fileMap);
                        }


                        promise.resolve(fileMaps);
                    } catch (Exception e) {
                        promise.reject("ERROR", e.getMessage());
                    }
                }
            }
        }).start();
    }

    @ReactMethod
    public void downloadFile(final String remoteFile, final String localFile, final Promise promise)
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.setFileType(FTP.BINARY_FILE_TYPE);
                    File downloadFile = new File(localFile);
                    OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(downloadFile));
                    boolean isSuccess = client.retrieveFile(remoteFile, outputStream);
                    outputStream.close();
                    promise.resolve(isSuccess);
                } catch (Exception e) {
                    promise.reject("ERROR", e.getMessage());
                }
            }
        }).start();
    }

    @ReactMethod
    public void disconnect(final Promise promise){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.logout();
                    client.disconnect();
                    promise.resolve(true);
                } catch (IOException e) {
                    promise.reject("ERROR", e.getMessage());
                }
            }
        }).start();
    }
}
