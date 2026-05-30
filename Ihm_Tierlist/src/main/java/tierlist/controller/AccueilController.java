package tierlist.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tierlist.model.TierList;
import tierlist.service.PersistenceService;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class AccueilController {

    @FXML private Button buttonCrreTierlist;
    @FXML private Button buttonImporter;
    @FXML private Button buttonTheme;
    @FXML private FlowPane tierlistContainer;

    private boolean isDarkTheme = true;
    private PersistenceService persistenceService = new PersistenceService();

    @FXML
    public void initialize() {
        List<TierList> tierLists = persistenceService.loadAll();
        refreshTierListCards(tierLists);
    }

    //Creer une nouvelle tier-list
    @FXML
    private void onCreerTierlist() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/CreationTierList.fxml")
            );
            Stage stage = (Stage) buttonCrreTierlist.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
        } catch (IOException e) { e.printStackTrace(); }
    }

    //Importer depuis un fichier binaire
    @FXML
    private void onImporter() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Importer une Tier List");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Tier List Files", "*.tl")
        );
        File file = fc.showOpenDialog(buttonImporter.getScene().getWindow());
        if (file != null) {
            TierList imported = persistenceService.importFrom(file);
            if (imported != null) {
                persistenceService.save(imported);
                refreshTierListCards(persistenceService.loadAll());
            }
        }
    }

    //Basculer thème clair/sombre
    //A faire
    @FXML
    private void onToggleTheme() {
        isDarkTheme = !isDarkTheme;
        Scene scene = buttonTheme.getScene();
        scene.getStylesheets().clear();
        String css = isDarkTheme ? "/css/dark.css" : "/css/light.css";
        scene.getStylesheets().add(getClass().getResource(css).toExternalForm());
    }

    //Genérer dynamiquement les cartes
    private void refreshTierListCards(List<TierList> tierLists) {
        tierlistContainer.getChildren().clear();
        for (TierList tl : tierLists) {
            tierlistContainer.getChildren().add(createCard(tl));
        }
    }

    private VBox createCard(TierList tl) {
        VBox card = new VBox(10);
        card.setPrefSize(300, 295);
        card.setStyle("-fx-cursor: hand;");

        Label nom = new Label(tl.getName());
        nom.setStyle("-fx-text-fill: white; -fx-font-size: 24;");

        ImageView img = new ImageView();
        img.setFitWidth(204);
        img.setFitHeight(172);
        img.setPreserveRatio(true);
        if (tl.getCoverImageData() != null) {
            img.setImage(new Image(
                    new java.io.ByteArrayInputStream(tl.getCoverImageData())
            ));
        }

        Button menu = new Button(". . .");
        menu.setStyle("-fx-background-color: #070707; -fx-border-color: white;");
        menu.setTextFill(javafx.scene.paint.Color.WHITE);
        menu.setOnAction(e -> showCardMenu(tl, card));

        // Clic sur la carte → ouvrir la tier-list
        card.setOnMouseClicked(e -> {
            if (e.getTarget() != menu) ouvrirTierList(tl);
        });

        card.getChildren().addAll(nom, img, menu);
        return card;
    }

    //Menu contextuel d'une carte (dupliquer / supprimer)
    private void showCardMenu(TierList tl, VBox card) {
        javafx.scene.control.ContextMenu menu = new javafx.scene.control.ContextMenu();

        javafx.scene.control.MenuItem dupliquer =
                new javafx.scene.control.MenuItem("Dupliquer");
        dupliquer.setOnAction(e -> {
            TierList copy = tl.duplicate();
            persistenceService.save(copy);
            refreshTierListCards(persistenceService.loadAll());
        });

        javafx.scene.control.MenuItem supprimer =
                new javafx.scene.control.MenuItem("Supprimer");
        supprimer.setOnAction(e -> {
            persistenceService.delete(tl.getId());
            refreshTierListCards(persistenceService.loadAll());
        });

        menu.getItems().addAll(dupliquer, supprimer);
        menu.show(card,
                javafx.geometry.Side.BOTTOM, 0, 0);
    }

    //Naviguer vers l'éditeur
    private void ouvrirTierList(TierList tl) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/TierListEditor.fxml")
            );
            Stage stage = (Stage) buttonCrreTierlist.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            TierListEditorController ctrl = loader.getController();
            ctrl.setTierList(tl);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
