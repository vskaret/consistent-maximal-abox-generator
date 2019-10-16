package com.geoassistant.scenariogen;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.util.OWLEntityRemover;
import org.semanticweb.owlapi.vocab.PrefixOWLOntologyFormat;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

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

        System.out.println("Loaded template ontology: " + ontology.getOntologyID());

        removeIndividuals();
    }

    public void generateScenarios() {
        //String[] individualClasses = new String[3];
        //String[] individualClasses = {"Fault", "Fault", "Fault"};
        ArrayList<String> queries = new ArrayList<String>();
        queries.add("Fault");
        queries.add("Fault");

        for (String q : queries) {
            Set<OWLNamedIndividual> individuals = queryTemplate(q).getFlattened();

            if (DEBUG) {
                System.out.println("queried for " + q + " and got " + individuals.size() + " individuals");
            }

            for (OWLNamedIndividual individual : individuals) {
                if (DEBUG) {
                    System.out.println("adding individual : " + individual.getIRI().getShortForm());
                }

                addIndividual(individual);
            }
        }
    }

    private NodeSet<OWLNamedIndividual> queryTemplate(String className) {
        return templateOntology.classQuery(className);
    }

    private void addIndividual(OWLNamedIndividual individual) {
        OWLClassAssertionAxiom assertion = factory.getOWLClassAssertionAxiom(individual.asOWLClass(), individual);
        manager.addAxiom(ontology, assertion);
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
}
