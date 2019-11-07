package com.geoassistant.scenariogen;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.OWLEntityRemover;
import org.semanticweb.owlapi.vocab.PrefixOWLOntologyFormat;

import java.io.File;
import java.util.Collections;
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
    protected static boolean DEBUG = false;

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

    public void setDebug(boolean debug) {
        this.DEBUG = debug;
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

    // removes axioms from the set where the individual does not exist in the ontology
    // need this to ensure that individuals are not added prematurely
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

    // removes all individuals from the ontology
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
}
