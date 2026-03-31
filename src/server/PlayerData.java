package server;
import java.io.*;

public class PlayerData {
    public static void saveGameResult(String teamNames, long timeTaken) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("highscores.txt", true))) {
            bw.write("Team: " + teamNames + " | Time: " + timeTaken + "s");
            bw.newLine();
        } catch (IOException e) { e.printStackTrace(); }
    }
}