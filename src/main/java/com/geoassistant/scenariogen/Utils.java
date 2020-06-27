package com.geoassistant.scenariogen;

import java.util.ArrayList;
import java.util.Set;

public final class Utils {
    /**
     * Checks if a set is a sub of a set of sets.
     *
     * Any better way of doing this? mainSet.contains(s) will not work because it will check if it
     * contains the whole specific set. Not if the set is a subset of any of the sets in mainSet.
     *
     * @param set
     * @param mainSet
     * @param <T>
     * @return
     */
    public static <T> boolean subsetOf(Set<T> set, Set<Set<T>> mainSet) {
        if (mainSet.isEmpty()) {
            return false;
        }

        for (Set<T> s : mainSet) {
            if (s.containsAll(set)) {
                return true;
            }
        }

        return false;
    }

    /**
     * (Used to get restOfPermutables)
     *
     * @param list list to copy
     * @param <T> ??
     * @return copy of list without list's first element
     * @throws Exception TODO
     */
    public static <T> ArrayList<T> copyWithoutFirstElement(ArrayList<T> list) throws Exception {
        if (list.isEmpty()) {
            throw new Exception("can't remove from empty list");
        }

        ArrayList<T> copy = new ArrayList<>(list);
        copy.remove(0);
        return copy;
    }
}
