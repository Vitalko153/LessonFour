package server;

import java.sql.*;


public class DatabaseAuthService implements AuthService {
    private static Connection connection;
    private static PreparedStatement psAuthorisation;
    private static PreparedStatement psRegistration;
    private static PreparedStatement psChangeNickname;

    //Подключение бд.
    public static boolean isSqlConnect(){
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:main.db");
            prepareAllStmt();
            return true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static void prepareAllStmt() throws SQLException{
        psAuthorisation = connection.prepareStatement("SELECT nickname FROM user WHERE login = ? AND password = ?;");
        psRegistration = connection.prepareStatement("INSERT INTO user (login, password, nickname) VALUES (?, ?, ?);");
        psChangeNickname = connection.prepareStatement("UPDATE user SET nickname = ? WHERE nickname = ?;");
    }

    //Аунтификация
    @Override
    public String getNicknameLogAndPass(String login, String password) {
        String nick = null;
        try{
        psAuthorisation.setString(1, login);
        psAuthorisation.setString(2, password);
        ResultSet rs = psAuthorisation.executeQuery();
        if(rs.next()){
            nick = rs.getString(1);
        }
        rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return nick;
    }

    //Регистрация
    @Override
    public boolean registration(String login, String password, String nickname) {
        try {
            psRegistration.setString(1, login);
            psRegistration.setString(2, password);
            psRegistration.setString(3, nickname);
            psRegistration.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    //Смена ника.
    @Override
    public boolean changeNick(String oldNickname, String newNickname) {
        try {
            psChangeNickname.setString(1, newNickname);
            psChangeNickname.setString(2, oldNickname);
            psChangeNickname.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void disconnect(){
        try {
            psAuthorisation.close();
            psChangeNickname.close();
            psRegistration.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

