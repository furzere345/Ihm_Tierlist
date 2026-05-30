module org.example.ihm_tierlist {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;

    opens tierlist.view to javafx.graphics, javafx.fxml;

    opens tierlist.controller to javafx.fxml;
    opens tierlist.model to javafx.base;

    exports tierlist;
}