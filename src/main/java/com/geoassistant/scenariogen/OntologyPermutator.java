package com.geoassistant.scenariogen;

import org.semanticweb.owlapi.model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class OntologyPermutator extends Ontology {
    private final String unknownClassName = "Unknown";

    // are all of these needed?
    private ArrayList<OWLClassAssertionAxiom> individuals = new ArrayList<>();

    // variables used in the recursive call
    private List<OWLClassExpression> currentUnknownClasses;
    private Set<OWLClassExpression> domains;
    private Set<OWLClassExpression> ranges;
    // domain
    // property
    // unknown
    //private OWLIndividual unknown;
    // unknowns
    //ArrayList<OWLClassAssertionAxiom> unknowns;


    public OntologyPermutator() {
        super();
    }

    /**
     * Initial call to start the permutation process. This process generates new ontologies based on the
     * rules in the ontology. More documentation should be available somewhere.
     *
     * The ontology needs to follow as certain "style(?)" to be able to use this permutation method.
     * @throws Exception
     */
    public void permutate() throws Exception {
        // all individuals with asserted classes in the ontology?
        this.individuals = allClassAssertionAxioms();

        Set<OWLClassAssertionAxiom> unknownSet = getClassAssertionAxioms(unknownClassName);
        OWLClassAssertionAxiom[] unknownArr = unknownSet.toArray(new OWLClassAssertionAxiom[unknownSet.size()]);
        ArrayList<OWLClassAssertionAxiom> unknowns = new ArrayList<>(Arrays.asList(unknownArr));
        permutate(unknowns);

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
    public void permutate(ArrayList<OWLClassAssertionAxiom> unknowns) throws Exception {
        // update reasoner
        reasoner.flush();

        // stop when inconsistent
        if (!reasoner.isConsistent()) {
            return;
        }

        // no more unknown assertion axioms, consistent ontology generated!
        if (unknowns.size() == 0) {
            writeOntologyToFile();
            return;
        }

        ArrayList<OWLClassAssertionAxiom> unknownsCopy = copyWithoutFirstElement(unknowns);

        OWLClassAssertionAxiom currentUnknownClassAxiom = unknowns.get(0);
        //OWLClassAssertionAxiom unknownAssertion = unknown
        OWLIndividual unknown = currentUnknownClassAxiom.getIndividual();
        Set<OWLClassAssertionAxiom> unknownClasses = ontology.getClassAssertionAxioms(unknown);

        // find all unknown classes the current unknown can be
        this.currentUnknownClasses = legalClasses(
                currentUnknownClassAxiom, unknownClasses
        );


        // a property is a binary relation
        Set<OWLObjectProperty> properties = ontology.getObjectPropertiesInSignature();

        for (OWLObjectProperty property : properties) {
            if (!property.isBuiltIn()) {
                Set<OWLClassExpression> domains = property.getDomains(ontology);
                Set<OWLClassExpression> ranges = property.getRanges(ontology);

                // properties where unknown is in the domain
                List<OWLClassAssertionAxiom> individualsInRange = individualsInRange(
                        domains, ranges, property
                );

                // generate permutations with new properties where unknown is in the domain of the property
                for (OWLClassAssertionAxiom individual : individualsInRange) {
                    List<OWLAxiom> axioms = generatePropertyAxioms(
                            property, individual, currentUnknownClassAxiom
                    );
                    permutateWithAxioms(unknownsCopy, axioms);
                }

                // properties where unknown is in the range
                List<OWLClassAssertionAxiom> individualsInDomain = individualsInRange(
                        ranges, domains, property
                );

                // generate permutations with new properties where unknown is in the range of the property
                for (OWLClassAssertionAxiom individual : individualsInDomain) {
                    List<OWLAxiom> axioms = generatePropertyAxioms(
                            property, currentUnknownClassAxiom, individual
                    );
                    permutateWithAxioms(unknownsCopy, axioms);
                }
            }
        }
    }

    private void permutateWithAxioms(ArrayList<OWLClassAssertionAxiom> unknownsCopy, List<OWLAxiom> axioms) throws Exception {
        for (OWLAxiom axiom : axioms) {
            manager.addAxiom(ontology, axiom);
            permutate(unknownsCopy);
            manager.removeAxiom(ontology, axiom);
        }

    }


    //private void permutate(ArrayList<OWLClassAssertionAxiom> unknowns, OWLObjectProperty property, OWLClassAssertionAxiom individualInRange, OWLClassAssertionAxiom individualInDomain) throws Exception {
    private List<OWLAxiom> generatePropertyAxioms(
            OWLObjectProperty property,
            OWLClassAssertionAxiom individualInRange,
            OWLClassAssertionAxiom individualInDomain
    ) throws Exception {
        List<OWLAxiom> propertyAxioms = new ArrayList<>();
        OWLAxiom axiom = factory.getOWLObjectPropertyAssertionAxiom(
                property, individualInDomain.getIndividual(), individualInRange.getIndividual()
        );

        if (!ontology.containsAxiom(axiom)) {
            propertyAxioms.add(axiom);
            /*
            manager.addAxiom(ontology, axiom);
            ArrayList<OWLClassAssertionAxiom> unknownsCopy =
                    (ArrayList<OWLClassAssertionAxiom>) unknowns.clone();
            unknownsCopy.remove(0);
            permutate(unknownsCopy);
            manager.removeAxiom(ontology, axiom);
            */
        }
        return propertyAxioms;
    }

    private ArrayList<OWLClassAssertionAxiom> copyWithoutFirstElement(ArrayList<OWLClassAssertionAxiom> list) {
        ArrayList<OWLClassAssertionAxiom> copy =
                (ArrayList<OWLClassAssertionAxiom>) list.clone();
        copy.remove(0);
        return copy;
    }

    // classes can be domains or ranges
    //public List<OWLAxiom> newAxioms(Set<OWLClassExpression> classExpressions, OWLObjectProperty property) {
    private List<OWLClassAssertionAxiom> individualsInRange(Set<OWLClassExpression> domains, Set<OWLClassExpression> ranges, OWLObjectProperty property) {
        //List<OWLAxiom> newAxioms = new ArrayList<>();
        List<OWLClassAssertionAxiom> individualsInRange = new ArrayList<>();

        // domains: all possible classes in the left side of the property
        for (OWLClassExpression domain : domains) {
            // currentUnknownClasses: all legal classes for the current unknown
            for (OWLClassExpression unknownClass : currentUnknownClasses) {
                // only apply to the classes that are equal to the domain
                if (domain.equals(unknownClass)) {
                    // ranges: all possible classes on the left side of the property
                    for (OWLClassExpression range : ranges) {
                        // all legal classes for the range
                        List<OWLClassExpression> legalRanges = allSubClasses(range);

                        for (OWLClassExpression legalRange : legalRanges) {
                            for (OWLClassAssertionAxiom individual : individuals) {
                                if (individual.getClassExpression().equals(legalRange)) {
                                    individualsInRange.add(individual);
                                    //OWLAxiom axiom = factory.getOWLObjectPropertyAssertionAxiom(property, unknown, individual.getIndividual());
                                    //newAxioms.add(axiom);
                                }
                            }
                        }
                    }
                }
            }
        }

        return individualsInRange;
    }

    private List<OWLClassAssertionAxiom> generateNewAxioms() {
        ArrayList<OWLClassAssertionAxiom> axioms = new ArrayList<>();

        return axioms;
    }

    /**
     * Returns a list with the range and all its sub-classes.
     *
     * @param ce
     * @return A list with all sub-classes of ce (including the class expression given as a paremeter).
     */
    private List<OWLClassExpression> allSubClasses(OWLClassExpression ce) {
        List<OWLClassExpression> allSubClasses = new ArrayList<>();
        allSubClasses.add(ce);

        for (OWLSubClassOfAxiom sub : ontology.getSubClassAxiomsForSuperClass(ce.asOWLClass())) {
            allSubClasses.add(sub.getSubClass());
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
     * Writes the ontology to a file.
     */
    public void writeOntologyToFile() {
        if (DEBUG) {
            System.out.println("Consistent ontology generated");

            Set<OWLAxiom> axioms = ontology.getAxioms();
            for (OWLAxiom axiom : axioms) {
                if (axiom instanceof OWLObjectPropertyAssertionAxiom) {
                    System.out.println(axiom);
                }
            }
        }
    }

    /**
     * Compares all class assertion axioms with the class assertion axiom to see if they are equal.
     * This is a way to make sure all the different assertion axioms that can be used are added.
     *
     * @param classAssertionAxioms
     * @return
     */
    private List<OWLClassExpression> legalClasses(OWLClassAssertionAxiom classAssertionAxiom, Set<OWLClassAssertionAxiom> classAssertionAxioms) {
        List<OWLClassExpression> legalClasses = new ArrayList<>();

        for (OWLClassAssertionAxiom ca : classAssertionAxioms) {
            if (!classAssertionAxiom.equals(ca)) {
                OWLClassExpression c = ca.getClassExpression();
                currentUnknownClasses.add(c);
            }
        }

        return legalClasses;
    }
}
