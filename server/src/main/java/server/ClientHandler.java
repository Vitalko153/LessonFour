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

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;

            inputMsg = new DataInputStream(socket.getInputStream());
            outMsg = new DataOutputStream(socket.getOutputStream());

            Thread thRead = new Thread(() -> {
                try {
                while (true) {
                    String str = inputMsg.readUTF();
                    if (str.equals("/exit")) {
                        outMsg.writeUTF("/exit");
                        System.out.println("User" + socket.getRemoteSocketAddress() + " disconnect.");
                        break;
                    }
                    server.broadcastMsg(str);
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
}
