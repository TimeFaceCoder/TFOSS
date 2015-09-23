package cn.timeface.tfoss;

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
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.WeakHashMap;


/**
 * author: rayboot  Created on 15/9/22.
 * email : sy0725work@gmail.com
 */
public class UploadManager extends OSSManager {
    WeakHashMap<String, TaskHandler> queue = new WeakHashMap<>(10);

    public UploadManager(Context context, String serverAddress, String endPoint, String bucketName) {
        super(context, serverAddress, endPoint, bucketName);
    }

    public boolean checkFileExist(File file) {
        OkHttpClient httpClient = new OkHttpClient();
        String url = String.format("http://%s.%s/%s", this.bucketName, this.endPoint, getObjectKey(file));
        Request request = new Request.Builder().head()
                .url(url)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {

            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.code() == 200) {
                    //已上传
                } else {
                    //未上传，执行上传操作
                }
            }
        });
        return true;
    }

    public void upload(File file) {
        String filePath = file.getAbsolutePath();
        String key = getObjectKey(file);
        OSSFile ossFile = ossService.getOssFile(bucket, key);
        try {
            ossFile.setUploadFilePath(filePath, "application/octet-stream");
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
                    ossException.getException().printStackTrace();
                }
            });

            this.queue.put(file.getAbsolutePath(), task);
            if (recorderStrategy != null) {
                recorderStrategy.addRecorder(key, file);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void cancel(File file) {
        String key = getObjectKey(file);
        if (this.queue.get(key) != null) {
            this.queue.remove(key).cancel();
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
}
