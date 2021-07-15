package ir.tesla_tic.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.scene.image.Image;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;



public class Meta {
    String meta_key;
    byte[] meta_value; // base64 or string
    Type meta_value_type;

    public Meta(String meta_key, byte[] meta_value, Type meta_value_type) {
        this.meta_key = meta_key;
        this.meta_value = meta_value;
        this.meta_value_type = meta_value_type;
    }

    public Meta(String meta_key, String meta_value, Type meta_value_type) {
        this.meta_key = meta_key;
        this.meta_value =meta_value.getBytes();
        this.meta_value_type = meta_value_type;
    }
    public Meta() {
    }

    public enum Type{
        IMG,
        STRING
    }

    public String getMeta_key() {
        return meta_key;
    }

    public void setMeta_key(String meta_key) {
        this.meta_key = meta_key;
    }

    public byte[] getMeta_value() {
        return meta_value;
    }

    public void setMeta_value(byte[] meta_value) {
        this.meta_value = meta_value;
    }

    public Type getMeta_value_type() {
        return meta_value_type;
    }

    public void setMeta_value_type(Type meta_value_type) {
        this.meta_value_type = meta_value_type;
    }

    transient Gson des = new GsonBuilder().excludeFieldsWithModifiers(java.lang.reflect.Modifier.TRANSIENT).create();
    public Object getValue(){
        if(meta_value_type.equals(Type.IMG)){
            ByteArrayInputStream bos = new ByteArrayInputStream((byte[])meta_value);
            return new Image(bos);
        }else {
            return new String(meta_value);
        }
    }

    @Override
    public String toString() {
        return des.toJson(Meta.this,Meta.class);
    }
}
