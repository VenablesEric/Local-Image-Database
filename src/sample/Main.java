package sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sample.controllers.MainWindowController;
import sample.model.DatasourceController;

/**
 * Main class that sets up the main window and database.
 */
public class Main extends Application {

    private static final String MAIN_WINDOW_DIRECTORY = "resources/MainWindow.fxml";
    private static final String MAIN_WINDOW_TITLE = "Local Image Database";

    private static final int MAIN_WINDOW_WIDTH = 800;
    private static final int MAIN_WINDOW_HEIGHT = 600;

    /**
     * Launches javaFX application
     *
     * @param args
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Entry points for JavaFX application, called after {@link #init()}. Load the main window.
     *
     * @param primaryStage the primary stage for this application.
     * @throws Exception if something does wrong.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(MAIN_WINDOW_DIRECTORY));
        Parent root = fxmlLoader.load();
        MainWindowController mainWindowController = fxmlLoader.getController();

        primaryStage.setTitle(MAIN_WINDOW_TITLE);
        primaryStage.setScene(new Scene(root, MAIN_WINDOW_WIDTH, MAIN_WINDOW_HEIGHT));

        // Close all windows when main window is closed.
        primaryStage.setOnCloseRequest(e -> Platform.exit());

        mainWindowController.initialise();

        primaryStage.show();
    }


    /**
     * Called before the application window opens. attempts to start database, close on fail.
     *
     * @throws Exception if something does wrong.
     */
    @Override
    public void init() throws Exception {
        super.init();
        if (!DatasourceController.open()) {
            System.out.println("FATAL ERROR: Couldn't connect to database");
            Platform.exit();
        }
    }

    /**
     * When application is closing, close the database.
     *
     * @throws Exception if something does wrong.
     */
    @Override
    public void stop() throws Exception {
        super.stop();
        DatasourceController.close();
    }
}
