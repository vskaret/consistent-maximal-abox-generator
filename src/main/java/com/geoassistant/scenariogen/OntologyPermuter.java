package com.geoassistant.scenariogen;

import com.sun.xml.internal.messaging.saaj.packaging.mime.util.QEncoderStream;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;

import java.lang.reflect.Array;
import java.util.*;

public class OntologyPermuter extends Ontology {
    private final String unknownClassName = "Permutable";
    private boolean printInconsistent = false;

    // variables used in the recursive call
    private List<OWLClassExpression> currentUnknownClasses;
    private List<OWLClassExpression> currentSubClasses;
    private Set<OWLClassExpression> domains;
    private Set<OWLClassExpression> ranges;

    private Set<OWLClass> discovered = new HashSet<>();  // hashset has O(1) complexity for .contains()
    private Set<Set<OWLClass>> combinations = new HashSet<>();
    private Set<OWLClass> combination = new HashSet<>();
    private Queue<OWLClass> queue = new LinkedList<>();
    private OWLIndividual unknown;

    public OntologyPermuter() {
    }

    public void setPrintInconsistent(boolean printInconsistent) {
        this.printInconsistent = printInconsistent;
    }

    /**
     * Initial call to start the permutation process. This process generates new ontologies based on the
     * rules in the ontology. More documentation should be available somewhere.
     *
     * The ontology needs to follow as certain "style(?)" to be able to use this permutation method.
     * @throws Exception
     */
    public void permute() throws Exception {
        System.out.println("initial ontology");
        printClassAssertions();
        //this.currentUnknownClasses = new ArrayList<OWLClassExpression>();

        Set<OWLClassAssertionAxiom> unknownSet = getClassAssertionAxioms(unknownClassName);
        //Set<OWLClassExpression> classExpressionSet = getClassExpressions(unknownClassName);
        OWLClassAssertionAxiom[] unknownArr = unknownSet.toArray(new OWLClassAssertionAxiom[unknownSet.size()]);
        ArrayList<OWLClassAssertionAxiom> permutables = new ArrayList<>(Arrays.asList(unknownArr));

        ArrayList<List<String>> a = new ArrayList<>();
        Utils.permuteList(a);

        ArrayList<ArrayList<OWLClassAssertionAxiom>> permutablesPermutations = Utils.permuteList(permutables);
        OWLClass thing = factory.getOWLThing();

        // permute each permutation of the individual ordering
        for (ArrayList<OWLClassAssertionAxiom> permutablePermutation : permutablesPermutations) {
            permute(permutablePermutation, thing);
        }
    }

    /**
     * Intermediary permuting call. Not really necessary?
     *
     * @param permutables
     * @throws Exception
     */
    public boolean permute(ArrayList<OWLClassAssertionAxiom> permutables, OWLClass root) throws Exception {
        Queue<OWLClass> queue = new LinkedList<>();
        Set<OWLClass> combination = new HashSet<>();
        discovered.add(root);
        queue.add(root);

        OWLClassAssertionAxiom unknownCAA = permutables.get(0);
        unknown = unknownCAA.getIndividual();
        //ArrayList<OWLClassAssertionAxiom> restOfPermutables = Utils.copyWithoutFirstElement(permutables);

        // the unknown class assertion axiom no longer needed
        manager.removeAxiom(ontology, unknownCAA);

        bfs(permutables, queue);

        return false;
    }

    private void bfs(ArrayList<OWLClassAssertionAxiom> permutables, Queue<OWLClass> queue) throws Exception {
        if (queue.isEmpty()) {
            return;
        }

        // pop front node from queue and print it
        OWLClass currentClass = queue.poll();
        //System.out.println(v + " ");

        Queue<OWLClass> queueCopy = new LinkedList<>(queue);



        // do something with current node
        // want to do it first with the current node so that the added combinations are maximal
        OWLAxiom ax = factory.getOWLClassAssertionAxiom(currentClass, unknown);
        boolean consistent = false;

        if (!ontology.containsAxiom(ax) && !currentClass.isOWLNothing() && !currentClass.isOWLThing()) {
            manager.addAxiom(ontology, ax);
            reasoner.flush();

            if (!reasoner.isConsistent()) {
                // remove added axiom when inconsistent
                manager.removeAxiom(ontology, ax);
                reasoner.flush();
            } else {
                consistent = true;
                combination.add(currentClass);
            }

        }

        if (queue.isEmpty()) {
            // end of one combination when there are more nodes in the queue
            if (!Utils.subsetOf(combination, combinations)) {
                System.out.print("end of one combination");
                System.out.println(combination);
                Set<OWLClass> result = new HashSet<>(combination);
                combinations.add(result);
            }
        }

        if (consistent) {
            // do for every edge (v -> u) with current node
            for (OWLClass u : reasoner.getSubClasses(currentClass, true).getFlattened()) {
                queue.add(u);
            }

            bfs(permutables, queue);

            combination.remove(currentClass);
            manager.removeAxiom(ontology, ax);
            reasoner.flush();
        }

        // do for every edge (v -> u) without current node
        for (OWLClass u : reasoner.getSubClasses(currentClass, true).getFlattened()) {
            queueCopy.add(u);
        }

        bfs(permutables, queueCopy);

    }

    /**
     * Main permutation method call.
     *
     * The properties of the ontology is used to generate the permutations. A property is a binary relation.
     *
     * If there is an unknown individual (unknown meaning it can have more values or something),
     * all classes the unknown can be is found and then all properties where these classes are a part of the
     * domain and the range is used to add new axioms to the ontology.
     *
     * Example (correct? should be some more detail to it):
     * Individuals: A (unknown), B
     * Properties: above(X, Y)
     *
     * --> above(A, B)
     * --> above(B, A)
     *
     * ..or something like this.
     *
     * @param permutables
     * @throws Exception
     */

    //public void permute(ArrayList<OWLClassAssertionAxiom> permutables, OWLClass currentClass, Stack<OWLClass> treeStack) throws Exception {
    public boolean permute(ArrayList<OWLClassAssertionAxiom> permutables, OWLIndividual unknown, OWLClassExpression currentClass, Stack<OWLClassExpression> treeStack) throws Exception {

        return false;
    }
}
