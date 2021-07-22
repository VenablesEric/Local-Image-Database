package sample.model;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Wrapper class for Datasource to make access to the database threadsafe.
 * The class is used to connects to a database that stores image locations and image tags
 */
public class DatasourceController {

    private static ReentrantLock lock = new ReentrantLock();

    /**
     * Opens or creates a new database.
     *
     * @return has the database been open.
     */
    public static boolean open() {
        lock.lock();
        try {
            return Datasource.getInstance().open();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Close database.
     */
    public static void close() {
        lock.lock();
        try {
            Datasource.getInstance().close();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Insert a folder directory that will be use to search for images.
     *
     * @param directory folder path to find images from.
     */
    public static void insertFolder(String directory) {
        lock.lock();
        try {
            Datasource.getInstance().insertFolder(directory);
        } finally {
            lock.unlock();
        }
    }


    /**
     * Insert a list of image directories and link them to their corresponding folder.
     *
     * @param directories list of image directories.
     * @param folderID    an id that can be found in the folders table.
     */
    public static void insertImages(List<String> directories, int folderID) {
        lock.lock();
        try {
            Datasource.getInstance().insertImages(directories, folderID);
        } finally {
            lock.unlock();
        }
    }


    /**
     * Insert the link between an image and tag into the imageTag table.
     *
     * @param directory directory of an image.
     * @param tag       name of a tag.
     */
    public static void insertImageTag(String directory, String tag) {
        lock.lock();
        try {
            Datasource.getInstance().insertImageTag(directory, tag);
        } finally {
            lock.unlock();
        }
    }


    /**
     * Remove directory of folder from the folders table.
     *
     * @param folder the folder directory.
     */
    public static void deleteFolder(String folder) {
        lock.lock();
        try {
            Datasource.getInstance().deleteFolder(folder);
        } finally {
            lock.unlock();
        }
    }


    /**
     * Remove image directory from the images table.
     *
     * @param directory the image directory.
     */
    public static void deleteImage(String directory) {
        lock.lock();
        try {
            Datasource.getInstance().deleteImage(directory);
        } finally {
            lock.unlock();
        }
    }


    /**
     * Remove images from image table that does not have a corresponding parent folder.
     */
    public static void deleteUnusedImages() {
        lock.lock();
        try {
            Datasource.getInstance().deleteUnusedImages();
        } finally {
            lock.unlock();
        }
    }


    /**
     * Remove tag from a image.
     *
     * @param directory the image directory.
     * @param tag       tag to remove.
     */
    public static void deleteImageTag(String directory, String tag) {
        lock.lock();
        try {
            Datasource.getInstance().deleteImageTag(directory, tag);
        } finally {
            lock.unlock();
        }
    }


    /**
     * Get all entries and rows in the folders table.
     *
     * @return a list of that contains folder directory and folder id.
     */
    public static List<QueryFolderEntry> queryFolders() {
        lock.lock();
        try {
            return Datasource.getInstance().queryFolders();
        } finally {
            lock.unlock();
        }
    }


    /**
     * Get a list of folder directories from the folders table.
     *
     * @return a list of folder directories.
     */
    public static List<String> queryFoldersForDirectory() {
        lock.lock();
        try {
            return Datasource.getInstance().queryFoldersForDirectory();
        } finally {
            lock.unlock();
        }
    }


    /**
     * Is image directory present in images table.
     *
     * @param directory image directory.
     * @return true if image if present in images table.
     */
    public static boolean hasImageInDatabase(String directory) {
        if (directory == null || directory.isBlank())
            return false;

        lock.lock();
        try {
            return Datasource.getInstance().hasImage(directory);
        } finally {
            lock.unlock();
        }
    }


    /**
     * Get a limited list of image directories from image table.
     *
     * @param from start point from the images table.
     * @param next how many image directories from image table.
     * @return list of image directories.
     */
    public static List<String> queryImages(int from, int next) {
        lock.lock();
        try {
            return Datasource.getInstance().queryImages(from, next);
        } finally {
            lock.unlock();
        }
    }


    /**
     * Get the total number of image directories in image table.
     *
     * @return the number of image in the images table.
     */
    public static int queryCountImages() {
        lock.lock();
        try {
            return Datasource.getInstance().queryCountImages();
        } finally {
            lock.unlock();
        }
    }


    /**
     * Get a limited list of image directories from image table that have matching tags.
     *
     * @param tags list of tags to filter by.
     * @param from start point from the images table.
     * @param next how many image directories from image table.
     * @return list of image directories which matching tags.
     */
    public static List<String> queryImagesWithTags(List<String> tags, int from, int next) {
        lock.lock();
        try {
            return Datasource.getInstance().queryImagesWithTags(tags, from, next);
        } finally {
            lock.unlock();
        }
    }


    /**
     * Get the total number of image directories in image table with matching tags.
     *
     * @param tags list of tags to filter by.
     * @return number of images with matching tags in images table.
     */
    public static int queryCountImagesWithTags(List<String> tags) {
        lock.lock();
        try {
            return Datasource.getInstance().queryCountImagesWithTags(tags);
        } finally {
            lock.unlock();
        }
    }


    /**
     * Get a list of all tags in tags table.
     *
     * @return list of tags.
     */
    public static List<String> queryTags() {
        lock.lock();
        try {
            return Datasource.getInstance().queryTags();
        } finally {
            lock.unlock();
        }
    }


    /**
     * Get a list of all tags that are linked with the selected image directory.
     *
     * @param directory image directory.
     * @return list of tags linked with image directory.
     */
    public static List<String> queryTagsOnImage(String directory) {
        lock.lock();
        try {
            return Datasource.getInstance().queryTagsOnImage(directory);
        } finally {
            lock.unlock();
        }
    }
}
