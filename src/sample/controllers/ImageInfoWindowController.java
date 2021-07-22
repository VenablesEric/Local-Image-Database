package sample.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;

import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import sample.model.DatasourceController;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Window that displays the selected image and it tag data.
 */
public class ImageInfoWindowController {

    private static Stage stage;
    private static ImageInfoWindowController instance;

    private final String IMAGE_INFO_WINDOW_TITLE = "Image Info: ";

    @FXML
    private ScrollPane imageScrollPane;
    @FXML
    private ListView<String> imageTagsListView;
    @FXML
    ComboBox imageTagsComboBox;

    // Use to keep imageTagsListView updated with tags on the image.
    private ObservableList<String> imageTagList = FXCollections.observableArrayList();
    private List<String> originalImageTagList = new ArrayList<>();

    private String imageDirectory;

    private final int imageSize = 500;

    /**
     * @return singleton instance.
     */
    public static ImageInfoWindowController getInstance() {
        return instance;
    }

    /**
     * @return stage that was used to create window.
     */
    public static Stage getStage() {
        return stage;
    }

    /**
     * Load image and image data that is link with imageDirectory.
     *
     * @param stage          stage that was used to create window.
     * @param imageDirectory directory of image
     */
    public void initialize(Stage stage, String imageDirectory) {

        instance = this;
        ImageInfoWindowController.stage = stage;

        stage.setTitle(IMAGE_INFO_WINDOW_TITLE + imageDirectory);

        // Ask to save unsaved changes.
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                List<String> tags = imageTagList.stream()
                        .map(object -> Objects.toString(object, null))
                        .collect(Collectors.toList());

                if (!tags.equals(originalImageTagList)) {
                    if (unsavedDataAlert()) {
                        ImageInfoWindowController.stage = null;
                    } else {
                        event.consume();
                    }
                } else {
                    ImageInfoWindowController.stage = null;
                }
            }
        });

        // Set tag table to be linked to list of tags.
        imageTagsListView.setItems(imageTagList);
        this.imageDirectory = imageDirectory;

        // Load image and tags
        Task task = new Task() {
            @Override
            protected Object call() throws Exception {
                setImageTagList(imageDirectory);
                createImage(imageDirectory);
                return null;
            }
        };

        new Thread(task).start();
    }

    /**
     * Load image and display it.
     *
     * @param imageDirectory image directory.
     */
    private void createImage(String imageDirectory) {

        try {
            File file = new File(imageDirectory);
            Image image = new Image(file.toURI().toString());
            ImageView imageView = new ImageView(image);

            imageView.setFitWidth(imageSize);
            imageView.setFitHeight(imageSize);

            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);

            VBox imageContainer = new VBox();
            imageContainer.setAlignment(Pos.CENTER);
            imageContainer.getChildren().add(imageView);

            // Set the image to display on the main ui thread
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    imageScrollPane.setContent(imageContainer);
                }
            });

        } catch (IllegalArgumentException e) {
            System.out.println("Could not load image: " + this.imageDirectory);
            System.out.println(e.getMessage());
        }
    }

    /**
     * Get tags that are linked with the image directory and display them on the view table
     *
     * @param imageDirectory directory of the image.
     */
    private void setImageTagList(String imageDirectory) {

        Task<ObservableList> task = new Task<ObservableList>() {
            @Override
            protected ObservableList call() throws Exception {
                originalImageTagList = DatasourceController.queryTagsOnImage(imageDirectory);

                // Update tag list on the main thread.
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        imageTagList.setAll(originalImageTagList);
                    }
                });

                return null;
            }
        };

        new Thread(task).start();
    }

    /**
     * Add new tag from combo box to tag list that has been entered.
     *
     * @param event the event the is linked to the combo box.
     */
    @FXML
    private void onEnterKeyImageTagComboBox(KeyEvent event) {
        if (event.getCode().equals(KeyCode.ENTER)) {
            if (imageTagsComboBox.getValue() == null || imageTagsComboBox.getValue().toString().isBlank())
                return;

            if (!imageTagList.contains(imageTagsComboBox.getValue().toString())) {
                imageTagList.add(imageTagsComboBox.getValue().toString());
            }
        }
    }

    /**
     * Add tag from combo box to tag list.
     */
    @FXML
    private void addImageTagToList() {
        if (imageTagsComboBox.getValue() == null || imageTagsComboBox.getValue().toString().isBlank())
            return;

        if (!imageTagList.contains(imageTagsComboBox.getValue().toString())) {
            imageTagList.add(imageTagsComboBox.getValue().toString());
        }
    }

    /**
     * Remove tag from tag list.
     */
    @FXML
    private void removeImageTagFromList() {
        String selectedItem = imageTagsListView.getSelectionModel().getSelectedItem();

        if (selectedItem != null)
            imageTagList.remove(selectedItem);
    }

    /**
     * Reset unsaved changes to tag list
     */
    @FXML
    private void resetImageTagList() {
        imageTagList.clear();
        imageTagList.addAll(originalImageTagList);
    }

    /**
     * Save tag on image to database.
     */
    @FXML
    private void saveImageTags() {

        // Is the image still in the database?
        if (!DatasourceController.hasImageInDatabase(imageDirectory)) {
            System.out.println("Missing image: " + imageDirectory);
            return;
        }

        List<String> tags = imageTagList.stream()
                .map(object -> Objects.toString(object, null))
                .collect(Collectors.toList());

        List<String> tageToRemove = new ArrayList<>(originalImageTagList);
        tageToRemove.removeAll(tags);

        List<String> tagsToAdd = new ArrayList<>(tags);
        tagsToAdd.removeAll(originalImageTagList);


        for (String tagToRemove : tageToRemove) {
            System.out.println("To Remove: " + tagToRemove);
            DatasourceController.deleteImageTag(imageDirectory, tagToRemove);
        }

        for (String tagToAdd : tagsToAdd) {
            System.out.println("To Add: " + tagToAdd);
            DatasourceController.insertImageTag(imageDirectory, tagToAdd);
        }

        originalImageTagList = tags;
    }

    /**
     * Get tags from database and add them to combo box.
     */
    @FXML
    private void setTagsForComboBox() {
        imageTagsComboBox.getItems().setAll(DatasourceController.queryTags());
    }

    /**
     * Check if the user wants to save unsaved data.
     *
     * @return true if data was saved.
     */
    private boolean unsavedDataAlert() {
        ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.YES);
        ButtonType doNotSaveButton = new ButtonType("Don't Save", ButtonBar.ButtonData.NO);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);


        Alert alert = new Alert(Alert.AlertType.WARNING, "", saveButton, doNotSaveButton, cancelButton);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.initOwner(stage);

        alert.getDialogPane().setHeaderText("Save Changes?");

        alert.getDialogPane().setContentText("Do you want to save changes to the image tags?");

        Optional<ButtonType> result = alert.showAndWait();

        if (result.get() == saveButton) {
            saveImageTags();
            return true;
        } else if (result.get() == doNotSaveButton) {
            return true;
        } else if (result.get() == cancelButton) {
            return false;
        }

        return true;
    }

    /**
     * Swap image and image data with the new selected image.
     *
     * @param imageDirectory directory of selected image.
     */
    public void swapImage(String imageDirectory) {
        // If same image focus on window
        if (this.imageDirectory.equals(imageDirectory)) {
            stage.requestFocus();
            return;
        }

        List<String> tags = imageTagList.stream()
                .map(object -> Objects.toString(object, null))
                .collect(Collectors.toList());

        if (!tags.equals(originalImageTagList)) {
            if (!unsavedDataAlert()) {
                return;
            }
        }

        stage.requestFocus();
        initialize(stage, imageDirectory);
    }
}
