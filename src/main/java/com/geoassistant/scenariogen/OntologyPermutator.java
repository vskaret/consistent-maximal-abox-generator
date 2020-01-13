package com.geoassistant.scenariogen;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.Node;

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
    private List<OWLClassExpression> currentSubClasses;
    private Set<OWLClassExpression> domains;
    private Set<OWLClassExpression> ranges;
    // domain
    // property
    // unknown
    //private OWLIndividual unknown;
    // unknowns
    //ArrayList<OWLClassAssertionAxiom> unknowns;


    public OntologyPermutator(boolean debug) {
        super(debug);
    }

    /**
     * Initial call to start the permutation process. This process generates new ontologies based on the
     * rules in the ontology. More documentation should be available somewhere.
     *
     * The ontology needs to follow as certain "style(?)" to be able to use this permutation method.
     * @throws Exception
     */
    public boolean permutate() throws Exception {
        System.out.println("initial ontology");
        writeOntologyToFile();
        // all individuals with asserted classes in the ontology?
        this.individuals = allClassAssertionAxioms();
        //this.currentUnknownClasses = new ArrayList<OWLClassExpression>();

        Set<OWLClassAssertionAxiom> unknownSet = getClassAssertionAxioms(unknownClassName);
        OWLClassAssertionAxiom[] unknownArr = unknownSet.toArray(new OWLClassAssertionAxiom[unknownSet.size()]);
        ArrayList<OWLClassAssertionAxiom> unknowns = new ArrayList<>(Arrays.asList(unknownArr));
        return permutate(unknowns);
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
    public boolean permutate(ArrayList<OWLClassAssertionAxiom> unknowns) throws Exception {
        // update reasoner
        reasoner.flush();

        // stop when inconsistent
        if (!reasoner.isConsistent()) {
            if (DEBUG) {
                System.out.println("inconsistent ontology");
                writeOntologyToFile();
            }
            return false;
        }

        // no more unknown assertion axioms, consistent ontology generated!
        if (unknowns.size() == 0) {
            System.out.println("consistent ontology generated");
            writeOntologyToFile();
            return true;
        }

        ArrayList<OWLClassAssertionAxiom> unknownsCopy = copyWithoutFirstElement(unknowns);

        OWLClassAssertionAxiom currentUnknownClassAxiom = unknowns.get(0);

        // the unknown class assertion axiom no longer needed
        manager.removeAxiom(ontology, currentUnknownClassAxiom);

        //OWLClassAssertionAxiom unknownAssertion = unknown
        OWLIndividual unknown = currentUnknownClassAxiom.getIndividual();
        Set<OWLClassAssertionAxiom> unknownClasses = ontology.getClassAssertionAxioms(unknown);

        // find all unknown classes the current unknown can be
        // is it assumed that the unknown can only be its own class or superclasses?
        this.currentUnknownClasses = legalClasses(
                currentUnknownClassAxiom, unknownClasses
        );

        this.currentSubClasses = subClasses(currentUnknownClasses);
        List<OWLAxiom> subclassAxioms = generateClassAssertionAxioms(currentSubClasses, currentUnknownClassAxiom);  // TODO : vær konsistent i bruken av unknown
        // can create the class assertion axioms already here?

        if (DEBUG) {
            System.out.println("Start subclasses");
            for (OWLClassExpression c : currentUnknownClasses) {
                System.out.println(c);
            }
            System.out.println("End subclasses");
        }


        // a property is a binary relation
        Set<OWLObjectProperty> properties = ontology.getObjectPropertiesInSignature();

        for (OWLObjectProperty property : properties) {
            if (!property.isBuiltIn()) {
                Set<OWLClassExpression> domains = property.getDomains(ontology);
                Set<OWLClassExpression> ranges = property.getRanges(ontology);
                List<OWLAxiom> axioms;


                // properties where unknown is in the domain
                List<OWLClassAssertionAxiom> individualsInRange = individualsInRange(
                        domains, ranges, property
                );

                // properties where unknown is in the range
                List<OWLClassAssertionAxiom> individualsInDomain = individualsInRange(
                        ranges, domains, property
                );

                boolean doubleSucceeded = false;

                //if (domains.contains(property) && ranges.contains(property)) {
                if (individualIsInClassAssertionSet(individualsInDomain, unknown) && individualIsInClassAssertionSet(individualsInRange, unknown)) {
                    if (DEBUG) {
                        System.out.println("property with individual in domain and range");
                    }

                    for (OWLClassAssertionAxiom individual : individualsInRange) {
                        // add axioms where individual is in the range
                        axioms = generatePropertyAxioms(
                                property, individual, currentUnknownClassAxiom
                        );

                        // add axioms where individual is in the domain
                        for (OWLClassAssertionAxiom individualInDomain : individualsInDomain) {
                            axioms.addAll(generatePropertyAxioms(
                                    property, currentUnknownClassAxiom, individualInDomain
                            ));
                            List<Boolean> returnVals = permutateWithAxioms(unknownsCopy, axioms, subclassAxioms);

                            // check if any permutation succeeded
                            for (Boolean returnVal : returnVals) {
                                if (returnVal) {
                                    doubleSucceeded = true;
                                }
                            }
                            axioms.remove(axioms.size()-1);
                        }
                    }
                }

                //System.out.println("***");
                //System.out.println(doubleSucceeded);
                //System.out.println("***");

                //if (!doubleSucceeded || individualIsInClassAssertionSet(individualsInDomain, unknown)) {
                else if (individualIsInClassAssertionSet(individualsInDomain, unknown)) {
                    if (DEBUG) {
                        System.out.println("property with individual in domain");
                    }

                    // generate permutations with new properties where unknown is in the domain of the property
                    for (OWLClassAssertionAxiom individual : individualsInRange) {
                        axioms = generatePropertyAxioms(
                                property, individual, currentUnknownClassAxiom
                        );
                        permutateWithAxioms(unknownsCopy, axioms, subclassAxioms);
                    }
                    //} else if (ranges.contains(property)) {
                }

                //if (!doubleSucceeded || individualIsInClassAssertionSet(individualsInRange, unknown)) {
                else if (individualIsInClassAssertionSet(individualsInRange, unknown)) {
                    if (DEBUG) {
                        System.out.println("property with individual in range");
                    }
                    // generate permutations with new properties where unknown is in the range of the property
                    for (OWLClassAssertionAxiom individual : individualsInDomain) {
                        axioms = generatePropertyAxioms(
                                property, currentUnknownClassAxiom, individual
                        );
                        permutateWithAxioms(unknownsCopy, axioms, subclassAxioms);
                    }
                } else {
                    if (DEBUG) {
                        //System.out.println("no matches :(");
                        System.out.println(unknown + " is neither in the range nor domain of " + property);
                    }
                }



            }
        }

        return false;
    }

    private List<Boolean> permutateWithAxioms(ArrayList<OWLClassAssertionAxiom> unknownsCopy, List<OWLAxiom> propertyAxioms, List<OWLAxiom> classAxioms) throws Exception {
        List<Boolean> returnVals = new ArrayList<>();
        // add all property axioms
        for (OWLAxiom propertyAxiom : propertyAxioms) {
            manager.addAxiom(ontology, propertyAxiom);
        }

        // permute on class axioms
        for (OWLAxiom classAxiom : classAxioms) {
            manager.addAxiom(ontology, classAxiom);
            //System.out.println(classAxiom);
            returnVals.add(permutate(unknownsCopy));
            manager.removeAxiom(ontology, classAxiom);
        }

        // remove property axioms
        for (OWLAxiom propertyAxiom : propertyAxioms) {
            manager.removeAxiom(ontology, propertyAxiom);
        }

        return returnVals;
    }


    //private void permutate(ArrayList<OWLClassAssertionAxiom> unknowns, OWLObjectProperty property, OWLClassAssertionAxiom individualInRange, OWLClassAssertionAxiom individualInDomain) throws Exception {

    /**
     * Generates a list of property assertion axioms that do not already exist in the ontology.
     *
     * @param property
     * @param individualInRange
     * @param individualInDomain
     * @return
     * @throws Exception
     */
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
        }
        return propertyAxioms;
    }

    /**
     * Generates a list of class assertion axioms that do not already exist in the ontology.
     *
     * @param ce
     * @param individual
     * @return
     * @throws Exception
     */
    private List<OWLAxiom> generateClassAssertionAxioms(List<OWLClassExpression> ce, OWLClassAssertionAxiom individual) throws Exception {
        List<OWLAxiom> classAxioms = new ArrayList<>();

        for (OWLClassExpression c : ce) {
            OWLAxiom ax = factory.getOWLClassAssertionAxiom(c, individual.getIndividual());
            if (!ontology.containsAxiom(ax)) {
                classAxioms.add(ax);
            }
        }

        return classAxioms;
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

        // TODO: erstatt med map (stream?) og filter

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
     *
     * @param unknownClasses A list containing OWL Classes
     * @return List containing OWL (sub)Classes
     */
    private List<OWLClassExpression> subClasses(List<OWLClassExpression> unknownClasses) {
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
     * Writes the ontology to a file.
     */
    public void writeOntologyToFile() {
        //if (DEBUG) {
        //System.out.println("Consistent ontology generated");

        Set<OWLAxiom> axioms = ontology.getAxioms();
        for (OWLAxiom axiom : axioms) {
            if (axiom instanceof OWLObjectPropertyAssertionAxiom || axiom instanceof OWLClassAssertionAxiom) {
                System.out.println(axiom);
            }
        }

        System.out.println();
        //}
    }

    /**
     * Compares all class assertion axioms with the class assertion axiom to see if they are equal.
     * This is a way to make sure all the different assertion axioms that can be used are added.
     *
     * Make a hashmap with (k, v) : (OWLClassAssertionAxiom, List<OWLClassAssertionAxiom>)?
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
