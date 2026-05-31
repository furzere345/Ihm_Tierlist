package tierlist.controller;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tierlist.model.TierList;
import tierlist.service.PersistenceService;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class OverlaySaveController {

    @FXML private TextField tierlistNameField;
    @FXML private TextArea descriptionArea;
    @FXML private Button saveButton;
    @FXML private Button downloadButton;
    @FXML private ImageView previewImage;
    @FXML private Button exportTlButton;

    private TierList tierList;
    private Pane tierListPane;
    private PersistenceService persistenceService = new PersistenceService();

    public void setData(TierList tl, Pane pane) {
        this.tierList = tl;
        this.tierListPane = pane;

        tierlistNameField.setText(tl.getName());
        descriptionArea.setText(tl.getDescription());

        //Generer l'aperçu depuis le vrai rendu
        WritableImage snapshot = pane.snapshot(new SnapshotParameters(), null);
        previewImage.setImage(snapshot);
    }

    //Sauvegarder en binaire
    @FXML
    private void onSave() {
        tierList.setName(tierlistNameField.getText().trim());
        tierList.setDescription(descriptionArea.getText().trim());
        persistenceService.save(tierList);
        closeStage();
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
        File file = fc.showSaveDialog(saveButton.getScene().getWindow());
        if (file != null) {
            persistenceService.exportTo(tierList, file);
            showSuccess("Tier-list exportée avec succès !");
        }
    }

    //Feedback succes
    private void showSuccess(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeStage() {
        ((Stage) saveButton.getScene().getWindow()).close();
    }
}
