package ir.tesla_tic;


import ir.tesla_tic.component.DiscoverableServer;
import ir.tesla_tic.model.DiscoveredServer;
import ir.tesla_tic.player.MediaPlayerASServer;
import javafx.application.Platform;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.ServerSocket;

public class Main {




    public static void main(String[] args)  {



        if(args.length>0 && args[0].equals("server")){
            Platform.startup(()->{});
            DiscoverableServer ds = new DiscoverableServer(new DiscoveredServer("mainsv","127.0.0.1",9090));
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            ds.getDiscovered();
                        } catch (IOException e) {
                           // e.printStackTrace();
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }).start();

            MediaPlayerASServer mediaPlayerASServer = null;

            try {
                mediaPlayerASServer = new MediaPlayerASServer(new ServerSocket(9090));
                while (true) {
                    try {
                        mediaPlayerASServer.doJob();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }else
            JFXRunner.run();

    }


}
