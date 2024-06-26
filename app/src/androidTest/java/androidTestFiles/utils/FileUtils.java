package androidTestFiles.utils;

import static androidTestFiles.utils.parent.BaseTest.PATH_COURSES_TESTS;

import android.content.Context;
import android.util.Log;

import org.digitalcampus.oppia.analytics.Analytics;
import org.digitalcampus.oppia.utils.storage.ExternalStorageStrategy;
import org.digitalcampus.oppia.utils.storage.InternalStorageStrategy;
import org.digitalcampus.oppia.utils.storage.Storage;
import org.digitalcampus.oppia.utils.storage.StorageAccessStrategy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import androidx.test.platform.app.InstrumentationRegistry;

public class FileUtils {

    public static  StorageAccessStrategy[] getStorageStrategiesBasedOnDeviceAvailableStorage() {
        Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();
        ExternalStorageStrategy externalStrategy = new ExternalStorageStrategy();
        String externalLocation = externalStrategy.getStorageLocation(ctx);

        ArrayList<StorageAccessStrategy> params = new ArrayList<>();
        params.add(new InternalStorageStrategy());
        if (externalLocation != null){
            params.add(externalStrategy);
        }
        return params.toArray(new StorageAccessStrategy[0]);
    }


    ///from https://github.com/riggaroo/android-retrofit-test-examples/blob/master/RetrofitTestExample/app/src/androidTest/java/za/co/riggaroo/retrofittestexample/RestServiceTestHelper.java

    public static String convertStreamToString(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString().trim();
    }

    public static String getStringFromFile(Context context, String filePath) throws IOException{

        try (InputStream stream = context.getResources().getAssets().open(filePath)) {
            String string = convertStreamToString(stream);
            return string;
        }
    }

    public static void copyFileFromAssets(Context context, String assetsDir, String filename, File destination, String destinationFilename){
        try {
            String source = assetsDir + File.separator + filename;
            InputStream is = InstrumentationRegistry.getInstrumentation().getContext().getResources().getAssets().open(source);
            if(!destination.exists()){
                Storage.createFolderStructure(context);
                boolean success = destination.mkdirs();
                Log.d("Utils", success ? "s":"n");
            }


            OutputStream os = new FileOutputStream(new File(destination, destinationFilename));
            //Copy File
            byte[] buffer = new byte[1024];
            int read;
            while((read = is.read(buffer)) != -1){
                os.write(buffer, 0, read);
            }
            is.close();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void createFileWithContents(Context ctx, String contents, File destination, String destinationFilename){

        if(!destination.exists()){
            Storage.createFolderStructure(ctx);
            boolean success = destination.mkdirs();
            Log.d("Utils", success ? "s":"n");
        }

        try {
            File pageFile = new File(destination, destinationFilename);
            FileOutputStream os = new FileOutputStream (pageFile);
            os.write(contents.getBytes(StandardCharsets.UTF_8));
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void copyFileFromAssets(Context context, String assetsDir, String filename, File destination){
        copyFileFromAssets(context, assetsDir, filename, destination, filename);
    }

    public static void copyZipFromAssets(Context context, String filename){
        File downloadPath = new File(Storage.getDownloadPath(context));
        copyFileFromAssets(context, PATH_COURSES_TESTS, filename, downloadPath);
    }

    public static void copyZipFromAssetsPath(Context context, String path, String filename){
        File downloadPath = new File(Storage.getDownloadPath(context));
        copyFileFromAssets(context, path, filename, downloadPath);
    }

    public static void copyFileToDir(File file, File mediaDir, boolean deleteOnError) {
        try {
            org.apache.commons.io.FileUtils.copyFileToDirectory(file, mediaDir, true);
        }catch (IOException e) {
            Analytics.logException(e);
            if (deleteOnError){
                org.digitalcampus.oppia.utils.storage.FileUtils.deleteFile(file);
            }
        }
    }
}
