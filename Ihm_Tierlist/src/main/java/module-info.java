module org.example.ihm_tierlist {
    requires javafx.controls;
    requires javafx.fxml;

    opens tierlist.view to javafx.graphics, javafx.fxml;
    exports tierlist;
}