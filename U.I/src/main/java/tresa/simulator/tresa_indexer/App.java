package tresa.simulator.tresa_indexer;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.text.DecimalFormat;


/**
 * JavaFX App
 */
public class App extends Application
{
    @Override
    public void start(Stage stage)
    {

        GridPane grid = new GridPane();

        Label Celsius = new Label("Κελσίου");
        Label Fahrernheit = new Label("Φαρενάιτ");
        Label ErrorLabel = new Label("Σφάλμα");

        ErrorLabel.setTextFill(Color.RED);
        ErrorLabel.setVisible(false);

        TextField txtCelsius = new TextField();
        TextField txtFahrernheit = new TextField();

        Button btnRight = new Button("-->");
        Button btnLeft = new Button("<--");

        Button clear = new Button("Clear");

        grid.add(Celsius,0,2,1,1);
        grid.add(txtCelsius,1,2,1,1);
        grid.add(Fahrernheit,4,2,1,1);
        grid.add(txtFahrernheit,3,2,1,1);
        grid.add(btnRight,2,1,2,2);
        grid.add(btnLeft,2,2,3,3);

        grid.add(ErrorLabel,2,4,1,3);

        grid.add(clear,2,4,1,8);


        txtCelsius.setPrefWidth(100);
        txtFahrernheit.setPrefWidth(100);
        clear.setPrefWidth(45);

        grid.setGridLinesVisible(false);

        var scene = new Scene(grid, 450, 170);
        grid.setVgap(15);
        grid.setHgap(15);

        //stage.getIcons().add(new Image("../logo.png"));

        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setAlignment(Pos.CENTER);
        stage.setScene(scene);
        stage.setTitle("Μετατροπές Θερμοκρασίας");
        stage.show();

        DecimalFormat df = new DecimalFormat("#");

        txtFahrernheit.setOnKeyReleased(event ->
        {
            if (event.getCode() == KeyCode.ENTER)
            {
                try
                {
                    double temperature,value;
                    temperature =Double.parseDouble(txtFahrernheit.getText());
                    value = 5.0/9.0 * (temperature - 32);
                    txtCelsius.setText(df.format(value));
                }
                catch(NumberFormatException e)
                {
                    ErrorLabel.setVisible(true);
                }
            }
        });

        btnLeft.setOnAction(event ->
        {
            try
            {

                int temperature;
                double value;
                temperature = Integer.parseInt(txtFahrernheit.getText());
                value = (5.0 / 9.0  * (temperature - 32));
                txtCelsius.setText(df.format(value));



            }
            catch(NumberFormatException e)
            {
                ErrorLabel.setVisible(true);
            }
        });

        txtCelsius.setOnKeyReleased(event ->
        {
            if (event.getCode() == KeyCode.ENTER)
            {
                try
                {
                    int temperature;
                    temperature =Integer.parseInt(txtCelsius.getText());
                    txtFahrernheit.setText(Integer.toString(temperature*9/5 + 32));
                }
                catch(NumberFormatException e)
                {
                    ErrorLabel.setVisible(true);
                }
            }
        });


        btnRight.setOnAction(event ->
        {
            try
            {
                int temperature;
                double value;
                temperature =Integer.parseInt(txtCelsius.getText());
                value = 9.0/5.0 * (temperature + 32);
                //txtFahrernheit.setText(String.valueOf(value));
                txtFahrernheit.setText(Integer.toString(temperature*9/5 + 32));
            }
            catch(NumberFormatException e)
            {
                ErrorLabel.setVisible(true);
            }
        });

        clear.setOnAction(event ->
        {
            txtFahrernheit.clear();
            txtCelsius.clear();
            if(ErrorLabel.isVisible())
                ErrorLabel.setVisible(false);

        });

    }

    public static void main(String[] args) {
        launch(args);
    }

}