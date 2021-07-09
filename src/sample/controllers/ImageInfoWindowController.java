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
import sample.model.Datasource;
import sample.model.DatasourceController;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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

    private ObservableList<String> imageTagList = FXCollections.observableArrayList();
    private List<String> originalImageTagList = new ArrayList<>();

    private String imageDirectory;

    private final int imageSize = 500;

    public static ImageInfoWindowController getInstance() {
        return instance;
    }

    public static Stage getStage() {
        return stage;
    }

    public void initialize(Stage stage, String imageDirectory) {

        instance = this;
        ImageInfoWindowController.stage = stage;

        stage.setTitle(IMAGE_INFO_WINDOW_TITLE + imageDirectory);

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                List<String> tags = imageTagList.stream()
                        .map(object -> Objects.toString(object, null))
                        .collect(Collectors.toList());

                if (!tags.equals(originalImageTagList)) {
                    if(unsavedDataAlert())
                    {
                        ImageInfoWindowController.stage = null;
                    } else
                    {
                        event.consume();
                    }
                } else
                {
                    ImageInfoWindowController.stage = null;
                }
            }
        });

        imageTagsListView.setItems(imageTagList);
        this.imageDirectory = imageDirectory;

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

    private void createImage(String path) {

        try {
            File file = new File(path);
            Image image = new Image(file.toURI().toString());
            ImageView imageView = new ImageView(image);

            imageView.setFitWidth(imageSize);
            imageView.setFitHeight(imageSize);

            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);

            VBox imageContainer = new VBox();
            imageContainer.setAlignment(Pos.CENTER);
            imageContainer.getChildren().add(imageView);

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    imageScrollPane.setContent(imageContainer);
                }
            });

        } catch (IllegalArgumentException e) {
            System.out.println("Could not load image: " + imageDirectory);
            System.out.println(e.getMessage());
        }
    }

    private void setImageTagList(String imageDirectory) {

        Task<ObservableList> task = new Task<ObservableList>() {
            @Override
            protected ObservableList call() throws Exception {
                originalImageTagList = DatasourceController.queryTagsOnImage(imageDirectory);

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

    @FXML
    private void addImageTagToList() {
        if (imageTagsComboBox.getValue() == null || imageTagsComboBox.getValue().toString().isBlank())
            return;

        if (!imageTagList.contains(imageTagsComboBox.getValue().toString())) {
            imageTagList.add(imageTagsComboBox.getValue().toString());
        }
    }

    @FXML
    private void removeImageTagFromList() {
        String selectedItem = imageTagsListView.getSelectionModel().getSelectedItem();

        if (selectedItem != null)
            imageTagList.remove(selectedItem);
    }

    @FXML
    private void resetImageTagList() {
        imageTagList.clear();
        imageTagList.addAll(originalImageTagList);
    }

    @FXML
    private void saveImageTags() {
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

    @FXML
    private void setTagsForComboBox() {
        imageTagsComboBox.getItems().setAll(DatasourceController.queryTags());
    }


    private boolean unsavedDataAlert()
    {
        ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.YES);
        ButtonType doNotSaveButton = new ButtonType("Don't Save", ButtonBar.ButtonData.NO);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        //Alert.AlertType type = Alert.AlertType.CONFIRMATION;

        Alert alert = new Alert(Alert.AlertType.WARNING, "", saveButton, doNotSaveButton, cancelButton);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.initOwner(stage);

        alert.getDialogPane().setHeaderText("Save Changes?");

        alert.getDialogPane().setContentText("Do you want to save changes to the image tags?");

        Optional<ButtonType> result = alert.showAndWait();

        if(result.get() == saveButton)
        {
            saveImageTags();
            return true;
        }
        else if(result.get() == doNotSaveButton)
        {
            return true;
        }
        else if(result.get() == cancelButton)
        {
            return false;
        }

        return true;
    }

    public void swapImage(String imageDirectory)
    {
        if(this.imageDirectory.equals(imageDirectory))
        {
            stage.requestFocus();
            return;
        }

        List<String> tags = imageTagList.stream()
                .map(object -> Objects.toString(object, null))
                .collect(Collectors.toList());

        if (!tags.equals(originalImageTagList)) {
            if(!unsavedDataAlert())
            {
                return;
            }
        }

        stage.requestFocus();
        initialize(stage, imageDirectory);
    }
}
