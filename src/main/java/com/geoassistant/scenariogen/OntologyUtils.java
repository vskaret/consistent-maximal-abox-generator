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
        for (OWLAxiom axiom : axioms) {
            if (axiom instanceof OWLClassAssertionAxiom) {
                System.out.println(axiom);
            }
        }

        System.out.println();
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
}
