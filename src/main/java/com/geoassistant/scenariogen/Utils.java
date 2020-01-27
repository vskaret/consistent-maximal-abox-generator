package com.geoassistant.scenariogen;

import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class Utils {

    private Utils() {
    }

    public static void prettyprint(Set<OWLClassAssertionAxiom> set) {
        for (OWLClassAssertionAxiom ax : set) {
            String className = ax.getClassExpression().asOWLClass().getIRI().getShortForm();
            String individual = ax.getIndividual().asOWLNamedIndividual().getIRI().getShortForm();

            //if (!className.equals("Permutable") && individual.equals("b")) {
            if (!className.equals("Permutable")) {
                System.out.print(className + "(" + individual + "), ");
            }
        }
        System.out.println();
    }

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
     * Takes a list and returns a list with all permutations of its elements.
     * @param list A list
     * @param <T> ??
     * @return A list of lists with all permutations of list's elements
     */
    public static <T> ArrayList<ArrayList<T>> permuteList(ArrayList<T> list) {
        ArrayList<ArrayList<T>> resultList = new ArrayList<>();
        permuteList(list, resultList, new ArrayList<T>());

        return resultList;
    }

    private static <T> void permuteList(ArrayList<T> list, ArrayList<ArrayList<T>> permutations, ArrayList<T> permutation) {
        if (list.isEmpty()) {
            permutations.add(new ArrayList<T>(permutation));
            return;
        }


        for (int i = 0; i < list.size(); i++) {
            T o = list.get(i);
            permutation.add(o);

            ArrayList<T> listCopy = new ArrayList<T>(list);
            listCopy.remove(i);

            permuteList(listCopy, permutations, permutation);

            permutation.remove(o);
        }
    }

    /**
     * Takes a list of lists and creates all possible permutations
     *
     * @param list a list of lists to be permuted, i.e. [[a, b], [c, d], [f, g]]
     * @return all permutations of the list given, i.e [[a, c, f], [a, c, g], ... , [b, d, g]]
     */
    public static <T> ArrayList<List<T>> permuteLists(ArrayList<List<T>> list) {
        ArrayList<List<T>> permutations = new ArrayList<>();
        permuteLists(list, permutations, new ArrayList<T>());
        return permutations;
    }

    /**
     * Helper method for permuteList to create permutations. The permutations are stored in the permutations parameter.
     *
     * @param list rest of the list to be permuted
     * @param permutations all the permutations
     * @param permutation permutation of the current branch
     */
    private static <T> void permuteLists(ArrayList<List<T>> list, List<List<T>> permutations, ArrayList<T> permutation) {
        if (list.isEmpty()) {
            permutations.add(new ArrayList<T>(permutation));
            return;
        }

        ArrayList<List<T>> l = new ArrayList<>(list);
        List<T> firstList = l.remove(0);
        ArrayList<T> p = new ArrayList<>(permutation);

        for (T o : firstList) {
            p.add(o);
            permuteLists(l, permutations, p);
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


    /**
     * Get all non-empty subsets of the set given as input.
     *
     * @param inputSet set with elements
     * @param <T> generic type
     * @return a set with all non-empty subsets
     */
    public static <T> Set<Set<T>> nonEmptySubsetsOf(Set<T> inputSet) {
        Set<Set<T>> result = new HashSet<>();
        Set<T> workSet = new HashSet<>(inputSet);

        for (T o : inputSet) {
            // first add each element on its own
            Set<T> singleton = new HashSet<>();
            singleton.add(o);
            workSet.remove(o);
            result.add(singleton);

            nonEmptySubsetsOf(workSet, result);
        }

        return result;
    }

    /**
     * Recursive call for nonEmptySubsetsOf
     *
     * @param inputSet set given
     * @param subsets resulting subsets
     * @param <T> generic type
     */
    private static <T> void nonEmptySubsetsOf(Set<T> inputSet, Set<Set<T>> subsets) {
        if (inputSet.isEmpty()) {
            return;
        }

        Set<T> workSet = new HashSet<>(inputSet);
        Set<Set<T>> currentSubsets = new HashSet<Set<T>>(subsets);

        // for each set in the subsets, add the current element of the inputSet and then add the rest
        for (Set<T> set : currentSubsets) {
            T o = inputSet.iterator().next();
            Set<T> newSet = new HashSet<>(set);
            workSet.remove(o);
            newSet.add(o);
            subsets.add(newSet);

            nonEmptySubsetsOf(workSet, subsets);
        }
    }
}
