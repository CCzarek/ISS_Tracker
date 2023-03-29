package projekt.iad.ISSTracker.tutorial;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.FileNotFoundException;

public class AppTutorial extends Application{

    Stage window;
    Button buttonNo;
    Button buttonNext;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        window = primaryStage;
        window.setTitle("Informacje wstępne");
        window.setMinWidth(400);
        window.setMinHeight(400);

        Label label = new Label();

        VBox layout = new VBox(30);
        buttonNo = new Button("Poradzę sobie");
        buttonNo.setOnAction(e -> window.close());
        buttonNo.setDefaultButton(true);
        buttonNext = new Button("Tak, z chęcią!");
        buttonNext.setOnAction(e -> {
            try {
                AlertBox.display("Tutorial", "Czy wszystko jasne kamracie?");
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
        });

        // Graphics
        Image image = new Image("ISS-tracker.png");
        ImageView imageView = new ImageView();
        imageView.setImage(image);
        imageView.setX(10);
        imageView.setY(10);
        imageView.setFitHeight(400);
        imageView.setPreserveRatio(true);

        Image icon = new Image("ISS_icon.png");
        window.getIcons().add(icon);

        Group root = new Group(imageView);
        layout.getChildren().addAll(root, label, buttonNext, buttonNo);
        layout.setAlignment(Pos.CENTER);
        layout.setSpacing(3);
        layout.setMinHeight(1000);

        Scene scene = new Scene(layout);
        window.setScene(scene);
        window.show();
    }
}
