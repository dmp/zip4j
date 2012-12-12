
package net.itsuha.android.zip4j;

import net.itsuha.android.zip4j.MainActivity.FragmentBackKeyListener;
import net.itsuha.android.zip4j.ZipEntry.ZipDirectory;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.util.Zip4jUtil;

import org.apache.commons.io.IOUtils;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.Collator;
import java.text.DateFormat;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Perform alphabetical comparison of application entry objects.
 */

public class ZipListFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<List<ZipEntry>>
        , FragmentBackKeyListener {

    /**
     * 
     */
    private static final int LOADER_ID = 0;

    public static final String ARGUMENT_ZIP_FILE = "arg_zipfile";

    // This is the Adapter being used to display the list's data.
    ZipListAdapter mAdapter;

    // If non-null, this is the current filter the user has provided.
    String mCurFilter;

    ZipListLoader mLoader;
    static String mFileName;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Give some text to display if there is no data. In a real
        // application this would come from a resource.
        setEmptyText("No files");

        // We have a menu item to show in action bar.
        setHasOptionsMenu(true);

        // Create an empty adapter we will use to display the loaded data.
        mFileName = getArguments().getString(ARGUMENT_ZIP_FILE);
        mAdapter = new ZipListAdapter(getActivity());
        setListAdapter(mAdapter);

        // Start out with a progress indicator.
        setListShown(false);

        // Prepare the loader. Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(LOADER_ID, null, this).forceLoad();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        ZipEntry item = (ZipEntry) l.getItemAtPosition(position);
        if (item instanceof ZipDirectory) {
            mLoader.cd(item.getName());
        } else {
            try {
                String path = Environment.getExternalStorageDirectory() + "/" + item.getName();
                File f = new File(path);
                FileOutputStream fos = new FileOutputStream(f);
                InputStream is = mLoader.mZipFile.getInputStream(item.getFileHeader());
                IOUtils.copy(is, fos);
                IOUtils.closeQuietly(fos);
                IOUtils.closeQuietly(is);
            } catch (ZipException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Loader<List<ZipEntry>> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created. This
        // sample only has one Loader with no arguments, so it is simple.
        mLoader = new ZipListLoader(getActivity());
        return mLoader;
    }

    @Override
    public void onLoadFinished(Loader<List<ZipEntry>> loader, List<ZipEntry> data) {
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
    public void onLoaderReset(Loader<List<ZipEntry>> loader) {
        // Clear the data in the adapter.
        mAdapter.setData(null);
    }

    @Override
    public boolean onBackKeyUp() {
        if (mLoader.canGoUp()) {
            mLoader.up();
            return true;
        } else {
            return false;
        }
    }

    /**
     * A custom Loader that loads all of the installed applications.
     */
    public static class ZipListLoader extends AsyncTaskLoader<List<ZipEntry>> {
        List<ZipEntry> mItems;
        ZipDirectory mBackendTree = null;
        Context mContext;
        ZipFile mZipFile;

        public ZipListLoader(Context context) {
            super(context);
            mContext = context.getApplicationContext();
        }

        /**
         * This is where the bulk of our work is done. This function is called
         * in a background thread and should generate a new set of data to be
         * published by the loader.
         */
        @Override
        public List<ZipEntry> loadInBackground() {
            if (mBackendTree == null) {
                ZipDirectory root = null;
                try {
                    root = readZipInFilesDir();
                } catch (ZipException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                mBackendTree = root;
            }

            // Create corresponding array of entries and load their labels.

            // Sort the list.

            // Done!
            return mBackendTree.getChildren();
        }

        private ZipDirectory createDummyData() {
            ZipDirectory root = new ZipDirectory();
            ZipDirectory foo = new ZipDirectory("foo");
            root.add(foo);
            ZipDirectory bar = new ZipDirectory("bar");
            root.add(bar);
            ZipEntry hoge = new ZipEntry();
            hoge.setName("hoge");
            foo.add(hoge);
            ZipEntry fuga = new ZipEntry();
            fuga.setName("fuga");
            root.add(fuga);
            return root;
        }

        private ZipDirectory readZipInFilesDir() throws ZipException {
            File f = new File(mContext.getFilesDir(), mFileName);
            ZipFile z = new ZipFile(f);
            mZipFile = z;
            z.setFileNameCharset("Shift_JIS");
            ZipDirectory root = new ZipDirectory();
            List<FileHeader> l = (List<FileHeader>) z.getFileHeaders();
            for (FileHeader h : l) {
                if (h.isDirectory()) {
                    root.addDirectory(h.getFileName());
                } else {
                    root.addFile(h.getFileName(), h);
                }
            }
            return root;
        }

        /**
         * Called when there is new data to deliver to the client. The super
         * class will take care of delivering it; the implementation here just
         * adds a little more logic.
         */
        @Override
        public void deliverResult(List<ZipEntry> items) {
            if (isReset()) {
                // An async query came in while the loader is stopped. We
                // don't need the result.
                if (items != null) {
                    onReleaseResources(items);
                }
            }
            List<ZipEntry> oldItems = items;
            mItems = items;

            if (isStarted()) {
                // If the Loader is currently started, we can immediately
                // deliver its results.
                super.deliverResult(items);
            }

            // At this point we can release the resources associated with
            // 'oldItems' if needed; now that the new result is delivered we
            // know that it is no longer in use.
            if (oldItems != null) {
                onReleaseResources(oldItems);
            }
        }

        /**
         * Handles a request to start the Loader.
         */
        @Override
        protected void onStartLoading() {
            if (mItems != null) {
                // If we currently have a result available, deliver it
                // immediately.
                deliverResult(mItems);
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
        public void onCanceled(List<ZipEntry> items) {
            super.onCanceled(items);

            // At this point we can release the resources associated with 'apps'
            // if needed.
            onReleaseResources(items);
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
            if (mItems != null) {
                onReleaseResources(mItems);
                mItems = null;
            }
        }

        /**
         * Helper function to take care of releasing resources associated with
         * an actively loaded data set.
         */
        protected void onReleaseResources(List<ZipEntry> apps) {
            // For a simple List<> there is nothing to do. For something
            // like a Cursor, we would close it here.
        }

        public void cd(String fileName) {
            mBackendTree = mBackendTree.changeDirectory(fileName);
            onContentChanged();
        }

        public boolean canGoUp() {
            return mBackendTree.hasParent();
        }

        public void up() {
            mBackendTree = mBackendTree.getParent();
            onContentChanged();
        }

        /**
         * Perform alphabetical comparison of application entry objects.
         */
        public static final Comparator<ZipEntry> ALPHA_COMPARATOR = new Comparator<ZipEntry>() {
            private final Collator sCollator = Collator.getInstance();

            @Override
            public int compare(ZipEntry object1, ZipEntry object2) {
                return sCollator.compare(object1.getName(), object2.getName());
            }
        };
    }

    public static class ZipListAdapter extends ArrayAdapter<ZipEntry> {
        private final LayoutInflater mInflater;

        public ZipListAdapter(Context context) {
            super(context, android.R.layout.simple_list_item_2);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void setData(Collection<ZipEntry> data) {
            clear();
            if (data != null) {
                for (ZipEntry e : data) {
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

            ZipEntry item = getItem(position);
            if (item instanceof ZipDirectory) {
                ((TextView) view.findViewById(R.id.ziplist_filename))
                        .setText(item.getName());
                ((TextView) view.findViewById(R.id.ziplist_size))
                        .setText("");
                ((TextView) view.findViewById(R.id.ziplist_date))
                        .setText("");
            } else {
                view.findViewById(R.id.ziplist_icon).setVisibility(View.INVISIBLE);
                ((TextView) view.findViewById(R.id.ziplist_filename))
                        .setText(item.getName());
                FileHeader h = item.getFileHeader();
                ((TextView) view.findViewById(R.id.ziplist_size))
                        .setText(String.valueOf(h.getUncompressedSize()));
                long time = Zip4jUtil.dosToJavaTme(h.getLastModFileTime());
                DateFormat sdf = getDateFormatInstance();
                ((TextView) view.findViewById(R.id.ziplist_date))
                        .setText(sdf.format(new Date(time)));
            }
            return view;
        }

        private DateFormat mSDF;

        private DateFormat getDateFormatInstance() {
            if (mSDF == null) {
                mSDF = DateFormat.getDateTimeInstance();
            }
            return mSDF;
        }
    }
}
