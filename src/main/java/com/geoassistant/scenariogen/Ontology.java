package com.geoassistant.scenariogen;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
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
    protected boolean DEBUG = true;

    // ontology file related variables
    protected File ontologyFile;
    protected final String srcPath = System.getProperty("user.dir") + "/src/";
    protected final String cwd = srcPath + "main/java/com/geoassistant/scenariogen/";
    protected final String owldir = srcPath + "owl/";

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
     * Set debugging value
     * @param debug true/false for debugging on/off
     */
    public void setDEBUG(boolean debug) {
        this.DEBUG = debug;
    }

    /**
     * Load an ontology from a file from same folder as the java source file. Also instantiates a Reasoner object.
     * @param filename name of ontology file
     * @throws Exception
     */
    protected void loadOntology(String filename) throws Exception {
        // only one ontology per ontology?
        if (ontology != null) {
            throw new Exception("Already loaded an ontology.");
        }

        this.ontologyFile = new File(owldir + filename);

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
        }

    }

    protected void storeOntology(String path) throws Exception {
        File output = new File(path);
        IRI documentIRI = IRI.create(output);
        manager.saveOntology(ontology, documentIRI);
    }


    /**
     * Get all non-anonymous class expressions in the ontology with className as name.
     * (Used to generate the list of permutables)
     *
     * @param className name of class to get expressions of
     * @return a set with all class expressions involving className
     */
    public Set<OWLClassExpression> getClassExpressions(String className) {
        //OWLClass oc = factory.getOWLClass(className, pm);
        //return ontology.getClassE

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
     * (Used to generate the list of permutables)
     *
     * @param className name of class to get assertion axioms of
     * @return A set with all class assertion axioms that is of class className
     */
    public Set<OWLClassAssertionAxiom> getClassAssertionAxioms(String className) {
        OWLClassExpression oce = factory.getOWLClass(className, pm);
        return ontology.getClassAssertionAxioms(oce);
    }

    public Set<OWLClassAssertionAxiom> getAllClassAssertionAxioms() {
        Set<OWLClassAssertionAxiom> result = new HashSet<>();
        Set<OWLNamedIndividual> individuals = ontology.getIndividualsInSignature();

        for (OWLNamedIndividual i : individuals) {
            Set<OWLClassAssertionAxiom> axioms = ontology.getClassAssertionAxioms(i);
            result.addAll(axioms);
        }

        return result;
    }

    /**
     * Generates a list of class assertion axioms.
     * (generateAxioms in the pseudo code)
     *
     * @param ce a list of class expressions
     * @param individual the individual to assert is of the classes in ce
     * @return a list of class assertion axioms
     * @throws Exception TODO
     */
    protected List<OWLAxiom> generateClassAssertionAxioms(List<OWLClassExpression> ce, OWLIndividual individual) {
        List<OWLAxiom> classAxioms = new ArrayList<>();

        for (OWLClassExpression c : ce) {
            OWLAxiom ax = factory.getOWLClassAssertionAxiom(c, individual);
            classAxioms.add(ax);
        }

        return classAxioms;
    }


    /**
     * Checks if a class is a leaf class. Leaf class as in it has no sub classes.
     *
     * Returns true iff the direct sub class of the class expression is only bottom/OWL:Nothing
     * @param ce class expression to check
     * @return true if leaf class, otherwise false
     */
    //protected boolean isLeafClass(OWLClassExpression ce) {
    protected boolean isLeafClass(OWLClassExpression ce) {
        return reasoner.getSubClasses(ce, true).isBottomSingleton();
    }

    public void subsetTest() {
        Set<OWLClass> classes = ontology.getClassesInSignature();
        OWLClass c1 = classes.iterator().next();

        Set<OWLNamedIndividual> individuals = ontology.getIndividualsInSignature();
        OWLNamedIndividual i1 = individuals.iterator().next();

        OWLClassAssertionAxiom ax1 = factory.getOWLClassAssertionAxiom(c1, i1);
        OWLClassAssertionAxiom ax2 = factory.getOWLClassAssertionAxiom(c1, i1);

        Set<OWLClassAssertionAxiom> set1 = new HashSet<>();
        set1.add(ax1);

        Set<OWLClassAssertionAxiom> set2 = new HashSet<>();
        set2.add(ax2);

        Set<Set<OWLClassAssertionAxiom>> mainSet = new HashSet<>();
        mainSet.add(set2);

        System.out.println(Utils.subsetOf(set1, mainSet));
    }

    public Set<OWLClassAssertionAxiom> getLeafClasses(Set<OWLClassAssertionAxiom> set) {
        Set<OWLClassAssertionAxiom> result = new HashSet<>();

        for (OWLClassAssertionAxiom ax : set) {
            if (isLeafClass(ax.getClassExpression())) {
                result.add(ax);
            }
        }

        return result;
    }
}
