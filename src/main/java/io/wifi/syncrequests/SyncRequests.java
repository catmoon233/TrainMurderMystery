package io.wifi.syncrequests;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

public class SyncRequests {
    public String url_root;
    public String key;

    public SyncRequests(String url, String key) {
        this.url_root = url;
        this.key = key;
    }

    /**
     * 设置某个Key的内容
     * 
     * @param playerUUID 玩家的UUID
     * @param key        请求需要的key
     * @return 返回JSON文本/文本。失败返回NULL
     */
    public Boolean setValue(UUID playerUUID, @Nullable String key, String value) {
        String uuidStr = playerUUID.toString();
        String reqUrl = url_root;
        if (key != null)
            reqUrl = reqUrl + "/set/" + key + "/" + uuidStr + "/" + key;
        else
            reqUrl = reqUrl + "/set/" + key + "/" + uuidStr;
        try {
            return sendPost(reqUrl, value);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取某个请求的内容
     * 
     * @param playerUUID 玩家的UUID
     * @param key        请求需要的key
     * @return 返回JSON文本/文本。失败返回NULL
     */
    public String getValue(UUID playerUUID, @Nullable String key) {
        String uuidStr = playerUUID.toString();
        String reqUrl = url_root;
        if (key != null)
            reqUrl = reqUrl + "/get/" + key + "/" + uuidStr + "/" + key;
        else
            reqUrl = reqUrl + "/get/" + key + "/" + uuidStr;

        try {
            return sendGet(reqUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String sendGet(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        try {
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("Accept", "text/plain");
            
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                return response.toString();
            } else {
                System.err.println("GET request failed with response code: " + responseCode);
                return null;
            }
        } finally {
            connection.disconnect();
        }
    }

    public static boolean sendPost(String urlString, String textBody) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        try {
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("Content-Type", "text/plain; charset=UTF-8");
            connection.setRequestProperty("Accept", "text/plain");
            
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = textBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return true;
            } else {
                System.err.println("POST request failed with response code: " + responseCode);
                return false;
            }
        } finally {
            connection.disconnect();
        }
    }
}
