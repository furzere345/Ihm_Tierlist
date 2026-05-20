module org.example.ihm_tierlist {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.ihm_tierlist to javafx.fxml;
    exports org.example.ihm_tierlist;
}