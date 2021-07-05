package sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sample.controllers.MainWindowController;
import sample.model.Datasource;

public class Main extends Application {

    private static final String MAIN_WINDOW_DIRECTORY = "resources/MainWindow.fxml";
    private static final String APPLICATION_TITLE = "Local Image Database";

    private static final int MAIN_WINDOW_WIDTH = 800;
    private static final int MAIN_WINDOW_HEIGHT = 600;


    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(MAIN_WINDOW_DIRECTORY));
        Parent root = fxmlLoader.load();
        MainWindowController mainWindowController = fxmlLoader.getController();

        primaryStage.setTitle(APPLICATION_TITLE);
        primaryStage.setScene(new Scene(root, MAIN_WINDOW_WIDTH, MAIN_WINDOW_HEIGHT));

        primaryStage.setOnCloseRequest(e -> Platform.exit());

        mainWindowController.initialise();

        primaryStage.show();
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
