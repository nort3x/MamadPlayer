package ir.tesla_tic.player;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.media.AudioSpectrumListener;
import javafx.util.Duration;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.media.Media;
import uk.co.caprica.vlcj.media.MediaEventAdapter;
import uk.co.caprica.vlcj.media.Meta;
import uk.co.caprica.vlcj.media.Picture;
import uk.co.caprica.vlcj.media.events.MediaEventFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.component.AudioPlayerComponent;

import java.rmi.RemoteException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class VLCMediaPlayer implements MediaPlayerEntity {

    SimpleDoubleProperty volume = new SimpleDoubleProperty(0.5);//




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
        mp.mediaPlayer().controls().setPosition(((float) where));
    }

    @Override
    public void volumeTo(double where) throws RemoteException {
        throw new RemoteException("not implemented");
    }

    @Override
    public void reInitializeWith(String path)  {
        mp.mediaPlayer().media().play(path);
    }


    @Override
    public void currentPositionUpdate(Consumer<Double> r)  {
     mp.mediaPlayer().events().addMediaPlayerEventListener(new MediaPlayerEventAdapter(){
         @Override
         public void positionChanged(MediaPlayer mediaPlayer, float newPosition) {
             super.positionChanged(mediaPlayer, newPosition);
             r.accept((double)newPosition);
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
     mp.mediaPlayer().events().addMediaEventListener(new MediaEventAdapter(){
         @Override
         public void mediaMetaChanged(Media media, Meta metaType) {
             super.mediaMetaChanged(media, metaType);
             media.meta().asMetaData().values().forEach((key,val)->{
                 consumer.accept(key.name(),val);
             });
         }

         @Override
         public void mediaThumbnailGenerated(Media media, Picture picture) {
             super.mediaThumbnailGenerated(media, picture);
             consumer.accept("image",picture.buffer());
         }
     });
    }

    @Override
    public void acceptAudioSpectrum(AudioSpectrumListener audioSpectrumListener, double howOftenSeconds)  {
        //TODO
    }

    public void softDispose(){
        mp.mediaPlayer().controls().stop();
    }
}
