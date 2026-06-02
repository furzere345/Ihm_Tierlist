package tierlist.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class CustomInputController {

    @FXML private Label labelTitre;
    @FXML private TextField champSaisie;

    private String resultat = null;

    public void configurer(String titre, String texteInitial) {
        labelTitre.setText(titre);
        if (texteInitial != null) {
            champSaisie.setText(texteInitial);
        }
    }

    public String getResultat() {
        return resultat;
    }

    @FXML
    private void onValider() {
        resultat = champSaisie.getText().trim();
        fermerFenetre();
    }

    @FXML
    private void onAnnuler() {
        resultat = null;
        fermerFenetre();
    }

    private void fermerFenetre() {
        Stage stage = (Stage) champSaisie.getScene().getWindow();
        stage.close();
    }
}