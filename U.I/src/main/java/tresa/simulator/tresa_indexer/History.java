package tresa.simulator.tresa_indexer;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class History extends Application {

    public static List<String> dataInput = new ArrayList<String>();
    public static ObservableList<String> fileData = FXCollections.observableArrayList(dataInput);

    TableView<HistoryDummy> table;


    @Override
    public void start(Stage stage) throws Exception {
        fileData.clear();
        HBox el = new HBox();


        readFile();



        TableColumn<HistoryDummy,String> column1 = new TableColumn<>("Query");
        column1.setMinWidth(300.00);
        column1.setCellValueFactory(new PropertyValueFactory<>("query"));
        TableColumn<HistoryDummy,String > column2 = new TableColumn<>("Results");
        column2.setMinWidth(300.00);
        column2.setCellValueFactory(new PropertyValueFactory<>("results"));
        TableColumn<HistoryDummy,String > column3 = new TableColumn<>("Date");
        column3.setMinWidth(300.00);
        column3.setCellValueFactory(new PropertyValueFactory<>("date"));


        table = new TableView<>();
        table.getColumns().add(column1);
        table.getColumns().add(column2);
        table.getColumns().add(column3);
        table.setStyle("-fx-alignment:CENTER;");
        column1.setStyle("-fx-alignment:CENTER;");
        column2.setStyle("-fx-alignment:CENTER;");
        column3.setStyle("-fx-alignment:CENTER;");


        table.setItems(getContent());
        el.getChildren().add(table);
        column1.prefWidthProperty().bind(el.widthProperty().divide(5));
        column2.prefWidthProperty().bind(el.widthProperty().divide(5));


        var scene = new Scene(el, 1024, 768);

        stage.setMinWidth(1024.00);
        stage.setMinHeight(768.00);
        stage.setMaxHeight(1080);
        stage.setMaxWidth(1920.00);
        stage.setScene(scene);
        stage.setTitle("History");

        stage.show();

        table.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.DELETE){
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Διαγραφή");
                alert.setContentText("Είσαστε σίγουροι;");
                alert.setHeaderText(null);
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK){

                    deleteElement(table.getSelectionModel().getSelectedItem().toString());

                    fileData.remove(table.getSelectionModel().getSelectedItem().toString());


                    table.setItems(getContent());
                }
            }
        });

        fileData.addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> change) {

                while (change.next()){
                    if (change.wasAdded()){


                        int ch = change.getFrom();

                    }
                }
            }
        });


    }

    public static void readFile()
    {
        File fl = new File("searches.txt");
        try {
            Scanner scanner = new Scanner(fl);
            while (scanner.hasNext()){
                String out = scanner.nextLine();

                fileData.add(out);

            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static ObservableList<HistoryDummy> getContent(){
        ObservableList<HistoryDummy> products = FXCollections.observableArrayList();
        for (String l : fileData){
            String input = l;
            String[] splitted = input.split("\t");

            products.add(new HistoryDummy(splitted[0],splitted[1],splitted[2]));



        }

        return products;


    }

    private void deleteElement(String label){
        String s = label;

        try {

            BufferedReader file = new BufferedReader(new FileReader("searches.txt"));
            StringBuffer inputBuffer = new StringBuffer();
            String line;

            while ((line = file.readLine()) != null) {
                if (line.trim().contains(s)){
                    line = "";
                    continue;
                }

                inputBuffer.append(line);
                inputBuffer.append('\n');
            }
            file.close();


            FileOutputStream fileOut = new FileOutputStream("searches.txt");
            fileOut.write(inputBuffer.toString().getBytes());
            fileOut.close();

        } catch (Exception e) {
            System.out.println("Problem reading file.");
        }


    }





}