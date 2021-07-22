package sample.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Contains static methods that are use to find images in a given directory.
 */
public class ImageFinder {

    // List of valid image types.
    private static final String[] imageTypes = {"png", "jpeg", "jpg"};

    /**
     * Look for all valid images in directory. Recurse for each folder in directory.
     *
     * @param folderDirectory folder to search for images.
     * @param foldersToSkip   prevents shortcut folder infinite loop.
     * @return list of image directories.
     */
    public static List<String> searchForImages(String folderDirectory, List<String> foldersToSkip) {
        List<String> imageDirectories = new ArrayList<>();

        if (foldersToSkip == null)
            foldersToSkip = new ArrayList<>();

        if (foldersToSkip.contains(folderDirectory))
            return imageDirectories;

        foldersToSkip.add(folderDirectory);

        File directory = new File(folderDirectory);
        File[] files = directory.listFiles();

        for (File file : files) {
            if (file.isDirectory()) {
                imageDirectories.addAll(searchForImages(file.getAbsolutePath(), foldersToSkip));
            } else if (isValidImageType(file.getName())) {
                imageDirectories.add(file.getAbsolutePath());
            }
        }
        return imageDirectories;
    }

    /**
     * Check to see if the image in the directory has a valid file type from {@code imagesTypes}
     *
     * @param imageDirectory the image to check.
     * @return true if images is of a valid type.
     */
    private static boolean isValidImageType(String imageDirectory) {
        if (imageDirectory == null)
            return false;

        // Get the file type that is at the end of the directory path.
        int dotIndex = imageDirectory.lastIndexOf('.');

        if (dotIndex == -1)
            return false;

        String fileType = imageDirectory.substring(dotIndex + 1);

        return Arrays.stream(imageTypes).anyMatch(fileType::equals);
    }

    /**
     * Check to see if image directory exists.
     *
     * @param imageDirectory the image directory to check.
     * @return true if image exists.
     */
    public static boolean doesImageExists(String imageDirectory) {
        File file = new File(imageDirectory);

        if (file.exists() && !file.isDirectory())
            return true;

        return false;
    }
}
