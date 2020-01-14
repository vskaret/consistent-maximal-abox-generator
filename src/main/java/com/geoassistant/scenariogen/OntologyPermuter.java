package com.geoassistant.scenariogen;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.Node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class OntologyPermuter extends Ontology {
    private final String unknownClassName = "Unknown";

    // are all of these needed?
    private ArrayList<OWLClassAssertionAxiom> individuals = new ArrayList<>();

    // variables used in the recursive call
    private List<OWLClassExpression> currentUnknownClasses;
    private List<OWLClassExpression> currentSubClasses;
    private Set<OWLClassExpression> domains;
    private Set<OWLClassExpression> ranges;
    // domain
    // property
    // unknown
    //private OWLIndividual unknown;
    // unknowns
    //ArrayList<OWLClassAssertionAxiom> unknowns;


    public OntologyPermuter(boolean debug) {
        super(debug);
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
        // all individuals with asserted classes in the ontology?
        this.individuals = allClassAssertionAxioms();
        //this.currentUnknownClasses = new ArrayList<OWLClassExpression>();

        Set<OWLClassAssertionAxiom> unknownSet = getClassAssertionAxioms(unknownClassName);
        OWLClassAssertionAxiom[] unknownArr = unknownSet.toArray(new OWLClassAssertionAxiom[unknownSet.size()]);
        ArrayList<OWLClassAssertionAxiom> unknowns = new ArrayList<>(Arrays.asList(unknownArr));

        permute(unknowns);
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
     * @param unknowns
     * @throws Exception
     */

    public void permute(ArrayList<OWLClassAssertionAxiom> unknowns) throws Exception {
        // update reasoner
        reasoner.flush();

        // stop when inconsistent
        if (!reasoner.isConsistent()) {
            if (DEBUG) {
                System.out.println("inconsistent ontology");
                printClassAssertions();
            }
            return;
        }

        // no more unknown assertion axioms, consistent ontology generated!
        if (unknowns.size() == 0) {
            System.out.println("consistent ontology generated");
            //printClassAssertions();
            return;
        }

        ArrayList<OWLClassAssertionAxiom> restOfUnknowns = copyWithoutFirstElement(unknowns);

        OWLClassAssertionAxiom currentUnknownClassAxiom = unknowns.get(0);

        // the unknown class assertion axiom no longer needed
        manager.removeAxiom(ontology, currentUnknownClassAxiom);

        //OWLClassAssertionAxiom unknownAssertion = unknown
        OWLIndividual unknown = currentUnknownClassAxiom.getIndividual();
        Set<OWLClassAssertionAxiom> unknownClasses = ontology.getClassAssertionAxioms(unknown);
        System.out.println(unknownClasses);
        System.out.println(currentUnknownClasses);

        // find all unknown classes the current unknown can be
        // is it assumed that the unknown can only be its own class or superclasses?
        this.currentUnknownClasses = legalClasses(
                currentUnknownClassAxiom, unknownClasses
        );


        this.currentSubClasses = subClassesOf(currentUnknownClasses);
        List<OWLAxiom> subclassAxioms = generateClassAssertionAxioms(currentSubClasses, currentUnknownClassAxiom);  // TODO : vær konsistent i bruken av unknown
        // can create the class assertion axioms already here?

        if (DEBUG) {
            System.out.println("Start subclasses");
            for (OWLClassExpression c : currentUnknownClasses) {
                System.out.println(c);
            }
            System.out.println("End subclasses");
        }

        for (OWLAxiom classAxiom : subclassAxioms) {
            if (!ontology.containsAxiom(classAxiom)) {
                manager.addAxiom(ontology, classAxiom);
                permute(restOfUnknowns);
                manager.removeAxiom(ontology, classAxiom);
            }
        }

    }


    /**
     * Returns a list with the range and all its sub-classes.
     *
     * @param ce
     * @return A list with all sub-classes of ce (including the class expression given as a parameter).
     */
    private List<OWLClassExpression> allSubClasses(OWLClassExpression ce) {
        List<OWLClassExpression> allSubClasses = new ArrayList<>();
        allSubClasses.add(ce);

        for (OWLSubClassOfAxiom sub : ontology.getSubClassAxiomsForSuperClass(ce.asOWLClass())) {
            allSubClasses.add(sub.getSubClass());
        }

        return allSubClasses;
    }

    // TODO : hva er forskjellen på denne og den over?
    /**
     * Finds all legal subclasses for all of the classes in the given parameter.
     * What is a legal subclass?
     *
     * @param unknownClasses A list containing OWL Classes
     * @return List containing OWL (sub)Classes
     */
    private List<OWLClassExpression> subClassesOf(List<OWLClassExpression> unknownClasses) {
        ArrayList<OWLClassExpression> allSubClasses = new ArrayList<>();

        for (OWLClassExpression ce : unknownClasses) {
            for (Node<OWLClass> classNode : reasoner.getSubClasses(ce, false)) {
                OWLClassExpression cAdd = classNode.getRepresentativeElement();
                if (!(cAdd.isOWLNothing() || cAdd.isOWLThing())) {
                    allSubClasses.add(cAdd);
                }
            }
        }

        return allSubClasses;
    }

    /**
     * All class assertion axioms in the ontology. In other words, all the individuals with an asserted
     * class?
     *
     * @return
     */
    private ArrayList<OWLClassAssertionAxiom> allClassAssertionAxioms() {
        ArrayList<OWLClassAssertionAxiom> classAxioms = new ArrayList<>();

        for (OWLClass ce : ontology.getClassesInSignature()) {
            for (OWLClassAssertionAxiom ca : ontology.getClassAssertionAxioms(ce)) {
                classAxioms.add(ca);
            }
        }

        return classAxioms;
    }



    /**
     * Compares all class assertion axioms with the class assertion axiom to see if they are equal.
     * This is a way to make sure all the different assertion axioms that can be used are added.
     *
     * Make a hashmap with (k, v) : (OWLClassAssertionAxiom, List<OWLClassAssertionAxiom>)?
     *
     * This is not necessary when using the reasoner, because then it will find all of the equal classes,
     * sub-classes, etc.
     *
     * @param classAssertionAxioms
     * @return
     */
    private List<OWLClassExpression> legalClasses(OWLClassAssertionAxiom classAssertionAxiom, Set<OWLClassAssertionAxiom> classAssertionAxioms) {
        List<OWLClassExpression> legalClasses = new ArrayList<>();

        for (OWLClassAssertionAxiom ca : classAssertionAxioms) {
            if (!classAssertionAxiom.equals(ca)) {
                OWLClassExpression c = ca.getClassExpression();
                legalClasses.add(c);
            }
        }

        return legalClasses;
    }

}
