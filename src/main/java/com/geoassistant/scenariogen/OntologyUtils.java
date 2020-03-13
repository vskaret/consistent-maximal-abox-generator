package com.geoassistant.scenariogen;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.NodeSet;

import java.util.Set;

public final class OntologyUtils {
    private OntologyUtils() {

    }

    private static <T> void printSet(Set<T> set) {
        for (T o : set) {
            System.out.println(o);
        }
        System.out.println();
    }

    public static void printClassAssertions(OWLOntology ontology) {
        Set<OWLAxiom> axioms = ontology.getAxioms();
        System.out.println(axioms);
        for (OWLAxiom axiom : axioms) {
            if (axiom instanceof OWLClassAssertionAxiom) {
                System.out.println(axiom);
            }
        }

        System.out.println();
    }

    public static void printAnonymous(OWLOntology ontology) {
        Set<OWLAnonymousIndividual> anons = ontology.getAnonymousIndividuals();
        System.out.println(anons);

        for (OWLAnonymousIndividual anon : anons) {
            System.out.println(anon);
            Set<OWLIndividualAxiom> axioms = ontology.getAxioms(anon);
            System.out.println(axioms);
            System.out.println();
        }
    }

    public static void printClassAxioms(OWLOntology ontology, OWLClassExpression oce) {
        printSet(ontology.getClassAssertionAxioms(oce));

    }

    public static void printIndividualClassAxioms(OWLOntology ontology, OWLIndividual i) {
        printSet(ontology.getClassAssertionAxioms(i));
    }

    public static boolean classInSet(OWLOntology ontology, OWLClass c, Set<OWLClassExpression> others) {
        for (OWLClassExpression o : others) {
            if (c.equals(o)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Prettyprint a set of class assertions
     */
    public static void prettyprint(Set<OWLClassAssertionAxiom> set) {
        for (OWLClassAssertionAxiom ax : set) {
            String className = ax.getClassExpression().asOWLClass().getIRI().getShortForm();
            String individual = ax.getIndividual().asOWLNamedIndividual().getIRI().getShortForm();

            //if (!className.equals("Permutable") && individual.equals("b")) {
            //if (!className.equals("Permutable")) {
            System.out.print(className + "(" + individual + ") ");
            //}
        }
        System.out.println();
    }

    /**
     * Print all leaf class assertions in the set.
     *
     * Did not work on forcetwoattributes.owl ontology. Somehow Porous and NonPorous were not printed.
     * They were not considered leaf classes?
     *
     * @param set
     * @param ont
     */
    public static void leafprint(Set<OWLClassAssertionAxiom> set, Ontology ont) {
        prettyprint(ont.getLeafClasses(set));
    }
}
