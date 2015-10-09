package cn.timeface.tfoss.upload;

import android.content.Context;
import android.util.Log;

import com.alibaba.sdk.android.oss.callback.DeleteCallback;
import com.alibaba.sdk.android.oss.callback.SaveCallback;
import com.alibaba.sdk.android.oss.model.OSSException;
import com.alibaba.sdk.android.oss.storage.OSSFile;
import com.alibaba.sdk.android.oss.storage.TaskHandler;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

import java.io.FileNotFoundException;
import java.util.WeakHashMap;

import cn.timeface.tfoss.OSSManager;


/**
 * author: rayboot  Created on 15/9/22.
 * email : sy0725work@gmail.com
 */
public class UploadManager extends OSSManager {
    WeakHashMap<String, TaskHandler> queue = new WeakHashMap<>(10);

    public UploadManager(Context context, String serverAddress, String endPoint, String bucketName) {
        super(context, serverAddress, endPoint, bucketName);
    }

    public void checkFileExist(UploadFileObj file, Callback callback) {
        OkHttpClient httpClient = new OkHttpClient();
        String url = String.format("http://%s.%s/%s", this.bucketName, this.endPoint, file.getObjectKey());
        Request request = new Request.Builder().head()
                .url(url)
                .build();
        httpClient.newCall(request).enqueue(callback);
    }

    public void upload(UploadFileObj file) {
        String key = file.getObjectKey();
        OSSFile ossFile = ossService.getOssFile(bucket, key);
        try {
            ossFile.setUploadFilePath(file.getFinalUploadFile().getAbsolutePath(), "application/octet-stream");
            TaskHandler task = ossFile.ResumableUploadInBackground(new SaveCallback() {
                @Override
                public void onSuccess(String objectKey) {
                    Log.d(TAG, "[onSuccess] - " + objectKey + " upload success!");
                    if (recorderStrategy != null) {
                        recorderStrategy.deleteRecorder(objectKey);
                    }
                    queue.remove(objectKey);
                }

                @Override
                public void onProgress(String objectKey, int byteCount, int totalSize) {
                    Log.d(TAG, "[onProgress] - current upload " + objectKey + " bytes: " + byteCount + " in total: " + totalSize);
                }

                @Override
                public void onFailure(String objectKey, OSSException ossException) {
                    Log.e(TAG, "[onFailure] - upload " + objectKey + " failed!\n" + ossException.toString());
                    ossException.printStackTrace();
                }
            });

            this.queue.put(file.getPath(), task);
            if (recorderStrategy != null) {
                recorderStrategy.addRecorder(key, file.getFile());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void cancel(UploadFileObj file) {
        String key = file.getObjectKey();
        if (this.queue.get(key) != null) {
            this.queue.remove(key).cancel();
        }
    }
}
