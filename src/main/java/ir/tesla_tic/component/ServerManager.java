package ir.tesla_tic.component;

import com.google.gson.Gson;
import ir.tesla_tic.Main;
import ir.tesla_tic.controller.ServerManagerController;
import ir.tesla_tic.model.DiscoveredServer;
import ir.tesla_tic.network.ServiceDiscovery;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class ServerManager extends VBox {

    private final static ServerManager instance = new ServerManager();
    private final ArrayList<ServerSelector> serverSelectors = new ArrayList<>();

    private ServerManager() {
        this.setSpacing(15);
    }

    public static ServerManager getInstance() {
        return instance;
    }

    public void addServer(ServerSelector sv) {
        if(!serverSelectors.contains(sv)) {
            serverSelectors.add(sv);
            this.getChildren().add(sv);
        }
    }


    AtomicBoolean isScanning = new AtomicBoolean(false);
    AtomicBoolean isAlreadyOnAScene = new AtomicBoolean(false);

    public void showManagerPage(Stage s) {
        Stage ss = new Stage();
        ss.initOwner(s);
        ss.setOnCloseRequest(e -> {
            isAlreadyOnAScene.set(false);
        });
        try {
            FXMLLoader fl = new FXMLLoader(Main.class.getClassLoader().getResource("server-manager.fxml"));
            BorderPane bp = fl.load();
            ServerManagerController sc = fl.getController();
            bp.setCenter(this);

            {
                sc.btn_start.setOnAction(e -> {
                    if (!isScanning.get()) {

                        isScanning.set(true);
                        scannerThread = new Thread(() -> {
                            Gson g = new Gson();
                            while (true) {
                                try {
                                    String data = ServiceDiscovery.discoverOverAllAddresses("mmdPlayer", "mmdServer", 1024);
                                    if(data!=null)
                                    Platform.runLater(() -> {
                                        addServer(new ServerSelector(g.fromJson(data, DiscoveredServer.class)));
                                    });
                                } catch (Exception socketException) {
                                    //socketException.printStackTrace();
                                }
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException interruptedException) {
                                    //interruptedException.printStackTrace();
                                }

                            }
                        });
                        scannerThread.setDaemon(true);
                        scannerThread.start();
                    }
                    e.consume();
                });


                sc.btn_stop.setOnAction(e->{
                    if(isScanning.get()) {
                        isScanning.set(false);
                        scannerThread.stop();
                        try {
                            scannerThread.join();
                        } catch (InterruptedException interruptedException) {
                            interruptedException.printStackTrace();
                        }
                    }

                    e.consume();
                });

            }


            VBox.setVgrow(this, Priority.ALWAYS);
            HBox.setHgrow(this, Priority.ALWAYS);


            ss.setScene(new Scene(bp));
            ss.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    Thread scannerThread;
    SimpleBooleanProperty shouldStream = new SimpleBooleanProperty();

    public BooleanProperty getShouldStreamProperty() {
        return shouldStream;
    }

    public List<DiscoveredServer> getSelected(){

        return serverSelectors.stream().filter(x->x.selected.get()).map(x->new DiscoveredServer(x.serverName,x.host,x.port)).collect(Collectors.toList());
    }
}
