package tierlist;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Charge ton fichier FXML
        Parent root = FXMLLoader.load(getClass().getResource("/view/Accueil.fxml"));
        root.getStylesheets().add(getClass().getResource("/dark.css").toExternalForm());

        primaryStage.setTitle("Test JavaFX FXML");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
