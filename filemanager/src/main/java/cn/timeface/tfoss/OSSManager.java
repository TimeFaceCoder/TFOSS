package cn.timeface.tfoss;

import android.content.Context;
import android.widget.Toast;

import com.alibaba.sdk.android.oss.OSSService;
import com.alibaba.sdk.android.oss.OSSServiceProvider;
import com.alibaba.sdk.android.oss.model.AccessControlList;
import com.alibaba.sdk.android.oss.model.AuthenticationType;
import com.alibaba.sdk.android.oss.model.ClientConfiguration;
import com.alibaba.sdk.android.oss.model.OSSFederationToken;
import com.alibaba.sdk.android.oss.model.StsTokenGetter;
import com.alibaba.sdk.android.oss.storage.OSSBucket;
import com.alibaba.sdk.android.oss.util.OSSLog;

import java.io.File;

import cn.timeface.tfoss.recorder.RecorderStrategy;
import cn.timeface.tfoss.token.FederationToken;
import cn.timeface.tfoss.token.FederationTokenGetter;

/**
 * author: rayboot  Created on 15/9/23.
 * email : sy0725work@gmail.com
 */
public class OSSManager {

    protected final String TAG = "OSSManager";

    protected Context context;
    protected String serverAddress;
    protected String endPoint;
    protected String bucketName;
    protected OSSService ossService;
    protected String folderName;
    protected OSSBucket bucket;
    protected RecorderStrategy recorderStrategy;


    public OSSManager(Context context, String serverAddress, String endPoint, String bucketName) {
        this.context = context;
        this.serverAddress = serverAddress;
        this.endPoint = endPoint;
        this.bucketName = bucketName;

        initOssService(context);
        bucket = ossService.getOssBucket(this.bucketName);
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public void setRecorderStrategy(RecorderStrategy recorderStrategy) {
        this.recorderStrategy = recorderStrategy;
    }

    private void initOssService(final Context context) {
        ossService = OSSServiceProvider.getService();

        ossService.setApplicationContext(context);
        ossService.setGlobalDefaultACL(AccessControlList.PRIVATE); // 默认为private
        ossService.setAuthenticationType(AuthenticationType.FEDERATION_TOKEN);
        ossService.setGlobalDefaultHostId(endPoint);

        // 打开调试log
        if (BuildConfig.DEBUG) {
            OSSLog.enableLog();
        }

        ossService.setGlobalDefaultStsTokenGetter(new StsTokenGetter() {
            @Override
            public OSSFederationToken getFederationToken() {
                // 为指定的用户拿取服务其授权需求的FederationToken
                FederationToken token = FederationTokenGetter.getToken(serverAddress);
                if (token == null) {
                    Toast.makeText(context, "获取FederationToken失败!!!", Toast.LENGTH_SHORT).show();
                    return null;
                }
                return new OSSFederationToken(token.getAk(), token.getSk(), token.getToken(), token.getExpiration());
                // 将FederationToken设置到OSSService中
            }
        });
        ossService.setCustomStandardTimeWithEpochSec(System.currentTimeMillis() / 1000);


        ClientConfiguration conf = new ClientConfiguration();
        conf.setConnectTimeout(30 * 1000); // 设置全局网络连接超时时间，默认30s
        conf.setSocketTimeout(30 * 1000); // 设置全局socket超时时间，默认30s
        conf.setMaxConcurrentTaskNum(5); // 替换设置最大连接数接口，设置全局最大并发任务数，默认为6
        conf.setIsSecurityTunnelRequired(false); // 是否使用https，默认为false
        ossService.setClientConfiguration(conf);
    }

    protected String getObjectKey(File file) {
        return this.folderName + "/" + MD5.md5sum(file) + ".jpg";
    }
}
