package cn.timeface.tfoss.download;

import android.content.Context;

import cn.timeface.tfoss.OSSManager;

/**
 * author: rayboot  Created on 15/9/23.
 * email : sy0725work@gmail.com
 */
public class DownloadManager extends OSSManager {
    public DownloadManager(Context context, String serverAddress, String endPoint, String bucketName) {
        super(context, serverAddress, endPoint, bucketName);
    }
}