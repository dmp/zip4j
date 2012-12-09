package net.itsuha.android.zip4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

import org.apache.commons.io.IOUtils;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity
{
	private static final String TAG = MainActivity.class.getSimpleName();
	private static final String ZIP_NAME = "zip4j_src_1.3.1.zip";
	private File mFile;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        try {
			copyAssetsZipToExternal();
			readAssetsZip();
		} catch (ZipException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
    }
    
    public void readAssetsZip() throws ZipException{
    		ZipFile z = new ZipFile(mFile);
    		List<FileHeader> l = (List<FileHeader>)z.getFileHeaders();
    		for(FileHeader h: l){
    			Log.i(TAG, "Name : " +h.getFileName());
    			Log.d(TAG, "directory: " +h.isDirectory());
    			Log.d(TAG, "size: "+h.getUncompressedSize());
    		}
    }
    
    public void copyAssetsZipToExternal() throws IOException{
    	AssetManager am = getAssets();
    	File targetDir = getFilesDir();
    	mFile = new File(targetDir, ZIP_NAME);
    	OutputStream os = new FileOutputStream(mFile);
    	InputStream is = am.open(ZIP_NAME);
    	IOUtils.copy(is, os);
    	os.close();
    	is.close();
    }
}
