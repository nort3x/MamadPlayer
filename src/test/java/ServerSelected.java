import ir.tesla_tic.component.ServerSelector;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

public class ServerSelected extends Application {



    @Test
    void shouldRun(){
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        VBox v = new VBox();
        v.getChildren().add(new ServerSelector("Human","comma",1));
        v.getChildren().add(new ServerSelector("Human","comma",1));
        v.getChildren().add(new ServerSelector("Human","comma",1));
        v.getChildren().add(new ServerSelector("Human","comma",1));
        primaryStage.setScene(new Scene(v));
        primaryStage.show();
    }
}
