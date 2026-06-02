package tierlist.controller;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tierlist.model.TierList;
import tierlist.service.PersistenceService;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class OverlaySaveController {

    @FXML private Button downloadButton;
    @FXML private ImageView previewImage;
    @FXML private Button exportTlButton;
    @FXML private Label tierlistName;

    private TierList tierList;
    private Pane tierListPane;
    private PersistenceService persistenceService = new PersistenceService();
    private boolean isDarkTheme;

    public void setData(TierList tl, Pane pane) {
        this.tierList = tl;
        this.tierListPane = pane;
        tierlistName.setText(tierList.getName());

        //Generer l'aperçu depuis le vrai rendu
        WritableImage snapshot = pane.snapshot(new SnapshotParameters(), null);
        previewImage.setImage(snapshot);
    }

    //Exporter en image PNG
    @FXML
    private void onDownload() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Exporter en image");
        fc.setInitialFileName(tierList.getName() + ".png");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG", "*.png"));
        File file = fc.showSaveDialog(downloadButton.getScene().getWindow());
        if (file != null) {
            WritableImage img = tierListPane.snapshot(new SnapshotParameters(), null);
            showSuccess("Tier-list exportée avec succès !");
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(img, null), "png", file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //Exporter en fichier .tl (binaire)
    @FXML
    private void onExportTl() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Exporter la tier-list");
        fc.setInitialFileName(tierList.getName() + ".tl");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Tier List", "*.tl"));
        File file = fc.showSaveDialog(downloadButton.getScene().getWindow());
        if (file != null) {
            persistenceService.exportTo(tierList, file);
            showSuccess("Tier-list exportée avec succès !");
        }
    }

    private void showSuccess(String message) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/CustomAlert.fxml"));
            Parent root = loader.load();
            CustomAlertController ctrl = loader.getController();
            ctrl.configurer("Succès", message);
            Scene scene = new Scene(root);
            String cssPath = this.isDarkTheme ? "/css/dark.css" : "/css/light.css";
            scene.getStylesheets().setAll(getClass().getResource(cssPath).toExternalForm());
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeStage() {
        ((Stage) downloadButton.getScene().getWindow()).close();
    }


    public void setDarkTheme(boolean isDarkTheme) {this.isDarkTheme=isDarkTheme;
    }
}
