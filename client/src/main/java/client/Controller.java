package client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

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

    private  Socket socket;
    private  DataOutputStream outMsg;
    private  DataInputStream inputMsg;

    private final int PORT = 8189;
    private String SERVER_ADDRESS = "localhost";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(()->{
            textField.requestFocus();
        });

        try {
            socket = new Socket(SERVER_ADDRESS, PORT);
            System.out.println("User connected");

            outMsg = new DataOutputStream(socket.getOutputStream());
            inputMsg = new DataInputStream(socket.getInputStream());

            //Поток для приема сообщений.
                Thread thread = new Thread(() -> {
                    try {
                while (true) {
                    String str = inputMsg.readUTF();
                    if (str.equals("/exit")) {
                        System.out.println("User disconnect.");
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
}
