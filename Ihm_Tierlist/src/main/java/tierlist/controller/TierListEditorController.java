package tierlist.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.*;
import tierlist.model.Item;
import tierlist.model.Tier;
import tierlist.model.TierList;
import tierlist.service.PersistenceService;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

public class TierListEditorController {

    @FXML private Label labelTierListName;
    @FXML private VBox tiersContainer;
    @FXML private FlowPane unclassifiedPane;
    @FXML private Button btnAddTier;
    @FXML private Button btnReset;
    @FXML private Button btnSave;
    @FXML private Button btnBack;
    @FXML private Button btnAddItem;
    @FXML private Button btnAddImageItem;
    @FXML private Label labelDescription;
    @FXML private Label itemCountBadge;
    @FXML private ImageView coverImageView;
    @FXML private Label coverPlaceholder;
    @FXML private Button btnTheme;

    private TierList tierList;
    private PersistenceService persistenceService = new PersistenceService();

    public void setTierList(TierList tl) {
        this.tierList = tl;
        labelTierListName.setText(tl.getName());

        //Description
        if (labelDescription != null)
            labelDescription.setText(tl.getDescription() != null ? tl.getDescription() : "");

        //Image de couverture
        if (tl.getCoverImageData() != null && coverImageView != null) {
            coverImageView.setImage(new Image(new ByteArrayInputStream(tl.getCoverImageData())));
            if (coverPlaceholder != null) coverPlaceholder.setVisible(false);
        }

        //Badge compteur
        updateItemCountBadge();

        refresh();
    }

    private void updateItemCountBadge() {
        if (itemCountBadge == null) return;
        int total = tierList.getUnclassifiedItems().size();
        for (Tier t : tierList.getTiers()) total += t.getItems().size();
        itemCountBadge.setText(String.valueOf(total));
    }

    @FXML
    public void initialize() {
        // Les handlers boutons sont déclarés ici
        // (les @FXML methods suffisent, pas besoin de setOnAction manuel)
    }

    // =========================================================================
    // AFFICHAGE
    // =========================================================================

    //Reconstruit toute la vue depuis le modèele
    private void refresh() {
        buildTiersUI();
        buildUnclassifiedUI();
        updateItemCountBadge();
    }

    //Construire la liste des tiers
    private void buildTiersUI() {
        tiersContainer.getChildren().clear();
        for (Tier tier : tierList.getTiers()) {
            tiersContainer.getChildren().add(buildTierRow(tier));
        }
    }

    //Cree une ligne complete pour un tier : [label color | items | bouton édition]
    private HBox buildTierRow(Tier tier) {
        Label label = new Label(tier.getName());
        label.setPrefWidth(60);
        label.setPrefHeight(tier.getHeight());
        label.setStyle(
                "-fx-background-color: " + tier.getColorHex() + ";" +
                        "-fx-alignment: center;" +
                        "-fx-font-size: 16px;" +
                        "-fx-text-fill: black;" +
                        "-fx-font-weight: bold;"
        );

        FlowPane itemsPane = new FlowPane();
        itemsPane.setHgap(6);
        itemsPane.setVgap(6);
        itemsPane.setPadding(new Insets(6));
        itemsPane.setPrefHeight(tier.getHeight());
        itemsPane.setStyle("-fx-background-color: #2a2a2a;");
        HBox.setHgrow(itemsPane, Priority.ALWAYS);

        for (Item item : tier.getItems()) {
            itemsPane.getChildren().add(buildItemNode(item, tier, itemsPane));
        }
        enableDrop(itemsPane, tier);

        //Boutons droite : edition + monter + descendre
        Button editBtn = new Button("⚙");
        editBtn.setPrefSize(30, 30);
        editBtn.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: white; -fx-font-size: 14;");
        editBtn.setOnAction(e -> openOverlayTier(tier));

        Button upBtn = new Button("∧");
        upBtn.setPrefSize(30, 30);
        upBtn.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: white; -fx-font-size: 12;");
        upBtn.setOnAction(e -> moveTier(tier, -1));

        Button downBtn = new Button("∨");
        downBtn.setPrefSize(30, 30);
        downBtn.setStyle("-fx-background-color: #3a3a3a; -fx-text-fill: white; -fx-font-size: 12;");
        downBtn.setOnAction(e -> moveTier(tier, +1));

        VBox controls = new VBox(2, editBtn, upBtn, downBtn);
        controls.setAlignment(javafx.geometry.Pos.CENTER);
        controls.setPadding(new Insets(4));
        controls.setStyle("-fx-background-color: #222222;");

        HBox row = new HBox(label, itemsPane, controls);
        row.setStyle("-fx-border-color: #333333; -fx-border-width: 0 0 1 0;");
        return row;
    }

