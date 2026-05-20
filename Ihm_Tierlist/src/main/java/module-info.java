module org.example.ihm_tierlist {
    requires javafx.controls;
    requires javafx.fxml;

    opens tierlist to javafx.fxml, javafx.graphics;

    opens tierlist.controller to javafx.fxml;

    exports tierlist;
}