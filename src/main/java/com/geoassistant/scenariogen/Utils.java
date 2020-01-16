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
     * @param list a list of lists to be permuted, i.e. [[a, b], [c, d], [f, g]]
     * @return all permutations of the list given, i.e [[a, c, f], [a, c, g], ... , [b, d, g]]
     */
    public static <T> List<List<T>> permuteList(ArrayList<List<T>> list) {
        List<List<T>> permutations = new ArrayList<>();
        permuteList(list, permutations, new ArrayList<T>());
        return permutations;
    }

    /**
     * Helper method for permuteList to create permutations. The permutations are stored in the permutations parameter.
     *
     * @param list rest of the list to be permuted
     * @param permutations all the permutations
     * @param permutation permutation of the current branch
     */
    private static <T> void permuteList(ArrayList<List<T>> list, List<List<T>> permutations, ArrayList<T> permutation) {
        if (list.isEmpty()) {
            permutations.add(new ArrayList<T>(permutation));
            return;
        }

        ArrayList<List<T>> l = new ArrayList<>(list);
        List<T> firstList = l.remove(0);
        ArrayList<T> p = new ArrayList<>(permutation);

        for (T o : firstList) {
            p.add(o);
            permuteList(l, permutations, p);
            p.remove(permutation.size());
        }
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
