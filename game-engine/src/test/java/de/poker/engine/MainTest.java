package de.poker.engine;

public class MainTest {

    public static void main(String[] args) {
        System.out.println((int)'d');
        "dsch".chars()
                .map(i -> (int)i)
                .forEach(System.out::println);
    }
}