    private void moveTier(Tier tier, int direction) {
        List<Tier> tiers = tierList.getTiers();
        int index = tiers.indexOf(tier);
        int newIndex = index + direction;

        //Verifier qu'on reste dans les bornes
        if (newIndex < 0 || newIndex >= tiers.size()) return;

        //Echanger les deux tiers
        tiers.remove(index);
        tiers.add(newIndex, tier);

        //Mettre à jour l'ordre dans le modele
        for (int i = 0; i < tiers.size(); i++) {
            tiers.get(i).setOrder(i);
        }

        persistenceService.save(tierList);
        buildTiersUI(); //rafraichir uniquement les tiers
    }

    //Construire la zone "à classer"
    private void buildUnclassifiedUI() {
        unclassifiedPane.getChildren().clear();
        for (Item item : tierList.getUnclassifiedItems()) {
            unclassifiedPane.getChildren().add(
                    buildItemNode(item, null, unclassifiedPane)
            );
        }
        enableDrop(unclassifiedPane, null); // null = zone non classifiée
    }

    // =========================================================================
    // ITEMS
    // =========================================================================

    //Cree le nœud visuel d'un item (texte ou image)
    private Node buildItemNode(Item item, Tier sourceTier, FlowPane sourcePane) {
        VBox box = new VBox();
        box.setAlignment(javafx.geometry.Pos.CENTER);
        box.setPrefSize(item.getSize(), item.getSize());
        box.setStyle(
                "-fx-background-color: #2a2a2a;" +
                        "-fx-border-color: #555;" +
                        "-fx-border-radius: 4;" +
                        "-fx-background-radius: 4;" +
                        "-fx-cursor: hand;"
        );
        box.setUserData(item.getId()); // utilisé par le drag & drop

        if (item.getType() == Item.ItemType.IMAGE && item.getImageData() != null) {
            ImageView iv = new ImageView(
                    new Image(new ByteArrayInputStream(item.getImageData()))
            );
            iv.setFitWidth(item.getSize() - 8);
            iv.setFitHeight(item.getSize() - 8);
            iv.setPreserveRatio(true);
            box.getChildren().add(iv);
        } else {
            Label lbl = new Label(item.getLabel());
            lbl.setTextFill(Color.WHITE);
            lbl.setWrapText(true);
            lbl.setMaxWidth(item.getSize() - 8);
            box.getChildren().add(lbl);
        }

        //Clic droit -> menu contextuel (modifier / supprimer)
        box.setOnContextMenuRequested(e -> showItemContextMenu(box, item, sourceTier));

        //Drag
        enableDrag(box, item);

        return box;
    }

