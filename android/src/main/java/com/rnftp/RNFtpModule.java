package com.rnftp;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

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
        String hostname = config.hasKey("host") ? config.getString("host") : "";
        String username = config.hasKey("username") ? config.getString("username") : "anonymous";
        String password = config.hasKey("password") ? config.getString("password") : "anonymous@";

        if (hostname == "") {
            promise.reject("ERROR","Expected hostname.");
        } else {
            try {
                client = new FTPClient();
                String[] address = hostname.split(":");
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

    @ReactMethod
    public void list(String path, final Promise promise){
        if (path == null) {
            promise.reject("ERROR", "Expected path.");
        } else {
            try {
                FTPFile[] files = client.listFiles(path);
                JSONObject json = new JSONObject();
                JSONArray arrfiles = new JSONArray();
                for (FTPFile file : files) {
                    Calendar modifiedDate = file.getTimestamp();
                    String modifiedDateString = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss zzz")).format(modifiedDate.getTime());

                    JSONObject data = new JSONObject();
                    data.put("name", file.getName());
                    data.put("type", file.getType());
                    data.put("size", file.getSize());
                    data.put("modifiedDate", modifiedDateString);

                    arrfiles.put(data);
                }
                json.put("results", arrfiles);
                promise.resolve(json.toString());
            } catch (Exception e) {
                promise.reject("ERROR", e.getMessage());
            }
        }
    }

    @ReactMethod
    public void downloadFile(final String remoteFile, final String localFile, final Promise promise)
    {
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
}
