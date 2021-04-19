package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {

    private static final int PORT = 8189;

    private static Socket socket;
    private static ServerSocket server;

    private List<ClientHandler> users;
    private AuthService authService;


    public Server() {
        users = new CopyOnWriteArrayList<>();
        if(!DatabaseAuthService.isSqlConnect()){
            throw new RuntimeException("Not connect to Database.");
        }
        authService = new DatabaseAuthService();

        try {
            server = new ServerSocket(PORT);
            System.out.println("Server started.");

            while (true) {
                socket = server.accept();
                new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
            }finally {
            DatabaseAuthService.disconnect();
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        }

        //Отправка сообщения всем.
        public void broadcastMsg(ClientHandler sender, String msg){
        for (ClientHandler c : users){
            String message = String.format("%s : %s", sender.getNickname(), msg);
            c.sendMsg(message);
        }
        }

        //Приватные сообщения
        public void personalMsg(ClientHandler sender, String recipient, String msg){
        String personalMsg = String.format("%s to %s --> %s ", sender.getNickname(), recipient, msg);
            for (ClientHandler c : users){
                if(c.getNickname().equals(recipient)){
                    c.sendMsg(personalMsg);
                    if(sender.equals(c)){
                        return;
                    }
                    sender.sendMsg(msg);
                    return;
                }
            }
            sender.sendMsg("User " + recipient + " not found");
        }

        public void subscribe(ClientHandler clientHandler){
        users.add(clientHandler);
        userList();
        }

        public void unsubscrebe(ClientHandler clientHandler){
        users.remove(clientHandler);
        userList();
        }

    public AuthService getAuthService() {
        return authService;
    }


    public boolean isLoginAuthication(String login){
        for (ClientHandler c : users){
            if(c.getLogin().equals(login)){
                return true;
            }
        }
        return false;
    }


    public void userList(){
        StringBuilder sb = new StringBuilder("/userList");
        for (ClientHandler c : users){
            sb.append(" ").append(c.getNickname());
        }

        String msg = sb.toString();
        for (ClientHandler c : users){
            c.sendMsg(msg);
        }
    }
}

