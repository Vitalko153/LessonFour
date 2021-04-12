package client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Scanner;


public class Controller implements Initializable {
    @FXML
    public TextField textField;
    @FXML
    public TextArea textArea;
    @FXML
    public TextField loginField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public HBox loginBox;
    @FXML
    public HBox msgBox;

    private  Socket socket;
    private  DataOutputStream outMsg;
    private  DataInputStream inputMsg;

    private final int PORT = 8189;
    private String SERVER_ADDRESS = "localhost";

    private boolean authentication;
    private String nickname;

    private Stage stage;

    public void setAuthentication(boolean authentication) {
        this.authentication = authentication;

        loginBox.setVisible(!authentication);
        loginBox.setManaged(!authentication);
        msgBox.setVisible(authentication);
        msgBox.setManaged(authentication);

        if (!authentication){
            nickname = "";
        }
        setTitle(nickname);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(()->{
            stage = (Stage) textArea.getScene().getWindow();
        });
        setAuthentication(false);
    }

    private void connect(){
        try {
            socket = new Socket(SERVER_ADDRESS, PORT);
            System.out.println("User connected");

            outMsg = new DataOutputStream(socket.getOutputStream());
            inputMsg = new DataInputStream(socket.getInputStream());

            //Поток для приема сообщений.
            Thread thread = new Thread(() -> {
                try {
                    //цикл аунтификации.
                    while (true) {
                        String str = inputMsg.readUTF();
                        if (str.startsWith("/")) {
                            if (str.equals("/exit")) {
                                System.out.println("User disconnect.");
                                break;
                            }
                            if(str.startsWith("/auth_ok")){
                                nickname =str.split("\\s+")[1];
                                setAuthentication(true);
                                textArea.clear();
                                break;
                            }
                        } else {
                            textArea.appendText(str + "\n");
                        }
                    }

                    //цикл чата.
                    while (true) {
                        String str = inputMsg.readUTF();
                        if (str.equals("/exit")) {
                            setAuthentication(false);
                            System.out.println("User disconnect.");
                            textArea.clear();
                            loginField.clear();
                            break;
                        }
                        textArea.appendText(str + "\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.setDaemon(true);
            thread.start();

        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    //Отправка сообщений.
    @FXML
    public void btnSend() {
        try {
            outMsg.writeUTF(textField.getText());
            textField.clear();
            textField.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Воод логина и пароля.
    @FXML
    public void login() {
        if (socket == null || socket.isClosed()){
            connect();
        }
        String msg = String.format("/auth %s %s", loginField.getText().trim(), passwordField.getText().trim());

        try {
            outMsg.writeUTF(msg);
            passwordField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setTitle(String nickname){
        Platform.runLater(()->{
            if(nickname.equals("")){
                stage.setTitle("MyChat");
            }else {
                stage.setTitle(String.format("MyChat: [ %s ] ", nickname));
            }
        });
    }
}
