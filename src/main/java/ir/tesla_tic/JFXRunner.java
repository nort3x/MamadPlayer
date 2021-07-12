package ir.tesla_tic;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

/**
 * this class is the JFX part of MainClass its in a new class because
 * ShadowJar can make a standalone runnable jar if do it this way
 * for more please
 * <a href="https://stackoverflow.com/questions/52569724/javafx-11-create-a-jar-file-with-gradle">CheckThis</a>
 */
public class JFXRunner extends Application {

    public static void run() {
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        primaryStage.setTitle("MamadPlayer");
        primaryStage.setScene(new Scene(FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource("main.fxml")))));

        primaryStage.show();


    }


}
