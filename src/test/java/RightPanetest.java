import ir.tesla_tic.component.RightPane;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

public class RightPanetest extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        RightPane rp = new RightPane();
        Scene s = new Scene(rp);
        rp.getDanceProperty().set(true);
        primaryStage.setScene(s);
        primaryStage.show();
    }

    @Test void shouldSpin(){
        launch();
    }
}
