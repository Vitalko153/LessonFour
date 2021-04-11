package server;

import java.util.ArrayList;
import java.util.List;

public class SimpleAuthService implements AuthService{
    private class UserData{
        String login;
        String password;
        String nickname;


        public UserData(String login, String password, String nickname) {
            this.login = login;
            this.password = password;
            this.nickname = nickname;
        }
    }

    //создаем список юзеров.
    private List<UserData> userList;

    public SimpleAuthService(){
        userList = new ArrayList<>();
        userList.add(new UserData("qwe", "qwe", "qwe"));
        userList.add(new UserData("asd", "asd", "asd"));
        userList.add(new UserData("zxc", "zxc", "zxc"));
    }


    @Override
    public String getNicknameLogAndPass(String login, String password) {
        for (UserData u: userList){
            if(u.login.equals(login) && u.password.equals(password)){
                return u.nickname;
            }
        }
        return null;
    }

}
