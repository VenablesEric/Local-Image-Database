package sample.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import sample.model.Datasource;
import sample.model.DatasourceController;
import sample.model.Folder;
import sample.util.FolderAndImageIO;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class SettingsWindowController {

    private static Stage stage;

    @FXML
    private ListView<String> folderListView;

    private ObservableList<String> foldersList = FXCollections.observableArrayList();
    private List<String> originalFolderList = new ArrayList<>();

    Task task;

    public static Stage getStage() {
        return stage;
    }

    public void initialise(Stage stage) {
        SettingsWindowController.stage = stage;

        folderListView.setItems(foldersList);

        getFolders();

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                if(task != null && task.isRunning())
                    return;

                List<String> folderList = foldersList.stream()
                        .map(object -> Objects.toString(object, null))
                        .collect(Collectors.toList());

                if (!foldersList.equals(originalFolderList)) {
                    ExitType exitType = unsavedDataAlert();

                    switch (exitType) {
                        case SAVE:
                            event.consume();
                            updateImages(true);
                            break;
                        case CANCEL:
                            event.consume();
                            break;
                        default:
                            SettingsWindowController.stage = null;
                            break;
                    }
                } else
                {
                    SettingsWindowController.stage = null;
                }
            }
        });
    }

    private void getFolders() {

        Task<ObservableList> task = new Task<ObservableList>() {
            @Override
            protected ObservableList call() throws Exception {
                foldersList.clear();
                originalFolderList = DatasourceController.queryFoldersForDirectory();
                foldersList.addAll(originalFolderList);
                return null;
            }
        };

        new Thread(task).start();
    }

    @FXML
    private void pickFolderToAddToList() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File folder = directoryChooser.showDialog(stage);

        if (folder != null) {
            String directory = folder.getAbsolutePath();
            if (!foldersList.contains(directory))
                foldersList.add(directory);
        }
    }

    @FXML
    private void updateImagesButton()
    {
        updateImages(false);
    }

    @FXML
    private void updateImages(boolean setCloseAfter) {
        if(task != null && task.isRunning())
            return;

        List<String> folderList = foldersList.stream()
                .map(object -> Objects.toString(object, null))
                .collect(Collectors.toList());

        List<String> foldersToRemove = new ArrayList<>(originalFolderList);
        foldersToRemove.removeAll(folderList);

        List<String> foldersToAdd = new ArrayList<>(folderList);
        foldersToAdd.removeAll(originalFolderList);


        task = new Task() {
            @Override
            protected Object call() throws Exception {

                for (String folderToAdd : foldersToAdd) {
                    DatasourceController.insertFolder(folderToAdd);
                }

                for (String folderToRemove : foldersToRemove) {
                    DatasourceController.deleteFolder(folderToRemove);
                }

                Datasource.getInstance().deleteUnusedImages();

                int imageCount = DatasourceController.queryCountImages();
                int sectionStart = 0;
                int toNext = 100;
                while (sectionStart < imageCount) {
                    if ((sectionStart + toNext) >= imageCount)
                        toNext = (imageCount - sectionStart);

                    List<String> images = DatasourceController.queryImages(sectionStart, toNext);

                    for (String image : images) {
                        if (!FolderAndImageIO.hasImage(image))
                            DatasourceController.deleteImage(image);
                    }

                    sectionStart += toNext;
                }


                for (Folder folder : Datasource.getInstance().queryFolders()) {
                    List<String> imageDirs = FolderAndImageIO.searchForImages(folder.getDirectory(), null);
                    DatasourceController.insertImages(imageDirs, folder.getId());
                }

                originalFolderList = folderList;
                MainWindowController.getInstances().refresh();

                return null;
            }
        };

        if(setCloseAfter) {
            task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    stage.close();
                    stage = null;
                }
            });
        }

        task.run();
    }

    @FXML
    private void removeFolders() {
        String selectedItem = folderListView.getSelectionModel().getSelectedItem();

        if (selectedItem != null)
            foldersList.remove(selectedItem);
    }

    @FXML
    private void resetFolderList() {
        foldersList.setAll(originalFolderList);
    }

    private enum ExitType {SAVE, DO_NOT_SAVE, CANCEL }
    private ExitType unsavedDataAlert()
    {
        ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.YES);
        ButtonType doNotSaveButton = new ButtonType("Don't Save", ButtonBar.ButtonData.NO);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        //Alert.AlertType type = Alert.AlertType.CONFIRMATION;

        Alert alert = new Alert(Alert.AlertType.WARNING, "", saveButton, doNotSaveButton, cancelButton);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.initOwner(stage);

        alert.getDialogPane().setHeaderText("Save Changes?");

        alert.getDialogPane().setContentText("Do you want to save changes to image database?");

        Optional<ButtonType> result = alert.showAndWait();

        if(result.get() == saveButton)
        {
            return ExitType.SAVE;
        }
        else if(result.get() == cancelButton)
        {
            return ExitType.CANCEL;
        }

        return ExitType.DO_NOT_SAVE;
    }
}
