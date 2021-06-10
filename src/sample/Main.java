package sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import sample.model.Datasource;

import java.util.List;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("sample.fxml"));
        Parent root = loader.load();
        Controller controller = loader.getController();
        //controller.listImages(0);

        Datasource.getInstance().insertFolder("C:\\Users\\Blank\\Pictures\\TestFolder");

        List<String> folders = Datasource.getInstance().queryFolders();
        for (String folder: folders) {
            System.out.println(folder);
            List<String> test = FolderImageScanner.Scan(folder);

            if(test != null) {
                for (String insert : test) {
                    Datasource.getInstance().insertImage(insert);
                }
            }
        }

        controller.UpdateData();
        controller.listImages(1);

        controller.textfield.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if(event.getCode().equals(KeyCode.ENTER))
                    controller.JumpToPage();
            }
        });

        primaryStage.setTitle("Local Image Database");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();

        primaryStage.setOnCloseRequest(e -> Platform.exit());
    }


    @Override
    public void init() throws Exception {
        super.init();
        if(!Datasource.getInstance().open()) {
            System.out.println("FATAL ERROR: Couldn't connect to database");
            Platform.exit();
        }
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        Datasource.getInstance().close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
