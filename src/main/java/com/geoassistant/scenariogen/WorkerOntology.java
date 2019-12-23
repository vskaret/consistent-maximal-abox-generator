package com.geoassistant.scenariogen;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.io.SystemOutDocumentTarget;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.Node;
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
    // an instance of the template ontology that can be queried
    private TemplateOntology templateOntology;

    private ArrayList<OWLEntity> individuals = new ArrayList<>();

    // variables used in the recursive call
    private List<OWLClassExpression> currentUnknownClasses;
    private Set<OWLClassExpression> domains;
    private Set<OWLClassExpression> ranges;
    // domain
    // property
    // unknown
    private OWLIndividual unknown;
    // unknowns
    ArrayList<OWLClassAssertionAxiom> unknowns;

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

        if (DEBUG) {
            System.out.println("Loaded template ontology: " + ontologyIRI);
        }

        //removeIndividuals();
    }
    public void permutate() throws Exception {
        /*
        Set<OWLAxiom> axioms = ontology.getAxioms();
        for (OWLAxiom axiom : axioms) {
            if (axiom instanceof OWLObjectPropertyAssertionAxiom) {
                System.out.println(axiom);
            }
        }
        System.exit(0);
        */

        // get all Unknown individuals
        Set<OWLClassAssertionAxiom> unknownSet = templateOntology.getClassAssertionAxioms("Unknown");
        OWLClassAssertionAxiom[] unknownArr = unknownSet.toArray(new OWLClassAssertionAxiom[unknownSet.size()]);
        ArrayList<OWLClassAssertionAxiom> unknowns = new ArrayList<>(Arrays.asList(unknownArr));
        permutate(unknowns);
    }

    //public void permutate(OWLClassAssertionAxiom[] unknowns) throws Exception {
    public void permutate(ArrayList<OWLClassAssertionAxiom> unknowns) throws Exception {
        // inconsistent case
        //System.out.println("test");
        reasoner.flush();

        if (!reasoner.isConsistent()) {
            /*
            System.out.println("inconsistent branch!");

            Set<OWLAxiom> axioms = ontology.getAxioms();
            for (OWLAxiom axiom : axioms) {
                if (axiom instanceof OWLObjectPropertyAssertionAxiom) {
                    System.out.println(axiom);
                }
            }
            */
            return;
        }

        // base case
        if (unknowns.size() == 0) {
            System.out.println("consistent ontology generated!");

            Set<OWLAxiom> axioms = ontology.getAxioms();
            for (OWLAxiom axiom : axioms) {
                if (axiom instanceof OWLObjectPropertyAssertionAxiom) {
                    System.out.println(axiom);
                }
            }
            //System.out.println(ontology.getAxioms());
            return;
        }



        OWLClassAssertionAxiom currentUnknownClassAxiom = unknowns.get(0);
        //System.out.println(currentUnknownClassAxiom);
        OWLIndividual unknown = currentUnknownClassAxiom.getIndividual();
        //System.out.println(unknown);

        //Set<OWLClassAssertionAxiom> unknownClasses = ontology.getClassAssertionAxioms(unknown);
        Set<OWLClassAssertionAxiom> unknownClasses = ontology.getClassAssertionAxioms(unknown);

        List<OWLClassAssertionAxiom> allClassAssertionAxioms = allClassAssertionAxioms();

        // all class assertions
        /*
        List<OWLClassAssertionAxiom> individuals = new ArrayList<>();
        //for (OWLNamedIndividual individual : ontology.getIndividualsInSignature()) {
        for (OWLClass ce : ontology.getClassesInSignature()) {
            for (OWLClassAssertionAxiom ca : ontology.getClassAssertionAxioms(ce)) {
                //if (!ca.getIndividual().equals(unknown)) {
                individuals.add(ca);
                //}

                // tar ogsaa med unknown i tilfelle det er en refleksiv object property
            }
        }
        */

        /*for (OWLClassAssertionAxiom individual : ontology.getClassAssertionAxioms()) {
            if (!individual.equals(currentUnknownClassAxiom)) {
                System.out.print("not equal : ");
                individuals.add(individual);
            } else {
                System.out.print("equal : ");
            }
            System.out.println(individual);
        }*/


        // find currentUnknownClasses the unknown can be
        List<OWLClassExpression> currentUnknownClasses = new ArrayList<OWLClassExpression>();

        for (OWLClassAssertionAxiom ca : unknownClasses) {
            if (!currentUnknownClassAxiom.equals(ca)) {
                OWLClassExpression c = ca.getClassExpression();
                currentUnknownClasses.add(c);
            }
        }

        //System.out.println(currentUnknownClasses);

        // find all object properties the unknown can be a part of

        Set<OWLObjectProperty> properties = ontology.getObjectPropertiesInSignature();
        List<OWLObjectProperty> propertiesPartOfDomain = new ArrayList<>();
        List<OWLObjectProperty> propertiesPartOfRange = new ArrayList<>();

        // domains and ranges for each property
        //HashMap<OWLObjectProperty, Set<OWLClassExpression>> propertyDomains = new HashMap<>();
        //HashMap<OWLObjectProperty, Set<OWLClassExpression>> propertyRanges = new HashMap<>();

        //HashMap<OWLObjectProperty, Set<OWLClassExpression>> propertyLegalDomain = new HashMap<>();
        //HashMap<OWLObjectProperty, Set<OWLClassExpression>> propertyLegalRange = new HashMap<>();

        // worst code ever written?

        for (OWLObjectProperty property : properties) {
            // skip owl:topproperty
            if (!property.isBuiltIn()) {
                // properties where unknown is part of the domain
                //propertyDomains.put(property, property.getDomains(ontology));
                //propertyRanges.put(property, property.getDomains(ontology));

                Set<OWLClassExpression> domains = property.getDomains(ontology);
                Set<OWLClassExpression> ranges = property.getRanges(ontology);

                for (OWLClassExpression domain : domains) {
                    for (OWLClassExpression currentUnknownClass : currentUnknownClasses) {
                        if (domain.equals(currentUnknownClass)) {
                            for (OWLClassExpression range : ranges) {
                                // all legal currentUnknownClasses for the range
                                List<OWLClassExpression> legalRanges = new ArrayList<>();
                                legalRanges.add(range);
                                for (OWLSubClassOfAxiom sub : ontology.getSubClassAxiomsForSuperClass(range.asOWLClass())) {
                                    legalRanges.add(sub.getSubClass());
                                }

                                for (OWLClassExpression legalRange : legalRanges) {
                                    for (OWLClassAssertionAxiom individual : individuals) {
                                        //System.out.print(individual);
                                        //System.out.print(" vs ");
                                        //System.out.println(range);

                                        if (individual.getClassExpression().equals(legalRange)) {
                                            OWLAxiom ax = factory.getOWLObjectPropertyAssertionAxiom(property, unknown, individual.getIndividual());

                                            if (!ontology.containsAxiom(ax)) {
                                                manager.addAxiom(ontology, ax);
                                                ArrayList<OWLClassAssertionAxiom> copy = (ArrayList<OWLClassAssertionAxiom>) unknowns.clone();
                                                copy.remove(0);
                                                permutate(copy);
                                                manager.removeAxiom(ontology, ax);
                                                //System.out.println(factory.getOWLObjectPropertyAssertionAxiom(property, unknown, individual.getIndividual()));

                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                for (OWLClassExpression range : ranges) {
                    for (OWLClassExpression currentUnknownClass : currentUnknownClasses) {
                        if (range.equals(currentUnknownClass)) {
                            for (OWLClassExpression domain : domains) {
                                // all legal currentUnknownClasses for the range
                                List<OWLClassExpression> legalDomains = new ArrayList<>();
                                legalDomains.add(domain);
                                for (OWLSubClassOfAxiom sub : ontology.getSubClassAxiomsForSuperClass(range.asOWLClass())) {
                                    legalDomains.add(sub.getSubClass());
                                }

                                for (OWLClassExpression legalDomain : legalDomains) {
                                    for (OWLClassAssertionAxiom individual : individuals) {
                                        //System.out.print(individual);
                                        //System.out.print(" vs ");
                                        //System.out.println(range);

                                        if (individual.getClassExpression().equals(legalDomain)) {
                                            //OWLAxiom ax = factory.getOWLObjectPropertyAssertionAxiom(property, unknown, individual.getIndividual());
                                            OWLAxiom ax = factory.getOWLObjectPropertyAssertionAxiom(property, individual.getIndividual(), unknown);
                                            if (!ontology.containsAxiom(ax)) {
                                                manager.addAxiom(ontology, ax);
                                                ArrayList<OWLClassAssertionAxiom> copy = (ArrayList<OWLClassAssertionAxiom>) unknowns.clone();
                                                copy.remove(0);
                                                permutate(copy);
                                                manager.removeAxiom(ontology, ax);
                                                //System.out.println(factory.getOWLObjectPropertyAssertionAxiom(property, unknown, individual.getIndividual()));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // properties where unknown is part of the range
                /*
                Set<OWLClassExpression> ranges = property.getRanges(ontology);
                for (OWLClassExpression range : ranges) {
                    for (OWLClassExpression currentUnknownClass : currentUnknownClasses) {
                        if (range.equals(currentUnknownClass)) {
                            propertiesPartOfRange.add(property);
                        }
                    }
                }
                */
            }
        }

        //System.out.println(propertiesPartOfDomain);
        //System.out.println(propertiesPartOfRange);
        // permutate on legal combinations in the object property


        // ??
    }

    /*
    void innerPermutateCall(OWLClassExpression domain) {
        for (OWLClassExpression currentUnknownClass : currentUnknownClasses) {
            if (domain.equals(currentUnknownClass)) {
                for (OWLClassExpression range : ranges) {
                    // all legal currentUnknownClasses for the range
                    List<OWLClassExpression> legalRanges = new ArrayList<>();
                    legalRanges.add(range);
                    for (OWLSubClassOfAxiom sub : ontology.getSubClassAxiomsForSuperClass(range.asOWLClass())) {
                        legalRanges.add(sub.getSubClass());
                    }

                    for (OWLClassExpression legalRange : legalRanges) {
                        for (OWLClassAssertionAxiom individual : individuals) {
                            //System.out.print(individual);
                            //System.out.print(" vs ");
                            //System.out.println(range);

                            if (individual.getClassExpression().equals(legalRange)) {
                                OWLAxiom ax = factory.getOWLObjectPropertyAssertionAxiom(property, unknown, individual.getIndividual());

                                if (!ontology.containsAxiom(ax)) {
                                    manager.addAxiom(ontology, ax);
                                    ArrayList<OWLClassAssertionAxiom> copy = (ArrayList<OWLClassAssertionAxiom>) unknowns.clone();
                                    copy.remove(0);
                                    permutate(copy);
                                    manager.removeAxiom(ontology, ax);
                                    //System.out.println(factory.getOWLObjectPropertyAssertionAxiom(property, unknown, individual.getIndividual()));

                                }
                            }
                        }
                    }
                }
            }
        }
    }
    }
    */

    //public void generateScenario(ArrayList<String> queries) throws Exception {
    public void generateScenario(ArrayList<OWLQuery> queries) throws Exception {

        for (OWLQuery q : queries) {
            //templateOntology.query(q);
            //System.out.println(q.getQuery());
            q.setResult(templateOntology.getClassAssertionAxioms(q.getQuery()));
        }

        /*
        ArrayList<ArrayList<OWLClassAssertionAxiom>> axiomLists = new ArrayList<>();

        for (int i = 0; i < queries.size(); i++) {
            ArrayList<OWLClassAssertionAxiom> axioms = new ArrayList<>();
            String q = queries.get(i);
            Set<OWLClassAssertionAxiom> newAxioms = templateOntology.getClassAssertionAxioms(q);

            for (OWLClassAssertionAxiom a : newAxioms) {
                axioms.add(a);
            }

            axiomLists.add(axioms);
        }
        */

        //generateScenarios(axioms);
        generateScenarios(queries, 0);
        //generateScenarios(axiomLists);
        //generateScenarios(queries, new ArrayList<OWLEntity>());
    }

    private List<OWLClassAssertionAxiom> allClassAssertionAxioms() {
        List<OWLClassAssertionAxiom> classAxioms = new ArrayList<>();

        for (OWLClass ce : ontology.getClassesInSignature()) {
            for (OWLClassAssertionAxiom ca : ontology.getClassAssertionAxioms(ce)) {
                classAxioms.add(ca);
            }
        }

        return classAxioms;
    }

    /*
    private List<OWLClassExpression> legalUnknownClasses(OWLClassAssertionAxiom allUnknownClasses) {
        List<OWLClassExpression> legalUnknownClasses = new ArrayList<>();

        for (OWLClassAssertionAxiom ca : allUnknownClasses) {
            if (!unknownClassAxiom.equals(ca)) {
                legalUnknownClasses.add(ca.getClassExpression());
            }
        }

        return legalUnknownClasses;
    }
    */

    protected void generateScenarios(ArrayList<OWLQuery> queries, int k) throws Exception {
        // base case, when the current proto-scenario is not consistent
        if (!reasoner.isConsistent()) {
            System.out.println("Aborting branch, inconsistent proto-scenario!");
            return;
        }

        // increment k
        if (queries.get(k).finished()) {
            k++;
        }

        // base case
        if (k >= queries.size()) {
            System.out.print("individuals in the end: ");
            System.out.println(individuals);
            return;
        }

        if (DEBUG) {
            System.out.println(ontology);
        }

        ArrayList<OWLClassAssertionAxiom> axioms = queries.get(k).getResults();

        for (int j = 0; j < axioms.size(); j++) {
             OWLClassAssertionAxiom axiom = axioms.get(j);

             if (DEBUG) {
                 System.out.println("addming axiom : " + axiom);
             }

             // add axiom in branch
             manager.addAxiom(ontology, axiom);

            // add all related axioms
            OWLEntity entity = (OWLEntity) axiom.getIndividual();
            Set<OWLAxiom> queryResult = templateOntology.getAllAxiomsWithEntity(entity);
            Set<OWLAxiom> newAxioms = removeDuplicates(queryResult);
            newAxioms = removeUnknownIndividuals(newAxioms);

            if (DEBUG) {
                System.out.println("*****relatedaxioms*****");
                for (OWLAxiom a : newAxioms) {
                    System.out.println(a);
                }
                System.out.println("***********************");

            }

            individuals.add(entity);
            manager.addAxioms(ontology, newAxioms);

            queries.get(k).countDown();
            generateScenarios(queries, k);
            queries.get(k).countUp();


            // this branch finished, so remove axiomx
            // remove all new axioms related to the individual
            manager.removeAxioms(ontology, newAxioms);
            // remove class axiom
            manager.removeAxiom(ontology, axiom);
            individuals.remove(entity);

        }

        if (DEBUG) {
            System.out.println(ontology);
        }
    }

    //protected void generateForQuery(OWLQuery query) {
        //ArrayList<OWLClassAssertionAxiom> axioms = queries.get(i).getResults();
        //
    //}

    // finds individuals of a class, but not useful? need to use axioms
    private NodeSet<OWLNamedIndividual> queryTemplate(String className) {
        return templateOntology.classQuery(className);
    }


    private void queryClassAssertionAxioms(String className) {
        Set<OWLClassAssertionAxiom> newAxioms = templateOntology.getClassAssertionAxioms(className);
        manager.addAxioms(ontology, newAxioms);
    }
}
