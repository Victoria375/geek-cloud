package ru.geekbrains.sep22.geekcloudclient;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class RenameController implements Initializable {

    private boolean isRename = false;

    @FXML
    private TextField newFileName;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    public void renameFile(ActionEvent event) {
        isRename = true;
        ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
    }

    public void cancelChanges(ActionEvent event) {
        isRename = false;
        ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
    }

    public boolean getResult() {
        return isRename;
    }

    public String getNewFileName() {
        return newFileName.getText();
    }

}
