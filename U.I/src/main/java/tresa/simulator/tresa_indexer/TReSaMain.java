package tresa.simulator.tresa_indexer;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;
import java.net.MalformedURLException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static tresa.simulator.tresa_indexer.TReSaArticleCompareResults.articleOBS;
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
        var scene = new Scene(main, 1200, 720);

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
//            TReSaResults.start(searchBar.getText());
            try (Socket socket = new Socket("localhost",5555)){
                PrintWriter toServer = new PrintWriter(socket.getOutputStream(),true);
                Scanner fromClient = new Scanner(socket.getInputStream());

                toServer.println(searchBar.getText().toLowerCase(Locale.ROOT));


                HashMap<String,HashMap<String,Float>> received = new HashMap<>();


                InputStream inputStream = socket.getInputStream();
                ObjectInputStream obj = new ObjectInputStream(inputStream);
                List<Button> buttonList = new ArrayList<>();
                received =  (HashMap) obj.readObject();
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                LocalDateTime now = LocalDateTime.now();

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

                PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter("searches.txt",true)));
                if (received.isEmpty()){
                    writer.println(searchBar.getText()  + "\t No results returned" + "\t" + dtf.format(now));
                }else{
                    writer.println(searchBar.getText() + "\t" + received.size() + " results where found" + "\t" + dtf.format(now));
                }

                writer.close();

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
                alertError(stage,"Server Down");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            TReSaResults.start(searchBar.getText());
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

                if(directory!=null)
                {
                    String url = directory.getAbsolutePath();
                    try (Socket socket = new Socket("localhost", 5555);
                         PrintWriter toServer = new PrintWriter(socket.getOutputStream(), true);
                         InputStream fromClient = socket.getInputStream();
                    ) {

                        toServer.println("6^7" + url);

                        ObjectInputStream in = new ObjectInputStream(fromClient);
                        String response = (String) in.readObject();

                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, response, ButtonType.YES);
                        alert.showAndWait();
                        if (alert.getResult() == ButtonType.YES) {
                            alert.close();
                        }

                    } catch (IOException | NullPointerException | ClassNotFoundException e) {
                        alertError(stage, "Connection refused");
                    }
                }
                else
                    alertError(stage, "Folder not found");
            }
        });

        Button addFile = new Button("Add File/Files");
        addButtons.getChildren().addAll(addFolder,addFile);
        addFile.setPadding(new Insets(10,5,10,6));

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
                             InputStream fromClient =  socket.getInputStream();
                        ){

                            toServer.println("@@@"+url);

                            ObjectInputStream in = new ObjectInputStream(fromClient);
                            String response = (String) in.readObject();

                            Alert alert = new Alert(Alert.AlertType.CONFIRMATION,response, ButtonType.YES);
                            alert.showAndWait();
                            if (alert.getResult()==ButtonType.YES)
                                alert.close();

                        }catch (IOException | NullPointerException e){
                            alertError(stage,"Connection refused");
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
                else
                    alertError(stage,"File/Files not found");
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

            if(directory!=null) {
                String url = directory.getAbsolutePath();
                try (Socket socket = new Socket("localhost", 5555);
                     PrintWriter toServer = new PrintWriter(socket.getOutputStream(), true);
                     InputStream fromClient = socket.getInputStream();
                ) {

                    toServer.println("@-!" + url);

                    ObjectInputStream in = new ObjectInputStream(fromClient);
                    String response = (String) in.readObject();

                    if (response.contains("true")) {
                        String output = directory.getAbsolutePath().toString() + " has been deleted from index";
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, output, ButtonType.YES);
                        alert.showAndWait();
                        if (alert.getResult() == ButtonType.YES) {
                            alert.close();
                        }
                    } else {
                        alertError(stage, ("Directory " + directory.getAbsolutePath() + " contains some files that do not exist in the index"));
                    }


                } catch (IOException | NullPointerException | ClassNotFoundException e) {
                    alertError(stage,"Connection refused");
                }
            }
            else
                alertError(stage,"Directory not found not found");
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
                             InputStream fromClient = socket.getInputStream();
                        ){

                            toServer.println("#()"+url);
                            ObjectInputStream in = new ObjectInputStream(fromClient);
                            String response = (String) in.readObject();

                            if (response.contains("true")) {
                                String output = file.getName() + " has been deleted from index";
                                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, output, ButtonType.YES);
                                alert.showAndWait();
                                if (alert.getResult() == ButtonType.YES) {
                                    alert.close();
                                }
                            } else {
                                alertError(stage, ("File " + file.getName() + " does not exist in the index"));
                            }

                        }catch (IOException | NullPointerException | ClassNotFoundException e){
                            alertError(stage,"Connection refused");
                        }
                    }
                }

            }
        });

        Button history = new Button("History");
        addButtons.getChildren().addAll(history);
        history.setPadding(new Insets(10,18,10,18));

        history.setStyle("-fx-pref-width:90");

        history.setOnAction(e -> {
            try {
                new History().start(new Stage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        Button articleCompare = new Button("Compare Article");
        deleteButtons.getChildren().addAll(articleCompare);
        articleCompare.setPadding(new Insets(10,18,10,18));

        articleCompare.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {

                File selected;
                selected = fileChooser.showOpenDialog(stage);

                TextInputDialog textInputDialog = new TextInputDialog("0");
                textInputDialog.setTitle("Select top articles");
                textInputDialog.setContentText("Please select a number");
                int top = 0;
//                textInputDialog.showAndWait();
                Optional<String> userInput = textInputDialog.showAndWait();
                if (userInput.isPresent()){
                    top = Integer.parseInt(textInputDialog.getEditor().getText());
                    textInputDialog.close();

                }else {
                    textInputDialog.close();
                }


                try (Socket socket = new Socket("localhost",5555)){
                    PrintWriter toServer = new PrintWriter(socket.getOutputStream(),true);
                    Scanner fromClient = new Scanner(socket.getInputStream());

                    toServer.println("*&&"+selected.getCanonicalPath() + " " + top);


                    HashMap<String ,Float> results = new HashMap<>();


                    InputStream inputStream = socket.getInputStream();
                    ObjectInputStream obj = new ObjectInputStream(inputStream);
                    List<Button> buttonList = new ArrayList<>();
                    results =  (HashMap) obj.readObject();

                    for (HashMap.Entry<String,Float> entry : results.entrySet()){
                        Button btn = new Button(entry.getKey());
                        buttonList.add(btn);
                        articleOBS.add(new Articles(btn,entry.getValue()));
                    }


                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                    LocalDateTime now = LocalDateTime.now();


                    PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter("searches.txt",true)));
                    if (results.isEmpty()){
                        writer.println(selected  + "\t returned results returned" + "\t" + dtf.format(now));
                    }else{
                        writer.println(selected + "\t" + results.size() + " similar documents found" + "\t" + dtf.format(now));
                    }

                    writer.close();

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
                    alertError(stage,"Server Down");
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

                TReSaArticleCompareResults.start(selected.toString()); //TODO another class
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

    private void alertError(Stage stage, String errorString)
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(errorString);
        alert.setHeaderText(null);
        alert.initOwner(stage);
        alert.initModality(Modality.WINDOW_MODAL);
        alert.show();
    }

    private void alertInfo(Stage stage, String infoString)
    {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setContentText(infoString);
        alert.setHeaderText(null);
        alert.initOwner(stage);
        alert.initModality(Modality.WINDOW_MODAL);
        alert.show();
    }

    public static void main(String[] args) {
        launch();
    }


}