package ir.tesla_tic;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXSlider;
import com.jfoenix.controls.JFXToggleButton;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.MapChangeListener;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioSpectrumListener;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MainController implements Initializable {


    SimpleLocalMediaPlayer mp = new SimpleLocalMediaPlayer();
    AtomicBoolean isPlaying = new AtomicBoolean(false);
    AtomicBoolean underClick = new AtomicBoolean(false);


    MusicModel currentMusic;
    File currentItemSelected;
    int currentIndex = -1;
    @FXML
    private VBox main_vbox;

    @FXML
    private JFXToggleButton btn_stream;

    @FXML
    private MenuItem btn_terminal;


    @FXML
    private MenuItem choose_btn;

    @FXML
    private MenuItem btn_connect;

    @FXML
    private MenuItem btn_disconnect;

    @FXML
    private MenuItem btn_manual;

    @FXML
    private JFXListView<MusicModel> list_music;

    @FXML
    private Pane right_pane;

    @FXML
    private JFXButton btn_prev;

    @FXML
    private JFXButton btn_play_stop;

    @FXML
    private JFXButton btn_next;

    @FXML
    private JFXSlider slider_volume;

    @FXML
    private JFXSlider slider_played;


    VBox v2 = new VBox(){{
        setPadding(new Insets(20));
    }};
    VBox v = new VBox(){{
        setAlignment(Pos.CENTER);
        setSpacing(30);
        setPadding(new Insets(20,20,20,20));
        getChildren().add(v2);
    }};

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        list_music.setCellFactory(param -> new LCell());

        slider_volume.setMax(1);
        slider_volume.setMin(0);
        slider_volume.setValue(0.5);

        VBox.setVgrow(v,Priority.ALWAYS);

        HBox.setHgrow(v, Priority.ALWAYS);
        VBox.setVgrow(v,Priority.ALWAYS);
        right_pane.getChildren().add(v);


        slider_volume.setLabelFormatter(new StringConverter<Double>() {
            @Override
            public String toString(Double object) {
                return Integer.toString((int) (object*100));
            }

            @Override
            public Double fromString(String string) {
                return Double.valueOf(string);
            }
        });



        slider_played.setValue(0);
        slider_played.setValueFactory(new Callback<JFXSlider, StringBinding>() {
            @Override
            public StringBinding call(JFXSlider param) {
                return Bindings.createStringBinding(new java.util.concurrent.Callable<String>(){
                    @Override
                    public String call() throws Exception {
                        DecimalFormat df = new DecimalFormat("#.0");
                        return df.format(slider_played.getValue());
                    }
                }, slider_played.valueProperty());
            }
        });
        slider_played.setOnMousePressed(e->{
            underClick.set(true);
        });

        slider_played.setOnMouseReleased(e->{
            underClick.set(false);
        });
        slider_played.setOnMouseClicked((MouseEvent mouseEvent) -> {
            if(mp!=null) {
                mp.seekTo(Duration.seconds(slider_played.getValue()).toMillis());
            }
        });


        slider_played.setLabelFormatter(new StringConverter<Double>() {
            @Override
            public String toString(Double object) {
                return String.valueOf(object);
            }

            @Override
            public Double fromString(String string) {
                return Double.parseDouble(string);
            }
        });


        EQ();
        InitializeMediaPlayer();
        addRelations();
    }


    private void InitializeMediaPlayer(){

        mp.totalTimeUpdate((d)->{
            slider_played.setMax(d.toSeconds());
        });

        mp.currentPositionUpdate((d)->{
            if(!underClick.get())
                slider_played.setValue(d.toSeconds());
        });

        mp.onStopped(new Runnable() {
            @Override
            public void run() {
                if(currentIndex!=-1 && currentIndex+1 < list_music.getItems().size()){
                    currentIndex++;
                    list_music.getSelectionModel().select(currentIndex);
                    selectMusic(list_music.getItems().get(currentIndex));

                }
            }
        });

        mp.acceptVolumeBinder(slider_volume.valueProperty());

        mp.onMetaDataChanged((key,value)->{
            if(value instanceof Image) {
                ImageView imv = new ImageView((Image) value);
                imv.setPreserveRatio(true);
                imv.setSmooth(true);
                imv.fitHeightProperty().setValue(300);
                v.getChildren().add(0,imv);
            }else if(key.contains("raw meta")){

            }else{
                v2.getChildren().add(new Text(key+" : " +value));
            }
        });

        mp.acceptAudioSpectrum((double d, double d1, float[] magnitudes , float[] phases) -> {

            for(int i=0;i<magnitudes.length;i++){

                series1Data[i].setYValue((magnitudes[i]+60)); //Top Series
                series2Data[i].setYValue(-(magnitudes[i]+60));//Bottom series
            }

        },0.05);


    }


    private void addRelations() {

        btn_manual.setOnAction(e -> {
            VBox p = new VBox();
            p.setAlignment(Pos.CENTER);
            p.setPadding(new Insets(30, 10, 30, 10));
            p.getChildren().add(new Text("Figure it out!"));
            Stage s = (Stage) main_vbox.getScene().getWindow();
            Stage newStage = new Stage(StageStyle.DECORATED);
            newStage.initOwner(s);
            newStage.setScene(new Scene(p));
            newStage.show();
            e.consume();
        });
        choose_btn.setOnAction(e -> {
            DirectoryChooser dc = new DirectoryChooser();
            dc.titleProperty().setValue("Choose Music Directory...");
            dc.setInitialDirectory(new File(System.getProperty("user.home") + System.getProperty("file.separator") + "Music"));
            reloadFolder(dc.showDialog(main_vbox.getScene().getWindow()));
            e.consume();
        });
        btn_play_stop.setOnAction(e -> {
            if (mp != null) {
                if (isPlaying.get()) {
                    mp.pause();
                    isPlaying.set(false);
                } else {
                    mp.play();
                    isPlaying.set(true);
                }
            }
        });
        btn_next.setOnAction(e->{
            if(currentIndex!=-1 && currentIndex+1 < list_music.getItems().size()){
                currentIndex++;
                list_music.getSelectionModel().select(currentIndex);
                selectMusic(list_music.getItems().get(currentIndex));

            }
        });

        btn_prev.setOnAction(e->{
            if(currentIndex!=-1 && currentIndex-1 >=0){
                currentIndex--;
                list_music.getSelectionModel().select(currentIndex);
                selectMusic(list_music.getItems().get(currentIndex));
            }
        });

        list_music.setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent click) {

                if (click.getClickCount() == 2) {
                    selectMusic(list_music.getSelectionModel().getSelectedItem());
                }
            }
        });

    }

    private void initializePlayer(File f) throws MalformedURLException {
        v.getChildren().clear();
        v.getChildren().add(v2);
        v.getChildren().add(bc);
        v2.getChildren().clear();

        mp.reInitializeWith(f.getAbsoluteFile().toURI().toURL().toString());
        mp.play();
        isPlaying.set(true);
    }


    XYChart.Data[] series1Data = new XYChart.Data[128];
    XYChart.Data[] series2Data = new XYChart.Data[128];

    private void EQ(){
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis(-50,50,10);

        final BarChart<String,Number>  bc = new BarChart<>(xAxis,yAxis);
        bc.setLegendVisible(false);
        bc.setAnimated(false);
        bc.setBarGap(0);
        bc.setCategoryGap(0);
        bc.setVerticalGridLinesVisible(false);
        bc.setHorizontalGridLinesVisible(false);
        bc.setHorizontalZeroLineVisible(false);
        bc.setVerticalZeroLineVisible(false);
        bc.setMaxSize(900, 400);
        bc.setMinSize(900, 400);

        yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis,null,"dB"));
        xAxis.setTickLabelFill(Color.TRANSPARENT);
        yAxis.setTickLabelFill(Color.TRANSPARENT);
        yAxis.setOpacity(0);
        xAxis.setOpacity(0);

        XYChart.Series<String,Number>  series1 =new XYChart.Series<> ();
        series1.setName("Series Neg");

        XYChart.Series<String,Number>  series2 =new XYChart.Series<> ();
        series1.setName("Series Neg");


        for (int i=0; i<series1Data.length; i++) {
            series1Data[i] = new XYChart.Data<>( Integer.toString(i+1),50);
            series1.getData().add(series1Data[i]);

        }
        for (int i=0; i<series2Data.length; i++) {
            series2Data[i] = new XYChart.Data<>( Integer.toString(i+1),50);
            series2.getData().add(series2Data[i]);

        }

        bc.getData().add(series1);
        bc.getData().add(series2);
        this.bc = bc;
    }
    BarChart bc;

    private void reloadFolder(File f) {

        try {
            list_music.getItems().addAll(
                    Files.walk(f.toPath())
                            .filter(path -> path.toString().endsWith(".mp3") || path.toString().endsWith(".m4a") || path.toString().endsWith(".ogg"))
                            .map(p -> p.toAbsolutePath().toString()).map(MusicModel::new).collect(Collectors.toList())
            );

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void selectMusic(MusicModel m){
        currentIndex = list_music.getItems().indexOf(m);
        currentMusic = m;
        currentItemSelected = currentMusic.getPath();

        try {
            initializePlayer(currentItemSelected);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

}
