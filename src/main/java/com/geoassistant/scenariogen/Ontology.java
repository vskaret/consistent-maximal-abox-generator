package com.geoassistant.scenariogen;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
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


    public Ontology(boolean debug) {
        manager = OWLManager.createOWLOntologyManager();
        this.DEBUG = debug;
    }

    /**
     * Load an ontology from a file.
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
     * Turn on debugging!
     * @param debug
     */
    public void setDebug(boolean debug) {
        this.DEBUG = debug;
    }

    /**
     * NOT NEEDED?
     * Removes axioms from the set that already exist in the ontology.
     * @param axioms
     * @return
     */
    protected Set<OWLAxiom> removeDuplicates(Set<OWLAxiom> axioms) {
        Set<OWLAxiom> result = new HashSet<>();

        for (OWLAxiom a : axioms) {
            if (!ontology.containsAxiom(a)) {
                result.add(a);
            }
        }

        return result;
    }

    // removes axioms from the set where the individual does not exist in the ontology
    // need this to ensure that individuals are not added prematurely

    /**
     * NOT NEEDED?
     * Removes axioms from the set where the individual does not exist in the ontology. (?)
     * Used to ensure that individuals are not added prematurely.
     * @param axioms
     * @return
     */
    /*
    protected Set<OWLAxiom> removeUnknownIndividuals(Set<OWLAxiom> axioms) {
        Set<OWLAxiom> result = new HashSet<>();

        //System.out.println("Start removeunknown");

        for (OWLAxiom a : axioms) {
            boolean addAxiom = true;

            for (OWLNamedIndividual i : a.getIndividualsInSignature()) {
                if (!ontology.containsEntityInSignature(i)) {
                    addAxiom = false;
                    //System.out.println(i);
                }
            }

            if (addAxiom) {
                //System.out.print("adding ");
                //System.out.println(a);
                result.add(a);
            }
            //if (!ontology.containsAxiom(a)) {
                //result.add(a);
            //}
        }

        //System.out.println("End removeunknown");

        return result;
    }

     */

    /**
     * NOT NEEDED?
     * Removes all individuals from the ontology.
     */
    /*
    protected void removeIndividuals() {
        OWLEntityRemover remover = new OWLEntityRemover(manager, Collections.singleton(ontology));

        Set<OWLNamedIndividual> individuals = ontology.getIndividualsInSignature();

        if (DEBUG) {
            System.out.println("individuals.size() before removal: " + individuals.size());
        }

        for (OWLNamedIndividual individual : individuals) {
            remover.visit(individual);
        }
        manager.applyChanges(remover.getChanges());

        if (DEBUG) {
            individuals = ontology.getIndividualsInSignature();
            System.out.println("individuals.size() after removal: " + individuals.size());
        }

        System.out.println("Removed all individuals from worker ontology");
    }
     */

    /**
     * Debug method. Printis all class axioms with fault.
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
     * Get all class assertion axioms concerning className.
     *
     * Used to generate the list of unknowns.
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
     * Returns true if the individual is in the class assertion set, otherwise false.
     *
     * @param list
     * @param individual
     * @return
     */
    public boolean individualIsInClassAssertionSet(List<OWLClassAssertionAxiom> list, OWLIndividual individual) {
        for (OWLClassAssertionAxiom ca : list) {
            if (ca.getIndividual().equals(individual)) {
                return true;
            }
        }
        return false;
    }
}
