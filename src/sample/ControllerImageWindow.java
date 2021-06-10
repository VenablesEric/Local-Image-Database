package sample;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;

import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import sample.model.Datasource;

import java.awt.TextField;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ControllerImageWindow implements Initializable {


    @FXML
    ScrollPane scrollPane;

    @FXML
    ListView tagTable;

    @FXML TextField input;

    List<String> image_Tags = new ArrayList<String>();

    private ObservableList<String> myListTest = FXCollections.observableArrayList();

    private String imagePath = "";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("1st");
        tagTable.setItems(myListTest);
    }

    @FXML
    ComboBox comboBox;
    public void SetImage(String path) {

        imagePath = path;

        Task task = new Task() {
            @Override
            protected Object call() throws Exception {
                createElements(path);
                SetTagTable(path);
                return  null;
            }
        };

        new Thread(task).start();
    }

    public void createElements(String path)
    {
        VBox vBox = CreateImage(path);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                scrollPane.setContent(vBox);
            }
        });
    }

    private VBox CreateImage(String path)
    {
        ImageView imageView = new ImageView();

        try
        {
            File file = new File(path);
            Image image = new Image(file.toURI().toString());
            imageView.setImage(image);

            imageView.setFitWidth(500);
            imageView.setFitHeight(500);

            imageView.setPreserveRatio(true);

            imageView.setSmooth(true);

            VBox pageBox = new VBox();
            pageBox.setAlignment(Pos.CENTER);
            pageBox.getChildren().add(imageView);

            return pageBox;

        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private void SetTagTable(String imagePath)
    {
        System.out.println("2nd");
        Task<ObservableList> task = new Task<ObservableList>() {
            @Override
            protected ObservableList call() throws Exception {
                myListTest.clear();
                myListTest.addAll(Datasource.getInstance().queryTagsForImage(imagePath));
                return null;
                /*return FXCollections.observableArrayList
                        (Datasource.getInstance().queryTagsForImage(imagePath));*/
            }
        };

        //tagTable.itemsProperty().bind(task.valueProperty());

        new Thread(task).start();

    }

    public void OnEnterKey(KeyEvent event) {
        if(event.getCode().equals(KeyCode.ENTER)) {
            if(comboBox.getValue() == null || comboBox.getValue().toString().isBlank())
                return;

            //if(!image_Tags.contains(comboBox.getValue().toString()))
            if(!myListTest.contains(comboBox.getValue().toString()))
            {
                //image_Tags.add(comboBox.getValue().toString());
                myListTest.add(comboBox.getValue().toString());
                System.out.println("Added");
            }
        }
    }

    public void SaveImageTags()
    {
        List<String> tmp = myListTest.stream()
                .map(object -> Objects.toString(object, null))
                .collect(Collectors.toList());

        for(String s : tmp)
        {
            Datasource.getInstance().insertNewTag(imagePath,s);
        }
        //MyCostomImage.WriteData(imagePath,tmp);
    }

    public void PrintMetaData()
    {
        //MyCostomImage.Read(imagePath);
    }

    public void FillComboBox()
    {
        comboBox.getItems().addAll(Datasource.getInstance().queryTagsAll());
    }

    public void AddTag()
    {

    }
}
