package sample;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import sample.model.Datasource;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;


import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

// Reset scroll area when goinh to nect paga
public class Controller {

    @FXML
    private TilePane tilePane;

    @FXML
    private ProgressBar progressBar;

    @FXML TextField textfield;
    @FXML Label label;
    @FXML ScrollPane scrollPane;

    private int imageCount = 0;
    private int imagesPerPge = 20;
    private int pageNo = 0;
    private int maxPageNo = 0;

    private ReentrantLock lock = new ReentrantLock();

    public Controller()
    {
    }

    @FXML
    public void listImages(int section){
        System.out.println("Start");

        Task task = new Task() {
            @Override
            protected Object call() throws Exception {
                System.out.println("Start Thread");
                if(lock.tryLock())
                {
                    System.out.println("Got Lock");
                    try
                    {
                        System.out.println("In try");
                        List<String> images = Datasource.getInstance().queryImages((section - 1) * imagesPerPge, imagesPerPge);
                        System.out.println(images.size());
                        createElements(images);
                        System.out.println("Made images");
                    } finally {
                        lock.unlock();
                        System.out.println("Unlocking");
                    }
                } else
                {
                    System.out.println("Still loading!");
                }
                System.out.println("End of Thread");
                return null;
            }
        };

        new Thread(task).start();
        System.out.println("End of Main");
        //List<String> images = Datasource.getInstance().queryImages(section * 20,20);
        //createElements(images);
    }

    // Think about binding label to maxPageNo
    public void UpdateData()
    {
        imageCount = Datasource.getInstance().queryImagesCount();
        if(imageCount > 0)
        {
            pageNo = 1;
            maxPageNo = (int)Math.ceil(imageCount / imagesPerPge) + 1;
            label.setText(Integer.toString(maxPageNo));
        }
        else
        {
            pageNo = 0;
            maxPageNo = 0;
            label.setText(Integer.toString(maxPageNo));
        }
    }

    public void createElements(List<String> listOfImageDir) {
        List<VBox> test = new ArrayList<>();
        for (String s : listOfImageDir) {
            test.add(createPage(s));
        }


        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                tilePane.getChildren().clear();
                SetTextFiledNo();
                for (VBox vBox: test) {
                    if (vBox != null)
                        tilePane.getChildren().add(vBox);
                }
                scrollPane.setVvalue(0.0);

            }
        });
    }

    public VBox createPage(String imagePath)
    {
        ImageView imageView = new ImageView();
        //MyCostomImage.ReadData(imagePath);

        try {
            //File sourceimage = new File(imagePath);
            //BufferedImage bufferedImage = ImageIO.read(sourceimage);
            //sourceimage.toURI().toString()
            File file = new File(imagePath);
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
                    OpenImage(imagePath);
                }
            }
        });
        return pageBox;
    }

    @FXML
    public void btn_Next()
    {
        if(lock.isLocked())
            return;

        if(pageNo < maxPageNo)
        {
            pageNo++;
            listImages(pageNo);
        }
    }

    @FXML
    public void btn_Pre()
    {
        if(lock.isLocked())
            return;

        if(pageNo > 1){
            pageNo--;
            listImages(pageNo);
        }
    }


    public void SetTextFiledNo()
    {
        textfield.setText(Integer.toString(pageNo));
    }

    public void JumpToPage()
    {
        if(lock.isLocked())
            return;

        try {
            int n = Integer.parseInt(textfield.getText());
            n = ClampInt(n,1, maxPageNo);

            pageNo = n;
            listImages(pageNo);

        } catch (NumberFormatException e)
        {
            return;
        }
    }

    public int ClampInt(int value, int min, int max)
    {
        if(value < min)
            return min;
        if(value > max)
            return max;

        return value;
    }

    @FXML
    public void OpenImage(String path) {

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("imageWindow.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = new Stage();
            ControllerImageWindow controllerImageWindow = fxmlLoader.getController();
            controllerImageWindow.SetImage(path);

            stage.setTitle("Image: " + path);
            stage.setScene(new Scene(root, 800, 600));
            stage.show();


        } catch (Exception e)
        {

        }
    }

}
