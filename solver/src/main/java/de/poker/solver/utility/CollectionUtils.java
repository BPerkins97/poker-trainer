package de.poker.solver.utility;

import java.util.Collection;
import java.util.List;

public class CollectionUtils {

    private CollectionUtils() {}

    public static boolean isNotEmpty(Collection collection) {
        return collection != null && !collection.isEmpty();
    }

    public static boolean isEmpty(Collection collection) {
        return collection == null || collection.isEmpty();
    }
}
