package com.geoassistant.scenariogen;

import org.semanticweb.owlapi.model.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Queue;

public class OntologyPermuter extends Ontology {
    // reserved TBox class names
    private String unknownClassName = "Unknown";
    private String nonPermutable = "NonPermutable";

    // path for maude outputs
    private String outputPath = "src/maude/combinations/proto-scenario";
    private boolean printInconsistent = false;


    private int combinationCounter = 0;
    private Set<Set<OWLClassAssertionAxiom>> combinations = new HashSet<>();
    private Set<OWLClassAssertionAxiom> combination = new HashSet<>();
    private Set<OWLClass> classCombination = new HashSet<>();

    public OntologyPermuter() {
    }

    public OntologyPermuter(String reservedClassName) {
        setReservedClassName(reservedClassName);
    }

    public void setPrintInconsistent(boolean printInconsistent) {
        this.printInconsistent = printInconsistent;
    }

    public void setReservedClassName(String reservedName) {
        this.unknownClassName = reservedName;
    }

    public void setOutputPath(String path) {
        outputPath = path;
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
        OntologyUtils.printClassAssertions(ontology);
        System.out.println("***");
        OWLClass thing = factory.getOWLThing();

        //MaudeSerializer.serializeTbox(ontology, reasoner, thing);
        //System.exit(0);

        Set<OWLClassAssertionAxiom> unknownSet = getClassAssertionAxioms(unknownClassName);
        OWLClassAssertionAxiom[] unknownArr = unknownSet.toArray(new OWLClassAssertionAxiom[unknownSet.size()]);
        ArrayList<OWLClassAssertionAxiom> permutables = new ArrayList<>(Arrays.asList(unknownArr));


        if (!permutables.isEmpty()) {
            permute(permutables, thing);
        }
    }

    /**
     * Intermediary permuting call. Not really necessary?
     * Needed to add children of owl:Thing! Otherwise must check for owl:Thing in recursive call (not allowed to
     * add class assertion axiom with owl:Thing to ontology during run).
     *
     * @param permutables
     * @throws Exception
     */
    public void permute(ArrayList<OWLClassAssertionAxiom> permutables, OWLClass root) throws Exception {
    //public boolean permute(ArrayList<OWLClassAssertionAxiom> permutables, OWLClass root) throws Exception {
        Queue<OWLClass> queue = new LinkedList<>();

        OWLClassAssertionAxiom unknownCAA = permutables.get(0);
        permutables = Utils.copyWithoutFirstElement(permutables);
        OWLIndividual unknown = unknownCAA.getIndividual();

        // the unknown class assertion axiom no longer needed
        manager.removeAxiom(ontology, unknownCAA);

        // need to handle owl:Thing (root) here, because it cannot be added to the ABox without getitng an
        // exceptoin. the alternative is to check in every recursive call below if the node is owl:Thing

        for (OWLClass c : reasoner.getSubClasses(root, true).getFlattened()) {
            if (!c.getIRI().getShortForm().equals(unknownClassName) && !c.getIRI().getShortForm().equals(nonPermutable)) {
                queue.add(c);
            }
        }

        bfs(permutables, queue, unknown);
    }

    /**
     * Recursive breadth-first traversal of the class hierarchy for an individual.
     *
     * The traversal code is inspired by https://www.techiedelight.com/breadth-first-search/
     * @param queue
     * @throws Exception
     */
    private void bfs(ArrayList<OWLClassAssertionAxiom> permutables, Queue<OWLClass> queue, OWLIndividual unknown) throws Exception {
        // when the breadth-first traversal is finished - also checked for consistency
        if (queue.isEmpty() && permutables.isEmpty()) {
            // end of one combination when there are more nodes in the queue
            if (!Utils.subsetOf(combination, combinations)) {
                System.out.print(++combinationCounter + " ");
                OntologyUtils.prettyprint(combination);

                //System.out.println(MaudeSerializer.serializeAbox(ontology, reasoner));
                MaudeSerializer.writeFile(ontology, reasoner, outputPath + combinationCounter + ".maude");
                //MaudeSerializer.serializeAbox(ontology);

                Set<OWLClassAssertionAxiom> result = new HashSet<>(combination);
                combinations.add(result);
            }
            return;
        } else if (queue.isEmpty() && !permutables.isEmpty()) {
            permute(permutables, factory.getOWLThing());
            return;
        }

        // pop front node from queue and print it
        OWLClass currentClass = queue.poll();
        Queue<OWLClass> queueCopy = new LinkedList<>(queue);

        // do something with current node
        // want to do it first with the current node so that the added combinations are maximal
        OWLClassAssertionAxiom ax = factory.getOWLClassAssertionAxiom(currentClass, unknown);
        boolean containedAxiom = ontology.containsAxiom(ax);

        if (!containedAxiom) {
            // update ABox
            manager.addAxiom(ontology, ax);
            reasoner.flush();
        }

        // continue traversing the hierarchy with the current node
        // (cannot be inside of the previous if in case the class was already assigned)
        if (reasoner.isConsistent()) {
            // add current node to current combination
            combination.add(ax);
            classCombination.add(ax.getClassExpression().asOWLClass());

            // add children of current node
            for (OWLClass c : reasoner.getSubClasses(currentClass, true).getFlattened()) {
                // skip adding owl:Nothing - don't care about leaf nodes
                if (!c.isOWLNothing()) {
                    queue.add(c);
                }
            }

            bfs(permutables, queue, unknown);

            // remove the current node from the current combination (for traversal without the node)
            combination.remove(ax);
            classCombination.remove(ax.getClassExpression().asOWLClass());
        }

        if (!containedAxiom) {
            // update ABox
            manager.removeAxiom(ontology, ax);
            reasoner.flush();
        }

        // continue traversing the hierarchy without the current node
        bfs(permutables, queueCopy, unknown);
    }
}
