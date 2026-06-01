package tierlist;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Accueil.fxml"));

        Parent root = loader.load();


        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/dark.css").toExternalForm());

        primaryStage.setTitle("Test JavaFX FXML");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
