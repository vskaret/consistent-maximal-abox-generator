package com.geoassistant.scenariogen;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.OWLEntityRemover;
import org.semanticweb.owlapi.vocab.PrefixOWLOntologyFormat;

import java.io.File;
import java.util.*;

/**
 * This abstract class is a way to represent an ontology in the OWLAPI.
 * Each ontology will have its own Ontology Manager.
 *
 * Its purpose is currently to be separated into a TemplateOntology and
 * a WorkerOntology, and these two will communicate with each other.
 * They will be duplicates of each other except for the individuals.
 *
 * Useful links:
 * - OWLAPI documentation: https://github.com/owlcs/owlapi/wiki
 * - Javadoc: http://owlcs.github.io/owlapi/apidocs_4/index.html
 */
public abstract class Ontology {
    protected static boolean DEBUG = true;

    // ontology file related variables
    protected File ontologyFile;
    protected final String cwd = System.getProperty("user.dir") + "/src/main/java/com/geoassistant/scenariogen";

    // the OWL ontology manager
    protected OWLOntologyManager manager;

    // OWLAPI variables
    protected OWLOntology ontology;
    protected OWLReasoner reasoner;
    protected OWLDataFactory factory;
    protected PrefixOWLOntologyFormat pm;

    protected String ontologyIRI;


    public Ontology() {
        manager = OWLManager.createOWLOntologyManager();
    }

    /**
     * Set debugging value (true for debug on).
     * @param debug
     */
    public void setDEBUG(boolean debug) {
        this.DEBUG = debug;
    }

    /**
     * Load an ontology from a file. Also instantiates a Reasoner object.
     * @param filename
     * @throws Exception
     */
    protected void loadOntology(String filename) throws Exception {
        // only one ontology per ontology?
        if (ontology != null) {
            throw new Exception("Already loaded an ontology.");
        }

        this.ontologyFile = new File(cwd + "/" + filename);

        ontology = manager.loadOntologyFromOntologyDocument(ontologyFile);
        reasoner = new Reasoner.ReasonerFactory().createReasoner(ontology);

        // check consistency
        if (!reasoner.isConsistent()) {
            throw new Exception("Loaded ontology is not consistent.");
        }

        factory = manager.getOWLDataFactory();
        pm = (PrefixOWLOntologyFormat) manager.getOntologyFormat(ontology);

        if (DEBUG) {
            System.out.println("Loaded template ontology: " + ontology.getOntologyID());
            printFaultClassAxioms();
        }

    }

    protected void storeOntology(String path) throws Exception {
        File output = new File(path);
        IRI documentIRI = IRI.create(output);
        manager.saveOntology(ontology, documentIRI);
    }

    /**
     * Writes the ontology to a file.
     */
    public void printClassAssertions() {
        Set<OWLAxiom> axioms = ontology.getAxioms();
        for (OWLAxiom axiom : axioms) {
            if (axiom instanceof OWLClassAssertionAxiom) {
                System.out.println(axiom);
            }
        }

        System.out.println();
    }

    /**
     * Turn on debugging!
     * @param debug
     */
    public void setDebug(boolean debug) {
        this.DEBUG = debug;
    }

    /**
     * Debug method. Print all class axioms with fault.
     */
    protected void printFaultClassAxioms() {
        // axioms
        Set<OWLClassExpression> ces = ontology.getNestedClassExpressions();

        for (OWLClassExpression ce : ces) {
            // only work with asserted class axioms?
            if (!ce.isAnonymous()) {
                if (ce.asOWLClass().getIRI().getShortForm().equalsIgnoreCase("Fault")) {
                    Set<OWLClassAssertionAxiom> classAxioms = ontology.getClassAssertionAxioms(ce);

                    for(OWLClassAssertionAxiom ca : classAxioms) {
                        System.out.println(ca);
                    }
                }
            }
        }
    }

    /**
     * Get all non-anonymous class expressions in the ontology with className as name.
     *
     * Used to generate the list of permutables.
     * @param className
     * @return
     */
    public Set<OWLClassExpression> getClassExpressions(String className) {
        Set<OWLClassExpression> classExpressions = ontology.getNestedClassExpressions();
        Set<OWLClassExpression> result = new HashSet<>();

        for (OWLClassExpression ce : classExpressions) {
            if (!ce.isAnonymous() && ce.asOWLClass().getIRI().getShortForm().equalsIgnoreCase(className)) {
                result.add(ce);
            }
        }

        return result;
    }


