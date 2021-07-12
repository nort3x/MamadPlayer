package ir.tesla_tic;

import com.jfoenix.controls.JFXListCell;

import java.io.File;

public class LCell extends JFXListCell<MusicModel> {
    MusicModel m = new MusicModel();
    @Override
    protected void updateItem(MusicModel item, boolean empty) {
        super.updateItem(item, empty);
        if(item!=null && !empty){
            m = item;
            setText(m.showName());
        }
    }


}
