package sample.controllers;

import javafx.application.Platform;
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
import sample.model.DatasourceController;
import sample.model.QueryFolderEntry;
import sample.util.ImageFinder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Settings window to add or remove folders, and scan for images in folders.
 */
public class SettingsWindowController {

    private static Stage stage;

    @FXML
    private ListView<String> folderListView;

    // Use to keep folderListView updated with folders in the database.
    private ObservableList<String> foldersList = FXCollections.observableArrayList();
    private List<String> originalFolderList = new ArrayList<>();

    // Use to take long tasks off the ui thread, while making sure that one task can run at a time on this window.
    Task task;

    /**
     * @return current stage for window
     */
    public static Stage getStage() {
        return stage;
    }

    /**
     * Setup the settings window.
     *
     * @param stage that was used to open window.
     */
    public void initialise(Stage stage) {
        SettingsWindowController.stage = stage;

        // Set folder list table to be linked to list of folders.
        folderListView.setItems(foldersList);

        // Get a list of folders in database.
        getFolders();

        // Brings up and alert asking do you want to save changes before closing if changes have been made.
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                // Check to see if currently saving data.
                if (task != null && task.isRunning()) {
                    System.out.println("Running");
                    event.consume(); // prevent window from closing
                    return;
                }

                List<String> folderList = foldersList.stream()
                        .map(object -> Objects.toString(object, null))
                        .collect(Collectors.toList());

                // Has changes to the folder been made.
                if (!foldersList.equals(originalFolderList)) {
                    ExitType exitType = unsavedDataAlert();

                    switch (exitType) {
                        case SAVE:
                            event.consume(); // prevent window from closing
                            updateImages(true);
                            break;
                        case CANCEL:
                            event.consume(); // prevent window from closing
                            break;
                        default:
                            SettingsWindowController.stage = null;
                            break;
                    }
                } else {
                    SettingsWindowController.stage = null;
                }
            }
        });
    }

    /**
     * Get folders from database and display them in the listview
     */
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

    /**
     * Open directory chooser to add folder to the listview
     */
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

    /**
     * Rescan folders for images
     */
    @FXML
    private void rescanImagesButton() {
        updateImages(false);
    }

    /**
     * Remove images from database that are not on the hard drive or database.
     * Add new images that are found in the folders
     *
     * @param setCloseAfter should the window close after completing the task.
     */
    @FXML
    private void updateImages(boolean setCloseAfter) {
        if (task != null && task.isRunning())
            return;

        List<String> folderList = foldersList.stream()
                .map(object -> Objects.toString(object, null))
                .collect(Collectors.toList());

        List<String> foldersToRemove = new ArrayList<>(originalFolderList);
        foldersToRemove.removeAll(folderList);

        List<String> foldersToAdd = new ArrayList<>(folderList);
        foldersToAdd.removeAll(originalFolderList);


        // Move task off the ui thread
        task = new Task() {
            @Override
            protected Object call() throws Exception {
                for (String folderToAdd : foldersToAdd) {
                    DatasourceController.insertFolder(folderToAdd);
                }

                for (String folderToRemove : foldersToRemove) {
                    DatasourceController.deleteFolder(folderToRemove);
                }

                DatasourceController.deleteUnusedImages();

                // Check images in the 100s to see if they exists on the hard drive.
                int imageCount = DatasourceController.queryCountImages();
                int sectionStart = 0;
                int toNext = 100;
                while (sectionStart < imageCount) {
                    if ((sectionStart + toNext) >= imageCount)
                        toNext = (imageCount - sectionStart);

                    List<String> imageDirectories = DatasourceController.queryImages(sectionStart, toNext);

                    for (String image : imageDirectories) {
                        if (!ImageFinder.doesImageExists(image))
                            DatasourceController.deleteImage(image);
                    }
                    sectionStart += toNext;
                }

                // Look for images on the hard drive and add them to the database.
                for (QueryFolderEntry queryFolderEntry : DatasourceController.queryFolders()) {
                    List<String> imageDirs = ImageFinder.searchForImages(queryFolderEntry.getFolderDirectory(), null);
                    DatasourceController.insertImages(imageDirs, queryFolderEntry.getId());
                }

                // Update originalFolderList with the saved folder list
                originalFolderList = folderList;

                // Refresh images on the main window with updated database images.
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        MainWindowController.getInstances().refresh();
                    }
                });

                return null;
            }
        };

        // Close the window when the task is over
        if (setCloseAfter) {
            task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    stage.close();
                    stage = null;
                }
            });
        }

        new Thread(task).start();
    }

    /**
     * Remove folder from database
     */
    @FXML
    private void removeFolders() {
        String selectedItem = folderListView.getSelectionModel().getSelectedItem();

        if (selectedItem != null)
            foldersList.remove(selectedItem);
    }

    /**
     * Revert updates to the list that have not been saved.
     */
    @FXML
    private void resetFolderList() {
        foldersList.setAll(originalFolderList);
    }


    private enum ExitType {SAVE, DO_NOT_SAVE, CANCEL}

    /**
     * Alert box used to ask the user to save unsaved changed to the folder list.
     * The user can save, not save and cancel.
     *
     * @return the an exitType enum of selected option.
     */
    private ExitType unsavedDataAlert() {
        ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.YES);
        ButtonType doNotSaveButton = new ButtonType("Don't Save", ButtonBar.ButtonData.NO);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        Alert alert = new Alert(Alert.AlertType.WARNING, "", saveButton, doNotSaveButton, cancelButton);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.initOwner(stage);

        alert.getDialogPane().setHeaderText("Save Changes?");

        alert.getDialogPane().setContentText("Do you want to save changes to image database?");

        Optional<ButtonType> result = alert.showAndWait();

        if (result.get() == saveButton) {
            return ExitType.SAVE;
        } else if (result.get() == cancelButton) {
            return ExitType.CANCEL;
        }

        return ExitType.DO_NOT_SAVE;
    }
}
