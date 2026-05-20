# Architecture – SAE 2.01 : Gestion de Tier-list
> Application bureau Java + JavaFX, pattern **MVC** (Model – View – Controller)

---

## Structure des packages

```
src/
├── main/
│   ├── java/
│   │   └── fr/iut/tierlist/
│   │       ├── Main.java
│   │       ├── model/
│   │       │   ├── TierList.java
│   │       │   ├── Tier.java
│   │       │   ├── Item.java
│   │       │   ├── ItemTexte.java
│   │       │   ├── ItemImage.java
│   │       │   └── ThemeVisuel.java
│   │       ├── controller/
│   │       │   ├── MainController.java
│   │       │   ├── TierListController.java
│   │       │   ├── TierController.java
│   │       │   ├── ItemController.java
│   │       │   └── ApiController.java
│   │       ├── view/
│   │       │   ├── MainView.fxml
│   │       │   ├── TierListView.fxml
│   │       │   ├── TierView.fxml
│   │       │   └── ItemView.fxml
│   │       ├── service/
│   │       │   ├── PersistanceService.java
│   │       │   ├── ApiService.java
│   │       │   └── ExportService.java
│   │       └── util/
│   │           ├── DragDropHelper.java
│   │           └── ThemeManager.java
│   └── resources/
│       ├── fxml/
│       ├── css/
│       │   ├── theme-clair.css
│       │   └── theme-sombre.css
│       └── images/
└── test/
    └── java/
        └── fr/iut/tierlist/
            ├── TierListTest.java
            ├── TierTest.java
            └── ItemTest.java
```

---

## Couche Modèle (`model/`)

### `TierList.java`
Classe principale représentant une tier-list complète.

**Attributs :**
| Attribut | Type | Description |
|---|---|---|
| `id` | `UUID` | Identifiant unique de la tier-list |
| `nom` | `String` | Nom donné par l'utilisateur |
| `tiers` | `List<Tier>` | Liste ordonnée des tiers (S, A, B, C…) |
| `itemsNonClasses` | `List<Item>` | Zone des items non encore classés |
| `theme` | `ThemeVisuel` | Thème visuel actif |

**Méthodes clés :**
- `ajouterTier(Tier t)` – ajoute un nouveau tier
- `supprimerTier(Tier t)` – supprime un tier et remet ses items dans la zone non classée
- `reinitialiser()` – remet tous les items de tous les tiers dans `itemsNonClasses`
- `dupliquer()` → `TierList` – retourne une copie profonde de la tier-list
- `deplacerTier(int indexSource, int indexCible)` – réordonne les tiers

**Interfaces :** `Serializable` (pour la persistance binaire)

---

### `Tier.java`
Représente une ligne (catégorie) dans la tier-list.

**Attributs :**
| Attribut | Type | Description |
|---|---|---|
| `id` | `UUID` | Identifiant unique |
| `label` | `String` | Nom du tier (ex : "S", "A", "Bon"…) |
| `couleur` | `String` | Couleur de fond en hexadécimal |
| `hauteur` | `int` | Hauteur en pixels |
| `items` | `List<Item>` | Items classés dans ce tier |

**Méthodes clés :**
- `ajouterItem(Item item)` – ajoute un item dans ce tier
- `supprimerItem(Item item)` – retire un item du tier
- `deplacerItem(int indexSource, int indexCible)` – réordonne les items par drag & drop
- `vider()` → `List<Item>` – retire tous les items et les retourne

**Interfaces :** `Serializable`

---

### `Item.java` *(classe abstraite)*
Classe parente commune aux deux types d'items.

**Attributs :**
| Attribut | Type | Description |
|---|---|---|
| `id` | `UUID` | Identifiant unique |
| `taille` | `int` | Taille d'affichage en pixels |

**Méthodes abstraites :**
- `getAffichage()` → `Node` – retourne le composant JavaFX à afficher
- `toString()` → `String`

**Interfaces :** `Serializable`

---

### `ItemTexte.java` *(hérite de Item)*
Item représenté par un simple texte.