    //Ajouter un item texte
    @FXML
    private void onAddItem() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nouvel item");
        dialog.setHeaderText(null);
        dialog.setContentText("Nom de l'item :");
        Optional<String> result = dialog.showAndWait();
        result.filter(s -> !s.isBlank()).ifPresent(name -> {
            tierList.getUnclassifiedItems().add(new Item(name));
            persistenceService.save(tierList);
            buildUnclassifiedUI();
        });
    }

    //Ajouter un item image
    @FXML
    private void onAddImageItem() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choisir une image");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File file = fc.showOpenDialog(btnAddImageItem.getScene().getWindow());
        if (file != null) {
            try {
                byte[] data = Files.readAllBytes(file.toPath());
                String label = file.getName().replaceFirst("[.][^.]+$", "");
                tierList.getUnclassifiedItems().add(new Item(label, data));
                persistenceService.save(tierList);
                buildUnclassifiedUI();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //Menu contextuel sur un item
    private void showItemContextMenu(VBox box, Item item, Tier sourceTier) {
        ContextMenu menu = new ContextMenu();

        MenuItem renommer = new MenuItem("Renommer");
        renommer.setOnAction(e -> {
            TextInputDialog d = new TextInputDialog(item.getLabel());
            d.setHeaderText(null);
            d.setContentText("Nouveau nom :");
            d.showAndWait().filter(s -> !s.isBlank()).ifPresent(s -> {
                item.setLabel(s);
                persistenceService.save(tierList);
                refresh();
            });
        });

        MenuItem supprimer = new MenuItem("Supprimer");
        supprimer.setOnAction(e -> {
            if (sourceTier != null) sourceTier.getItems().remove(item);
            else tierList.getUnclassifiedItems().remove(item);
            persistenceService.save(tierList);
            refresh();
        });

        menu.getItems().addAll(renommer, supprimer);
        menu.show(box, Side.BOTTOM, 0, 0);
    }

    // =========================================================================
    // TIERS
    // =========================================================================

    //Ajouter un tier
    @FXML
    private void onAddTier() {
        openOverlayTier(null); // null = mode création
    }

    //Ouvrir l'overlay de creation/édition d'un tier
    private void openOverlayTier(Tier tierToEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/Overlay.fxml")
            );
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(tierToEdit == null ? "Nouveau tier" : "Modifier le tier");
            stage.setScene(new Scene(loader.load()));

            OverlayTierController ctrl = loader.getController();
            ctrl.setTier(
                    tierToEdit,
                    //onConfirm
                    savedTier -> {
                        if (tierToEdit == null) {
                            savedTier.setOrder(tierList.getTiers().size());
                            tierList.getTiers().add(savedTier);
                        }
                        persistenceService.save(tierList);
                        refresh();
                    },
                    //onDelete
                    () -> {
                        tierList.getTiers().remove(tierToEdit);
                        persistenceService.save(tierList);
                        refresh();
                    }
            );

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // =========================================================================
    // ACTIONS GLOBALES
    // =========================================================================

    //Reinitialiser la tier-list
    @FXML
    private void onReset() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Réinitialiser");
        confirm.setHeaderText(null);
        confirm.setContentText("Remettre tous les items dans la zone à classer ?");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                tierList.reset();
                persistenceService.save(tierList);
                refresh();
            }
        });
    }

    //Ouvrir l'overlay de sauvegarde
    @FXML
    private void onSave() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/Overlay-save.fxml")
            );
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Sauvegarder");
            stage.setScene(new Scene(loader.load()));

            OverlaySaveController ctrl = loader.getController();
            ctrl.setData(tierList, tiersContainer);

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Retour a l'accueil
    @FXML
    private void onBack() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/Accueil.fxml")
            );
            Stage stage = (Stage) btnBack.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // =========================================================================
    // DRAG & DROP (a enrichir pour plus fluid)
    // =========================================================================

    private void enableDrag(VBox itemNode, Item item) {
        itemNode.setOnDragDetected(e -> {
            var db = itemNode.startDragAndDrop(javafx.scene.input.TransferMode.MOVE);
            var content = new javafx.scene.input.ClipboardContent();
            content.putString(item.getId()); // on passe l'ID de l'item
            db.setContent(content);
            e.consume();
        });
    }

    private void enableDrop(FlowPane target, Tier targetTier) {
        target.setOnDragOver(e -> {
            if (e.getDragboard().hasString()) {
                e.acceptTransferModes(javafx.scene.input.TransferMode.MOVE);
            }
            e.consume();
        });

        target.setOnDragDropped(e -> {
            String itemId = e.getDragboard().getString();
            Item movedItem = findItemById(itemId);
            if (movedItem == null) { e.consume(); return; }

            //Retirer l'item de sa source
            removeItemFromAllLocations(movedItem);

            //Ajouter dans la cible
            if (targetTier != null) targetTier.getItems().add(movedItem);
            else tierList.getUnclassifiedItems().add(movedItem);

            persistenceService.save(tierList);
            refresh();
            e.setDropCompleted(true);
            e.consume();
        });
    }

    //Cherche un item dans tous les tiers + unclassified
    private Item findItemById(String id) {
        for (Item item : tierList.getUnclassifiedItems())
            if (item.getId().equals(id)) return item;
        for (Tier tier : tierList.getTiers())
            for (Item item : tier.getItems())
                if (item.getId().equals(id)) return item;
        return null;
    }

    //Retire un item de partout (avant de le déplacer)
    private void removeItemFromAllLocations(Item item) {
        tierList.getUnclassifiedItems().remove(item);
        for (Tier tier : tierList.getTiers())
            tier.getItems().remove(item);
    }
}
