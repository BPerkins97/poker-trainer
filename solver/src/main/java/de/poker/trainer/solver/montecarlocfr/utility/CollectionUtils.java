package de.poker.trainer.solver.montecarlocfr.utility;

import java.util.Collection;

public class CollectionUtils {

    private CollectionUtils() {}

    public static boolean isNotEmpty(Collection collection) {
        return collection != null && !collection.isEmpty();
    }
}
