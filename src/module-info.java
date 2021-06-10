module LocalImageDatabase {
    requires javafx.fxml;
    requires javafx.controls;
    requires java.sql;
    requires java.desktop;
    requires javafx.swing;

    opens sample;
    opens sample.model;
}