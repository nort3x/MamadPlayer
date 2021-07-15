package ir.tesla_tic.controller;

import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXTextField;
import ir.tesla_tic.Main;
import ir.tesla_tic.component.LCell;
import ir.tesla_tic.model.MusicModel;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SearchDialog extends VBox implements Initializable {
    @FXML
    private JFXTextField txt_search;

    @FXML
    private JFXListView<MusicModel> lst_result;

    ListView<MusicModel> input;
    Consumer<MusicModel> onSelection;
    public SearchDialog(ListView<MusicModel> input, Consumer<MusicModel> onSelection)  {
        this.input = input;
        this.onSelection = onSelection;
        FXMLLoader loader = new FXMLLoader(Main.class.getClassLoader().getResource("search.fxml"));
        try {
            loader.setRoot(this);
            loader.setController(this);
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        lst_result.setCellFactory(new Callback<ListView<MusicModel>, ListCell<MusicModel>>() {
            @Override
            public ListCell<MusicModel> call(ListView<MusicModel> param) {
                return new LCell();
            }
        });
        lst_result.setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent click) {

                if (click.getClickCount() == 2) {
                    onSelection.accept(lst_result.getSelectionModel().getSelectedItem());

                }
                Platform.runLater(()->{
                    txt_search.requestFocus();
                });
            }
        });

        txt_search.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                List<MusicModel> mm =  input.getItems().stream().filter(x-> x.showName().toLowerCase(Locale.ROOT).trim().contains(newValue.toLowerCase(Locale.ROOT).trim())).collect(Collectors.toList());
                lst_result.getItems().clear();
                lst_result.getItems().addAll(mm);
                lst_result.getItems().sorted(new Comparator<MusicModel>() {
                    @Override
                    public int compare(MusicModel musicModel, MusicModel t1) {
                        return String.CASE_INSENSITIVE_ORDER.compare(musicModel.showName(),t1.showName());
                    }
                });
                lst_result.refresh();
            }
        });
        txt_search.setFocusTraversable(false);
        Platform.runLater(()->{
            txt_search.requestFocus();
        });
    }
}
