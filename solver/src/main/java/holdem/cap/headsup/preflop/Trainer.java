package holdem.cap.headsup.preflop;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class Trainer {

    public static void main(String[] args) {
        Map<String, Node> map = new HashMap<>();
        double[] expectedValues = new double[2];
        BigInteger avgRegret = BigInteger.ZERO;
        for (int i=1;i<=10000;i++) {
            double[] regret = new double[]{0};
            double[] cfr = VanillaCFR.cfr(map, Game.randomGame(ThreadLocalRandom.current()), new double[]{1.0, 1.0}, regret);
            avgRegret = avgRegret.add(BigInteger.valueOf((long)regret[0]));
            System.out.println(avgRegret.divide(BigInteger.valueOf(i)));
            for (int j=0;j<2;j++) {
                expectedValues[j] += cfr[j];
            }
            map.forEach((k, v) -> v.updateStrategy());
        }
        System.out.println(expectedValues[0] / 10000);
    }
}
