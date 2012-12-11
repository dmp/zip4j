
package net.itsuha.android.zip4j;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * This class holds the per-item data in our Loader.
 */
public class ZipEntry {
    protected String mFullPath;
    protected String mName;
    protected long mSize;
    protected String mDate;
    protected Drawable mIcon;
    protected ZipDirectory mParent;

    public ZipEntry() {
    }

    public ZipEntry(String fullPath, String name, long size, String date, Drawable icon) {
        mFullPath = fullPath;
        mName = name;
        mSize = size;
        mDate = date;
        mIcon = icon;
    }

    public String getFullPath() {
        return mFullPath;
    }

    public void setFullPath(String fullPath) {
        mFullPath = fullPath;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public long getSize() {
        return mSize;
    }

    public void setSize(long size) {
        mSize = size;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        mDate = date;
    }

    public Drawable getIcon() {
        return mIcon;
    }

    public void setIcon(Drawable icon) {
        mIcon = icon;
    }

    public ZipDirectory getParent() {
        return mParent;
    }

    public boolean hasParent() {
        return mParent != null;
    }

    public static class ZipDirectory extends ZipEntry {
        protected LinkedList<ZipEntry> mChildren = new LinkedList<ZipEntry>();

        public ZipDirectory() {
        }

        public ZipDirectory(String name) {
            mName = name;
        }

        public List<ZipEntry> getChildren() {
            return mChildren;
        }

        public ZipDirectory changeDirectory(String name) {
            for (ZipEntry e : mChildren) {
                if (e instanceof ZipDirectory && e.mName.equals(name)) {
                    return (ZipDirectory) e;
                }
            }
            return this;
        }

        public ZipDirectory cdOrMkdir(String name) {
            ZipDirectory cd = changeDirectory(name);
            if (cd == this) {
                ZipDirectory newDir = new ZipDirectory(name);
                add(newDir);
                return newDir;
            } else {
                return cd;
            }
        }

        public void add(ZipEntry entry) {
            entry.mParent = this;
            mChildren.add(entry);
        }

        protected ZipDirectory addDirectory(LinkedList<String> path) {
            if (path == null || path.isEmpty()) {
                throw new IllegalArgumentException();
            }
            String head = path.remove();
            if (TextUtils.isEmpty(head)) {
                if (path.isEmpty()) {
                    return this;
                } else {
                    return addDirectory(path);
                }
            } else {
                // Change directory or create new directory with name 'head'
                ZipDirectory newDir = cdOrMkdir(head);
                if (path.isEmpty()) {
                    return newDir;
                } else {
                    return newDir.addDirectory(path);
                }
            }
        }

        public ZipDirectory addDirectory(String relativePath) {
            String[] pathArray = relativePath.split("/");
            LinkedList<String> path = new LinkedList<String>();
            Collections.addAll(path, pathArray);
            return addDirectory(path);
        }

        public void addFile(String relativePath, long size, int lastmod) {
            String[] pathArray = relativePath.split("/");
            LinkedList<String> path = new LinkedList<String>();
            Collections.addAll(path, pathArray);

            String file = path.removeLast();
            addDirectory(path)
                    .add(new ZipEntry(relativePath, file, size, String.valueOf(lastmod), null));
        }
    }
}
