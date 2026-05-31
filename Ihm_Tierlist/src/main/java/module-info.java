module org.example.ihm_tierlist {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;
    requires java.net.http;

    opens tierlist to javafx.fxml;
    opens tierlist.controller to javafx.fxml;
    opens tierlist.model to javafx.base, javafx.fxml;

    exports tierlist;
}