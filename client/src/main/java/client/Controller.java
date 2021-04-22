package client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import java.io.*;
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
    @FXML
    public ListView<String> userList;

    private  Socket socket;
    private  DataOutputStream outMsg;
    private  DataInputStream inputMsg;

    private final int PORT = 8189;
    private String SERVER_ADDRESS = "localhost";

    private boolean authentication;
    private String nickname;
    private String login;

    private Stage stage;
    private Stage regStage;
    private RegController regController;
    TextChatHandler historySave = new TextChatHandler();

    public void setAuthentication(boolean authentication) {
        this.authentication = authentication;
        loginBox.setVisible(!authentication);
        loginBox.setManaged(!authentication);
        msgBox.setVisible(authentication);
        msgBox.setManaged(authentication);
        userList.setVisible(authentication);
        userList.setManaged(authentication);

        if (!authentication){
            nickname = "";
        }
        setTitle(nickname);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(()->{
            stage = (Stage) textArea.getScene().getWindow();

            //закрытие соединения при нажатие на крестик
            stage.setOnCloseRequest(event -> {
                if(socket != null && !socket.isClosed()){
                    try {
                        outMsg.writeUTF("/exit");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        });
        setAuthentication(false);
    }

    private void connect(){
        try {
            socket = new Socket(SERVER_ADDRESS, PORT);
            System.out.println("User connected.");

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
                                outMsg.writeUTF("/exit");
                                System.out.println("User disconnect.");
                                break;
                            }
                            if(str.startsWith("/auth_ok")){
                                nickname =str.split("\\s+")[1];
                                login = str.split("\\s")[2];
                                setAuthentication(true);
                                textArea.clear();
                                break;
                            }
                            if(str.startsWith("/reg_ok")){
                                regController.resultReg("/reg_ok");
                            }
                            if(str.startsWith("/reg_no")){
                                regController.resultReg("/reg_no");
                            }
                        } else {
                            textArea.appendText(str + "\n");
                        }
                    }

                    textArea.appendText(historySave.readHistory(login));

                    //цикл чата.
                    while (authentication) {
                        String str = inputMsg.readUTF();

                        if (str.startsWith("/")) {
                            if (str.equals("/exit")) {
                                setAuthentication(false);
                                System.out.println("User disconnect.");
                                textArea.clear();
                                loginField.clear();
                                break;
                            }
                            //Обновление списака пользователей.
                            if(str.startsWith("/userList")){
                                String[] token = str.split("\\s+");
                                Platform.runLater(()->{
                                    userList.getItems().clear();
                                    for (int i = 1; i < token.length; i++){
                                        userList.getItems().add(token[i]);
                                    }
                                });
                            }
                            if (str.startsWith("/newNick ")){
                                nickname = str.split(" ")[1];
                                setTitle(nickname);
                            }
                        }else {
                            historySave.writeHistory(str, login);
                            textArea.appendText(str + "\n");
                        }


                    }
                }catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    ///
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

    //выбор пользователя для отправки личных сообщений.
    public void privateMsg(MouseEvent mouseEvent) {
        String recepient = userList.getSelectionModel().getSelectedItem();
        textField.setText("private mess to [" + recepient + "] ");
    }

    private void createRegWindow(){
        try{
            FXMLLoader fl = new FXMLLoader(getClass().getResource("/fxml/reg.fxml"));
            Parent root = fl.load();
            regStage = new Stage();
            regStage.setTitle("Registration");
            regStage.setScene(new Scene(root, 400, 300));

            regStage.initModality(Modality.APPLICATION_MODAL);
            regStage.initStyle(StageStyle.UTILITY);

            regController = fl.getController();
            regController.setController(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void pressRegistration(ActionEvent actionEvent) {
        if(regStage == null){
            createRegWindow();
        }
        Platform.runLater(()-> {
            regStage.show();
        });

    }

    public void registration(String login, String password, String nickname){
        if (socket == null || socket.isClosed()){
            connect();
        }
        String msg = String.format("/reg %s %s %s", login, password, nickname);
        try {
            outMsg.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
