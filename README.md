# TFOSS
ALiYun OSS.


#### What is TFOSS
A Lib contain upload & download manager use aliyun oss.


## How to use

###First of all
Config the `ServerAddress`,`BucketName` and `EndPoint` which in `AndroidManifest.xml`.

```xml
        <meta-data android:name="ServerAddress" android:value="http://your/server/address/that/request/sts/token"/>
        <meta-data android:name="EndPoint" android:value="oss-cn-hangzhou.aliyuncs.com"/>
        <meta-data android:name="BucketName" android:value="your_bucket_name"/>
```

###Second 
Implement your Recorder strategy.
The `RecorderStrategy` have some method like `addRecorder`,`deleteRecorder`,`getAllRecorders` and `clear`.
I implement a `SimpleRecorderStrategy` which use `SharedPreferences`.You could use some other strategy like DB and so on.

```java
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
```


###Third
Extend UploadFileObj and Override some method.

`getFinalUploadFile`(default is the origin file)
You could change the final upload file.
`getObjectKey` The ObjectKey in aliyun oss.
 


###Forth 
Init your UploadManager or DownloadManager in `Application` or `Activity`.

```java
        
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
    }
```


###The last step 
DO  DO  DO

```java
uploadManager.upload(new UploadFileObj(new File("/mnt/sdcard/Download/swift.jpg")));
```


###Download

```java
downloadManager.download(objectKey, "mnt/sdcard/oss_demo_dir/1.jpg");
```


Usage
--------

Gradle:

    dependencies {
        compile 'cn.timeface.tfoss:filemanager:1.1.1'
    }


    
