package cn.timeface.tfoss.download;

import android.content.Context;
import android.util.Log;

import com.alibaba.sdk.android.oss.callback.GetFileCallback;
import com.alibaba.sdk.android.oss.model.OSSException;
import com.alibaba.sdk.android.oss.storage.OSSFile;
import com.alibaba.sdk.android.oss.storage.TaskHandler;

import java.util.WeakHashMap;

import cn.timeface.tfoss.OSSManager;

/**
 * author: rayboot  Created on 15/9/23.
 * email : sy0725work@gmail.com
 */
public class DownloadManager extends OSSManager {
    WeakHashMap<String, TaskHandler> queue = new WeakHashMap<>(10);

    public DownloadManager(Context context, String serverAddress, String endPoint, String bucketName) {
        super(context, serverAddress, endPoint, bucketName);
    }

    public void download(String objectKey, String filePath) {

        OSSFile ossFile = ossService.getOssFile(bucket, objectKey);
        TaskHandler task = ossFile.ResumableDownloadToInBackground(filePath, new GetFileCallback() {
            @Override
            public void onSuccess(String objectKey, String filePath) {
                Log.d(TAG, "[onSuccess] - " + objectKey + " storage path: " + filePath);

                queue.remove(objectKey);
            }

            @Override
            public void onProgress(String objectKey, int byteCount, int totalSize) {
                Log.d(TAG, "[onProgress] - current download: " + objectKey + " bytes:" + byteCount + " in total:" + totalSize);
            }

            @Override
            public void onFailure(String objectKey, OSSException ossException) {
                Log.e(TAG, "[onFailure] - download " + objectKey + " failed!\n" + ossException.toString());
                ossException.printStackTrace();
            }
        });
        this.queue.put(objectKey, task);
    }

    public void cancel(String key) {
        if (this.queue.get(key) != null) {
            this.queue.remove(key).cancel();
        }
    }
}
