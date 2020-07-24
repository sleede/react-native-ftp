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

                if (hostname == "") {
                    promise.reject("ERROR","Expected hostname.");
                } else {
                    try {
                        client = new FTPClient();
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
