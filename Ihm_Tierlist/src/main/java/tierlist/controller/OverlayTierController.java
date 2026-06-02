package tierlist.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import tierlist.model.Tier;

import java.util.function.Consumer;

public class OverlayTierController {

    @FXML private ImageView wheelImage;
    @FXML private TextField tierField;
    @FXML private TextField hexField;
    @FXML private Button confirmButton;
    @FXML private Button deleteButton;

    private Tier tierToEdit;
    private Consumer<Tier> onConfirm;
    private Runnable onDelete;
    private Color selectedColor;
    private Circle selectedCircle;

    //Appele par le parent pour pré-remplir les champs (mode édition)
    public void setTier(Tier tier, Consumer<Tier> onConfirm, Runnable onDelete) {
        this.tierToEdit = tier;
        this.onConfirm  = onConfirm;
        this.onDelete   = onDelete;

        if (tier != null) {
            tierField.setText(tier.getName());
            hexField.setText(tier.getColorHex());
        }
        //Mode creation : champs vides -> pas de bouton Supprimer
        deleteButton.setVisible(tier != null);
    }

    @FXML
    private void handleColorClick(MouseEvent event) {
        Circle circle = (Circle) event.getSource();

        //reset ancien cercle
        if (selectedCircle != null) {
            selectedCircle.setStroke(null);
        }

        //selectionner le nouveau
        selectedCircle = circle;
        selectedCircle.setStroke(javafx.scene.paint.Color.WHITE);
        selectedCircle.setStrokeWidth(2);

        //couleur selectionne
        Color color = (Color) circle.getFill();
        selectedColor = color;

        String hex = String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255)
        );

        hexField.setText(hex);
    }

    @FXML
    private void onConfirm() {
        String nom = tierField.getText().trim();
        String hex = hexField.getText().trim();

        if (nom.isEmpty()) {
            tierField.setStyle("-fx-border-color: red;");
            return;
        }

        if (tierToEdit == null) {
            tierToEdit = new Tier(nom, hex);
        } else {
            tierToEdit.setName(nom);
            tierToEdit.setColorHex(hex);
        }

        if (onConfirm != null) onConfirm.accept(tierToEdit);
        closeStage();
    }

    @FXML
    private void onDelete() {
        if (onDelete != null) onDelete.run();
        closeStage();
    }

    private void closeStage() {
        ((Stage) confirmButton.getScene().getWindow()).close();
    }


}
