package sample.controllers;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import sample.Main;
import sample.model.Datasource;
import sample.model.DatasourceController;
import sample.util.MathematicalEquations;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

// Reset scroll area when goinh to nect paga
public class MainWindowController {

    private static MainWindowController instances;

    @FXML
    private ScrollPane ImageContainerScrollPane;
    @FXML
    private TilePane ImageContainerTilePane;
    @FXML
    private TextField pageNumberField;
    @FXML
    private Label maxPageNumberLabel;
    @FXML
    private ComboBox imageTagsComboBox;
    @FXML
    private ListView<String> imageTagsFilterTable;

    private ObservableList<String> imageTagsFilterList = FXCollections.observableArrayList();

    private final int imagesPerPge = 20;
    private int imageCount = 0;
    private int currentPageNumber = 0;
    private int maxPageNumber = 0;

    private final int imageWidth = 200;

    //private Stage imageInfoWindowStage;
    private final String IMAGE_INFO_WINDOW_DIRECTORY = "resources/ImageInfoWindow.fxml";
    private final int IMAGE_INFO_WINDOW_WIDTH = 800;
    private final int IMAGE_INFO_WINDOW_HEIGHT = 400;

    //private Stage settingsWindowStage;
    private final String SETTINGS_WINDOW_DIRECTORY = "resources/SettingsWindow.fxml";
    private final String SETTINGS_WINDOW_TITLE = "Settings Window";
    private final int SETTINGS_WINDOW_WIDTH = 400;
    private final int SETTINGS_WINDOW_HEIGHT = 400;

    private Task task;

    public static MainWindowController getInstances() {
        return instances;
    }

