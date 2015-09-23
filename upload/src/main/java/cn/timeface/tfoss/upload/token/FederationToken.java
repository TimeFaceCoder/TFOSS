package cn.timeface.tfoss.upload.token;

/**
 * author: rayboot  Created on 15/9/22.
 * email : sy0725work@gmail.com
 */
public class FederationToken {
    private String ak;
    private String sk;
    private String token;
    private long expiration;

    public FederationToken(String ak, String sk, String token, long expiredTime) {
        this.ak = ak;
        this.sk = sk;
        this.token = token;
        this.expiration = expiredTime;
    }

    public String getAk() {
        return ak;
    }

    public void setAk(String ak) {
        this.ak = ak;
    }

    public String getSk() {
        return sk;
    }

    public void setSk(String sk) {
        this.sk = sk;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getExpiration() {
        return expiration;
    }

    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }
}
