package ir.tesla_tic;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.scene.media.AudioSpectrumListener;
import javafx.util.Duration;

import java.rmi.Remote;
import java.rmi.RemoteException;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface MediaPlayerEntity extends Remote {

    void play() throws RemoteException;
    void pause() throws RemoteException;

    void seekTo(double where) throws RemoteException;
    void reInitializeWith(String path) throws RemoteException;



    void totalTimeUpdate(Consumer<Duration> r) throws RemoteException;
    void currentPositionUpdate(Consumer<Duration> r) throws RemoteException;
    void onStopped(Runnable r) throws RemoteException;
    void acceptVolumeBinder(ReadOnlyDoubleProperty readOnlyDoubleProperty) throws RemoteException;
    void onMetaDataChanged(BiConsumer<String,Object> consumer) throws RemoteException;
    void acceptAudioSpectrum(AudioSpectrumListener audioSpectrumListener,double howOftenSeconds) throws RemoteException;

}
