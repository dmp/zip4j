
package net.itsuha.android.zip4j;

import org.apache.commons.io.IOUtils;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends FragmentActivity
{
    @SuppressWarnings("unused")
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String FRAGMENT_TAG = "zip";
    public static final String[] ZIP_NAME = { "zip4j_src_1.3.1.zip", "7zip-default.zip" };

    private File mFile;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        try {
            copyAssetsZipToFiles();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        Button b = (Button) findViewById(R.id.zip_button);
        b.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getSupportFragmentManager();
                FragmentTransaction ft = manager.beginTransaction();
                ZipListFragment f = new ZipListFragment();
                // Retain fields of Fragment as-is on configuration changes
                // "This can only be used with fragments not in the back stack"
                // http://developer.android.com/reference/android/support/v4/app/Fragment.html#setRetainInstance(boolean)
                f.setRetainInstance(true);
                ft.add(R.id.root_view, f, FRAGMENT_TAG);
                ft.commit();
            }
        });

    }

    public void copyAssetsZipToFiles() throws IOException {
        AssetManager am = getAssets();
        File targetDir = getFilesDir();
        mFile = new File(targetDir, ZIP_NAME[1]);
        OutputStream os = new FileOutputStream(mFile);
        InputStream is = am.open(ZIP_NAME[1]);
        IOUtils.copy(is, os);
        os.close();
        is.close();
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onKeyUp(int, android.view.KeyEvent)
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            FragmentManager fm = getSupportFragmentManager();
            Fragment f = fm.findFragmentByTag(FRAGMENT_TAG);
            if (f != null && f instanceof FragmentBackKeyListener) {
                if (!((FragmentBackKeyListener) f).onBackKeyUp()) {
                    FragmentTransaction ft = fm.beginTransaction();
                    ft.remove(f);
                    ft.commit();
                }
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    /**
     * ZipListFragment must implement this interface.
     */
    public interface FragmentBackKeyListener {
        /**
         * @return True if the listener has consumed the event, false otherwise.
         */
        public boolean onBackKeyUp();
    }

}
