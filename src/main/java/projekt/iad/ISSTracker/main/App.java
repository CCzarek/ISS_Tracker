package projekt.iad.ISSTracker.main;

import com.dlsc.gemsfx.TimePicker;
import com.dlsc.pickerfx.LocalDateTimePicker;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.view.MapView;

import com.esri.arcgisruntime.symbology.Symbol;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import projekt.iad.ISSTracker.data.DataCSVReader;
import projekt.iad.ISSTracker.data.DataManagerDates;
import projekt.iad.ISSTracker.data.DataManagerNow;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;

import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import projekt.iad.ISSTracker.enums.Mode;
import projekt.iad.ISSTracker.tutorial.AlertBox;
import javafx.scene.control.TextField;
import projekt.iad.ISSTracker.tutorial.StartBox;

import javax.script.Bindings;
import java.awt.*;
import java.io.FileNotFoundException;
import java.util.concurrent.TimeUnit;


public class App extends Application {

    private MapView mapView;
    private Graphic graphic;
    private Point point;

    private final DataManagerNow managerNow = new DataManagerNow(3600*24);
    private final DataManagerDates managerDates = new DataManagerDates(3600*24);
    private final DataCSVReader dataCSVReader = new DataCSVReader();


    private DatePicker datePicker;
    private TimePicker timePicker;

    private Button buttonInfo;
    private Button buttonDate;
    private Button buttonNow;
    private Button buttonSave;
    private Button buttonDownload;

    private Slider slider;

    private Text params;
    private Text downloadFlag;
    private Text modeFlag;

    private Mode mode = Mode.Now;
    private int speed;
    private int i;
    private long time;





    public static void main(String[] args) {
       Application.launch(args);
    }


