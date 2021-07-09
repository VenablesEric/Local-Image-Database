package sample.model;

import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class DatasourceController {

    private static ReentrantLock lock = new ReentrantLock();

    public static boolean open() {
        lock.lock();
        try {
            return Datasource.getInstance().open();
        } finally {
            lock.unlock();
        }
    }

    public static void close() {
        lock.lock();
        try {
            Datasource.getInstance().close();
        } finally {
            lock.unlock();
        }
    }

    public static void insertFolder(String directory) {
        if (lock.tryLock()) ;
        {
            try {
                Datasource.getInstance().insertFolder(directory);
            } finally {
                lock.unlock();
            }
        }
    }

    public static void insertImage(String directory, int folderID) {
        if (lock.tryLock()) ;
        {
            try {
                Datasource.getInstance().insertImage(directory, folderID);
            } finally {
                lock.unlock();
            }
        }
    }

    public static void insertImages(List<String> directories, int folderID) {
        if (lock.tryLock()) ;
        {
            try {
                Datasource.getInstance().insertImages(directories, folderID);
            } finally {
                lock.unlock();
            }
        }
    }

    public static void insertImageTag(String directory, String tag) {
        if (lock.tryLock()) ;
        {
            try {
                Datasource.getInstance().insertImageTag(directory, tag);
            } finally {
                lock.unlock();
            }
        }
    }

    public static void deleteFolder(String folder) {
        if (lock.tryLock()) ;
        {
            try {
                Datasource.getInstance().deleteFolder(folder);
            } finally {
                lock.unlock();
            }
        }
    }

    public static void deleteImage(String directory) {
        if (lock.tryLock()) ;
        {
            try {
                Datasource.getInstance().deleteImage(directory);
            } finally {
                lock.unlock();
            }
        }
    }

    public static void deleteUnusedImages() {
        if (lock.tryLock()) ;
        {
            try {
                Datasource.getInstance().deleteUnusedImages();
            } finally {
                lock.unlock();
            }
        }
    }

    public static void deleteImageTag(String directory, String tag) {
        if (lock.tryLock()) ;
        {
            try {
                Datasource.getInstance().deleteImageTag(directory, tag);
            } finally {
                lock.unlock();
            }
        }
    }

    public static void deleteUnusedTags() {
        if (lock.tryLock()) ;
        {
            try {
                Datasource.getInstance().deleteUnusedTags();
            } finally {
                lock.unlock();
            }
        }
    }

    public static List<Folder> queryFolders() {
        if (lock.tryLock()) ;
        {
            try {
                return Datasource.getInstance().queryFolders();
            } finally {
                lock.unlock();
            }
        }
    }

    public static List<String> queryFoldersForDirectory() {
        if (lock.tryLock()) ;
        {
            try {
                return Datasource.getInstance().queryFoldersForDirectory();
            } finally {
                lock.unlock();
            }
        }
    }

    public static List<String> queryImages(int from, int next) {
        if (lock.tryLock()) ;
        {
            try {
                return Datasource.getInstance().queryImages(from, next);
            } finally {
                lock.unlock();
            }
        }
    }

    public static int queryCountImages() {
        if (lock.tryLock()) ;
        {
            try {
                return Datasource.getInstance().queryCountImages();
            } finally {
                lock.unlock();
            }
        }
    }

    public static List<String> queryImagesWithTags(List<String> tags, int from, int next) {
        if (lock.tryLock()) ;
        {
            try {
                return Datasource.getInstance().queryImagesWithTags(tags, from, next);
            } finally {
                lock.unlock();
            }
        }
    }

    public static int queryCountImagesWithTags(List<String> tags) {
        if (lock.tryLock()) ;
        {
            try {
                return Datasource.getInstance().queryCountImagesWithTags(tags);
            } finally {
                lock.unlock();
            }
        }
    }

    public static List<String> queryTags() {
        if (lock.tryLock()) ;
        {
            try {
                return Datasource.getInstance().queryTags();
            } finally {
                lock.unlock();
            }
        }
    }

    public static List<String> queryTagsOnImage(String directory) {
        if (lock.tryLock()) ;
        {
            try {
                return Datasource.getInstance().queryTagsOnImage(directory);
            } finally {
                lock.unlock();
            }
        }
    }
}
