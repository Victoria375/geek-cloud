package ru.geekbrains.sep22.geekcloudclient;

import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ru.geekbrains.DaemonThreadFactory;
import ru.geekbrains.model.*;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import static ru.geekbrains.Command.*;
import static ru.geekbrains.FileUtils.readFileFromStream;

public class CloudMainController implements Initializable {
    public ListView<String> clientView;
    public ListView<String> serverView;
    private String currentDirectory;

    private Network<ObjectDecoderInputStream, ObjectEncoderOutputStream> network;

//    private DataInputStream dis;
//    private DataOutputStream dos;
    private Socket socket;
    private boolean needReadMessages = true;
    private DaemonThreadFactory factory;

    //private static final String SEND_FILE_COMMAND = "file";

    private String login;
    Label label = new Label("Enter login and password:");

    public void downloadFile(ActionEvent actionEvent) throws IOException {
        String fileName = serverView.getSelectionModel().getSelectedItem();
        network.getOutputStream().writeObject(new FileRequest(FileRequest.Command.DOWNLOAD, fileName));
    }

    public void sendToServer(ActionEvent actionEvent) throws IOException {
        String fileName = clientView.getSelectionModel().getSelectedItem();
        network.getOutputStream().writeObject(new FileMessage(Path.of(currentDirectory).resolve(fileName)));
    }

    public void renameClientFile(ActionEvent actionEvent) throws IOException {
        File selected = new File(clientView.getSelectionModel().getSelectedItem());
        renameFile(selected);
    }

    private void renameFile(File file) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("rename.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Stage stage = new Stage();
        stage.setTitle("Rename window");
        stage.setScene(scene);
        stage.show();

        RenameController renameController = fxmlLoader.getController();
        if (renameController.getResult()) {
            String newName = renameController.getNewFileName();
            File newFileName = new File(newName);
            file.renameTo(newFileName);
        }
    }

    public void deleteClientFile(ActionEvent actionEvent) throws IOException {
        String fileName = clientView.getSelectionModel().getSelectedItem();
        network.getOutputStream().writeObject(new FileRequest(FileRequest.Command.DELETE, fileName));
    }

    public void renameServerFile(ActionEvent actionEvent) throws IOException {
        String fileName = serverView.getSelectionModel().getSelectedItem();
        network.getOutputStream().writeObject(new FileRequest(FileRequest.Command.RENAME, fileName));
        File selected = new File(fileName);
        renameFile(selected);
    }

    public void deleteServerFile(ActionEvent actionEvent) throws IOException {
        String fileName = serverView.getSelectionModel().getSelectedItem();
        network.getOutputStream().writeObject(new FileRequest(FileRequest.Command.DELETE, fileName));
    }

    private void readMessages() {
        try {
            while (needReadMessages) {
                CloudMessage message = (CloudMessage) network.getInputStream().readObject();
                if (message instanceof FileMessage fileMessage) {
                    Files.write(Path.of(currentDirectory).resolve(fileMessage.getFileName()), fileMessage.getBytes());
                    Platform.runLater(() -> fillView(clientView, getFiles(currentDirectory)));
                } else if (message instanceof ListMessage listMessage) {
                    Platform.runLater(() -> fillView(serverView, listMessage.getFiles()));
                }
            }
        } catch (Exception e) {
            System.err.println("Server off");
            e.printStackTrace();
        }
    }

    private void initNetwork() {
        try {
            socket = new Socket("localhost", 8189);
            network = new Network<>(
                    new ObjectDecoderInputStream(socket.getInputStream()),
                    new ObjectEncoderOutputStream(socket.getOutputStream())
            );
            openLoginForm();
            factory.getThread(this::readMessages, "cloud-client-read-thread")
                    .start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        needReadMessages = true;
        factory = new DaemonThreadFactory();
        initNetwork();
        setCurrentDirectory(System.getProperty("user.home"));
        fillView(clientView, getFiles(currentDirectory));
        clientView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selected = clientView.getSelectionModel().getSelectedItem();
                File selectedFile = new File(currentDirectory + "/" + selected);
                if (selectedFile.isDirectory()) {
                    setCurrentDirectory(currentDirectory + "/" + selected);
                }
            }
        });
        serverView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selected = serverView.getSelectionModel().getSelectedItem();
                try {
                    network.getOutputStream().writeObject(new ServerFilesRequest(selected));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void setCurrentDirectory(String directory) {
        currentDirectory = directory;
        fillView(clientView, getFiles(currentDirectory));
    }

    private void fillView(ListView<String> view, List<String> data) {
        view.getItems().clear();
        view.getItems().addAll(data);
    }

    private List<String> getFiles(String directory) {
        File dir = new File(directory);
        if (dir.isDirectory()) {
            String[] list = dir.list();
            if (list != null) {
                List<String> files = new ArrayList<>(Arrays.asList(list));
                files.add(0, "..");
                return files;
            }
        }
        return List.of();
    }

    private void openLoginForm() {
        Stage loginForm = new Stage();
        loginForm.initModality(Modality.APPLICATION_MODAL);
        loginForm.setOnCloseRequest(e -> System.exit(0));

        TextField login = new TextField();
        login.setMaxWidth(200);
        login.setPromptText("login");
        login.setFocusTraversable(false);

        TextField password = new TextField();
        password.setMaxWidth(200);
        password.setPromptText("password");
        password.setFocusTraversable(false);

        Button loginBtn = new Button("Log in");
        loginBtn.setMinSize(200, 40);
        loginBtn.setFocusTraversable(false);

        loginBtn.setOnAction(event -> {

            if (login.getText().isBlank() | password.getText().isBlank()) {
                label.setText("Enter login AND password!");
            } else {
                Authentication auth = new Authentication(login.getText(), password.getText());
                try {
                    network.getOutputStream().writeObject(auth);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                this.login = login.getText();
                loginForm.close();
            }
        });

        VBox vBox = new VBox(20);
        vBox.setPadding(new Insets(40, 5, 5, 50));
        vBox.getChildren().addAll(label, login, password, loginBtn);
        Scene scene = new Scene(vBox, 300, 300);

        loginForm.setScene(scene);
        loginForm.setTitle("Login form");
        loginForm.setResizable(false);
        loginForm.showAndWait();
    }


}
