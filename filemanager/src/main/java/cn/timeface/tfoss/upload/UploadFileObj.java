package cn.timeface.tfoss.upload;

import java.io.File;

import cn.timeface.tfoss.utils.MD5;

/**
 * author: rayboot  Created on 15/9/28.
 * email : sy0725work@gmail.com
 */
public class UploadFileObj {

    File file;

    public UploadFileObj(File file) {
        this.file = file;
    }

    public UploadFileObj(String filePath) {
        this.file = new File(filePath);
    }

    public File getRealUploadFile() {
        return this.file;
    }

    public String getPath() {
        return this.file.getAbsolutePath();
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getObjectKey() {
        int index = file.getAbsolutePath().lastIndexOf(".");
        if (index < 0) {
            throw new IllegalArgumentException("没有查找到合法后缀.");
        }
        return "some_folder_name" + "/" + MD5.md5sum(file) + file.getAbsolutePath().substring(index - 1);
    }
}