**Attributs supplémentaires :**
| Attribut | Type | Description |
|---|---|---|
| `texte` | `String` | Contenu textuel de l'item |
| `stylePolicé` | `String` | Police et style CSS optionnel |

**Méthodes :**
- `getAffichage()` → `Label` JavaFX contenant le texte
- `modifier(String nouveauTexte)` – met à jour le contenu

---

### `ItemImage.java` *(hérite de Item)*
Item représenté par une image.

**Attributs supplémentaires :**
| Attribut | Type | Description |
|---|---|---|
| `imageData` | `byte[]` | Données binaires de l'image (pour sérialisation) |
| `cheminLocal` | `String` | Chemin d'accès au fichier local (optionnel) |
| `sourceUrl` | `String` | URL source si importée via API |

**Méthodes :**
- `getAffichage()` → `ImageView` JavaFX
- `chargerDepuisFichier(File f)` – charge l'image depuis un fichier local
- `chargerDepuisUrl(String url)` – charge l'image depuis une URL (API)

---

### `ThemeVisuel.java`
Enumération ou classe représentant le thème de l'interface.

```java
public enum ThemeVisuel {
    CLAIR("theme-clair.css"),
    SOMBRE("theme-sombre.css");

    private final String fichierCss;
    // constructeur + getter
}
```

---

## Couche Contrôleur (`controller/`)

### `MainController.java`
Contrôleur principal, point d'entrée de l'interface.

**Responsabilités :**
- Gérer la liste des tier-lists disponibles (créer, dupliquer, supprimer)
- Naviguer vers une tier-list sélectionnée
- Gérer le changement de thème visuel global
- Orchestrer les appels à `PersistanceService` (sauvegarde/chargement)

**Méthodes principales :**
- `creerNouvelleTierList()` – crée une TierList vide et l'ajoute à la liste
- `dupliquerTierList(TierList tl)` – duplique une tier-list existante
- `supprimerTierList(TierList tl)` – supprime une tier-list après confirmation
- `ouvrirTierList(TierList tl)` – charge la vue d'édition de la tier-list
- `changerTheme(ThemeVisuel theme)` – applique le CSS correspondant

---

### `TierListController.java`
Contrôleur de la vue d'édition d'une tier-list.

**Responsabilités :**
- Afficher et mettre à jour les tiers et la zone "à classer"
- Gérer le drag & drop des items entre les tiers
- Déclencher la réinitialisation de la tier-list

**Méthodes principales :**
- `ajouterTier()` – ajoute un tier par défaut en bas de liste
- `supprimerTier(Tier t)` – supprime un tier (avec confirmation)
- `reinitialiserTierList()` – remet tous les items dans la zone non classée
- `onItemDragged(Item item, Tier cible)` – gère le dépôt d'un item dans un tier
- `importerImage()` – ouvre un `FileChooser` pour importer une image locale

---

### `TierController.java`
Contrôleur d'un tier individuel (une ligne).

**Responsabilités :**
- Permettre le renommage du tier (double-clic sur le label)
- Permettre la modification de la couleur et de la hauteur
- Gérer le réordonnancement du tier (monter / descendre)

**Méthodes principales :**
- `renommer(String nouveauLabel)` – modifie le label du tier
- `modifierCouleur(String hexColor)` – change la couleur de fond
- `modifierHauteur(int hauteur)` – redimensionne le tier
- `monterTier()` / `descendreTier()` – déplace le tier d'une position

---

### `ItemController.java`
Contrôleur pour la création et modification d'un item.

**Responsabilités :**
- Créer un `ItemTexte` ou un `ItemImage`
- Modifier ou supprimer un item existant
- Ajuster la taille d'affichage d'un item

**Méthodes principales :**
- `creerItemTexte(String texte)` → `ItemTexte`
- `creerItemImage(File fichier)` → `ItemImage`
- `modifierItem(Item item)` – ouvre un formulaire de modification
- `supprimerItem(Item item)` – retire l'item de son tier ou de la zone non classée
- `ajusterTaille(Item item, int taille)` – modifie la taille d'affichage

