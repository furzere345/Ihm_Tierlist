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

    @FXML
    private Label labelTierListName;
    @FXML
    private VBox tiersContainer;
    @FXML
    private FlowPane unclassifiedPane;
    @FXML
    private Button btnAddTier;
    @FXML
    private Button btnReset;
    @FXML
    private Button btnSave;
    @FXML
    private Button btnBack;
    @FXML
    private Button btnAddItem;
    @FXML
    private Button btnAddImageItem;
    @FXML
    private Label labelDescription;
    @FXML
    private Label itemCountBadge;
    @FXML
    private ImageView coverImageView;
    @FXML
    private Label coverPlaceholder;
    @FXML
    private Button btnTheme;
    @FXML
    private Button btnRename;

    private TierList tierList;
    private PersistenceService persistenceService = new PersistenceService();
    private boolean isDarkTheme = true;

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

    @FXML
    private void onToggleTheme() {
        isDarkTheme = !isDarkTheme;
        String css = isDarkTheme ? "/css/dark.css" : "/css/light.css";
        String cssUrl = getClass().getResource(css).toExternalForm();

        Scene scene = btnTheme.getScene();
        if (scene != null) {
            // 1. On applique sur la scène globale
            scene.getStylesheets().setAll(cssUrl);

            // 2. CORRECTION : On force le FXML racine à vider son CSS et à prendre le nouveau
            if (scene.getRoot() != null) {
                scene.getRoot().getStylesheets().setAll(cssUrl);
            }
        }
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
        label.setPrefWidth(100);
        label.setPrefHeight(tier.getHeight());
        label.setMinWidth(100);
        label.setWrapText(true);

        // On garde uniquement la couleur de fond dynamique propre au tier, le reste va en CSS
        label.getStyleClass().add("tier-label");
        label.setStyle("-fx-background-color: " + tier.getColorHex() + ";");

        FlowPane itemsPane = new FlowPane();
        itemsPane.setHgap(6);
        itemsPane.setVgap(6);
        itemsPane.setPadding(new Insets(6));
        itemsPane.setPrefHeight(tier.getHeight());
        itemsPane.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // CORRECTION : Utilise la classe CSS pour la zone de dépôt
        itemsPane.getStyleClass().add("tier-row-content");
        HBox.setHgrow(itemsPane, Priority.ALWAYS);

        for (Item item : tier.getItems()) {
            itemsPane.getChildren().add(buildItemNode(item, tier, itemsPane));
        }
        enableDrop(itemsPane, tier);

        // Boutons droite : edition + monter + descendre
        Button editBtn = new Button("⚙");
        editBtn.setPrefSize(32, 32);
        editBtn.getStyleClass().add("tier-control-button"); // Classe CSS
        editBtn.setOnAction(e -> openOverlayTier(tier));

        Button upBtn = new Button("∧");
        upBtn.setPrefSize(32, 32);
        upBtn.getStyleClass().add("tier-control-button");   // Classe CSS
        upBtn.setOnAction(e -> moveTier(tier, -1));

        Button downBtn = new Button("∨");
        downBtn.setPrefSize(32, 32);
        downBtn.getStyleClass().add("tier-control-button"); // Classe CSS
        downBtn.setOnAction(e -> moveTier(tier, +1));

        VBox controls = new VBox(2, editBtn, upBtn, downBtn);
        controls.setAlignment(javafx.geometry.Pos.CENTER);
        controls.setPadding(new Insets(4));

        // CORRECTION : Classe CSS pour le petit conteneur des boutons à droite
        controls.getStyleClass().add("tier-controls-box");

        HBox row = new HBox(label, itemsPane, controls);

        // CORRECTION : Classe CSS pour la ligne complète
        row.getStyleClass().add("tier-full-row");

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

    //Construire la zone "a classer"
    private void buildUnclassifiedUI() {
        unclassifiedPane.getChildren().clear();
        for (Item item : tierList.getUnclassifiedItems()) {
            unclassifiedPane.getChildren().add(buildItemNode(item, null, unclassifiedPane));
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
        box.getStyleClass().add("tierlist-item");
        box.setUserData(item.getId()); // utilisé par le drag & drop

        if (item.getType() == Item.ItemType.IMAGE && item.getImageData() != null) {
            ImageView iv = new ImageView(new Image(new ByteArrayInputStream(item.getImageData())));
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
        enableDrag(box, item, sourceTier);

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
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));
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

    @FXML
    private void onImportApi() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/OverlayApi.fxml"));
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Importer depuis RAWG");
            stage.setScene(new Scene(loader.load()));
            OverlayApiController ctrl = loader.getController();
            ctrl.setOnImport(items -> {
                tierList.getUnclassifiedItems().addAll(items);
                persistenceService.save(tierList);
                refresh();
            });

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Overlay.fxml"));
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
                        tierList.getUnclassifiedItems().addAll(tierToEdit.getItems());
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Overlay-save.fxml"));
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Accueil.fxml"));
            Stage stage = (Stage) btnBack.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onModifierTierList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/RenommerTierList.fxml"));

            Scene scene = new Scene(loader.load());

            RenommerTierListController controller = loader.getController();

            controller.setTierList(tierList);

            Stage stage = new Stage();
            stage.setTitle("Modifier la Tier-List");
            stage.setScene(scene);
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);

            stage.showAndWait();
            setTierList(this.tierList);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // =========================================================================
    // DRAG & DROP
    // =========================================================================

    //Item en cours de drag
    private Item draggedItem = null;
    private Tier draggedFromTier = null; //null = vient de unclassified

    private void enableDrag(VBox itemNode, Item item, Tier sourceTier) {
        itemNode.setOnDragDetected(e -> {
            draggedItem = item;
            draggedFromTier = sourceTier;

            var db = itemNode.startDragAndDrop(javafx.scene.input.TransferMode.MOVE);
            var content = new javafx.scene.input.ClipboardContent();
            content.putString(item.getId());
            db.setContent(content);

            //Feedback visuel : l'item devient semi-transparent
            itemNode.setOpacity(0.4);
            e.consume();
        });

        itemNode.setOnDragDone(e -> {
            //Remettre l'opacite normale si le drop n'a pas eu lieu
            itemNode.setOpacity(1.0);
            draggedItem = null;
            draggedFromTier = null;
            e.consume();
        });
    }

    private void enableDrop(FlowPane target, Tier targetTier) {

        //Accepter le drag
        target.setOnDragOver(e -> {
            if (e.getDragboard().hasString() && draggedItem != null) {
                e.acceptTransferModes(javafx.scene.input.TransferMode.MOVE);
                //Feedback : bordure colore sur la zone cible
                target.setStyle(target.getStyle().replace("-fx-border-color: #444444;", "") + "-fx-border-color: #3D81FF; -fx-border-width: 2;");
            }
            e.consume();
        });

        //Retirer le feedback quand on quitte la zone
        target.setOnDragExited(e -> {
            resetPaneStyle(target);
            e.consume();
        });

        //Drop effectif
        target.setOnDragDropped(e -> {
            if (draggedItem == null) {
                e.consume();
                return;
            }

            //Calculer la position d'insertion selon la souris
            int insertIndex = getInsertIndex(target, e.getX());

            //Retirer l'item de sa source
            removeItemFromAllLocations(draggedItem);

            //Inserer a la bonne position dans la cible
            if (targetTier != null) {
                insertIndex = Math.min(insertIndex, targetTier.getItems().size());
                targetTier.getItems().add(insertIndex, draggedItem);
            } else {
                insertIndex = Math.min(insertIndex, tierList.getUnclassifiedItems().size());
                tierList.getUnclassifiedItems().add(insertIndex, draggedItem);
            }

            persistenceService.save(tierList);
            refresh();

            resetPaneStyle(target);
            e.setDropCompleted(true);
            e.consume();
        });
    }

    //Calcule l'index d'insertion en fonction de la position X de la souris
    private int getInsertIndex(FlowPane pane, double mouseX) {
        int index = 0;
        for (javafx.scene.Node child : pane.getChildren()) {
            //Ignorer l'indicateur de position si présent
            if (child.getUserData() != null && child.getUserData().equals("drop-indicator")) {
                continue;
            }
            javafx.geometry.Bounds bounds = child.getBoundsInParent();
            double midX = bounds.getMinX() + bounds.getWidth() / 2;
            if (mouseX > midX) index++;
            else break;
        }
        return index;
    }

    //Remet le style normal d'un FlowPane
    private void resetPaneStyle(FlowPane pane) {
        if (pane == unclassifiedPane) {
            pane.setStyle(
                    "-fx-background-color: #222222;" +
                            "-fx-border-color: #444444;" +
                            "-fx-border-radius: 5;" +
                            "-fx-background-radius: 5;"
            );
        } else {
            pane.setStyle("-fx-background-color: #2a2a2a;");
        }
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

    //Retire un item de partout
    private void removeItemFromAllLocations(Item item) {
        tierList.getUnclassifiedItems().remove(item);
        for (Tier tier : tierList.getTiers())
            tier.getItems().remove(item);
    }

    public void setDarkTheme(boolean isDarkTheme) { this.isDarkTheme=isDarkTheme;
    }
}
