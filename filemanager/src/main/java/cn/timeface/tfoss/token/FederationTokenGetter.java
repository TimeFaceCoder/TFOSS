package cn.timeface.tfoss.token;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * author: rayboot  Created on 15/9/22.
 * email : sy0725work@gmail.com
 */
public class FederationTokenGetter {

    private static FederationToken token;

    public static FederationToken getToken(String serverAddress) {
        token = getTokenFromServer(serverAddress);
        return token;
    }

    private static FederationToken getTokenFromServer(String serverAddress) {
        String queryUrl = "http://" + serverAddress + "/auth/stsSign";
        String responseStr = null;
        try {
            OkHttpClient httpClient = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(queryUrl)
                    .build();

            Response response = httpClient.newCall(request).execute();
            responseStr = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (responseStr == null) {
            return null;
        }
        try {
            JSONObject jsonObject = new JSONObject(responseStr);
            String ak = jsonObject.getString("tempAK");
            String sk = jsonObject.getString("tempSK");
            String securityToken = jsonObject.getString("token");
            long expireTime = jsonObject.getLong("expiration");
            return new FederationToken(ak, sk, securityToken, expireTime);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
