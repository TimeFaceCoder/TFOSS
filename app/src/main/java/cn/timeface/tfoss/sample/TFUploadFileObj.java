package cn.timeface.tfoss.sample;

import java.io.File;

import cn.timeface.tfoss.upload.UploadFileObj;

/**
 * author: rayboot  Created on 15/9/28.
 * email : sy0725work@gmail.com
 */
public class TFUploadFileObj extends UploadFileObj {
    public TFUploadFileObj(File file) {
        super(file);
    }

    public TFUploadFileObj(String filePath) {
        super(filePath);
    }


}
