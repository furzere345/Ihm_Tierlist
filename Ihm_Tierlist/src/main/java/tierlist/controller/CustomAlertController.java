package tierlist.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class CustomAlertController {

    @FXML private Label labelTitre;
    @FXML private Label labelMessage;

    public void configurer(String titre, String message) {
        labelTitre.setText(titre);
        labelMessage.setText(message);
    }

    @FXML
    private void onFermer() {
        Stage stage = (Stage) labelTitre.getScene().getWindow();
        stage.close();
    }
}