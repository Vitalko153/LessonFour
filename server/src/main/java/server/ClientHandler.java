package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataOutputStream outMsg;
    private DataInputStream inputMsg;
    private String nickname;
    private String login;

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;

            inputMsg = new DataInputStream(socket.getInputStream());
            outMsg = new DataOutputStream(socket.getOutputStream());

            Thread thRead = new Thread(() -> {
                try {
//                    socket.setSoTimeout(2000);  //таймаут на бездействие.
                    //цикл авторизации.
                    while (true) {
                        String str = inputMsg.readUTF();
                        if (str.equals("/exit")) {
                            outMsg.writeUTF("/exit");
                            System.out.println(socket.getRemoteSocketAddress() + " disconnected.");
                            throw new RuntimeException("User not logged in.");
                        }

                        if(str.startsWith("/auth")){
                            String[] token = str.split("\\s+",3);
                            if(token.length < 3){
                                continue;
                            }
                            socket.setSoTimeout(2000);
                            String newNick = server.getAuthService().getNicknameLogAndPass(token[1], token[2]);
                            if(newNick != null){
                                login = token[1];
                                if(!server.isLoginAutication(login)){
                                    nickname = newNick;
                                    sendMsg("/auth_ok " + nickname);
                                    server.subscribe(this);
                                    System.out.println("User: " + nickname + ". Socket" + socket.getRemoteSocketAddress() + " connected.");
                                    break;
                                } else {
                                    sendMsg("User is login logged in");
                                }
                            }else{
                                sendMsg("Incorrect login or password.");
                            }
                        }
                        //регистрация
                        if(str.startsWith("/reg")){
                            String[] token = str.split("\\s+", 4);
                            if(token.length < 4){
                                continue;
                            }
                            boolean b = server.getAuthService().registration(token[1], token[2],token[3]);
                            if(b){
                                sendMsg("/reg_ok");
                            }else{
                                sendMsg("/reg_no");
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
                    }catch (SocketTimeoutException e){
                    try {
                        outMsg.writeUTF("/exit");
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }catch (RuntimeException e) {
                    System.out.println(e.getMessage());
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

    public String getLogin() {
        return login;
    }
}
