package tierlist.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class RawgApiService {

    private static final String BASE_URL = "https://api.rawg.io/api";
    private static final String API_KEY = "CLE_API";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    //Resultat d'une recherche
    public static class GameResult {
        public String name;
        public String imageUrl;

        public GameResult(String name, String imageUrl) {
            this.name = name;
            this.imageUrl = imageUrl;
        }
    }

    //Rechercher des jeux par nom
    public List<GameResult> searchGames(String query) throws IOException, InterruptedException {
        String url = BASE_URL + "/games?key=" + API_KEY
                + "&search=" + java.net.URLEncoder.encode(query, "UTF-8")
                + "&page_size=20";

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        List<GameResult> results = new ArrayList<>();
        JsonNode root = objectMapper.readTree(response.body());
        JsonNode games = root.get("results");

        if (games != null && games.isArray()) {
            for (JsonNode game : games) {
                String name = game.get("name").asText();
                JsonNode imgNode = game.get("background_image");
                String imageUrl = (imgNode != null && !imgNode.isNull()) ? imgNode.asText() : null;
                results.add(new GameResult(name, imageUrl));
            }
        }

        return results;
    }

    //Telecharger une image depuis une URL
    public byte[] downloadImage(String imageUrl) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(imageUrl)).GET().build();

        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

        return response.body();
    }
}