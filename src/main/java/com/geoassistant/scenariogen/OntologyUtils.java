package com.geoassistant.scenariogen;

import org.semanticweb.owlapi.model.*;
import java.util.Set;

public final class OntologyUtils {
    /**
     * Prettyprint a set of class assertions
     */
    public static void prettyprint(Set<OWLClassAssertionAxiom> set) {
        for (OWLClassAssertionAxiom ax : set) {
            String className = ax.getClassExpression().asOWLClass().getIRI().getShortForm();
            String individual = ax.getIndividual().asOWLNamedIndividual().getIRI().getShortForm();
            System.out.print(className + "(" + individual + ") ");
        }
        System.out.println();
    }
}
