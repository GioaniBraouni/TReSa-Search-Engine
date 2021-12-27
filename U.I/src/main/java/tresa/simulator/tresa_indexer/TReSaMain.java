package tresa.simulator.tresa_indexer;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.MalformedURLException;
import java.net.Socket;
import java.util.*;

import static tresa.simulator.tresa_indexer.TReSaResults.obs;


/**
 * JavaFX App
 */
public class TReSaMain extends Application
{
    private GridPane main = new GridPane();
    private TextField searchBar = new TextField();
    private String text = "";
    private Stage stage;
    private boolean showStatus = false;

    @Override
    public void start(Stage stage) throws IOException
    {

        createLayout();
        //indexReuters();

        main.setStyle("-fx-background-color: white;");
        var scene = new Scene(main, 1200, 768);

        stage.setScene(scene);
        stage.setTitle("TReSA");
        stage.show();

    }

    private void createLayout()
    {
        final FileChooser fileChooser = new FileChooser();

        final DirectoryChooser directoryChooser = new DirectoryChooser();

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

        HBox search_options = new HBox(10);

        Button search = new Button("Search");
        search.setPadding(new Insets(10,30,10,30));
        search_options.getChildren().add(search);

        search.setOnAction(event ->
        {
            TReSaResults.start(searchBar.getText());
            try (Socket socket = new Socket("localhost",5555)){
                PrintWriter toServer = new PrintWriter(socket.getOutputStream(),true);
                Scanner fromClient = new Scanner(socket.getInputStream());
                toServer.println(searchBar.getText().toLowerCase(Locale.ROOT));

                HashMap<String,HashMap<String,Float>> received = new HashMap<>();


                InputStream inputStream = socket.getInputStream();
                ObjectInputStream obj = new ObjectInputStream(inputStream);
                List<Button> buttonList = new ArrayList<>();
                received =  (HashMap) obj.readObject();

                for (HashMap.Entry<String,HashMap<String ,Float>> entry : received.entrySet()){
                    String filename = entry.getKey();
                    for (HashMap.Entry<String,Float> next : entry.getValue().entrySet()){
                        String output = next.getKey();
                        Float score = next.getValue();
                        Button btn = new Button(filename);
                        buttonList.add(btn);
                        obs.add(new Articles(btn,output,score));
                    }
                }

                for (Button btn : buttonList){
                    btn.setOnMouseEntered(new EventHandler<MouseEvent>() {

                        @Override
                        public void handle(MouseEvent t) {
                            btn.setStyle("-fx-color:blue;");
                        }
                    });
                    btn.setOnMouseExited(new EventHandler<MouseEvent>() {

                        @Override
                        public void handle(MouseEvent t) {
                            btn.setStyle("-fx-color:white;");
                        }
                    });
                    btn.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent actionEvent) {
                            HostServices hostServices = getHostServices();
                            hostServices.showDocument("../Server_Parsing/Reuters/"+btn.getText());
                        }
                    });


                }



            }catch (IOException e){
                System.err.println("Server Down");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        });

        Button options = new Button("Options");
        options.setPadding(new Insets(10,30,10,30));
        search_options.getChildren().add(options);

        search_options.setAlignment(Pos.CENTER);

        VBox addButtons = new VBox();

        Button addFolder = new Button("Add Folder");
        addFolder.setPadding(new Insets(10));

        addFolder.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {

                File directory = directoryChooser.showDialog(stage);
                System.out.println(directory);

                        String url = directory.getAbsolutePath();
                        try (Socket socket = new Socket("localhost",5555);
                             PrintWriter toServer = new PrintWriter(socket.getOutputStream(),true);
                        ){

                            toServer.println("6^7"+url);

                        }catch (IOException e){
                            System.err.println("Server Down");
                        }

            }
        });

        Button addFile = new Button("Add File");
        addButtons.getChildren().addAll(addFolder,addFile);
        addFile.setPadding(new Insets(10,18,10,18));

        addFile.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                List<File> list = new LinkedList<>();
                list = fileChooser.showOpenMultipleDialog(stage);

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


        addButtons.setSpacing(15);

        VBox deleteButtons = new VBox();
        Button deleteFolder = new Button("Delete Folder");
        deleteFolder.setPadding(new Insets(10));

        deleteFolder.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                File directory = directoryChooser.showDialog(stage);
                System.out.println(directory);

                String url = directory.getAbsolutePath();
                try (Socket socket = new Socket("localhost",5555);
                     PrintWriter toServer = new PrintWriter(socket.getOutputStream(),true);
                ){

                    toServer.println("@-!"+url);

                }catch (IOException e){
                    System.err.println("Server Down");
                }

            }
        });

        Button deleteFile = new Button("Delete File");
        deleteButtons.getChildren().addAll(deleteFolder,deleteFile);
        deleteFile.setPadding(new Insets(10,18,10,18));

        deleteFile.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                List<File> list = fileChooser.showOpenMultipleDialog(stage);

                if (list != null){
                    for (File file : list){
                        String url = file.getAbsolutePath();
                        try (Socket socket = new Socket("localhost",5555);
                             PrintWriter toServer = new PrintWriter(socket.getOutputStream(),true);
                        ){

                            toServer.println("#()"+url);

                        }catch (IOException e){
                            System.err.println("Server Down");
                        }
                    }
                }

            }
        });

        deleteButtons.setSpacing(15);

        HBox add_delete = new HBox();
        add_delete.getChildren().addAll(addButtons,deleteButtons);
        add_delete.setPadding(new Insets(45,0,0,0));
        add_delete.setSpacing(10);

        add_delete.setVisible(showStatus);

        options.setOnAction(event ->
        {
            showStatus = !showStatus;
            add_delete.setVisible(showStatus);
        });

        add_delete.setAlignment(Pos.BOTTOM_CENTER);

        main.add(search_options,0,1);
        main.add(add_delete,0,2);
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