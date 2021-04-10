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

    public Server() {
        users = new CopyOnWriteArrayList<>();
        try {
            server = new ServerSocket(PORT);
            System.out.println("Server started.");

            while (true) {
                socket = server.accept();
                System.out.println("User" + socket.getRemoteSocketAddress() + " connect");
                subscribe(new ClientHandler(this, socket));
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

        public void broadcastMsg(String msg){
        for (ClientHandler c : users){
            c.sendMsg(msg);
        }
        }

        public void subscribe(ClientHandler clientHandler){
        users.add(clientHandler);
        }

        public void unsubscrebe(ClientHandler clientHandler){
        users.remove(clientHandler);
        }

    }


//            Scanner sc = new Scanner(System.in);
//
//            Thread threadRead = new Thread(() -> {
//                try {
//                    while (true) {
//                        outMsg.writeUTF(sc.nextLine());
//                    }
//                } catch (IOException ioException) {
//                    ioException.printStackTrace();
//                }
//            });

