package sample.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FolderAndImageIO {

    private static final String[] imageTypes = {"png","jpeg", "jpg"};

    public static List<String> searchForImages(String folderDirectory, List<String> foldersToSkip)
    {
        List<String> imageDirectories = new ArrayList<>();

        if(foldersToSkip == null)
            foldersToSkip = new ArrayList<>();

        if(foldersToSkip.contains(folderDirectory))
            return imageDirectories;

        foldersToSkip.add(folderDirectory);

        File directory = new File(folderDirectory);
        File[] files = directory.listFiles();

        for (File file : files) {
            if (file.isDirectory()) {
                imageDirectories.addAll(searchForImages(file.getAbsolutePath(),foldersToSkip));
            } else if (isValidImageType(file.getName())) {
                imageDirectories.add(file.getAbsolutePath());
            }
        }
        return imageDirectories;
    }

    private static boolean isValidImageType(String imageDirectory)
    {
        if(imageDirectory == null)
            return  false;

        int dotIndex = imageDirectory.lastIndexOf('.');

        if(dotIndex == -1)
            return false;

        String fileType = imageDirectory.substring(dotIndex + 1);

        for(String imageType : imageTypes)
        {
            if(imageType.equals(fileType))
                return true;
        }

        return false;
    }

    public static boolean hasImage(String imageDirectory)
    {
        File file = new File(imageDirectory);

        if(file.exists() && !file.isDirectory())
            return true;

        return false;
    }
}
