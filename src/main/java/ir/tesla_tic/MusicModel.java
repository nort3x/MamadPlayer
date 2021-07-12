package ir.tesla_tic;

import java.io.File;

public class MusicModel {
    File path;
    public void setPath(File path){
        this.path =path;
    }
    public MusicModel(){

    }
    public MusicModel(String path){
        this.path = new File(path);
    }
    public String showName(){
        return path.toPath().getFileName().toString();
    }
    public File getPath(){
        return  path;
    }
}
