package cn.timeface.tfoss.upload;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.sdk.android.oss.OSSService;
import com.alibaba.sdk.android.oss.OSSServiceProvider;
import com.alibaba.sdk.android.oss.callback.DeleteCallback;
import com.alibaba.sdk.android.oss.callback.SaveCallback;
import com.alibaba.sdk.android.oss.model.AccessControlList;
import com.alibaba.sdk.android.oss.model.AuthenticationType;
import com.alibaba.sdk.android.oss.model.ClientConfiguration;
import com.alibaba.sdk.android.oss.model.OSSException;
import com.alibaba.sdk.android.oss.model.OSSFederationToken;
import com.alibaba.sdk.android.oss.model.StsTokenGetter;
import com.alibaba.sdk.android.oss.storage.OSSBucket;
import com.alibaba.sdk.android.oss.storage.OSSFile;
import com.alibaba.sdk.android.oss.util.OSSLog;

import java.io.File;
import java.io.FileNotFoundException;

import cn.timeface.tfoss.upload.recorder.RecorderStrategy;
import cn.timeface.tfoss.upload.token.FederationToken;
import cn.timeface.tfoss.upload.token.FederationTokenGetter;

/**
 * author: rayboot  Created on 15/9/22.
 * email : sy0725work@gmail.com
 */
public class UploadManager {

    private final String TAG = "GetAndUploadFileDemo";

    public Context context;
    public String serverAddress;
    public String endPoint;
    public String bucketName;
    public OSSService ossService;
    public String folderName;
    private OSSBucket bucket;
    RecorderStrategy recorderStrategy;


    public UploadManager(Context context, String serverAddress, String endPoint, String bucketName) {
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

    public boolean checkFileExist() {
        return false;
    }

    public void upload(File file) {
        String filePath = file.getAbsolutePath();
        String key = getObjectKey(file);
        OSSFile ossFile = ossService.getOssFile(bucket, key);
        try {
            ossFile.setUploadFilePath(filePath, "application/octet-stream");
            ossFile.ResumableUploadInBackground(new SaveCallback() {
                @Override
                public void onSuccess(String objectKey) {
                    Log.d(TAG, "[onSuccess] - " + objectKey + " upload success!");
                    if (recorderStrategy != null) {
                        recorderStrategy.deleteRecorder(objectKey);
                    }
                }

                @Override
                public void onProgress(String objectKey, int byteCount, int totalSize) {
                    Log.d(TAG, "[onProgress] - current upload " + objectKey + " bytes: " + byteCount + " in total: " + totalSize);
                }

                @Override
                public void onFailure(String objectKey, OSSException ossException) {
                    Log.e(TAG, "[onFailure] - upload " + objectKey + " failed!\n" + ossException.toString());
                    ossException.printStackTrace();
                    ossException.getException().printStackTrace();
                }
            });
            if (recorderStrategy != null) {
                recorderStrategy.addRecorder(key, file);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    // 断点上传
    public void upload(String filePath) {
        upload(new File(filePath));
    }

    public void delete(String filePath) {
        delete(new File(filePath));
    }

    public void delete(File file) {
        String key = getObjectKey(file);
        OSSFile ossFile = ossService.getOssFile(bucket, key);
        ossFile.deleteInBackground(new DeleteCallback() {
            @Override
            public void onSuccess(String s) {
                Log.e(TAG, "[onSuccess] - delete " + s);

                if (recorderStrategy != null) {
                    recorderStrategy.deleteRecorder(s);
                }
            }

            @Override
            public void onFailure(String s, OSSException e) {
                Log.e(TAG, "[onFailure] - delete " + s + " failed!\n" + e.toString());


            }
        });
    }

    private String getObjectKey(File file) {
        return this.folderName + "/" + MD5.md5sum(file) + ".jpg";
    }
}