    /**
     * Get all class assertion axioms concerning className (all individuals instantiated to be this class).
     *
     * Used to generate the list of permutables.
     *
     * @param className
     * @return A set with all class assertion axioms that is of class className
     */
    public Set<OWLClassAssertionAxiom> getClassAssertionAxioms(String className) {
        Set<OWLClassExpression> classExpressions = getClassExpressions(className);
        Set<OWLClassAssertionAxiom> result = new HashSet<>();

        for (OWLClassExpression ce : classExpressions) {
            Set<OWLClassAssertionAxiom> axioms = ontology.getClassAssertionAxioms(ce);

            for (OWLClassAssertionAxiom a : axioms) {
                result.add(a);
            }
        }

        return result;
    }

    /**
     * Generates a list of class assertion axioms that do not already exist in the ontology.
     *
     * Just like generateAxioms in the pseudo code.
     * @param ce
     * @param individual
     * @return
     * @throws Exception
     */
    protected List<OWLAxiom> generateClassAssertionAxioms(List<OWLClassExpression> ce, OWLIndividual individual) throws Exception {
        List<OWLAxiom> classAxioms = new ArrayList<>();

        //System.out.println(ce);

        for (OWLClassExpression c : ce) {
            OWLAxiom ax = factory.getOWLClassAssertionAxiom(c, individual);
            classAxioms.add(ax);
            //if (!ontology.containsAxiom(ax)) {
                //classAxioms.add(ax);
            //}
        }

        return classAxioms;
    }

    /**
     * Used to get restOfPermutables.
     * @param list
     * @return
     */
    protected ArrayList<OWLClassAssertionAxiom> copyWithoutFirstElement(ArrayList<OWLClassAssertionAxiom> list) {
        ArrayList<OWLClassAssertionAxiom> copy =
                (ArrayList<OWLClassAssertionAxiom>) list.clone();
        copy.remove(0);
        return copy;
    }

    /**
     * Returns all leaf sub classes of the class expression given. See isLeafClass() for definition of
     * leaf sub class.
     * @param ce
     * @return
     */
    //protected ArrayList<OWLClassExpression> allLeafSubClasses(OWLClassExpression ce) {
    protected ArrayList<ArrayList<OWLClassExpression>> allLeafSubClasses(OWLClassExpression ce) {
        ArrayList<OWLClassExpression> leafSubClasses = new ArrayList<>();
        ArrayList<ArrayList<OWLClassExpression>> listOfLeafClasses = new ArrayList<>();

        NodeSet<OWLClass> dirSubClasses = reasoner.getSubClasses(ce, true);

        System.out.print("Starting point: ");
        System.out.println(ce);


        try {
            for (OWLClass dirSubClass : dirSubClasses.getFlattened()) {
                //System.out.println(dirSubClass.getRepresentativeElement());
                ArrayList<OWLClassExpression> leafClasses = new ArrayList<>();

                for (OWLClass subClass : reasoner.getSubClasses(dirSubClass, false).getFlattened()) {
                    if (isLeafClass(subClass)) {
                        //System.out.print("leaf: ");
                        //System.out.println(subClass);
                        leafClasses.add(subClass);
                    } else {
                        //System.out.print("no leaf: ");
                        //System.out.println(subClass);
                    }
                }

                listOfLeafClasses.add(leafClasses);
            }
        } catch (Exception e) {
            System.out.println("ERRORRRR");
            printClassAssertions();
            throw e;
        }

        System.out.println(listOfLeafClasses);
        return listOfLeafClasses;
    }

    /**
     * Checks if a class is a leaf class. Leaf class as in it has no sub classes.
     *
     * Returns true iff the direct sub class of the class expression is only bottom/OWL:Nothing
     * @param ce
     * @return
     */
    protected boolean isLeafClass(OWLClassExpression ce) {
        return reasoner.getSubClasses(ce, true).isBottomSingleton();
    }

    protected OWLClassExpression greatestCommonClass(Set<OWLClassExpression> commonClasses) throws Exception {
        OWLClassExpression thing = factory.getOWLThing();
        NodeSet<OWLClass> topClasses = reasoner.getSubClasses(thing, true);

        return greatestCommonClass(commonClasses, topClasses);
    }

    /**
     * Finds the greatest common class of a set of class expressions.
     *
     * @param commonClasses
     * @param classes
     * @return
     * @throws Exception
     */
    protected OWLClassExpression greatestCommonClass(Set<OWLClassExpression> commonClasses, NodeSet<OWLClass> classes) throws Exception {
        OWLClassExpression greatest = factory.getOWLThing();
        //System.out.print("common classes is ");
        //System.out.println(commonClasses);
        //System.out.print("classes is ");
        //System.out.println(classes);
        boolean classFound = false;

        for (OWLClass treeClass : classes.getFlattened()) {
            for (OWLClassExpression c : commonClasses) {
                if (c.asOWLClass().equals(treeClass)) {
                    //System.out.println(treeClass);
                    if (classFound) {
                        throw new Exception("Several common classes found");
                    }
                    greatest = c;
                    classFound = true;
                }
            }

            if (!classFound) {
                return greatestCommonClass(commonClasses, reasoner.getSubClasses(treeClass, true));
            }
        }

        //System.out.print("returning ");
        //System.out.println(greatest);
        return greatest;
    }

