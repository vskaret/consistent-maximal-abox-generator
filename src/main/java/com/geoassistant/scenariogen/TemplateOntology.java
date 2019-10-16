package com.geoassistant.scenariogen;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.vocab.PrefixOWLOntologyFormat;

import java.io.File;

/**
 * An ontology with all legal individuals instantiated for all classes.
 */
public class TemplateOntology extends Ontology {

    // this should be a consistent ontology populated with all possible individuals
    private String templateOntologyFilename;
    private File templateOntology;

    /*
    // the OWL ontology manager (manages a set of ontologies)
    private OWLOntologyManager manager;

    // OWLAPI variables for the template ontology
    private OWLOntology ontology;
    private OWLReasoner reasoner;
    private OWLDataFactory factory;
    private PrefixOWLOntologyFormat pm;
    */

    // file for loaded ontology
    private File ontologyFile;

    public TemplateOntology() {
        super();
    }

    // for worker ontologies to get a copy
    public File getOntologyFile() {
        return ontologyFile;
    }

    public NodeSet<OWLNamedIndividual> classQuery(String query) {
        OWLClass c = factory.getOWLClass(":" + query, pm);
        return reasoner.getInstances(c, false);
        //NodeSet<OWLNamedIndividual> individuals = reasoner.getInstances(c, false);

    }

    public void loadFromFile(String filename) throws Exception {
        // only one ontology per ontology object
        if (ontology != null) {
            throw new Exception("Already loaded an ontology");
        }

        // get working dir
        String cwd = System.getProperty("user.dir") + "/src/main/java/com/geoassistant/scenariogen/";
        this.ontologyFile = new File(cwd + filename);

        ontology = manager.loadOntologyFromOntologyDocument(ontologyFile);
        reasoner = new Reasoner.ReasonerFactory().createReasoner(ontology);

        // check consistency of loaded ontology
        if (!reasoner.isConsistent()) {
            throw new Exception("The loaded ontology is not consistent");
        }

        factory = manager.getOWLDataFactory();
        pm = (PrefixOWLOntologyFormat) manager.getOntologyFormat(ontology);

        System.out.println("Loaded template ontology: " + ontology.getOntologyID());
    }
}
