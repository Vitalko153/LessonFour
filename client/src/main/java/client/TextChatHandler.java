package client;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

public class TextChatHandler {

    public void writeHistory(String history, String login) throws IOException {
        FileWriter writer = new FileWriter("client/src/main/java/client/history/history_[" + login + "].txt", true);
        writer.write(history + "\n");
        writer.close();
    }


    public String readHistory(String login) throws IOException {
        if(!Files.exists(Paths.get("client/src/main/java/client/history/history_[" + login + "].txt"))){
            return "";
        }
        StringBuilder sb =  new StringBuilder();
        List<String> readHistory =  Files.readAllLines(Paths.get("client/src/main/java/client/history/history_[" + login + "].txt"));
        int startPosition = 0;
        if(readHistory.size()>100){
            startPosition = readHistory.size() - 100;
        }
        for (int i = startPosition; i < readHistory.size(); i++) {
            sb.append(readHistory.get(i)).append("\n");
        }
        return sb.toString();
    }
}


