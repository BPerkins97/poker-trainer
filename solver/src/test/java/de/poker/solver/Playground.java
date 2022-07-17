package de.poker.solver;

public class Playground {

    public static void main(String[] args) {
        int stacks = 0;
        stacks = 10000 | 10000 << 16;
        System.out.println(stacks);
        System.out.println(stacks >> 16);
        System.out.println((stacks << 16) >> 16);

        stacks = ((~0 >> 16) << 16) & stacks | 9500;
        System.out.println(stacks);
    }
}
