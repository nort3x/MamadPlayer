package ir.tesla_tic.player;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.media.AudioSpectrumListener;
import javafx.util.Duration;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.media.MediaEventAdapter;
import uk.co.caprica.vlcj.media.events.MediaEventFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.component.AudioPlayerComponent;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class VLCMediaPlayer implements MediaPlayerEntity {

    SimpleDoubleProperty volume = new SimpleDoubleProperty(0.5);//
    AudioSpectrumListener audioSpectrumListener = new AudioSpectrumListener() {
        @Override
        public void spectrumDataUpdate(double timestamp, double duration, float[] magnitudes, float[] phases) {

        }
    };
    double interval = 0.01;

    BiConsumer<String, Object> meta = new BiConsumer<String, Object>() {
        @Override
        public void accept(String s, Object o) {

        }
    };



    AudioPlayerComponent mp = new AudioPlayerComponent();

    public VLCMediaPlayer() {
        volume.addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                mp.mediaPlayer().audio().setVolume((int)(newValue.doubleValue()*100));
            }
        });

    }

    @Override
    public void dispose()  {
        mp.release();
    }

    @Override
    public void play()  {
        mp.mediaPlayer().controls().play();
    }

    @Override
    public void pause()  {
        mp.mediaPlayer().controls().pause();
    }

    @Override
    public void seekTo(double where)  {
        mp.mediaPlayer().controls().setPosition((float) where);
    }

    @Override
    public void reInitializeWith(String path)  {
        mp.mediaPlayer().media().play(path);
    }

    @Override
    public void totalTimeUpdate(Consumer<Duration> r)  {
     r.accept(Duration.seconds(100));
    }

    @Override
    public void currentPositionUpdate(Consumer<Duration> r)  {
     mp.mediaPlayer().events().addMediaPlayerEventListener(new MediaPlayerEventAdapter(){
         @Override
         public void positionChanged(MediaPlayer mediaPlayer, float newPosition) {
             super.positionChanged(mediaPlayer, newPosition);
             r.accept(Duration.seconds(newPosition*100));
         }
     });
    }

    @Override
    public void onStopped(Runnable r)  {
        mp.mediaPlayer().events().addMediaPlayerEventListener(new MediaPlayerEventAdapter(){
            @Override
            public void finished(MediaPlayer mediaPlayer) {
                super.finished(mediaPlayer);
                r.run();
            }
        });
    }

    @Override
    public void acceptVolumeBinder(ReadOnlyDoubleProperty readOnlyDoubleProperty)  {
     volume.bind(readOnlyDoubleProperty);
    }

    @Override
    public void onMetaDataChanged(BiConsumer<String, Object> consumer)  {
     meta = consumer;
    }

    @Override
    public void acceptAudioSpectrum(AudioSpectrumListener audioSpectrumListener, double howOftenSeconds)  {
     this.audioSpectrumListener = audioSpectrumListener;
     interval = howOftenSeconds;
    }

    public void softDispose(){
        mp.mediaPlayer().controls().stop();
    }
}
