package sample.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import sample.model.Datasource;
import sample.model.Folder;
import sample.util.FolderAndImageIO;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SettingsWindowController {

    @FXML
    ListView<String> listView;

    private ObservableList<String> folders = FXCollections.observableArrayList();

    List<String> originalFolderList = new ArrayList<String>();

    private  static Stage stage;

    public static Stage getStage() {
        return stage;
    }

    public void initialise(Stage stage)
    {
        SettingsWindowController.stage = stage;

        listView.setItems(folders);

        getFolders();

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                SettingsWindowController.stage = null;
            }
        });
    }

    public  void OpenFolderChooser() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File dir = directoryChooser.showDialog(stage);

        if (dir != null) {
            String path = dir.getAbsolutePath();
            if (!folders.contains(path))
                folders.add(path);
        }
    }

    public void getFolders()
    {
        Datasource.getInstance().queryFolders();

        Task<ObservableList> task = new Task<ObservableList>() {
            @Override
            protected ObservableList call() throws Exception {
                folders.clear();
                originalFolderList = Datasource.getInstance().queryFoldersForDirectory();
                folders.addAll(originalFolderList);
                return null;
            }
        };

        new Thread(task).start();
    }

    @FXML
    public void update()
    {
        List<String> folderList = folders.stream()
                .map(object -> Objects.toString(object, null))
                .collect(Collectors.toList());

        List<String> foldersToRemove = new ArrayList<>(originalFolderList);
        foldersToRemove.removeAll(folderList);

        List<String> foldersToAdd = new ArrayList<>(folderList);
        foldersToAdd.removeAll(originalFolderList);

        System.out.println("Current");
        folderList.forEach(System.out::println);
        System.out.println("OG");
        originalFolderList.forEach(System.out::println);
        System.out.println("To Add");
        foldersToAdd.forEach(System.out::println);

        for(String folderToAdd : foldersToAdd)
        {
            System.out.println("Adding new Folders");
            Datasource.getInstance().insertFolder(folderToAdd);
            System.out.println("Added new Folders");
        }

        for (String folderToRemove : foldersToRemove)
        {
            System.out.println("Removing new Folders");
            Datasource.getInstance().deleteFolder(folderToRemove);
            System.out.println("Removed new Folders");
        }

        System.out.println("STEP 1");
        Datasource.getInstance().deleteUnusedImages();

        System.out.println("STEP 2");
        int imageCount = Datasource.getInstance().queryCountImages();
        int sectionStart = 0;
        int toNext = 100;
        while (sectionStart < imageCount)
        {
            if((sectionStart + toNext) >= imageCount )
                toNext = (imageCount - sectionStart);

            List<String> images = Datasource.getInstance().queryImages(sectionStart,toNext);

            for (String image: images) {
                if (!FolderAndImageIO.hasImage(image))
                    Datasource.getInstance().deleteImage(image);
            }

            sectionStart += toNext;
        }

        System.out.println("STEP 3");

        for(Folder folder : Datasource.getInstance().queryFolders())
        {
            List<String> imageDirs = FolderAndImageIO.getAllImages2(folder.getDirectory(), null);
            Datasource.getInstance().insertImages(imageDirs,folder.getId());
        }

        System.out.println("STEP 4");
        originalFolderList = folderList;
        MainWindowController.getInstances().Refresh();
    }

    @FXML
    public void removeFolders()
    {
        String selectedItem = listView.getSelectionModel().getSelectedItem();

        if (selectedItem != null)
            folders.remove(selectedItem);
    }

/*    public void addFolders()
    {

    }*/

/*    public void scanForImages()
    {

    }

    public void removeImages()
    {

    }*/

    public void restore()
    {
        folders.clear();
        folders.addAll(originalFolderList);
    }

}
