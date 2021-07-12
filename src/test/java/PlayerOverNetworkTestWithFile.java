import ir.tesla_tic.SimpleLocalMediaPlayer;
import ir.tesla_tic.network.MediaPlayerASClient;
import ir.tesla_tic.network.MediaPlayerASServer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;

public class PlayerOverNetworkTestWithFile {
    @Test void shouldPlayOverNetwork() throws IOException, InterruptedException {

        final JFXPanel fxPanel = new JFXPanel();

        MediaPlayerASServer mps = new MediaPlayerASServer(new ServerSocket(9090));

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        mps.doJob();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();


        MediaPlayerASClient mpc = new MediaPlayerASClient("127.0.0.1",9090);
        mpc.reInitializeWith("/root/Music/Pat-Benatar-Heartbreaker.mp3");
        mpc.play();

        SimpleLocalMediaPlayer smp = new SimpleLocalMediaPlayer();
        //smp.reInitializeWith(new File("/root/Music/Pat-Benatar-Heartbreaker.mp3").toURI().toURL().toString());
        //smp.play();
        synchronized (mpc){
            mpc.wait();
        }



    }


    public static class DumbINIT extends Application{

        @Override
        public void start(Stage primaryStage) throws Exception {

        }
        public void init(){
            launch();
        }
    }
}