    public void initialise() {
        instances = this;

        pageNumberField.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode().equals(KeyCode.ENTER))
                    jumpToPage();
            }
        });


        pageNumberField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if(!newValue.matches("\\d*")) {
                    pageNumberField.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });

        imageTagsFilterTable.setItems(imageTagsFilterList);
        refresh();
    }

    public void refresh() {
        if (imageTagsFilterList.size() == 0) {
            imageCount = DatasourceController.queryCountImages();
        } else {
            List<String> tags = imageTagsFilterList.stream()
                    .map(object -> Objects.toString(object, null))
                    .collect(Collectors.toList());
            imageCount = DatasourceController.queryCountImagesWithTags(tags);
        }

        if (imageCount > 0) {
            currentPageNumber = 1;
            maxPageNumber = (int) Math.ceil(imageCount / imagesPerPge);
        } else {
            currentPageNumber = 0;
            maxPageNumber = 0;
        }

        maxPageNumberLabel.setText(Integer.toString(maxPageNumber));

        startDisplayImagesTask(1);
    }
    
    private void startDisplayImagesTask(int pageNumber)
    {
        if(task != null && task.isRunning())
        {
            Task oldTask = task;

            task = new Task() {
                @Override
                protected Object call() throws Exception {
                    oldTask.cancel();

                    while (true)
                    {
                        if(oldTask.isCancelled() || oldTask.isDone())
                            break;
                    }

                    displayImages(pageNumber);
                    return  null;
                }
            };
        }
        else {
            task = new Task() {
                @Override
                protected Object call() throws Exception {
                    displayImages(pageNumber);
                    return null;
                }
            };
        }

        task.run();
    }

    private void displayImages(int pageNumber)
    {
        List<String> imageDirectories;
        if (imageTagsFilterList.size() == 0)
            imageDirectories = DatasourceController.queryImages((pageNumber - 1) * imagesPerPge, imagesPerPge);
        else {
            List<String> tags = imageTagsFilterList.stream()
                    .map(object -> Objects.toString(object, null))
                    .collect(Collectors.toList());
            imageDirectories = DatasourceController.queryImagesWithTags(tags, (pageNumber - 1) * imagesPerPge, imagesPerPge);
        }

        startDisplayImagesTask(imageDirectories);
    }

    private void startDisplayImagesTask(List<String> imageDirectories) {
        List<VBox> images = new ArrayList<>();
        for (String imageDirectory : imageDirectories) {
            VBox image = loadImage(imageDirectory);
            if (image != null)
                images.add(image);
        }

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                ImageContainerTilePane.getChildren().clear();
                pageNumberField.setText(Integer.toString(currentPageNumber));
                for (VBox image : images) {
                    if (image != null)
                        ImageContainerTilePane.getChildren().add(image);
                }
                ImageContainerScrollPane.setVvalue(0.0);
            }
        });
    }

    private VBox loadImage(String imageDirectory) {
        ImageView imageView = new ImageView();

        try {
            File file = new File(imageDirectory);

            if (!file.exists())
                return null;

            Image image = new Image(file.toURI().toString(), imageWidth, 0, true, false);
            imageView.setImage(image);

            imageView.setFitWidth(imageWidth);
            imageView.setFitHeight(imageWidth);

            imageView.setPreserveRatio(true);

            imageView.setSmooth(true);
            imageView.setCache(false);

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        VBox imageContainer = new VBox();
        imageContainer.setAlignment(Pos.CENTER);
        imageContainer.getChildren().add(imageView);

        imageView = null;

        imageContainer.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent click) {
                if (click.getClickCount() == 2) {
                    openImageInfoWindow(imageDirectory);
                }
            }
        });

        return imageContainer;
    }

    @FXML
    private void previousPage() {

        if (currentPageNumber > 1) {
            currentPageNumber--;
            startDisplayImagesTask(currentPageNumber);
        }
    }

    @FXML
    private void nextPage() {

        if (currentPageNumber < maxPageNumber) {
            currentPageNumber++;
            startDisplayImagesTask(currentPageNumber);
        }
    }

    private void jumpToPage() {

        try {
            int pageNumber = Integer.parseInt(pageNumberField.getText());
            pageNumber = MathematicalEquations.clampInt(pageNumber, 1, maxPageNumber);

            currentPageNumber = pageNumber;
            startDisplayImagesTask(currentPageNumber);

        } catch (NumberFormatException e) {
            return;
        }
    }

    @FXML
    private void setTagsForComboBox() {
        imageTagsComboBox.getItems().setAll(DatasourceController.queryTags());
    }

    @FXML
    private void addImageTagToFilterList() {
        if (imageTagsComboBox.getValue() == null || imageTagsComboBox.getValue().toString().isBlank())
            return;

        if (!imageTagsFilterList.contains(imageTagsComboBox.getValue().toString())) {
            imageTagsFilterList.add(imageTagsComboBox.getValue().toString());
            refresh();
        }
    }

    @FXML
    private void removeImageTagFromFilterList() {
        String selectedItem = imageTagsFilterTable.getSelectionModel().getSelectedItem();

        if (selectedItem != null) {
            imageTagsFilterList.remove(selectedItem);
            refresh();
        }
    }

    @FXML
    private void openImageInfoWindow(String imageDirectory) {

        Stage stage = ImageInfoWindowController.getStage();
        if(stage != null && stage.isShowing())
        {
            ImageInfoWindowController.getInstance().swapImage(imageDirectory);
            return;
        }

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource(IMAGE_INFO_WINDOW_DIRECTORY));
            Parent root = fxmlLoader.load();
            Stage imageInfoWindowStage = new Stage();
            ImageInfoWindowController imageInfoWindowController = fxmlLoader.getController();

            imageInfoWindowController.initialize(imageInfoWindowStage, imageDirectory);

            imageInfoWindowStage.setScene(new Scene(root, IMAGE_INFO_WINDOW_WIDTH, IMAGE_INFO_WINDOW_HEIGHT));

            imageInfoWindowStage.show();

        } catch (Exception e) {
            System.out.println("Could not open Image Info Window for " + imageDirectory);
            System.out.println(e.getMessage());
        }
    }

    @FXML
    private void openSettingsWindow() {
        if (SettingsWindowController.getStage() != null)
            return;

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource(SETTINGS_WINDOW_DIRECTORY));
            Parent root = fxmlLoader.load();
            Stage settingsWindowStage = new Stage();
            SettingsWindowController settingsWindowController = fxmlLoader.getController();


            settingsWindowController.initialise(settingsWindowStage);

            settingsWindowStage.setTitle(SETTINGS_WINDOW_TITLE);
            settingsWindowStage.setScene(new Scene(root, SETTINGS_WINDOW_WIDTH, SETTINGS_WINDOW_HEIGHT));
            settingsWindowStage.setResizable(false);
            settingsWindowStage.initModality(Modality.APPLICATION_MODAL);

            settingsWindowStage.show();

        } catch (Exception e) {
            System.out.println("Could not open settings window: " + e.getMessage());
        }
    }
}
