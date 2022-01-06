package tresa.simulator.tresa_indexer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import static javafx.collections.FXCollections.observableArrayList;

public class TReSaArticleCompareResults {

    private static TextField searchBar = new TextField();

    public static ObservableList<Articles> articleOBS = observableArrayList();

    public static void start(String text)
    {
        GridPane pane = new GridPane();
        pane.setStyle("-fx-background-color: white;");
        Stage primaryStage = new Stage();
        primaryStage.setTitle("TReSA Results");
        primaryStage.initModality(Modality.APPLICATION_MODAL);
        HBox searchBox = new HBox(15);
        ImageView image = new ImageView(new Image(("file:src/TRESA.png"),150,0,true,false));
        searchBox.getChildren().add(image);
        searchBar.setText(text);
        searchBar.setPrefWidth(450);
        searchBar.setFocusTraversable(false);

        searchBar.setPadding(new Insets(10, 10, 10, 10));
        searchBox.getChildren().add(searchBar);
        searchBox.setPadding(new Insets(10));

        pane.getChildren().add(searchBox);

        VBox results = new VBox();

        TableView articleElements = new TableView<>();

        TableColumn<Articles,String> column1 = new TableColumn<>("File Name");
        TableColumn<Articles,String> column2 = new TableColumn<>("Score");
        column1.setMinWidth(500.00);
        column1.setCellValueFactory(new PropertyValueFactory<>("title"));
        column2.setCellValueFactory(new PropertyValueFactory<>("score"));
        column2.setMinWidth(500.00);


        column2.setSortType(TableColumn.SortType.DESCENDING);
        articleElements.getColumns().addAll(column1,column2);

        articleElements.setItems(articleOBS);

        articleElements.getSortOrder().add(column2);
        column2.setSortable(true);
        articleElements.sort();

        articleElements.setMinWidth(1200);
        results.getChildren().add(articleElements);
        articleElements.setStyle("-fx-alignment:CENTER;");

        results.minHeight(1000);
        //MINE


        results.setPadding(new Insets( 50));
        results.setAlignment(Pos.TOP_LEFT);

        pane.getChildren().add(results);


        pane.setAlignment(Pos.TOP_LEFT);



        primaryStage.setScene(new Scene(pane, 1200, 768));
        primaryStage.show();



        primaryStage.setOnCloseRequest(event -> {
            articleOBS.clear();

        });


    }
}