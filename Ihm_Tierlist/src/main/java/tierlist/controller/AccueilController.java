package tierlist.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tierlist.model.TierList;
import tierlist.service.PersistenceService;
import javafx.scene.Parent;
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/CreationTierList.fxml"));
            Parent root = loader.load();
            CreationTierListController nextController = loader.getController();
            nextController.setDarkTheme(this.isDarkTheme);
            Stage stage = (Stage) buttonCrreTierlist.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Importer depuis un fichier binaire
    @FXML
    private void onImporter() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Importer une Tier List");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Tier List Files", "*.tl"));
        File file = fc.showOpenDialog(buttonImporter.getScene().getWindow());
        if (file != null) {
            TierList imported = persistenceService.importFrom(file);
            if (imported != null) {
                //Generer un nouvel ID pour eviter les conflit
                imported = reIdTierList(imported);
                persistenceService.save(imported);
                refreshTierListCards(persistenceService.loadAll());
                showImportSuccess(imported.getName());
            } else {
                showImportError();
            }
        }
    }

    //evite les conflits d'ID si on importe 2x le meme fichier
    private TierList reIdTierList(TierList tl) {
        TierList copy = tl.duplicate();
        copy.setName(tl.getName()); //duplicate() prefixe "Copie de", on remet le vrai nom
        return copy;
    }

    private void showImportSuccess(String name) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Import réussi");
        alert.setHeaderText(null);
        alert.setContentText("\"" + name + "\" importée avec succès !");
        alert.showAndWait();
    }

    private void showImportError() {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Erreur d'import");
        alert.setHeaderText(null);
        alert.setContentText("Le fichier sélectionné n'est pas valide.");
        alert.showAndWait();
    }

    //Basculer theme clair/sombre
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
        card.setPrefSize(220, 250);
        card.setStyle("-fx-background-color: #2a2a2a;" +
                        "-fx-border-color: #444444;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 10;"
        );

        Label nom = new Label(tl.getName());
        nom.setTextFill(javafx.scene.paint.Color.WHITE);
        nom.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        nom.setMaxWidth(200);
        nom.setWrapText(true);

        //Image de couverture
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefSize(200, 160);
        imageContainer.setMinSize(200, 160);
        imageContainer.setMaxSize(200, 160);
        imageContainer.setStyle("-fx-background-color: #1a1a1a;" +
                        "-fx-border-color: #1a1a1a;" +
                        "-fx-border-radius: 4;" +
                        "-fx-background-radius: 4;"
        );

        if (tl.getCoverImageData() != null) {
            ImageView iv = new ImageView(new Image(new java.io.ByteArrayInputStream(tl.getCoverImageData())));
            iv.setFitWidth(200);
            iv.setFitHeight(160);
            iv.setPreserveRatio(true);
            imageContainer.getChildren().add(iv);
        } else {
            //Placeholder si pas d'image
            Label placeholder = new Label("Aucune image");
            placeholder.setTextFill(javafx.scene.paint.Color.web("#666666"));
            placeholder.setStyle("-fx-font-size: 12px;");
            imageContainer.getChildren().add(placeholder);
        }

        Button menu = new Button(". . .");
        menu.setStyle("-fx-background-color: #3a3a3a;" +
                        "-fx-border-color: #666666;" +
                        "-fx-border-radius: 4;" +
                        "-fx-background-radius: 4;"
        );
        menu.setTextFill(javafx.scene.paint.Color.WHITE);
        menu.setMaxWidth(Double.MAX_VALUE);
        menu.setOnAction(e -> showCardMenu(tl, card));

        card.setOnMouseClicked(e -> {
            if (e.getTarget() != menu) ouvrirTierList(tl);
        });

        card.getChildren().addAll(imageContainer, nom, menu);
        return card;
    }

    //Menu contextuel d'une carte (dupliquer / supprimer)
    private void showCardMenu(TierList tl, VBox card) {
        javafx.scene.control.ContextMenu menu = new javafx.scene.control.ContextMenu();

        javafx.scene.control.MenuItem dupliquer = new javafx.scene.control.MenuItem("Dupliquer");
        dupliquer.setOnAction(e -> {
            TierList copy = tl.duplicate();
            persistenceService.save(copy);
            refreshTierListCards(persistenceService.loadAll());
        });

        javafx.scene.control.MenuItem supprimer = new javafx.scene.control.MenuItem("Supprimer");
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/TierListEditor.fxml"));
            Parent root = loader.load();
            TierListEditorController ctrl = loader.getController();
            ctrl.setTierList(tl);
            Stage stage = (Stage) buttonCrreTierlist.getScene().getWindow();
            Scene scene = new Scene(root, 1200, 800);
            stage.setScene(scene);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
