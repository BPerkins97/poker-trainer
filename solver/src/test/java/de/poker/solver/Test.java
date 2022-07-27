package de.poker.solver;

import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;

public class Test {

    public static void main(String[] args) {
        ChronicleMap<CharSequence, PostalCodeRange> cityPostalCodes = ChronicleMapBuilder
                .of(CharSequence.class, PostalCodeRange.class)
                .name("city-postal-codes-map")
                .averageKey("Amsterdam")
                .entries(50_000)
                .create();
    }

    interface PostalCodeRange {
        int minCode();
        void minCode(int minCode);

        int maxCode();
        void maxCode(int maxCode);
    }
}
