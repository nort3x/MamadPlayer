import ir.tesla_tic.player.VLCMediaPlayer;
import org.junit.jupiter.api.Test;

import java.rmi.RemoteException;

public class VLCTest {
    @Test void shouldRunVLC(){
        VLCMediaPlayer vm = new VLCMediaPlayer();
        try {
            vm.reInitializeWith("http://127.0.0.1:4546/");
            vm.play();
            synchronized (this){
                this.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
