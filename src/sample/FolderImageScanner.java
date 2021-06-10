package sample;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FolderImageScanner {

    public static List<String> Scan(String directory) {
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

    static String[] fileTypes = {"png","jpeg", "jpg"};
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
}
