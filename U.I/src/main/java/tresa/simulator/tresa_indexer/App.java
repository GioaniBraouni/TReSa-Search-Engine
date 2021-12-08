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
{private GridPane main = new GridPane();
    private TextField searchBar = new TextField();
    private String text = "";
    private Stage stage;

    @Override
    public void start(Stage stage) throws IOException
    {

        createLayout();

        main.setStyle("-fx-background-color: white;");
        var scene = new Scene(main, 1200, 768);

        stage.setScene(scene);
        stage.setTitle("TReSA");
        stage.show();

    }

    private void createLayout()
    {
        VBox searchBox = new VBox(15);
        ImageView image = new ImageView(new Image(("file:src/TRESA.png"),350,0,true,false));
        searchBox.getChildren().add(image);
        searchBox.setAlignment(Pos.TOP_CENTER);

        searchBar.setPrefWidth(450);
        searchBar.setFocusTraversable(false);
        searchBar.setPromptText("Αναζήτηση στο TReSA");
        searchBar.setPadding(new Insets(10, 10, 10, 10));
        searchBox.getChildren().add(searchBar);
        main.add(searchBox, 0, 0);

        HBox buttons = new HBox(10);

        Button search = new Button("Search");
        search.setPadding(new Insets(10,30,10,30));
        buttons.getChildren().add(search);

        Button options = new Button("Options");
        options.setPadding(new Insets(10,30,10,30));
        buttons.getChildren().add(options);

        buttons.setAlignment(Pos.CENTER);

        main.add(buttons,0,1);
        main.setVgap(10);
        main.setAlignment(Pos.CENTER);
    }

    private void Options()
    {
        final FileChooser fileChooser = new FileChooser();

        final DirectoryChooser directoryChooser = new DirectoryChooser();


        fileChooser.getExtensionFilters().add( // Only txt documents
                new FileChooser.ExtensionFilter("Text Files","*.txt")
        );


        BorderPane borderPane = new BorderPane();

        Button addFiles = new Button("Add File");

        Button addDirectory = new Button("Add Folder");

        HBox forButtons = new HBox();

        forButtons.getChildren().add(addFiles);

        forButtons.getChildren().add(addDirectory);

        VBox box = new VBox(20);

        Image image = null;
        try {
            image = new Image(new File("/home/dimitris/IdeaProjects/Testing_Project/U.I/src/main/java/tresa/simulator/tresa_indexer/images/not_google2.png").toURI().toURL().toExternalForm());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        ImageView imageView = new ImageView(image);

        TextField searchBar = new TextField();

        searchBar.setMaxWidth(300);

        box.getChildren().addAll((imageView),searchBar);

        box.setAlignment(Pos.TOP_CENTER);

        borderPane.setCenter(box);

        forButtons.setAlignment(Pos.TOP_LEFT);

        borderPane.setTop(forButtons);

        addFiles.setOnAction(new EventHandler<ActionEvent>() { // Gets path of Files
            @Override
            public void handle(ActionEvent actionEvent) {

                List<File> list = fileChooser.showOpenMultipleDialog(stage);

                if (list != null){
                    for (File file : list){
                        String url = file.getAbsolutePath();
                        try (Socket socket = new Socket("localhost",5555);
                             PrintWriter toServer = new PrintWriter(socket.getOutputStream(),true);
                        ){

                            toServer.println("@@@"+url);

                        }catch (IOException e){
                            System.err.println("Server Down");
                        }
                    }
                }

            }
        });


        addDirectory.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                File fileDir = directoryChooser.showDialog(stage);

                if (fileDir != null){
                    File[] fileList = fileDir.listFiles();
//                    for(File file:fileList) {
//                        if (!file.toString().contains(".txt")) {
//                            Alert alert = new Alert(Alert.AlertType.ERROR, "Only txt file " +
//                                    "extension is valid!", ButtonType.CLOSE);
//                            alert.showAndWait();
//                            fileList = null;
//                            break;
//                        }
//                    }
                    try (Socket socket = new Socket("localhost",5555);
                         PrintWriter toServer = new PrintWriter(socket.getOutputStream(),true);
                    ){

                        toServer.println("!@#" +fileDir.getAbsolutePath());

                    }catch (IOException e){
                        System.err.println("Server Down");
                    }

                }
            }
        });
    }

    public static void main(String[] args) {
        launch();
    }
}