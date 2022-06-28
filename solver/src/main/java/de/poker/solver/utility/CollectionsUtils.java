package de.poker.solver.utility;

import java.util.Collection;

public class CollectionsUtils {
    public static <T> boolean isNotEmpty(Collection<T> list) {
        return list != null && !list.isEmpty();
    }
}
