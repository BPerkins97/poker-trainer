package de.poker.solver.tree;

import de.poker.solver.cfr.GameTreeNode;

import java.io.FileWriter;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        GameTreeNode build = GameTreeBuilder.build(GameConfiguration.defaultConfig()).toNode();
        String finalStr = build.asString("");
        System.out.println(build.countLeafes());
        try (FileWriter fileWriter = new FileWriter("C:\\Temp\\test.txt")) {
            fileWriter.write(finalStr);
        }
    }
}
