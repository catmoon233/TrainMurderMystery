package io.wifi.syncrequests;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.Nullable;

public class SyncRequests {
    public String url_root;

    public SyncRequests(String url) {
        this.url_root = url;
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
            reqUrl = reqUrl + "/" + uuidStr + "/" + key;
        else
            reqUrl = reqUrl + "/" + uuidStr;
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
            reqUrl = reqUrl + "/" + uuidStr + "/" + key;
        else
            reqUrl = reqUrl + "/" + uuidStr;

        try {
            return sendGet(reqUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String sendGet(String URL) throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            URI uri = URI.create(URL);
            HttpGet request = new HttpGet(uri);
            // request.setHeader("Accept", "application/json");
            String response = client.execute(request,
                    httpResponse -> EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8));
            return response;
        }
    }

    public static boolean sendPost(String url, String textBody) throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(url);
            request.setHeader("Content-Type", "text/plain; charset=UTF-8");
            request.setEntity(new StringEntity(textBody, StandardCharsets.UTF_8));

            String response = client.execute(request,
                    httpResponse -> EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8));
            // System.out.println("POST Response: " + response);
            return true;
        }
    }
}
