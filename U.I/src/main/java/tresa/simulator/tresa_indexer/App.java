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
    {


        @Override
        public void start(Stage stage) throws FileNotFoundException, MalformedURLException {

        final FileChooser fileChooser = new FileChooser();

        fileChooser.getExtensionFilters().add( // Only txt documents
                new FileChooser.ExtensionFilter("Text Files","*.txt")
        );

        BorderPane borderPane = new BorderPane();

        Button viewFiles = new Button("File");

        HBox forButtons = new HBox();

        forButtons.getChildren().add(viewFiles);

        VBox box = new VBox(20);

        Image image = new Image(new File("/home/dimitris/IdeaProjects/Testing_Project/U.I/src/main/java/tresa/simulator/tresa_indexer/images/not_google2.png").toURI().toURL().toExternalForm());

        ImageView imageView = new ImageView(image);

        TextField searchBar = new TextField();

        searchBar.setMaxWidth(300);

        box.getChildren().addAll((imageView),searchBar);

        box.setAlignment(Pos.TOP_CENTER);

        borderPane.setCenter(box);

        forButtons.setAlignment(Pos.TOP_LEFT);

        borderPane.setTop(forButtons);






        var scene = new Scene(borderPane, 800, 800);
        stage.setScene(scene);
        stage.setTitle("Not Google");
        stage.show();


        viewFiles.setOnAction(new EventHandler<ActionEvent>() { // Gets path of Files
            @Override
            public void handle(ActionEvent actionEvent) {

                List<File> list = fileChooser.showOpenMultipleDialog(stage);
                if (list != null){
                    for (File file : list){
                        String url = file.getAbsolutePath();
                        try (Socket socket = new Socket("localhost",5555);
                             PrintWriter toServer = new PrintWriter(socket.getOutputStream(),true);
                        ){

                            toServer.println(url);

                        }catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                }

            }
        });







    }

        public static void main(String[] args) {
        launch(args);
    }

}