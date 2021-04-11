package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
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
        authService = new SimpleAuthService();
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

        public void broadcastMsg(ClientHandler sender, String msg){
        for (ClientHandler c : users){
            String message = String.format("%s : %s", sender.getNickname(), msg);
            c.sendMsg(message);
        }
        }

        public void personalMsg(ClientHandler sender, String recipient, String msg){
        String personalMsg = String.format("%s to %s --> %s ", sender.getNickname(), recipient, msg);
            for (ClientHandler c : users){
                if(c.getNickname().equals(recipient)){
                    c.sendMsg(personalMsg);
                    sender.sendMsg(personalMsg);
                }
            }
        }

        public void subscribe(ClientHandler clientHandler){
        users.add(clientHandler);
        }

        public void unsubscrebe(ClientHandler clientHandler){
        users.remove(clientHandler);
        }

    public AuthService getAuthService() {
        return authService;
    }
}

