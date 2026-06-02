package tierlist.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
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

public class CreationTierListController {

    @FXML
    private TextField nomField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private StackPane coverImagePane;
    @FXML
    private Button buttonTheme;
    @FXML
    private Button btnAccueil;
    @FXML private Button btnCommencer;

    private byte[] coverImageData;
    private PersistenceService persistenceService = new PersistenceService();
    private boolean isDarkTheme;

    public void setDarkTheme(boolean darkTheme) {
        this.isDarkTheme = darkTheme;
    }

    @FXML
    private void onToggleTheme() {
        isDarkTheme = !isDarkTheme;
        String css = isDarkTheme ? "/css/dark.css" : "/css/light.css";
        String cssUrl = getClass().getResource(css).toExternalForm();

        Scene scene = buttonTheme.getScene();
        if (scene != null) {
            // 1. On applique sur la scène globale
            scene.getStylesheets().setAll(cssUrl);

            // 2. CORRECTION : On force le FXML racine à vider son CSS et à prendre le nouveau
            if (scene.getRoot() != null) {
                scene.getRoot().getStylesheets().setAll(cssUrl);
            }
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
        if (nom.isEmpty()) {
            nomField.setStyle("-fx-border-color: red;");
            return;
        }

        TierList tl = new TierList(nom);
        tl.setDescription(descriptionArea.getText().trim());
        tl.setCoverImageData(coverImageData);
        persistenceService.save(tl);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/TierListEditor.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 800);

            TierListEditorController ctrl = loader.getController();
            ctrl.setTierList(tl);
            ctrl.setDarkTheme(this.isDarkTheme);

            String cssPath = this.isDarkTheme ? "/css/dark.css" : "/css/light.css";
            scene.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());

            Stage stage = (Stage) nomField.getScene().getWindow();
            stage.setScene(scene);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onAccueil() {
        try {
            // 1. Chargement de l'Accueil
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Accueil.fxml"));
            Parent root = loader.load();

            // 2. Récupération du BON contrôleur (AccueilController) et envoi du thème
            AccueilController nextController = loader.getController();
            nextController.setDarkTheme(this.isDarkTheme);

            // 3. Création de la scène et application du CSS global
            Scene nextScene = new Scene(root);
            String cssPath = this.isDarkTheme ? "/css/dark.css" : "/css/light.css";
            nextScene.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());

            // 4. Affichage
            Stage stage = (Stage) btnAccueil.getScene().getWindow();
            stage.setScene(nextScene);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}