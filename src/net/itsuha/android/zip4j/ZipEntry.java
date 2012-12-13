
package net.itsuha.android.zip4j;

import net.lingala.zip4j.model.FileHeader;

import android.text.TextUtils;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * This class holds the per-item data in our Loader.
 */
@SuppressWarnings("serial")
public class ZipEntry implements Serializable{
    protected String mName;
    protected FileHeader mFileHeader;
    protected ZipDirectory mParent;

    public ZipEntry() {
    }

    public ZipEntry(String name, FileHeader fileHeader) {
        super();
        mName = name;
        mFileHeader = fileHeader;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public ZipDirectory getParent() {
        return mParent;
    }

    public boolean hasParent() {
        return mParent != null;
    }

    public FileHeader getFileHeader() {
        return mFileHeader;
    }

    public void setFileHeader(FileHeader fileHeader) {
        mFileHeader = fileHeader;
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

        public void addFile(String relativePath, FileHeader fileHeader) {
            if (TextUtils.isEmpty(relativePath)) {
                throw new IllegalArgumentException();
            }
            String[] pathArray = relativePath.split("/");
            if (pathArray.length == 1) {
                add(new ZipEntry(relativePath, fileHeader));
            } else {
                LinkedList<String> path = new LinkedList<String>();
                Collections.addAll(path, pathArray);

                String file = path.removeLast();
                addDirectory(path)
                        .add(new ZipEntry(file, fileHeader));

            }
        }
    }
}
