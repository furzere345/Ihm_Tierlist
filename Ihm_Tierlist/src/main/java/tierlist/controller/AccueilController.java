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
import javafx.stage.Modality;
import javafx.stage.Stage;
import tierlist.Main;
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
    public void setDarkTheme(boolean darkTheme) {
        this.isDarkTheme = darkTheme;
    }
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
            Parent scene = loader.load();
            CreationTierListController nextController = loader.getController();
            nextController.setDarkTheme(this.isDarkTheme);
            if (isDarkTheme==true){
                scene.getStylesheets().add(getClass().getResource("/css/dark.css").toExternalForm());
            }
            else{
                scene.getStylesheets().add(getClass().getResource("/css/light.css").toExternalForm());
            }
            Stage stage = (Stage) buttonCrreTierlist.getScene().getWindow();
            stage.setScene(new Scene(scene));

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

        @FXML
        private void onToggleTheme() {
            isDarkTheme = !isDarkTheme;
            String css = isDarkTheme ? "/css/dark.css" : "/css/light.css";
            String cssUrl = getClass().getResource(css).toExternalForm();

            // 1. On récupère la scène
            Scene scene = buttonTheme.getScene();
                // On nettoie la scène ET on applique le nouveau CSS
                scene.getStylesheets().clear();
                scene.getStylesheets().add(cssUrl);


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
        card.getStyleClass().add("tierlist-card"); // Classe de la carte globale

        Label nom = new Label(tl.getName());
        nom.getStyleClass().add("tierlist-card-title"); // Classe du titre
        nom.setMaxWidth(200);
        nom.setWrapText(true);

        // Image de couverture
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefSize(200, 160);
        imageContainer.setMinSize(200, 160);
        imageContainer.setMaxSize(200, 160);
        imageContainer.getStyleClass().add("tierlist-card-image-container"); // Classe du conteneur d'image

        if (tl.getCoverImageData() != null) {
            ImageView iv = new ImageView(new Image(new java.io.ByteArrayInputStream(tl.getCoverImageData())));
            iv.setFitWidth(200);
            iv.setFitHeight(160);
            iv.setPreserveRatio(true);
            imageContainer.getChildren().add(iv);
        } else {
            // Placeholder si pas d'image
            Label placeholder = new Label("Aucune image");
            placeholder.getStyleClass().add("tierlist-card-placeholder"); // Classe du texte d'absence d'image
            imageContainer.getChildren().add(placeholder);
        }

        Button menu = new Button(". . .");
        menu.getStyleClass().add("tierlist-card-menu-button"); // Classe du bouton option
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
            Stage stage = (Stage) buttonCrreTierlist.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            TierListEditorController ctrl = loader.getController();
            ctrl.setTierList(tl);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
