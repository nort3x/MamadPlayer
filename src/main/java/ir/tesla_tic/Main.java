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

        //args = new String[]{"-server","127.0.0.1","9090","human"};
        if(args.length>0 && args[0].equals("-server")){


            String[] finalArgs = args;
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    DiscoverableServer ds = new DiscoverableServer(new DiscoveredServer(finalArgs[3], finalArgs[1],Integer.parseInt(finalArgs[2])));
                    while (true) {
                        try {
                            ds.getDiscovered();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            t.setDaemon(true);
            t.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread thread, Throwable throwable) {
                    System.out.println("error!");
                    throwable.printStackTrace();
                }
            });
            t.start();

            try {
                MediaPlayerASServer mediaPlayerASServer = new MediaPlayerASServer(new ServerSocket(Integer.parseInt(args[2])));
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

        }else {
            JFXRunner.run();
        }
    }


}
