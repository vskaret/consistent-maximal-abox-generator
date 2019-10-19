package com.geoassistant.scenariogen;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.vocab.PrefixOWLOntologyFormat;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

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

    //public OWLClassAssertionAxiom getClass(OWLNamedIndividual individual) {
    public void getClass(OWLNamedIndividual individual) {
        Set<OWLClassExpression> e = individual.getTypes(ontology);

        /*
        System.out.println(individual.getAnnotationAssertionAxioms(ontology).size());

        for (OWLAnnotationAssertionAxiom a : individual.getAnnotationAssertionAxioms(ontology)) {
            System.out.println(a);
        }
        */



        //System.out.println(e.size());

        //for (OWLClassExpression ce : e) {
           //System.out.println(ce.toString());
        //}

        //return factory.getOWLClassAssertionAxiom(individual.)
    }

    // returns all axioms involving entity
    public Set<OWLAxiom> getAllAxiomsWithEntity(OWLEntity entity) {
        Set<OWLAxiom> allAxioms = ontology.getAxioms();
        Set<OWLAxiom> result = new HashSet<>();

        for (OWLAxiom a : allAxioms) {
            if (a.containsEntityInSignature(entity)) {
                result.add(a);
            }
        }

        return result;
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

        // print axioms
        Set<OWLAxiom> aAxioms = ontology.getABoxAxioms(false);
        Set<OWLAxiom> tAxioms = ontology.getTBoxAxioms(false);
        //Set<OWLClassAssertionAxiom> classAxioms = ontology.getClassAssertionAxioms();
        //Set<OWLClassExpressionAxiom> classAxioms = ontology.getClassAssertionAxoms();
        Set<OWLClassExpression> ces = ontology.getNestedClassExpressions();

        for (OWLClassExpression ce : ces) {
            if (!ce.isAnonymous()) {
                //System.out.print("class: ");
                //System.out.println(ce.asOWLClass().getIRI().getShortForm());
                if (ce.asOWLClass().getIRI().getShortForm().equalsIgnoreCase("Fault")) {
                    Set<OWLClassAssertionAxiom> classAxioms = ontology.getClassAssertionAxioms(ce);

                    for (OWLClassAssertionAxiom ca : classAxioms) {
                        System.out.println(ca);
                    }

                }
            } else {
                //System.out.print("anonymous: ");
                //System.out.println(ce);
            }
            //System.out.println(ce.asOWLClass().getIRI().getShortForm());
        }

        // tbox aksiomer er like i begge ontologiene
        /*
        System.out.println("tbox axioms");
        for (OWLAxiom a : tAxioms) {
            System.out.println(a);
        }
        */
        /*
        System.out.println(classAxioms.size());
        System.out.println("abox axioms");
        for (OWLAxiom a : classAxioms) {
            System.out.println(a);
        }
        */

        //for (OWLAxiom a : aAxioms) {
            //if (a instanceof OWLClassAssertionAxiom) {
                //System.out.println(a.getClassExpression());
                /*
                Set<OWLClass> s = a.getClassesInSignature();
                for (OWLClass c : s) {
                    System.out.println(c);
                    System.out.println(c.getIRI().getShortForm());
                }
                */
                //System.out.println(((OWLClassAssertionAxiom) a).getClassExpression().getClassExpressionType());
                //System.out.println(a.getAxiomWithoutAnnotations());
            //}
            //System.out.println(a.getAnnotatedAxiom());
            //System.out.println(a);
        //}
    }
}