    @Override
    public void start(Stage stage) throws FileNotFoundException {
        managerNow.start();
        managerDates.turnOff();
        managerDates.start();


        stage.setTitle("ISS position");
        stage.setWidth(1100);
        stage.setHeight(850);
        stage.show();
        stage.getIcons().add(new Image("ISS_icon.png"));
        StackPane stackPane = new StackPane();
        Scene scene = new Scene(stackPane);
        stage.setScene(scene);

        String yourApiKey = new String(Files.readAllBytes(Paths.get("api_key.txt"))).trim();
        ArcGISRuntimeEnvironment.setApiKey(yourApiKey);

        mapView = new MapView();
        ArcGISMap map = new ArcGISMap(BasemapStyle.ARCGIS_COMMUNITY);
        mapView.setMap(map);
        stackPane.getChildren().add(mapView);

        GraphicsOverlay graphicsOverlay = new GraphicsOverlay();
        mapView.getGraphicsOverlays().add(graphicsOverlay);


        VBox sideBar = new VBox();
        sideBar.setMaxWidth(10);
        sideBar.getStyleClass().add("color-palette");
        sideBar.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

        Image image = new Image("TrackerIntro.png");
        ImageView imageView = new ImageView();
        imageView.setImage(image);
        imageView.setFitWidth(220);
        imageView.setPreserveRatio(true);
        Group root = new Group(imageView);
        sideBar.getChildren().add(root);

        buttonInfo = new Button("Tutorial");
        buttonInfo.setOnAction(e -> {
            try {
                AlertBox.display("Tutorial", "Czy wszystko jasne kamracie?");
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
        });
        sideBar.getChildren().add(buttonInfo);

        modeFlag = new Text("Tryb działania: Dane aktualne");
        sideBar.getChildren().add(modeFlag);

        params = new Text("params:");
        sideBar.getChildren().add(params);

        buttonNow = new Button("Pokaż aktualne położenie ISS");
        buttonNow.setOnAction(e -> {
            managerNow.turnOn();
            managerDates.turnOff();
            this.setMode(Mode.Now);
            modeFlag.setText("Tryb działania: Dane aktualne");
        });
        sideBar.getChildren().add(buttonNow);

        datePicker = new DatePicker();
        datePicker.setValue(LocalDate.now());
        timePicker = new TimePicker();

        sideBar.getChildren().add(datePicker);
        sideBar.getChildren().add(timePicker);


        buttonDate = new Button("Pokaż położenie ISS z podanej daty");
        buttonDate.setOnAction(e -> {
            time = datePicker.getValue().atTime(timePicker.getTime()).toEpochSecond(ZoneOffset.ofHours(1));
            managerDates.setDate(time);
            managerDates.turnOn();
            this.setMode(Mode.Past);
            managerNow.turnOff();
            modeFlag.setText("Tryb działania: Dane z podanej daty");
        });
        sideBar.getChildren().add(buttonDate);


        buttonSave = new Button("Zapisz do CSV");
        buttonSave.setOnAction(e -> {
            switch (mode) {
                case Now:
                    this.managerNow.setSave("data" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd,HH-mm-ss")) + ".csv");
                    break;
                case Past:
                    this.managerDates.setSave("data" + LocalDateTime.ofEpochSecond(Long.valueOf(this.time), 0, ZoneOffset.ofHours(1)).format(DateTimeFormatter.ofPattern("yyyy-MM-dd,HH-mm-ss")) + ".csv");
                    break;
                case Downloaded:
                    break;
            }
        });
        sideBar.getChildren().add(buttonSave);

        FileChooser fileChooser = new FileChooser();
        buttonDownload = new Button("Załaduj plik CSV");
        buttonDownload.setOnAction(e -> {
            File selectedFile = fileChooser.showOpenDialog(stage);
            if (dataCSVReader.readCSV(selectedFile, ',')) {
                setMode(Mode.Downloaded);
                downloadFlag.setText("");
                modeFlag.setText("Tryb działania: Dane pobrane");
            } else {
                downloadFlag.setText("Zły plik!");
            }
        });
        sideBar.getChildren().add(buttonDownload);

        downloadFlag = new Text("");
        sideBar.getChildren().add(downloadFlag);

        Text text = new Text("Wybierz tempo dla danych pobranych");
        sideBar.getChildren().add(text);

        slider = new Slider();
        slider.setMajorTickUnit(1);
        slider.setMinorTickCount(0);
        slider.setSnapToTicks(true);
        slider.setMin(1);
        slider.setMax(5);
        slider.setValue(1);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setBlockIncrement(1);
        slider.setOnMouseReleased(mouseEvent -> {
            speed = Double.valueOf(slider.getValue()).intValue();
        });
        sideBar.getChildren().add(slider);

        sideBar.setAlignment(Pos.TOP_CENTER);
        stackPane.getChildren().add(sideBar);
        StackPane.setAlignment(sideBar, Pos.CENTER_LEFT);

        point = new Point(0, 0, SpatialReferences.getWgs84());
        Image issImg = new Image("ISS_spacecraft_model_1.png");
        PictureMarkerSymbol symbol = new PictureMarkerSymbol(issImg);
        symbol.setHeight(50);
        symbol.setWidth(50);
        graphic = new Graphic(point, symbol);
        graphicsOverlay.getGraphics().add(graphic);

        mainLoop();

        StartBox.display();
    }

    private void mainLoop() {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                switch (mode) {
                    case Now:
                        if (managerNow.isValue()) {
                            managerNow.lock();
                            point = managerNow.getNewestPosition();
                            params.setText(managerNow.getParams());
                            managerNow.unlock();
                            graphic.setGeometry(point);
                        }
                        break;
                    case Past:
                        if (managerDates.isValue()) {
                            managerDates.lock();
                            point = managerDates.getNewestPosition();
                            params.setText(managerDates.getParams());
                            managerDates.unlock();
                            graphic.setGeometry(point);
                        }
                        break;
                    case Downloaded:
                        if (i % (6-speed) == 0) {
                            point = dataCSVReader.getNextPosition();
                            graphic.setGeometry(point);
                            params.setText(dataCSVReader.getParams());
                        }
                        i++;
                }

            }
        };
        timer.scheduleAtFixedRate(task, 200, 200);
    }

    private void setMode(Mode mode) {
        this.mode = mode;
    }


}
