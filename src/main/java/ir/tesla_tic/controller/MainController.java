package ir.tesla_tic.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXSlider;
import com.jfoenix.controls.JFXToggleButton;
import ir.tesla_tic.component.LCell;
import ir.tesla_tic.component.RightPane;
import ir.tesla_tic.component.ServerManager;
import ir.tesla_tic.model.MusicModel;
import ir.tesla_tic.player.MediaPlayerASClient;
import ir.tesla_tic.player.MediaPlayerEntity;
import ir.tesla_tic.player.SimpleLocalMediaPlayer;
import ir.tesla_tic.serial.SexyArduinoLights;
import ir.tesla_tic.utils.AmplitudeAverager;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
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
import java.rmi.RemoteException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class MainController implements Initializable {


    MediaPlayerEntity mp = new SimpleLocalMediaPlayer();
    SimpleBooleanProperty isPlaying = new SimpleBooleanProperty(false);
    AtomicBoolean underClick = new AtomicBoolean(false);


    MusicModel currentMusic;
    File currentItemSelected;
    int currentIndex = -1;
    @FXML
    private VBox main_vbox;

    @FXML
    private HBox downBox;
    @FXML
    private MenuBar mbar;
    @FXML
    private SplitPane splitpane;

    @FXML
    private JFXToggleButton btn_stream;

    @FXML
    private MenuItem btn_terminal;

    @FXML
    private RightPane rightPane;

    @FXML
    private MenuItem choose_btn;

    @FXML
    private MenuItem btn_scan;

    @FXML
    private MenuItem btn_manual;

    @FXML
    private JFXListView<MusicModel> list_music;


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


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        SexyArduinoLights.getInstance().start();
        rightPane.setMinWidth(1);
        rightPane.setMinHeight(1);
        list_music.setCellFactory(param -> new LCell());

        slider_volume.setMax(1);
        slider_volume.setMin(0);
        slider_volume.setValue(0.5);
        rightPane.setPrefWidth(10);
        rightPane.getDanceProperty().bind(isPlaying);

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
                try {
                    mp.seekTo(slider_played.getValue()/100d);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
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
        rightPane.setBarChart(bc);
        bc.visibleProperty().bind(isPlaying);
        try {
            InitializeMediaPlayer();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        addRelations();
    }


    private void InitializeMediaPlayer() throws RemoteException {

        rightPane.clearExtra();
//        mp.seekTo(new Duration(slider_played.getValue()).toMillis());

        slider_played.setMax(100);

        mp.currentPositionUpdate((d)->{
            if(!underClick.get())
                slider_played.setValue(d*100);
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
              rightPane.setImage((Image)value);
            }else if(key.contains("raw meta")){

            }else if(key.equals("artist")){
                rightPane.getTxt_singer().setText(value.toString());
            }
            else{
                rightPane.addExtra(key,value.toString());
            }
        });

        mp.acceptAudioSpectrum((double d, double d1, float[] magnitudes , float[] phases) -> {

            float[] arr = AmplitudeAverager.reduced(magnitudes,series1Data.length);
            for(int i=0;i<series1Data.length;i++){

                series1Data[i].setYValue(arr[i]+60); //Top Series
                //SexyArduinoLights.getInstance().emitLight(arr);
//                series2Data[i].setYValue(-(magnitudes[i]+60));//Bottom series
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
                    try {
                        mp.pause();
                        isPlaying.set(false);
                    } catch (RemoteException remoteException) {
                        remoteException.printStackTrace();
                    }
                } else {
                    try {
                        mp.play();
                        isPlaying.set(true);
                    } catch (RemoteException remoteException) {
                        remoteException.printStackTrace();
                    }
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
        btn_scan.setOnAction(e->{
            ServerManager.getInstance().showManagerPage((Stage) main_vbox.getScene().getWindow());
            e.consume();
        });

        btn_stream.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(newValue){
                    ServerManager.getInstance().getSelected().stream().findFirst().ifPresent(x->{
                        try {
                            mp.dispose();
                            mp = new MediaPlayerASClient(x.getHost(),x.getPort());
                            InitializeMediaPlayer();
                            if(currentMusic!=null) {
                                double d = slider_played.getValue();
                                mp.reInitializeWith(currentMusic.getPath().getAbsolutePath());
                                mp.seekTo(d/100d);
                                mp.volumeTo(slider_volume.getValue());
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }else if(!(mp instanceof SimpleLocalMediaPlayer)){
                    try {
                        mp.dispose();
                        mp = new SimpleLocalMediaPlayer();
                        InitializeMediaPlayer();
                        if(currentMusic!=null) {
                            double d = slider_played.getValue();
                            mp.reInitializeWith(currentMusic.getPath().getAbsoluteFile().toURI().toURL().toString());
                            ((SimpleLocalMediaPlayer)mp).onStartSeekTo(()->{
                                try {
                                    mp.seekTo(d / 100d);
                                    mp.volumeTo(slider_volume.getValue());
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                    } catch (RemoteException | MalformedURLException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    private void initializePlayer(File f) throws MalformedURLException, RemoteException {
        if(mp instanceof SimpleLocalMediaPlayer)
        mp.reInitializeWith(f.getAbsoluteFile().toURI().toURL().toString());
        else
            mp.reInitializeWith(f.getAbsolutePath());
        mp.play();
        isPlaying.set(true);
    }


    XYChart.Data[] series1Data = new XYChart.Data[30];
    XYChart.Data[] series2Data = new XYChart.Data[128];

    private void EQ(){
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis(0,30,10);

        final BarChart<String,Number>  bc = new BarChart<>(xAxis,yAxis);
        bc.setLegendVisible(false);
        bc.setAnimated(false);
        bc.setBarGap(0);
        bc.setCategoryGap(0);
        bc.setVerticalGridLinesVisible(false);
        bc.setHorizontalGridLinesVisible(false);
        bc.setHorizontalZeroLineVisible(false);
        bc.setVerticalZeroLineVisible(false);
        bc.setStyle("-fx-background-color: transparent;");
        yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis,null,"dB"));
        xAxis.setTickLabelFill(Color.TRANSPARENT);
        yAxis.setTickLabelFill(Color.TRANSPARENT);
        yAxis.setOpacity(0);
        xAxis.setOpacity(0);
        xAxis.setPrefHeight(0);
        xAxis.setMinHeight(0);
        xAxis.setMaxHeight(0);

        XYChart.Series<String,Number>  series1 =new XYChart.Series<> ();
        series1.setName("Series Neg");

        XYChart.Series<String,Number>  series2 =new XYChart.Series<> ();
        series1.setName("Series Neg");


        for (int i=0; i<series1Data.length; i++) {
            series1Data[i] = new XYChart.Data<>( Integer.toString(i+1),0);
            series1.getData().add(series1Data[i]);
        }
//        for (int i=0; i<series2Data.length; i++) {
//            series2Data[i] = new XYChart.Data<>( Integer.toString(i+1),50);
//            series2.getData().add(series2Data[i]);
//
//        }

        bc.getData().add(series1);
//        bc.getData().add(series2);
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
            rightPane.clear();
            rightPane.getTxt_title().setText(m.showName());
            initializePlayer(currentItemSelected);
        } catch (MalformedURLException | RemoteException e) {
            e.printStackTrace();
        }
    }

}
