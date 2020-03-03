package com.geoassistant.scenariogen;

import com.sun.xml.internal.messaging.saaj.packaging.mime.util.QEncoderStream;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;

import java.lang.reflect.Array;
import java.util.*;

public class OntologyPermuter extends Ontology {
    private String unknownClassName = "Unknown";
    private String nonPermutable = "NonPermutable";
    private String permutationDirectory;
    private boolean printInconsistent = false;

    // variables used in the recursive call
    private List<OWLClassExpression> currentUnknownClasses;
    private List<OWLClassExpression> currentSubClasses;
    private Set<OWLClassExpression> domains;
    private Set<OWLClassExpression> ranges;

    private Set<OWLClass> discovered = new HashSet<>();  // hashset has O(1) complexity for .contains()
    private int combinationCounter = 0;
    private Set<Set<OWLClassAssertionAxiom>> combinations = new HashSet<>();
    private Set<Set<OWLClass>> classCombinations = new HashSet<>();
    private Set<OWLClassAssertionAxiom> combination = new HashSet<>();
    private Set<OWLClass> classCombination = new HashSet<>();
    private Queue<OWLClass> queue = new LinkedList<>();
    //private OWLIndividual unknown;
    private Set<Set<String>> stringCombinations = new HashSet<>();
    private Set<String> stringCombination = new HashSet<>();

    public OntologyPermuter(String permutationDirectory) {
        this.permutationDirectory = permutationDirectory;
    }

    public OntologyPermuter(String permutationDirectory, String reservedClassName) {
        this.permutationDirectory = permutationDirectory;
        setReservedClassName(reservedClassName);
    }

    public void setPrintInconsistent(boolean printInconsistent) {
        this.printInconsistent = printInconsistent;
    }

    public void setReservedClassName(String reservedName) {
        this.unknownClassName = reservedName;
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
        OntologyUtils.printAnonymous(this.ontology);
        System.out.println("***");
        //this.currentUnknownClasses = new ArrayList<OWLClassExpression>();

        Set<OWLClassAssertionAxiom> unknownSet = getClassAssertionAxioms(unknownClassName);
        //Set<OWLClassExpression> classExpressionSet = getClassExpressions(unknownClassName);
        OWLClassAssertionAxiom[] unknownArr = unknownSet.toArray(new OWLClassAssertionAxiom[unknownSet.size()]);
        ArrayList<OWLClassAssertionAxiom> permutables = new ArrayList<>(Arrays.asList(unknownArr));

        ArrayList<List<String>> a = new ArrayList<>();

        OWLClass thing = factory.getOWLThing();

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
        Set<OWLClass> combination = new HashSet<>();

        OWLClassAssertionAxiom unknownCAA = permutables.get(0);
        permutables = Utils.copyWithoutFirstElement(permutables);
        OWLIndividual unknown = unknownCAA.getIndividual();

        // the unknown class assertion axiom no longer needed
        manager.removeAxiom(ontology, unknownCAA);

        // need to handle owl:Thing (root) here, because it cannot be added to the ontology without getitng an
        // exceptoin. the alternative is to check in every recursive call below if the node is owl:Thing

        for (OWLClass c : reasoner.getSubClasses(root, true).getFlattened()) {
            if (!c.getIRI().getShortForm().equals(unknownClassName) && !c.getIRI().getShortForm().equals(nonPermutable)) {
                queue.add(c);
            }
        }

        //System.out.println(queue + " <> " + unknown.asOWLNamedIndividual().getIRI().getShortForm());
        //System.out.println();

        //return bfs(permutables, queue, unknown);
        bfs(permutables, queue, unknown);
    }

    /**
     *
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

                Set<OWLClassAssertionAxiom> mappingAxioms = allMappings();

                // find already existing axioms:
                for (OWLClassAssertionAxiom ax : mappingAxioms) {
                    if (ontology.containsAxiom(ax)) {
                        mappingAxioms.remove(ax);
                    }
                }

                // add axioms
                combination.addAll(mappingAxioms);
                manager.addAxioms(ontology, mappingAxioms);



                //System.out.print("end of one combination");
                //System.out.println(combination);
                //System.out.print(++combinationCounter + ".\n");
                System.out.print(++combinationCounter + " ");
                //Utils.prettyprint(combination);
                Utils.leafprint(combination, this);
                //System.out.println(MaudeSerializer.serializeAbox(ontology, reasoner));
                MaudeSerializer.writeFile(ontology, reasoner, "src/maude/combinations/combination" + combinationCounter + ".maude");

                //MaudeSerializer.serializeAbox(ontology);

                String filename = this.permutationDirectory + "/permutation" + combinationCounter + ".maude";

                //MaudeSerializer.writeAboxToFile(ontology, reasoner, filename);
                //System.out.println("\n");

                Set<OWLClassAssertionAxiom> result = new HashSet<>(combination);
                combinations.add(result);

                combination.removeAll(mappingAxioms);
                manager.removeAxioms(ontology, mappingAxioms);
            }
            //return true;
            return;
        } else if (queue.isEmpty() && !permutables.isEmpty()) {
            permute(permutables, factory.getOWLThing());
            return;
            //return permute(permutables, factory.getOWLThing());
        }

        // pop front node from queue and print it
        OWLClass currentClass = queue.poll();

        Queue<OWLClass> queueCopy = new LinkedList<>(queue);

        //System.out.println(queue + " | " + unknown.asOWLNamedIndividual().getIRI().getShortForm() + " | " + permutables);
        //System.out.println(queueCopy + " | " + unknown.asOWLNamedIndividual().getIRI().getShortForm() + " | " + permutables);



        // do something with current node
        // want to do it first with the current node so that the added combinations are maximal
        OWLClassAssertionAxiom ax = factory.getOWLClassAssertionAxiom(currentClass, unknown);
        boolean containedAxiom = ontology.containsAxiom(ax);


        //if (!ontology.containsAxiom(ax) && !currentClass.isOWLThing()) {
        if (!containedAxiom) {
            manager.addAxiom(ontology, ax);
            reasoner.flush();
        }


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

            // continue traversal WITH the current node in the combination
            /*if (!bfs(permutables, queue, unknown)) {
                return false;
            } else {
            }*/


            // remove the current node from the current combination (for traversal without the node)
            combination.remove(ax);
            classCombination.remove(ax.getClassExpression().asOWLClass());

            // also remove the current node/axiom from the ontology - this is done both if the ontology is
            // consistent with the current node and if not, because the traversal with the current node
            // is done inside of the if above
        } //else

        if (!containedAxiom) {
            manager.removeAxiom(ontology, ax);
            reasoner.flush();
        }

        //System.out.println(queue + " || " + unknown.asOWLNamedIndividual().getIRI().getShortForm() + " || " + permutables);
        //System.out.println(queueCopy + " || " + unknown.asOWLNamedIndividual().getIRI().getShortForm() + " || " + permutables);

        // the first element never arrives here - it returns inside the for loop probably
        // continue traversal WITHOUT the current node in the combination

        //return bfs(permutables, queueCopy, unknown);
        bfs(permutables, queueCopy, unknown);

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
