package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataOutputStream outMsg;
    private DataInputStream inputMsg;
    private String nickname;

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;

            inputMsg = new DataInputStream(socket.getInputStream());
            outMsg = new DataOutputStream(socket.getOutputStream());

            Thread thRead = new Thread(() -> {
                try {
                    //цикл авторизации.
                    while (true) {
                        String str = inputMsg.readUTF();
                        if (str.equals("/exit")) {
                            outMsg.writeUTF("/exit");
                            System.out.println("User" + socket.getRemoteSocketAddress() + " disconnect.");
                            break;
                        }

                        if(str.startsWith("/auth")){
                            String[] token = str.split("\\s+");
                            String newNick = server.getAuthService().getNicknameLogAndPass(token[1], token[2]);
                            if(newNick != null){
                                nickname = newNick;
                                sendMsg("/auth_ok " + nickname);
                                server.subscribe(this);
                                System.out.println("User: " + nickname + ". Socket" + socket.getRemoteSocketAddress() + " connected");
                                break;
                            }else{
                                sendMsg("Неверный логин или пароль.");
                            }
                        }
                    }
                //цикл отправки сообщений
                while (true) {
                    String str = inputMsg.readUTF();
                    if (str.startsWith("/")) {
                        if (str.equals("/exit")) {
                            outMsg.writeUTF("/exit");
                            System.out.println("User" + socket.getRemoteSocketAddress() + " disconnect.");
                            break;
                        }
                        //Отправка личных сообщений.
                        if (str.startsWith("/w")) {
                            String[] token = str.split("\\s+", 3);
                            server.personalMsg(this, token[1], token[2]);
                        }
                    }else {
                        server.broadcastMsg(this, str);
                    }
                }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }finally {
                    server.unsubscrebe(this);
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
            });
            thRead.setDaemon(true);
            thRead.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(String msg){
        try {
            outMsg.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNickname() {
        return nickname;
    }
}
