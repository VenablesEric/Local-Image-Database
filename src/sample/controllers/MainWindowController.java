package sample.controllers;

import javafx.application.Platform;
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
import sample.util.MathematicalEquations;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

// Reset scroll area when goinh to nect paga
public class MainWindowController {

    @FXML
    private TilePane tilePane;

    @FXML
    private ProgressBar progressBar;

    @FXML TextField pageNumberfield;
    @FXML Label maxPageNolabel;
    @FXML ScrollPane scrollPane;

    @FXML
    ComboBox comboBox;
    private ObservableList<String> filterTags = FXCollections.observableArrayList();
    @FXML
    ListView<String> tagTable;

    private int imageCount = 0;
    private final int imagesPerPge = 20;
    private int pageNo = 0;
    private int maxPageNo = 0;

    //private  List<String> tagFilter = new ArrayList<>();

    private ReentrantLock lock = new ReentrantLock();

    private Stage folderStage;

    private static MainWindowController instances;

    public static MainWindowController getInstances() {
        return instances;
    }

    public void initialise()
    {
        instances = this;

        pageNumberfield.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if(event.getCode().equals(KeyCode.ENTER))
                   JumpToPage();
            }
        });

        tagTable.setItems(filterTags);
        UpdateData();
    }

    @FXML
    public void displayImages(int pageNumber){

        Task task = new Task() {
            @Override
            protected Object call() throws Exception {
                if(lock.tryLock())
                {
                    try
                    {
                        List<String> imageDirectories;
                        if(filterTags.size() == 0)
                            imageDirectories = Datasource.getInstance().queryImages((pageNumber - 1) * imagesPerPge, imagesPerPge);
                        else {
                            List<String> tags = filterTags.stream()
                                    .map(object -> Objects.toString(object, null))
                                    .collect(Collectors.toList());
                            imageDirectories = Datasource.getInstance().queryImagesWithTags(tags, (pageNumber - 1) * imagesPerPge, imagesPerPge);
                        }

                        createImages(imageDirectories);
                    } finally {
                        lock.unlock();
                    }
                } else
                {
                    System.out.println("Still loading!");
                }
                return null;
            }
        };

        new Thread(task).start();
    }

    public void UpdateData()
    {
        if(filterTags.size() == 0) {
            imageCount = Datasource.getInstance().queryCountImages();
        }
        else {
            List<String> tags = filterTags.stream()
                    .map(object -> Objects.toString(object, null))
                    .collect(Collectors.toList());
            imageCount = Datasource.getInstance().queryCountImagesWithTags(tags);
        }

        if(imageCount > 0)
        {
            pageNo = 1;
            maxPageNo = (int)Math.ceil(imageCount / imagesPerPge) + 1;
        }
        else
        {
            pageNo = 0;
            maxPageNo = 0;
        }

        maxPageNolabel.setText(Integer.toString(maxPageNo));

        displayImages(1);
    }

    public void createImages(List<String> imageDirectories) {
        List<VBox> images = new ArrayList<>();
        for (String imageDirectory : imageDirectories) {
            VBox image = createImage(imageDirectory);
            if(image != null)
                images.add(image);
        }

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                tilePane.getChildren().clear();
                pageNumberfield.setText(Integer.toString(pageNo));
                for (VBox image: images) {
                    if (image != null)
                        tilePane.getChildren().add(image);
                }
                scrollPane.setVvalue(0.0);

            }
        });
    }

    public VBox createImage(String imageDirectory)
    {
        ImageView imageView = new ImageView();

        try {
            File file = new File(imageDirectory);

            if(!file.exists())
                return null;

            Image image = new Image(file.toURI().toString(),500,0,true,false);
            imageView.setImage(image);

            imageView.setFitWidth(500);
            imageView.setFitHeight(500);

            imageView.setPreserveRatio(true);

            imageView.setSmooth(true);
            imageView.setCache(true);

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        VBox pageBox = new VBox();
        pageBox.setAlignment(Pos.CENTER);
        pageBox.getChildren().add(imageView);

        imageView = null;

        pageBox.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent click) {
                if(click.getClickCount() == 2) {
                    openImageInfoWindow(imageDirectory);
                }
            }
        });

        return pageBox;
    }

    @FXML
    public void nextPage()
    {
        if(lock.isLocked())
            return;

        if(pageNo < maxPageNo)
        {
            pageNo++;
            displayImages(pageNo);
        }
    }

    @FXML
    public void previousPage()
    {
        if(lock.isLocked())
            return;

        if(pageNo > 1){
            pageNo--;
            displayImages(pageNo);
        }
    }

    public void JumpToPage()
    {
        if(lock.isLocked())
            return;

        try {
            int n = Integer.parseInt(pageNumberfield.getText());
            n = MathematicalEquations.clampInt(n,1, maxPageNo);

            pageNo = n;
            displayImages(pageNo);

        } catch (NumberFormatException e)
        {
            return;
        }
    }


    @FXML
    public void openImageInfoWindow(String path) {

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("resources/ImageInfoWindow.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = new Stage();
            ImageInfoWindowController imageInfoWindowController = fxmlLoader.getController();
            imageInfoWindowController.SetImage(path);

            stage.setTitle("Image: " + path);
            stage.setScene(new Scene(root, 800, 600));
            stage.show();

        } catch (Exception e)
        {

        }
    }

    @FXML
    public void FillComboBox()
    {
        comboBox.getItems().setAll(Datasource.getInstance().queryTags());
        //comboBox.getItems().addAll(Datasource.getInstance().queryTags());
    }

    @FXML
    public void addFilter()
    {
        if(comboBox.getValue() == null || comboBox.getValue().toString().isBlank())
            return;

        if(!filterTags.contains(comboBox.getValue().toString()))
        {
            //image_Tags.add(comboBox.getValue().toString());
            filterTags.add(comboBox.getValue().toString());
            UpdateData();
            displayImages(1);
            System.out.println("Added");
        }
    }

    @FXML
    public void removeFilter() {
        String selectedItem = tagTable.getSelectionModel().getSelectedItem();

        if (selectedItem != null) {
            filterTags.remove(selectedItem);
            UpdateData();
            displayImages(1);
        }
    }

    @FXML
    public void openSettingsWindow() {
        if(SettingsWindowController.getStage() != null)
            return;

        try {
            //FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/resources/SettingsWindow.fxml"));
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("resources/SettingsWindow.fxml"));
            Parent root = fxmlLoader.load();
            folderStage = new Stage();
            SettingsWindowController settingsWindowController = fxmlLoader.getController();


            settingsWindowController.initialise(folderStage);

            folderStage.setTitle("Folder Window");
            folderStage.setScene(new Scene(root, 400, 400));
            folderStage.setResizable(false);
            folderStage.initModality(Modality.APPLICATION_MODAL);

            folderStage.show();
            System.out.println("DONE");

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void Refresh()
    {
        UpdateData();
    }

/*    public void ScanForImages()
    {
        List<Folder> folders = Datasource.getInstance().queryFolders();
        for (Folder folder: folders) {
            System.out.println(folder);
            List<String> test = FolderAndImageIO.ScanFolder(folder.getDirectory());

            if(test != null) {
                for (String insert : test) {
                    Datasource.getInstance().insertImage(insert, folder.getId());
                }
            }
        }
    }*/

}
