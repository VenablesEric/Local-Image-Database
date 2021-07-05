package sample.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FolderAndImageIO {

    private static final String[] fileTypes = {"png","jpeg", "jpg"};

    public static List<String> ScanForImages(List<String> folders)
    {
        List<String> imageDirectories = new ArrayList<>();

        for (String folder : folders) {
            imageDirectories.addAll(ScanFolder(folder));
        }

        return imageDirectories;
    }

    public static List<String> ScanFolder(String directory) {
        List<String> test = new ArrayList<>();
        getAllImages(directory,test);

        return test;
    }

    private static void getAllImages(String directoryName, List<String> images) {

        if(directoryName == null || images == null)
            return;

        File directory = new File(directoryName);

        if(!directory.exists())
            return;

        File[] files = directory.listFiles();

        for (File file : files) {
            if(file.isDirectory()) {
                getAllImages(file.getAbsolutePath(), images);
                continue;
            }

            if(isValidImageType(file.getName()))
            {
                images.add(file.getAbsolutePath());
                //images.add(file.toURI().toString());
                System.out.println(file.getAbsolutePath());
            }
        }
    }

    private static boolean isValidImageType(String fileName)
    {
        if(fileName == null)
            return  false;

        int dotIndex = fileName.lastIndexOf('.');

        if(dotIndex == -1)
            return false;

        String ft = fileName.substring(dotIndex + 1);

        for(String fileType : fileTypes)
        {
            if(ft.equals(fileType))
                return true;
        }

        return false;
    }

    public static String getVailedFileType(String path)
    {
        if(path == null)
            return null;

        int dotIndex = path.lastIndexOf('.');

        if(dotIndex == -1)
            return null;

        String ft = path.substring(dotIndex + 1);

        for(String fileType : fileTypes)
        {
            if(ft.equals(fileType))
                return fileType;
        }

        return null;
    }

    public static List<String> getAllImages2(String directoryName, List<String> toSkip)
    {
        List<String> images = new ArrayList<>();

        if(toSkip == null)
            toSkip = new ArrayList<>();

        if(toSkip.contains(directoryName))
            return images;

        toSkip.add(directoryName);

        File directory = new File(directoryName);
        File[] files = directory.listFiles();

        for (File file : files) {
            if (file.isDirectory()) {
                images.addAll(getAllImages2(file.getAbsolutePath(),toSkip));
            } else if (isValidImageType(file.getName())) {
                images.add(file.getAbsolutePath());
            }
        }
        return images;
    }

    public static boolean hasImage(String imageDir)
    {
        File file = new File(imageDir);

        if(file.exists() && !file.isDirectory())
            return true;

        return false;
    }
}
