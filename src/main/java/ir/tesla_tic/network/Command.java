package ir.tesla_tic.network;


public class Command {
    public enum Type{
        //COMMAND FROM CLIENT
        PLAY,
        PAUSE,
        LOAD,
        SEEK_TO,
        VOLUME_TO,

        //SERVER TO CLIENT
        FINISHED,
        ERROR,
        META,
        TOTAL,
        CURRENT,
        AUDIO_EQ
    }

    Type t;
    String meta_data;


    public Command(Type t, String meta_data) {
        this.t = t;
        this.meta_data = meta_data;
    }

    public Command() {
    }

    public Type getT() {
        return t;
    }

    public void setT(Type t) {
        this.t = t;
    }

    public String getMeta_data() {
        return meta_data;
    }

    public void setMeta_data(String meta_data) {
        this.meta_data = meta_data;
    }
}
