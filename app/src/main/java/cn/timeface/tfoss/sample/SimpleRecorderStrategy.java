package cn.timeface.tfoss.sample;

import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.timeface.tfoss.recorder.RecorderStrategy;


/**
 * author: rayboot  Created on 15/9/23.
 * email : sy0725work@gmail.com
 */
public class SimpleRecorderStrategy extends RecorderStrategy {

    final String UPLOAD_TASK = "UPLOAD_TASK";


    @Override
    public void clear() {
        String result = Remember.getString(UPLOAD_TASK, null);

        String[] keys = result.split("|");

        for (String key : keys) {
            Remember.remove(key);
        }
        Remember.remove(UPLOAD_TASK);
    }

    @Override
    public void deleteRecorder(String key) {
        StringBuilder res = new StringBuilder();
        String[] keys = Remember.getString(UPLOAD_TASK, null).split("|");
        for (String k : keys) {
            if (k.equals(key)) {
                continue;
            }
            res.append(k);
            res.append("|");
        }
        if (res.length() > 0) {
            res.deleteCharAt(res.length() - 1);
        }

        Remember.putString(UPLOAD_TASK, res.toString());
        Remember.remove(key);
    }

    @Override
    public void addRecorder(String key, File file) {
        String result = Remember.getString(UPLOAD_TASK, null);
        if (TextUtils.isEmpty(result)) {
            Remember.putString(UPLOAD_TASK, key);
        } else {
            Remember.putString(UPLOAD_TASK, result + "|" + key);
        }

        Remember.putString(key, file.getAbsolutePath());
    }

    @Override
    public List<File> getAllRecorders() {
        List<File> res = new ArrayList<>(10);
        String[] keys = Remember.getString(UPLOAD_TASK, null).split("|");

        for (String key : keys) {
            File file = new File(Remember.getString(key, ""));
            if (!file.exists()) {
                continue;
            }
            res.add(file);
        }
        return res;
    }
}
