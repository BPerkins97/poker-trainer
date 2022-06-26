package de.poker.solver;

import java.io.FileWriter;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        Node build = Node.GameState.build(GameConfiguration.defaultConfig()).toNode();
        String finalStr = build.asString("");
        System.out.println(build.countLeafes());
        try (FileWriter fileWriter = new FileWriter("C:\\Temp\\test.txt")) {
            fileWriter.write(finalStr);
        }
    }
}
