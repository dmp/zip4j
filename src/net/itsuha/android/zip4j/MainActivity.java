
package net.itsuha.android.zip4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.Collator;
import java.util.Comparator;
import java.util.List;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

import org.apache.commons.io.IOUtils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SearchViewCompat;
import android.support.v4.widget.SearchViewCompat.OnQueryTextListenerCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

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

    public void readAssetsZip() throws ZipException {
        ZipFile z = new ZipFile(mFile);
        List<FileHeader> l = (List<FileHeader>) z.getFileHeaders();
        for (FileHeader h : l) {
            Log.i(TAG, "Name : " + h.getFileName());
            Log.d(TAG, "directory: " + h.isDirectory());
            Log.d(TAG, "size: " + h.getUncompressedSize());
        }
    }

    public void copyAssetsZipToExternal() throws IOException {
        AssetManager am = getAssets();
        File targetDir = getFilesDir();
        mFile = new File(targetDir, ZIP_NAME);
        OutputStream os = new FileOutputStream(mFile);
        InputStream is = am.open(ZIP_NAME);
        IOUtils.copy(is, os);
        os.close();
        is.close();
    }

    /**
     * This class holds the per-item data in our Loader.
     */
    public static class AppEntry {
        private String mFileName;
        private long mSize;
        private boolean mDirectory;
        private String mDate;
        private Drawable mIcon;

        public String getFileName() {
            return mFileName;
        }

        public void setFileName(String filename) {
            this.mFileName = filename;
        }

        public long getSize() {
            return mSize;
        }

        public void setSize(long size) {
            this.mSize = size;
        }

        public boolean isDirectory() {
            return mDirectory;
        }

        public void setmDirectory(boolean directory) {
            this.mDirectory = directory;
        }

        public String getDate() {
            return mDate;
        }

        public void setDate(String date) {
            this.mDate = date;
        }

        public Drawable getIcon() {
            return mIcon;
        }

        public void setIcon(Drawable icon) {
            mIcon = icon;
        }
    }

    /**
     * Perform alphabetical comparison of application entry objects.
     */
    public static final Comparator<AppEntry> ALPHA_COMPARATOR = new Comparator<AppEntry>() {
        private final Collator sCollator = Collator.getInstance();

        @Override
        public int compare(AppEntry object1, AppEntry object2) {
            return sCollator.compare(object1.getFileName(), object2.getFileName());
        }
    };

    /**
     * A custom Loader that loads all of the installed applications.
     */
    public static class AppListLoader extends AsyncTaskLoader<List<AppEntry>> {
        List<AppEntry> mApps;

        public AppListLoader(Context context) {
            super(context);
        }

        /**
         * This is where the bulk of our work is done. This function is called
         * in a background thread and should generate a new set of data to be
         * published by the loader.
         */
        @Override
        public List<AppEntry> loadInBackground() {
            // Retrieve all known applications.

            // Create corresponding array of entries and load their labels.

            // Sort the list.

            // Done!
            return null;
        }

        /**
         * Called when there is new data to deliver to the client. The super
         * class will take care of delivering it; the implementation here just
         * adds a little more logic.
         */
        @Override
        public void deliverResult(List<AppEntry> apps) {
            if (isReset()) {
                // An async query came in while the loader is stopped. We
                // don't need the result.
                if (apps != null) {
                    onReleaseResources(apps);
                }
            }
            List<AppEntry> oldApps = apps;
            mApps = apps;

            if (isStarted()) {
                // If the Loader is currently started, we can immediately
                // deliver its results.
                super.deliverResult(apps);
            }

            // At this point we can release the resources associated with
            // 'oldApps' if needed; now that the new result is delivered we
            // know that it is no longer in use.
            if (oldApps != null) {
                onReleaseResources(oldApps);
            }
        }

        /**
         * Handles a request to start the Loader.
         */
        @Override
        protected void onStartLoading() {
            if (mApps != null) {
                // If we currently have a result available, deliver it
                // immediately.
                deliverResult(mApps);
            }
        }

        /**
         * Handles a request to stop the Loader.
         */
        @Override
        protected void onStopLoading() {
            // Attempt to cancel the current load task if possible.
            cancelLoad();
        }

        /**
         * Handles a request to cancel a load.
         */
        @Override
        public void onCanceled(List<AppEntry> apps) {
            super.onCanceled(apps);

            // At this point we can release the resources associated with 'apps'
            // if needed.
            onReleaseResources(apps);
        }

        /**
         * Handles a request to completely reset the Loader.
         */
        @Override
        protected void onReset() {
            super.onReset();

            // Ensure the loader is stopped
            onStopLoading();

            // At this point we can release the resources associated with 'apps'
            // if needed.
            if (mApps != null) {
                onReleaseResources(mApps);
                mApps = null;
            }
        }

        /**
         * Helper function to take care of releasing resources associated with
         * an actively loaded data set.
         */
        protected void onReleaseResources(List<AppEntry> apps) {
            // For a simple List<> there is nothing to do. For something
            // like a Cursor, we would close it here.
        }
    }

    public static class AppListAdapter extends ArrayAdapter<AppEntry> {
        private final LayoutInflater mInflater;

        public AppListAdapter(Context context) {
            super(context, android.R.layout.simple_list_item_2);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void setData(List<AppEntry> data) {
            clear();
            if (data != null) {
                for (AppEntry e : data) {
                    add(e);
                }
            }
        }

        /**
         * Populate new items in the list.
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;

            if (convertView == null) {
                view = mInflater.inflate(R.layout.zip_list_items, parent, false);
            } else {
                view = convertView;
            }

            AppEntry item = getItem(position);
            ((ImageView) view.findViewById(R.id.ziplist_icon))
                    .setImageDrawable(item.getIcon());
            ((TextView) view.findViewById(R.id.ziplist_filename))
                    .setText(item.getFileName());
            ((TextView) view.findViewById(R.id.ziplist_size))
                    .setText((String.valueOf(item.getSize())));
            ((TextView) view.findViewById(R.id.ziplist_date))
                    .setText(item.getDate());

            return view;
        }
    }

    public static class AppListFragment extends ListFragment
            implements LoaderManager.LoaderCallbacks<List<AppEntry>> {

        // This is the Adapter being used to display the list's data.
        AppListAdapter mAdapter;

        // If non-null, this is the current filter the user has provided.
        String mCurFilter;

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            // Give some text to display if there is no data. In a real
            // application this would come from a resource.
            setEmptyText("No applications");

            // We have a menu item to show in action bar.
            setHasOptionsMenu(true);

            // Create an empty adapter we will use to display the loaded data.
            mAdapter = new AppListAdapter(getActivity());
            setListAdapter(mAdapter);

            // Start out with a progress indicator.
            setListShown(false);

            // Prepare the loader. Either re-connect with an existing one,
            // or start a new one.
            getLoaderManager().initLoader(0, null, this);
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            // Place an action bar item for searching.
            MenuItem item = menu.add("Search");
            item.setIcon(android.R.drawable.ic_menu_search);
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
                    | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
            View sv = SearchViewCompat.newSearchView(getActivity());
            SearchViewCompat.setOnQueryTextListener(sv, new OnQueryTextListenerCompat() {
                @Override
                public boolean onQueryTextChange(String newText) {
                    // Called when the action bar search text has changed. Since
                    // this
                    // is a simple array adapter, we can just have it do the
                    // filtering.
                    mCurFilter = !TextUtils.isEmpty(newText) ? newText : null;
                    mAdapter.getFilter().filter(mCurFilter);
                    return true;
                }

                @Override
                public boolean onQueryTextSubmit(String query) {
                    // Don't care about this.
                    return true;
                }

            });
            item.setActionView(sv);
        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            // Insert desired behavior here.
            Log.i("LoaderCustom", "Item clicked: " + id);
        }

        @Override
        public Loader<List<AppEntry>> onCreateLoader(int id, Bundle args) {
            // This is called when a new Loader needs to be created. This
            // sample only has one Loader with no arguments, so it is simple.
            return new AppListLoader(getActivity());
        }

        @Override
        public void onLoadFinished(Loader<List<AppEntry>> loader, List<AppEntry> data) {
            // Set the new data in the adapter.
            mAdapter.setData(data);

            // The list should now be shown.
            if (isResumed()) {
                setListShown(true);
            } else {
                setListShownNoAnimation(true);
            }
        }

        @Override
        public void onLoaderReset(Loader<List<AppEntry>> loader) {
            // Clear the data in the adapter.
            mAdapter.setData(null);
        }
    }
}
