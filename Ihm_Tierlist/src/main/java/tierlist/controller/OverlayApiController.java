package tierlist.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import tierlist.model.Item;
import tierlist.service.RawgApiService;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.function.Consumer;

public class OverlayApiController {

    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private FlowPane resultsPane;
    @FXML private Label statusLabel;

    private RawgApiService apiService = new RawgApiService();
    private Consumer<List<Item>> onImport;

    public void setOnImport(Consumer<List<Item>> onImport) {
        this.onImport = onImport;
    }

    @FXML
    private void onSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) return;

        statusLabel.setText("Recherche en cours...");
        resultsPane.getChildren().clear();
        searchButton.setDisable(true);

        new Thread(() -> {
            try {
                List<RawgApiService.GameResult> games = apiService.searchGames(query);

                Platform.runLater(() -> {
                    statusLabel.setText(games.size() + " résultats");
                    searchButton.setDisable(false);

                    for (RawgApiService.GameResult game : games) {
                        resultsPane.getChildren().add(buildGameCard(game));
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Erreur de connexion.");
                    searchButton.setDisable(false);
                });
            }
        }).start();
    }

    //Carte visuelle pour un jeu dans résultat
    private VBox buildGameCard(RawgApiService.GameResult game) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.CENTER);
        card.setPrefSize(130, 150);
        card.setStyle("-fx-background-color: #2a2a2a;" +
                        "-fx-border-color: #555555;" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 6;" +
                        "-fx-cursor: hand;"
        );

        //Image du jeu
        ImageView iv = new ImageView();
        iv.setFitWidth(118);
        iv.setFitHeight(90);
        iv.setPreserveRatio(true);

        if (game.imageUrl != null) {
            //Charger l'image en arrière-plan
            new Thread(() -> {
                try {
                    byte[] data = apiService.downloadImage(game.imageUrl);
                    Platform.runLater(() ->
                            iv.setImage(new Image(new ByteArrayInputStream(data)))
                    );
                } catch (Exception ignored) {
                }
            }).start();
        }

        Label nom = new Label(game.name);
        nom.setTextFill(javafx.scene.paint.Color.WHITE);
        nom.setStyle("-fx-font-size: 11px;");
        nom.setWrapText(true);
        nom.setMaxWidth(118);

        card.setOnMouseClicked(e -> importGame(game, card));

        card.getChildren().addAll(iv, nom);
        return card;
    }

    //Importer le jeu sélectionné
    private void importGame(RawgApiService.GameResult game, VBox card) {
        card.setStyle(card.getStyle() + "-fx-border-color: #3D81FF; -fx-border-width: 2;");

        new Thread(() -> {
            try {
                byte[] imageData = (game.imageUrl != null) ? apiService.downloadImage(game.imageUrl) : null;

                Item item = (imageData != null) ? new Item(game.name, imageData) : new Item(game.name);

                Platform.runLater(() -> {
                    if (onImport != null) onImport.accept(List.of(item));
                    statusLabel.setText("\"" + game.name + "\" ajouté !");
                    //Remettre le style normal après confirmation
                    card.setStyle(card.getStyle().replace("-fx-border-color: #3D81FF; -fx-border-width: 2;", ""));
                });

            } catch (Exception e) {
                Platform.runLater(() ->
                        statusLabel.setText("Erreur lors de l'import.")
                );
            }
        }).start();
    }
}