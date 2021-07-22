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
import sample.model.DatasourceController;
import sample.util.MathematicalEquations;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Main window for application.
 * displays / filters images in the database.
 * Open a window to add folders to database.
 * Open a new window to add tags to selected image.
 */
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

    // Use to keep imageTagsFilterTable updated with selected tags.
    private ObservableList<String> imageTagsFilterList = FXCollections.observableArrayList();

    // Use to display selected number of images.
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

    // Use to take long tasks off the ui thread, while making sure that one task can run at a time on this window.
    private Task task;

    /**
     * @return singleton instance.
     */
    public static MainWindowController getInstances() {
        return instances;
    }

    /**
     * Set up the window.
     */
    public void initialise() {
        instances = this;

        // Change the page number by inputting a number and pressing the enter key.
        pageNumberField.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode().equals(KeyCode.ENTER))
                    jumpToPage();
            }
        });


        // Add a listener to page number field to only allow numbers to be entered.
        pageNumberField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d*")) {
                    pageNumberField.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });

        imageTagsFilterTable.setItems(imageTagsFilterList); // Set tag table to be linked to list of tags.
        refresh();
    }

    /**
     * Get image count with filters and refresh the image page back to page one.
     */
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

        displayImagesTask(1);
    }

    /**
     * Start a task that will get images and display them on the screen.
     *
     * @param pageNumber use to load images for the correct page.
     */
    private void displayImagesTask(int pageNumber) {
        // Cancel task and start loading next page if current task has not completed.
        if (task != null && task.isRunning()) {
            Task oldTask = task;

            task = new Task() {
                @Override
                protected Object call() throws Exception {
                    oldTask.cancel();

                    while (true) {
                        if (oldTask.isCancelled() || oldTask.isDone())
                            break;
                    }

                    List<String> imageDirectories = getImageDirectories(pageNumber);
                    displayImages(imageDirectories);
                    return null;
                }
            };
        } else {
            task = new Task() {
                @Override
                protected Object call() throws Exception {
                    List<String> imageDirectories = getImageDirectories(pageNumber);
                    displayImages(imageDirectories);
                    return null;
                }
            };
        }

        new Thread(task).start();
    }

    /**
     * Get a list of image directories to be use to load the image.
     *
     * @param pageNumber use to get images for the correct page.
     * @return list of image directories
     */
    private List<String> getImageDirectories(int pageNumber) {
        List<String> imageDirectories;
        if (imageTagsFilterList.size() == 0)
            imageDirectories = DatasourceController.queryImages((pageNumber - 1) * imagesPerPge, imagesPerPge);
        else {
            List<String> tags = imageTagsFilterList.stream()
                    .map(object -> Objects.toString(object, null))
                    .collect(Collectors.toList());
            imageDirectories = DatasourceController.queryImagesWithTags(tags, (pageNumber - 1) * imagesPerPge, imagesPerPge);
        }

        return imageDirectories;
    }

    /**
     * Load images and set a task on the main ui thread to display the images.
     *
     * @param imageDirectories list of image directories.
     */
    private void displayImages(List<String> imageDirectories) {

        // Holder for loaded images.
        List<VBox> images = new ArrayList<>();

        // Load each image
        for (String imageDirectory : imageDirectories) {
            VBox image = loadImage(imageDirectory);
            if (image != null)
                images.add(image);
        }

        // Task will run on the main ui thread at some point.
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                ImageContainerTilePane.getChildren().clear();
                pageNumberField.setText(Integer.toString(currentPageNumber));
                for (VBox image : images) {
                    if (image != null)
                        ImageContainerTilePane.getChildren().add(image);
                }

                // Reset scroll pane to top.
                ImageContainerScrollPane.setVvalue(0.0);
            }
        });
    }

    /**
     * Load image from hard drive.
     *
     * @param imageDirectory image directory to load.
     * @return loaded image.
     */
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

        // Open image info window when double clicked.
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

    /**
     * Load images from previous page.
     */
    @FXML
    private void previousPage() {

        if (currentPageNumber > 1) {
            currentPageNumber--;
            displayImagesTask(currentPageNumber);
        }
    }

    /**
     * Load images on next page.
     */
    @FXML
    private void nextPage() {

        if (currentPageNumber < maxPageNumber) {
            currentPageNumber++;
            displayImagesTask(currentPageNumber);
        }
    }

    /**
     * Load images from selected page.
     */
    private void jumpToPage() {

        try {
            int pageNumber = Integer.parseInt(pageNumberField.getText());
            pageNumber = MathematicalEquations.clampInt(pageNumber, 1, maxPageNumber);

            currentPageNumber = pageNumber;
            displayImagesTask(currentPageNumber);

        } catch (NumberFormatException e) {
            return;
        }
    }

    /**
     * Get all tags from database and set them in the combo box when combo box is clicked.
     */
    @FXML
    private void setTagsForComboBox() {
        imageTagsComboBox.getItems().setAll(DatasourceController.queryTags());
    }

    /**
     * Add selected tag from combo box to list of current tags and refresh images.
     */
    @FXML
    private void addImageTagToFilterList() {
        if (imageTagsComboBox.getValue() == null || imageTagsComboBox.getValue().toString().isBlank())
            return;

        if (!imageTagsFilterList.contains(imageTagsComboBox.getValue().toString())) {
            imageTagsFilterList.add(imageTagsComboBox.getValue().toString());
            refresh();
        }
    }

    /**
     * Remove selected tag from list of current tags and refresh images.
     */
    @FXML
    private void removeImageTagFromFilterList() {
        String selectedItem = imageTagsFilterTable.getSelectionModel().getSelectedItem();

        if (selectedItem != null) {
            imageTagsFilterList.remove(selectedItem);
            refresh();
        }
    }

    /**
     * Open image info window with image and tags that are linked with image directory.
     * If image window is already open the new image and data will replace the old one.
     *
     * @param imageDirectory directory of image.
     */
    @FXML
    private void openImageInfoWindow(String imageDirectory) {

        Stage stage = ImageInfoWindowController.getStage();
        // Swap image and data with new one if window is open.
        if (stage != null && stage.isShowing()) {
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

    /**
     * Open settings window to add or remove folders, and scan for images in folders.
     */
    @FXML
    private void openSettingsWindow() {
        // Only one settings window can be open.
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
            settingsWindowStage.initModality(Modality.APPLICATION_MODAL); // Settings window will be the only window selectable when open.

            settingsWindowStage.show();

        } catch (Exception e) {
            System.out.println("Could not open settings window: " + e.getMessage());
        }
    }
}
