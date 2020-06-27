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
}
