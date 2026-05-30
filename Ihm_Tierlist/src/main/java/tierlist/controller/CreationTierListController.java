package tierlist.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
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

public class CreationTierListController {

    @FXML private TextField nomField;
    @FXML private TextArea  descriptionArea;
    @FXML private StackPane coverImagePane;
    @FXML private Label     coverLabel;

    private byte[] coverImageData;
    private PersistenceService persistenceService = new PersistenceService();

    @FXML
    private void onSelectCover() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choisir une image de couverture");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );
        File file = fc.showOpenDialog(coverImagePane.getScene().getWindow());
        if (file != null) {
            try {
                coverImageData = Files.readAllBytes(file.toPath());
                ImageView iv = new ImageView(new Image(file.toURI().toString()));
                iv.setFitWidth(200); iv.setFitHeight(200);
                iv.setPreserveRatio(true);
                coverImagePane.getChildren().setAll(iv);
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    @FXML
    private void onCommencer() {
        String nom = nomField.getText().trim();
        if (nom.isEmpty()) {
            nomField.setStyle("-fx-border-color: red;");
            return;
        }
        TierList tl = new TierList(nom);
        tl.setDescription(descriptionArea.getText().trim());
        tl.setCoverImageData(coverImageData);
        persistenceService.save(tl);

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/TierListEditor.fxml")
            );
            Stage stage = (Stage) nomField.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            TierListEditorController ctrl = loader.getController();
            ctrl.setTierList(tl);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    private void onAccueil() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/Accueil.fxml")
            );
            Stage stage = (Stage) nomField.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
        } catch (IOException e) { e.printStackTrace(); }
    }
}