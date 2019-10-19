package com.geoassistant.scenariogen;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.io.SystemOutDocumentTarget;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.util.OWLEntityRemover;
import org.semanticweb.owlapi.vocab.PrefixOWLOntologyFormat;

import java.io.File;
import java.util.*;

/**
 * This class represents an ontology that is used to create permutations
 * based on constraints set by the ontology rules. To achieve this, it
 * communicates with a "template ontology" that has all allowed insstantiations
 * of individuals for every class. It will try to add each individual it gets
 * from querying the template ontology and check for consistency. If it's
 * consistent, it will continue populating that permutation until it's
 * finished or not consistent. If it's not consistent, it will remove
 * the latest added individual and go up one step (to parent).
 * If there's an individual to add there, it will go down that branch.
 * If there are no more individuals to add, it will go back another
 * step.
 */
public class WorkerOntology extends Ontology {
    private final static boolean DEBUG = true;

    // an instance of the template ontology that can be queried
    private TemplateOntology templateOntology;

    public WorkerOntology(TemplateOntology templateOntology) {
        super();
        this.templateOntology = templateOntology;
    }

    /**
     * Load ontology. Only allowed to load ontologies that's in a template ontology.
     * @throws Exception
     */
    public void loadOntology() throws Exception {
        File ontologyFile = templateOntology.getOntologyFile();

        if (ontologyFile == null) {
            throw new Exception("Tried to copy ontology from a template ontology that hasn't yet looaded an ontology.");
        }

        ontology = manager.loadOntologyFromOntologyDocument(ontologyFile);
        reasoner = new Reasoner.ReasonerFactory().createReasoner(ontology);
        factory = manager.getOWLDataFactory();
        pm = (PrefixOWLOntologyFormat) manager.getOntologyFormat(ontology);

        ontologyIRI = ontology.getOntologyID().getOntologyIRI().toString();
        System.out.println("Loaded template ontology: " + ontologyIRI);

        System.out.println(ontologyIRI);

        removeIndividuals();
    }

    public void generateScenarios(ArrayList<String> queries) throws Exception {
        // base case, when the current proto-scenario is not consistent
        if (!reasoner.isConsistent()) {
            System.out.println("Aborting branch, inconsistent proto-scenario!");
            return;
        }

        // base case, when no queries left
        if (queries.isEmpty()) {
            System.out.println("Consistent proto-scenario generated!");
            // store ontology
            // save as an ontology file or a maude file or something else?
            return;
        }

        if (DEBUG) {
            System.out.println(ontology);
        }

        //for (String q : queries) {
        for (int i = 0; i < queries.size(); i++) {
            String q = queries.get(i);
            Set<OWLClassAssertionAxiom> permutationAxioms = templateOntology.getClassAssertionAxioms(q);
            queries.remove(0);

            if (permutationAxioms.isEmpty()) {
                throw new Exception("No permutation axioms returned when querying template ontology for " + q);
            }

            if (DEBUG) {
                System.out.println("queried for " + q + " and got " + permutationAxioms.size() + " axioms");
            }

            // continue with scenario generation in this branch only if the ontology is consistent
            //for (OWLNamedIndividual individual : individuals) {
            for (OWLClassAssertionAxiom axiom : permutationAxioms) {
                if (DEBUG) {
                    System.out.println("adding axiom : " + axiom);
                }

                // add axiom to use in current branch
                manager.addAxiom(ontology, axiom);

                // add all related axioms
                System.out.println("individ: " + axiom.getIndividual());
                OWLEntity entity = (OWLEntity) axiom.getIndividual();
                Set<OWLAxiom> queryResult = templateOntology.getAllAxiomsWithEntity(entity);
                Set<OWLAxiom> newAxioms = removeDuplicates(queryResult);

                if (DEBUG) {
                    System.out.println("*****relatedaxioms*****");
                    for (OWLAxiom a : newAxioms) {
                        System.out.println(a);
                    }
                    System.out.println("***********************");

                }

                manager.addAxioms(ontology, newAxioms);


                // copy of query list
                ArrayList<String> copy = (ArrayList<String>) queries.clone();

                // recurse onwards to victory
                generateScenarios(copy);

                // this branch finished, so remove axiomx
                // remove all new axioms related to the individual
                manager.removeAxioms(ontology, newAxioms);
                // remove class axiom
                manager.removeAxiom(ontology, axiom);
            }
        }

        if (DEBUG) {
            System.out.println(ontology);
        }
    }

    // finds individuals of a class, but not useful? need to use axioms
    private NodeSet<OWLNamedIndividual> queryTemplate(String className) {
        return templateOntology.classQuery(className);
    }

    private void removeIndividuals() {
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

    private void queryClassAssertionAxioms(String className) {
        Set<OWLClassAssertionAxiom> newAxioms = templateOntology.getClassAssertionAxioms(className);
        manager.addAxioms(ontology, newAxioms);
    }

}
