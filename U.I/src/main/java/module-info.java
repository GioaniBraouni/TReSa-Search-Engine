module tresa.simulator.tresa_indexer {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;


    opens tresa.simulator.tresa_indexer to javafx.fxml;
    exports tresa.simulator.tresa_indexer;
}