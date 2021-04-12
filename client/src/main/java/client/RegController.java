package client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class RegController {
    private Controller controller;
    @FXML
    public TextField loginField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public TextField nickNameField;
    @FXML
    private TextArea textArea;


    public void resultReg(String result){
        if(result.equals("/reg_ok")){
            clearFields();
            textArea.appendText("Registration completed. \nWelcome to chat.\n");
        }
        if(result.equals("/reg_no")){
            clearFields();
            textArea.appendText("Registration not completed. \nIncorrect login or nickname.\n");
        }
    }

    public void clearFields(){
        loginField.clear();
        passwordField.clear();
        nickNameField.clear();
        textArea.clear();
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

    @FXML
    public void registration(ActionEvent actionEvent) {
            String login = loginField.getText().trim();
            String password = passwordField.getText().trim();
            String nickname = nickNameField.getText().trim();

            controller.registration(login, password, nickname);
    }
}
