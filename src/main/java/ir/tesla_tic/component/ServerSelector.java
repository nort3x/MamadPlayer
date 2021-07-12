package ir.tesla_tic.component;

import com.jfoenix.controls.JFXToggleButton;
import ir.tesla_tic.Main;
import ir.tesla_tic.model.DiscoveredServer;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServerSelector extends GridPane implements Initializable {
    @FXML
    private Text txt_name;
    @FXML
     private JFXToggleButton toggle_connect;

    SimpleBooleanProperty selected = new SimpleBooleanProperty();


    String host,serverName;
    int port;

    public ServerSelector(String serverName,String host,int port){
        this.serverName = serverName;
        this.host = host;
        this.port = port;
        FXMLLoader loader = new FXMLLoader(Main.class.getClassLoader().getResource("server-selector.fxml"));
        loader.setController(this);
        loader.setRoot(this);
        try {
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ServerSelector(DiscoveredServer ds){
        this(ds.getName(),ds.getHost(),ds.getPort());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        selected.bind(toggle_connect.selectedProperty());
        txt_name.setText(serverName);
    }

    public String getHost() {
        return host;
    }

    public String getServerName() {
        return serverName;
    }

    public int getPort() {
        return port;
    }

    public ReadOnlyBooleanProperty isSelectedProperty(){
        return selected;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerSelector that = (ServerSelector) o;
        return port == that.port && Objects.equals(host, that.host) && Objects.equals(serverName, that.serverName);
    }


}
