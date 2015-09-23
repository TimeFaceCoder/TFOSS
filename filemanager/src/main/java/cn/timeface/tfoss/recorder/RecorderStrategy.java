package cn.timeface.tfoss.recorder;

import java.io.File;
import java.util.List;

/**
 * author: rayboot  Created on 15/9/23.
 * email : sy0725work@gmail.com
 */
public abstract class RecorderStrategy {
    //删除所有记录
    public abstract void clear();

    //删除记录
    public abstract void deleteRecorder(String key);

    //添加任务
    public abstract void addRecorder(String key, File file);

    //获取上传任务列表
    public abstract List<File> getAllRecorders();
}
