package com.geoassistant.scenariogen;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.vocab.PrefixOWLOntologyFormat;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * This abstract class is a way to represent an ontology in the OWLAPI.
 * Each ontology will have its own Ontology Manager.
 *
 * Its purpose is currently to be separated into a TemplateOntology and
 * a WorkerOntology, and these two will communicate with each other.
 * They will be duplicates of each other except for the individuals.
 */
public abstract class Ontology {
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

    protected void storeOntology(String path) throws Exception {
        File output = new File(path);
        IRI documentIRI = IRI.create(output);
        manager.saveOntology(ontology, documentIRI);
    }

    // removes axioms from the set that already exist in this ontology
    protected Set<OWLAxiom> removeDuplicates(Set<OWLAxiom> axioms) {
        Set<OWLAxiom> result = new HashSet<>();

        for (OWLAxiom a : axioms) {
            if (!ontology.containsAxiom(a)) {
                result.add(a);
            }
        }

        return result;
    }
}
