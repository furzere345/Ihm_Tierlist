package tierlist.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tierlist.model.TierList;
import tierlist.service.PersistenceService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class RenommerTierListController {

    @FXML private TextField nomField;
    @FXML private TextArea descriptionArea;
    @FXML private StackPane coverImagePane;

    private final PersistenceService persistenceService = new PersistenceService();

    private TierList tierList;

    private byte[] coverImageData;


    public void setTierList(TierList tierList) {
        this.tierList = tierList;
        onLoaded();
    }

    @FXML
    private void onLoaded() {
        if (tierList == null) return;

        nomField.setText(tierList.getName());
        descriptionArea.setText(tierList.getDescription());

        if (tierList.getCoverImageData() != null) {
            Image img = new Image(
                    new java.io.ByteArrayInputStream(tierList.getCoverImageData())
            );

            ImageView iv = new ImageView(img);
            iv.setFitWidth(200);
            iv.setFitHeight(200);
            iv.setPreserveRatio(true);

            coverImagePane.getChildren().setAll(iv);

            coverImageData = tierList.getCoverImageData();
        }
    }

    @FXML
    private void onSelectCover() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choisir une image de couverture");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));

        File file = fc.showOpenDialog(coverImagePane.getScene().getWindow());

        if (file != null) {
            try {
                coverImageData = Files.readAllBytes(file.toPath());

                ImageView iv = new ImageView(new Image(file.toURI().toString()));
                iv.setFitWidth(200);
                iv.setFitHeight(200);
                iv.setPreserveRatio(true);

                coverImagePane.getChildren().setAll(iv);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void onCommencer() {
        String nom = nomField.getText().trim();
        String description = descriptionArea.getText().trim();

        if (nom.isEmpty()) {
            nomField.setStyle("-fx-border-color: red;");
            return;
        }

        tierList.setName(nom);
        tierList.setDescription(description);
        if (coverImageData != null) {
            tierList.setCoverImageData(coverImageData);
        }

        persistenceService.save(tierList);

        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.close();
    }
}