    protected Set<OWLClassExpression> classAssertionsToClassExpressions(Set<OWLClassAssertionAxiom> classAssertions) {
        Set<OWLClassExpression> classExpressions = new HashSet<>();

        for (OWLClassAssertionAxiom ax : classAssertions) {
            classExpressions.add(ax.getClassExpression());
        }

        return classExpressions;
    }

    /**
     * Returns the top super class of a class. If none are found (meaning the class is a top super class
     * itself), the class given as a parameter is returned back.
     *
     * @param c
     * @return
     */
    /*
    protected OWLClassExpression topSuperClassOf(OWLClass c) throws Exception {
        OWLClass thing = factory.getOWLThing();
        NodeSet<OWLClass> superClasses = reasoner.getSuperClasses(c, false);
        NodeSet<OWLClass> topClasses = reasoner.getSubClasses(thing, true);
        OWLClassExpression topSuperClass = c;

        boolean foundTopSuperClass = false;

        for (Node<OWLClass> topClassNode : topClasses) {
            if (superClasses.containsEntity(topClassNode.getRepresentativeElement())) {
                if (foundTopSuperClass) {
                    throw new Exception("More than one top super class found for: " + c);
                }

                foundTopSuperClass = true;
                topSuperClass = topClassNode.getRepresentativeElement();
            }
        }

        return topSuperClass;
    }
     */

    /**
     * Finds top super classes of the class assertions. It is assumed to be class assertions for only one
     * individual.
     * @param classAssertions
     * @return
     * @throws Exception
     */
    /*
    protected OWLClassExpression topSuperClass(Set<OWLClassAssertionAxiom> classAssertions) throws Exception {
        OWLClassExpression c = factory.getOWLThing();
        OWLClassExpression temp = c;
        boolean changed = false;

        for (OWLClassAssertionAxiom classAssertion : classAssertions) {
            temp = topSuperClassOf(classAssertion.getClassExpression().asOWLClass());
            if (changed && !temp.equals(c)) {
                System.out.println(temp);
                System.out.println(c);
                throw new Exception("Conflicting top super classes found for " + classAssertion.getIndividual());
            }
            c = temp;
            changed = true;
        }

        return c;
    }
     */

    /**
     * Returns a list with the range and all its sub-classes.
     *
     * @param ce
     * @return A list with all sub-classes of ce (including the class expression given as a parameter).
     */
    /*
    protected List<OWLClassExpression> allSubClasses(OWLClassExpression ce) {
        List<OWLClassExpression> allSubClasses = new ArrayList<>();
        allSubClasses.add(ce);

        for (OWLSubClassOfAxiom sub : ontology.getSubClassAxiomsForSuperClass(ce.asOWLClass())) {
            allSubClasses.add(sub.getSubClass());
        }

        return allSubClasses;
    }
     */

    // TODO : hva er forskjellen på denne og den over?
    /**
     * Finds all legal subclasses for all of the classes in the given parameter.
     * What is a legal subclass?
     *
     * @param unknownClasses A list containing OWL Classes
     * @return List containing OWL (sub)Classes
     */
    /*
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
     */


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
    /*
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
     */

    /**
     * All class assertion axioms in the ontology. In other words, all the individuals with an asserted
     * class?
     *
     * @return
     */
    /*
    protected ArrayList<OWLClassAssertionAxiom> allClassAssertionAxioms() {
        ArrayList<OWLClassAssertionAxiom> classAxioms = new ArrayList<>();

        for (OWLClass ce : ontology.getClassesInSignature()) {
            for (OWLClassAssertionAxiom ca : ontology.getClassAssertionAxioms(ce)) {
                classAxioms.add(ca);
            }
        }

        return classAxioms;
    }
     */

    /**
     * Returns true if the individual is in the class assertion set, otherwise false.
     *
     * @param list
     * @param individual
     * @return
     */
    /*
    public boolean individualIsInClassAssertionSet(List<OWLClassAssertionAxiom> list, OWLIndividual individual) {
        for (OWLClassAssertionAxiom ca : list) {
            if (ca.getIndividual().equals(individual)) {
                return true;
            }
        }
        return false;
    }
     */
}
