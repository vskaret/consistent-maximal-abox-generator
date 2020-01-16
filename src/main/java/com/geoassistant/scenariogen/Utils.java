package com.geoassistant.scenariogen;

import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;

import java.util.ArrayList;
import java.util.List;

public final class Utils {

    private Utils() {
    }

    /**
     * Takes a list of lists and creates all possible permutations
     *
     * @param list a list of lists, i.e. [[a, b], [c, d], [f, g]]
     * @return all permutations of the list given, i.e [[a, c, f], [a, c, g], ... , [b, d, g]]
     */
    public static final <T> List<List<T>> permuteList(ArrayList<List<T>> list) {
        List<List<T>> permutations = new ArrayList<>();
        permuteList(list, permutations, new ArrayList<T>());
        return permutations;
    }

    /**
     * Helper method for permuteList to create permutations. The permutations are stored in the permutations parameter.
     *
     * @param list
     * @param permutations
     * @param permutation
     */
    private static final <T> void permuteList(ArrayList<List<T>> list, List<List<T>> permutations, ArrayList<T> permutation) {
        if (list.isEmpty()) {
            permutations.add((List<T>) permutation.clone());
            return;
        }

        ArrayList<List<T>> l = (ArrayList<List<T>>) list.clone();
        List<T> firstList = l.remove(0);
        ArrayList<T> p = (ArrayList<T>) permutation.clone();

        for (T o : firstList) {
            p.add(o);
            permuteList(l, permutations, p);
            p.remove(permutation.size());
        }
    }

    /**
     * Used to get restOfPermutables.
     * @param list
     * @return
     */
    public static final <T> ArrayList<T> copyWithoutFirstElement(ArrayList<T> list) {
        ArrayList<T> copy =
                (ArrayList<T>) list.clone();
        copy.remove(0);
        return copy;
    }
}
