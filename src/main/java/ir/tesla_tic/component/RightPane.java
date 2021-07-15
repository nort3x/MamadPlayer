package ir.tesla_tic.component;

import ir.tesla_tic.Main;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.When;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.chart.BarChart;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.text.Text;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

public class RightPane extends VBox implements Initializable {


    @FXML
    private HBox hbox_top;

    @FXML
    private ImageView img_main;

    @FXML
    private Text txt_title;

    @FXML
    private Text txt_singer;

    @FXML
    private VBox vbox_extra;

    SimpleIntegerProperty deg = new SimpleIntegerProperty();

    AtomicInteger p1x = new AtomicInteger(0);
    AtomicInteger p1y = new AtomicInteger(0);
    AtomicInteger p2x = new AtomicInteger(100);
    AtomicInteger p2y = new AtomicInteger(100);

    final SimpleBooleanProperty dance = new SimpleBooleanProperty(false);

    public RightPane() {
        FXMLLoader loader = new FXMLLoader(Main.class.getClassLoader().getResource("rightpane.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        dance.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                synchronized (dance) {
                    dance.notifyAll();
                }
            }
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //

        deg.addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                synchronized (RightPane.this) {
                    int q = newValue.intValue() % 100;

                    if (newValue.intValue() < 100) {
                        p1x.set(0);
                        p1y.set(q);

                        p2x.set(100);
                        p2y.set(100 - q);

                    } else if (newValue.intValue() < 200) {
                        p1x.set(q);
                        p1y.set(100);

                        p2x.set(100 - q);
                        p2y.set(0);
                    } else if (newValue.intValue() < 300) {

                        p1x.set(100);
                        p1y.set(100 - q);

                        p2x.set(0);
                        p2y.set(q);

                    } else if (newValue.intValue() < 400) {
                        p1x.set(100 - q);
                        p1y.set(0);

                        p2x.set(q);
                        p2y.set(100);

                    }

                    hbox_top.setStyle("-fx-border-color: linear-gradient(from " + p1x.get() + "% " + p1y.get() + "% to " + p2x.get() + "% " + p2y.get() + "% ,rgb(0,255,64),rgb(255,0,235));\n"
                            + "-fx-border-width: 15; \n" +
                            "-fx-border-insets: -2;");

                }
            }
        });

        deg.set(1);
        txt_title.setText("MamadPlayer v 0.0.1");
        txt_singer.setText("amu");
        setImage(new Image(Objects.requireNonNull(Main.class.getClassLoader().getResourceAsStream("color_small.png"))));

        hbox_top.setEffect(new InnerShadow(10, Color.BLACK));
        img_main.imageProperty().addListener(new ChangeListener<Image>() {
            @Override
            public void changed(ObservableValue<? extends Image> observable, Image oldValue, Image newValue) {
                VBox.setMargin(vbox_extra,new Insets(0,10,0,10+img_main.getFitWidth()));
            }

        });

        VBox.setMargin(vbox_extra,new Insets(0,10,0,10+img_main.getFitWidth()));
        vbox_extra.setStyle("");
        this.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                img_main.setFitHeight(0.2*newValue.doubleValue());
            }
        });
        img_main.setFitHeight(0.4*this.heightProperty().get());
        t_dancer = new Thread(() -> {
            try {
                while (true) {
                    if (dance.get()) {
                        Platform.runLater(RightPane.this::wiggle);
                        Thread.sleep(10);
                    } else
                        synchronized (dance) {

                            dance.wait(300);
                        }

                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        });
        t_dancer.setDaemon(true);
        t_dancer.start();
    }

    Thread t_dancer;

    public BooleanProperty getDanceProperty() {
        return dance;
    }

    private void wiggle() {
        synchronized (this) {
            int i = deg.get();
            i++;
            deg.set((i) % 400);
        }
    }

    public Text getTxt_title() {
        return txt_title;
    }

    public Text getTxt_singer() {
        return txt_singer;
    }

    public void addExtra(String key,String value){
        vbox_extra.getChildren().add(new Text(key+":\t"+value));
    }

    public void clearExtra(){
        vbox_extra.getChildren().clear();
    }

    public void setImage(Image m){
        if(m==null){
            img_main.setFitWidth(0);
        }else {
            img_main.setImage(m);
        }
    }

    public void clear() {
        clearExtra();
        img_main.setImage(null);
        txt_singer.setText("????");
        txt_title.setText("????");
    }

    public void setBarChart(BarChart bc){
        if(this.getChildren().get(this.getChildren().size()-1) instanceof BarChart){
            this.getChildren().remove(this.getChildren().size()-2,this.getChildren().size());
        }
        Region r = new Region();
        VBox.setVgrow(r,Priority.ALWAYS);
//        VBox.setVgrow(bc,Priority.ALWAYS);
        this.getChildren().add(r);
        this.getChildren().add(bc);
    }
}
