package tresa.simulator.tresa_indexer;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;


/**
 * JavaFX App
 */
public class App extends Application
{
    @Override
    public void start(Stage stage) throws FileNotFoundException, MalformedURLException {

        BorderPane borderPane = new BorderPane();

        VBox box = new VBox(20);


        Image image = new Image(new File("/home/dimitris/IdeaProjects/Testing_Project/U.I/src/main/java/tresa/simulator/tresa_indexer/images/not_google2.png").toURI().toURL().toExternalForm());

        ImageView imageView = new ImageView(image);


        TextField searchBar = new TextField();

        searchBar.setMaxWidth(300);

        box.getChildren().addAll((imageView),searchBar);

        box.setAlignment(Pos.CENTER);

        borderPane.setCenter(box);






        var scene = new Scene(borderPane, 800, 800);
        stage.setScene(scene);
        stage.setTitle("Not Google");
        stage.show();







    }

    public static void main(String[] args) {
        launch(args);
    }

}