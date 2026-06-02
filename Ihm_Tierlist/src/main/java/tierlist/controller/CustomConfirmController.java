package tierlist.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class CustomConfirmController {

    @FXML
    private Label labelMessage;

    private boolean isConfirmed = false;
    private boolean isDarkTheme;

    public void configurer(String message) {
        labelMessage.setText(message);
    }


    public boolean isConfirmed() {
        return isConfirmed;
    }

    @FXML
    private void onOui() {
        isConfirmed = true;
        fermerFenetre();
    }

    @FXML
    private void onNon() {
        isConfirmed = false;
        fermerFenetre();
    }

    private void fermerFenetre() {
        Stage stage = (Stage) labelMessage.getScene().getWindow();
        stage.close();
    }

    public void setDarkTheme(boolean isDarkTheme) { this.isDarkTheme=isDarkTheme;
    }
}