package com.geoassistant.scenariogen;

/**
 * Some inspiration (copypastes?) taken from:
 * https://dior.ics.muni.cz/~makub/owl/
 * http://owlapi.sourceforge.net/owled2011_tutorial.pdf
 */

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.OWLEntityRemover;
import org.semanticweb.owlapi.vocab.PrefixOWLOntologyFormat;

import java.io.File;
import java.util.Collections;
import java.util.Set;

public class ScenarioGenerator {
    private static final String pwd = "src/main/java/com/geoassistant/scenariogen/";

    // this should be a consistent ontology populated with all possible individuals
    private String templateOntologyFilename;
    private File templateOntology;

    // the OWL ontology manager (manages a set of ontologies)
    private OWLOntologyManager manager;

    // OWLAPI variables for the template ontology
    private OWLOntology ontology;
    private OWLReasoner reasoner;
    private OWLDataFactory factory;
    private PrefixOWLOntologyFormat pm;

    public static void main(String[] args) throws Exception {
        ScenarioGenerator sg = new ScenarioGenerator("geo.owl");
        //sg.generate();
        sg.test();


    }

    public ScenarioGenerator(String ontFile) throws Exception {
        templateOntologyFilename = ontFile;
        manager = OWLManager.createOWLOntologyManager();
        openOntology();
    }

    public void test() throws Exception {
        //OWLOntology o2 = manager.loadOntologyFromOntologyDocument(templateOntology);
        //OWLOntology o2 = manager.createOntology();
        OWLOntology o2 = duplicateOntology();
        //removeIndividuals(o2);
        generate("SealingFault", o2);
    }

    public void generate(String searchValue, OWLOntology ont2) throws Exception {
        //String searchValue = "SealingFault";
        OWLClass hitClass = factory.getOWLClass(":" + searchValue, pm);
        NodeSet<OWLNamedIndividual> individuals = reasoner.getInstances(hitClass, false);

        // iterate through individuals (all values/instances that make sense)
        // til å begynne med er ikke dette så viktig, da skal man bare kopiere individet inn i ny ontologi
        // og sjekke at det er konsistent
        for (OWLNamedIndividual individual : individuals.getFlattened()) {
            // print individual's name
            //System.out.println(individual.toString());
            //System.out.println(individual.toStringID());
            //System.out.println(individual.getIRI().getFragment());
            //System.out.println(individual.getIRI().getShortForm());
            addIndividual(individual, ont2);
        }


    }

    // copy classes and object properties from the template ontology to a new one
    private OWLOntology duplicateOntology() throws Exception {
        OWLOntology ont = manager.createOntology();

        // copy classes
        for (OWLClass c : ontology.getClassesInSignature()) {
            //manager.addAxiom(ont, c);
        }

        // copy object properties
        for (OWLObjectProperty p : ontology.getObjectPropertiesInSignature()) {
            //manager.addAxiom(ont, p);
        }

        return ont;
    }

    void removeIndividuals(OWLOntology o) throws Exception {
        //OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        //OWLOntology o = m.loadOntologyFromOntologyDocument(templateOntology);

        OWLEntityRemover r = new OWLEntityRemover(manager, Collections.singleton(o));

        Set<OWLNamedIndividual> individuals = o.getIndividualsInSignature();
        for (OWLNamedIndividual i : individuals) {
            //System.out.println(i);
            r.visit(i);
        }

        manager.applyChanges(r.getChanges());
        //m.saveOntology(ontology);

        //Set<OWLNamedIndividual> individuals2 = o.getIndividualsInSignature();
        //System.out.println(individuals2.size());
    }

    void addIndividual(OWLNamedIndividual individual, OWLOntology ont) {
        System.out.print("pre add: ");
        System.out.println(ont.getIndividualsInSignature().size());
        //AddAxiom a = new AddAxiom(ont, individual);
        //individual.
        //OWLClass tClass = factory.getOWLClass(individual.)
        OWLClassAssertionAxiom classAssertion = factory.getOWLClassAssertionAxiom(individual.asOWLClass(), individual);
        manager.addAxiom(ont, classAssertion);

        System.out.print("post add: ");
        System.out.println(ont.getIndividualsInSignature().size());


    }

    // open and load ontology
    void openOntology(String filename) throws Exception {
        templateOntology = new File(pwd + filename);
        ontology = manager.loadOntologyFromOntologyDocument(templateOntology);
        reasoner = new Reasoner.ReasonerFactory().createReasoner(ontology);

        // check consistency of ontology
        if (!reasoner.isConsistent()) {
            throw new Exception("The template ontology is not consistent");
        }

        factory = manager.getOWLDataFactory();
        pm = (PrefixOWLOntologyFormat) manager.getOntologyFormat(ontology);
        System.out.println("Loaded template ontology: " + ontology.getOntologyID());
    }

    void openOntology() throws Exception {
        openOntology(templateOntologyFilename);
    }

}
