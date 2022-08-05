package kuhn;

public class Solver {
    public static void main(String[] args) {
        KuhnCFR k = new KuhnCFR(10000000, 3);
        k.cfrIterationsExternal();
        k.nodes.forEach((key, value) -> {
            double[] strategy = value.getAverageStrategy();
            System.out.println(String.format("%s\t\t%.2f\t\t%.2f", rightPad(key, 7), strategy[0], strategy[1]));
        });
    }

    public static String rightPad(String value, int length) {
        for (int i=value.length();i<length;i++) {
            value += " ";
        }
        return value;
    }
}
