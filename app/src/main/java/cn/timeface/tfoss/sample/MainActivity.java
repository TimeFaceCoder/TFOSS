package cn.timeface.tfoss.sample;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;

import cn.timeface.tfoss.upload.UploadFileObj;
import cn.timeface.tfoss.upload.UploadManager;

public class MainActivity extends AppCompatActivity {

    public String serverAddress;
    public String endPoint;
    public String bucketName;
    UploadManager uploadManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Remember.init(getApplicationContext(), "temp");

        try {
            ApplicationInfo appInfo = this.getPackageManager(). getApplicationInfo(this.getPackageName(), PackageManager.GET_META_DATA);
            this.serverAddress = appInfo.metaData.getString("ServerAddress");
            this.bucketName = appInfo.metaData.getString("BucketName");
            this.endPoint = appInfo.metaData.getString("EndPoint");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        uploadManager = new UploadManager(getApplicationContext(), serverAddress, endPoint, bucketName);
        uploadManager.setRecorderStrategy(new SimpleRecorderStrategy());
        uploadManager.setFolderName("test1");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            uploadManager.upload(new UploadFileObj(new File("/mnt/sdcard/Download/swift.jpg")));
            return true;
        }else if (id == R.id.action_delete) {
        }

        return super.onOptionsItemSelected(item);
    }

}
