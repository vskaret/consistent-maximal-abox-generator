package com.geoassistant.scenariogen;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;

import java.util.*;

public class OntologyPermuter extends Ontology {
    private final String unknownClassName = "Permutable";
    private boolean printInconsistent = false;

    // variables used in the recursive call
    private List<OWLClassExpression> currentUnknownClasses;
    private List<OWLClassExpression> currentSubClasses;
    private Set<OWLClassExpression> domains;
    private Set<OWLClassExpression> ranges;

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

        permute(permutables);
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

    public void permute(ArrayList<OWLClassAssertionAxiom> permutables) throws Exception {
        // update reasoner
        reasoner.flush();

        // stop when inconsistent
        if (!reasoner.isConsistent()) {
            if (printInconsistent) {
                System.out.println("inconsistent ontology");
                printClassAssertions();
            }
            return;
        }

        // no more unknown assertion axioms, consistent ontology generated!
        if (permutables.size() == 0) {
            System.out.println("consistent ontology generated");
            printClassAssertions();
            return;
        }

        OWLClassAssertionAxiom currentUnknownClassAxiom = permutables.get(0);
        ArrayList<OWLClassAssertionAxiom> restOfPermutables = Utils.copyWithoutFirstElement(permutables);
        OWLIndividual unknown = currentUnknownClassAxiom.getIndividual();

        // the unknown class assertion axiom no longer needed
        manager.removeAxiom(ontology, currentUnknownClassAxiom);

        // all class assertions for the individual unknown
        Set<OWLClassAssertionAxiom> unknownClassAssertions = ontology.getClassAssertionAxioms(unknown);
        OWLClassExpression gcc = greatestCommonClass(classAssertionsToClassExpressions(unknownClassAssertions));
        //System.out.print("gcc is ");
        //System.out.println(gcc);

        ArrayList<List<OWLAxiom>> axiomLists = new ArrayList<>();
        List<ArrayList<OWLClassExpression>> listOfListOfLeaves = allLeafSubClasses(gcc);

        for (List<OWLClassExpression> listOfLeaves : listOfListOfLeaves) {
            axiomLists.add(generateClassAssertionAxioms(listOfLeaves, unknown));
        }

        //System.out.println(axiomLists);
        List<List<OWLAxiom>> axiomPermutations = Utils.permuteList(axiomLists);

        // permute with the current permutation of class expressions asserted to the individual
        for (List<OWLAxiom> permutation : axiomPermutations) {
            Set<OWLAxiom> newAxioms = removeAxiomsAlreadyInTheOntology(permutation);
            manager.addAxioms(ontology, newAxioms);
            permute(restOfPermutables);
            manager.removeAxioms(ontology, newAxioms);
        }
    }
}