---

### `ApiController.java`
Contrôleur pour l'import d'images via une API externe (RAWG, TMDB…).

**Responsabilités :**
- Effectuer des recherches via l'API choisie
- Afficher les résultats sous forme de grille
- Créer un `ItemImage` à partir d'un résultat sélectionné

**Méthodes principales :**
- `rechercherImages(String motCle)` – lance une requête HTTP vers l'API
- `afficherResultats(List<ItemImage> resultats)` – met à jour la vue
- `importerImage(ItemImage image)` – ajoute l'image sélectionnée dans la zone non classée

---

## Couche Service (`service/`)

### `PersistanceService.java`
Gestion de la sauvegarde et du chargement des données.

**Méthodes :**
- `sauvegarder(List<TierList> liste, String cheminFichier)` – sérialise la liste via `ObjectOutputStream`
- `charger(String cheminFichier)` → `List<TierList>` – désérialise via `ObjectInputStream`
- `exporter(TierList tl, File fichier)` – exporte une seule tier-list
- `importer(File fichier)` → `TierList` – importe une tier-list depuis un fichier

---

### `ApiService.java`
Couche d'accès à l'API externe (découplée du contrôleur).

**Méthodes :**
- `rechercher(String motCle)` → `List<ResultatApi>` – effectue la requête HTTP et parse le JSON (Jackson)
- `telechargerImage(String url)` → `byte[]` – télécharge les données binaires d'une image

> Utilise la librairie **Jackson** pour le parsing JSON.

---

### `ExportService.java`
*(Optionnel)* Export de la tier-list sous forme d'image ou de fichier partageable.

**Méthodes :**
- `exporterEnImage(TierList tl, File destination)` – capture la scène JavaFX et l'enregistre en PNG

---

## Utilitaires (`util/`)

### `DragDropHelper.java`
Centralise la logique de drag & drop pour éviter la duplication dans les contrôleurs.

**Méthodes statiques :**
- `configurerSource(Node node, Item item)` – configure un nœud JavaFX comme source de drag
- `configurerCible(Node node, Tier cible, TierListController ctrl)` – configure un nœud comme zone de dépôt

---

### `ThemeManager.java`
Applique et mémorise le thème visuel sur la scène principale.

**Méthodes statiques :**
- `appliquerTheme(Scene scene, ThemeVisuel theme)` – charge le CSS et l'applique
- `getThemeActuel()` → `ThemeVisuel`

---

## Point d'entrée

### `Main.java`
Lance l'application JavaFX.

```java
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
        Scene scene = new Scene(loader.load());
        ThemeManager.appliquerTheme(scene, ThemeVisuel.CLAIR);
        primaryStage.setTitle("Tier-List Manager");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
```

---

## Diagramme de dépendances simplifié

```
Main
 └─► MainController
       ├─► TierListController ──► TierList (Model)
       │     ├─► TierController  ──► Tier (Model)
       │     └─► ItemController  ──► Item / ItemTexte / ItemImage (Model)
       ├─► ApiController ──► ApiService
       └─► PersistanceService
            └─► sérialise TierList → Tier → Item
```

---

## Récapitulatif des dépendances externes

| Librairie | Rôle | Dépendance Maven/Gradle |
|---|---|---|
| JavaFX | Interface graphique | `org.openjfx:javafx-controls` |
| Jackson | Parsing JSON (API) | `com.fasterxml.jackson.core:jackson-databind` |
| JUnit 5 | Tests unitaires | `org.junit.jupiter:junit-jupiter` |

---

## Exigences rappelées

- **Persistance** : sérialisation binaire Java (`ObjectOutputStream`) → toutes les classes du modèle implémentent `Serializable`
- **Robustesse** : support d'au moins 40 items images par tier-list
- **Performance** : drag & drop fluide (éviter les rechargements complets de vue)
- **Ergonomie** : thèmes clair/sombre, responsive sur différentes résolutions
