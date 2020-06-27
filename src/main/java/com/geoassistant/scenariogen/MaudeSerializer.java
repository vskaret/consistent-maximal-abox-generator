package com.geoassistant.scenariogen;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import java.io.IOException;
import java.io.*;
import java.util.Set;

public class MaudeSerializer {
    static final String prefix = "mod OWL-ABOX is\n" +
            "  protecting OWL-CONVERTER .\n";

    static final String postfix = "\nendm";

    public static String serializeAbox(OWLOntology ontology, OWLReasoner reasoner) {
        Set<OWLNamedIndividual> individuals = ontology.getIndividualsInSignature();
        StringBuffer result = new StringBuffer(prefix);
        result.append("  op unknowns : -> Configuration .\n");
        result.append("  eq unknowns = ");

        for (OWLNamedIndividual i : individuals) {
            String oid = i.getIRI().getShortForm();
            Set<OWLClassAssertionAxiom> instantiations = ontology.getClassAssertionAxioms(i);

            for (OWLClassAssertionAxiom cax : instantiations) {
                String sort = cax.getClassExpression().asOWLClass().getIRI().getShortForm();
                result.append("type(" + sort + ", " + '"' + oid + '"' + ") ");
            }

            // object properties
            Set<OWLObjectPropertyAssertionAxiom> axioms = ontology.getObjectPropertyAssertionAxioms(i);
            for (OWLObjectPropertyAssertionAxiom ax : axioms) {
                String prop = ax.getProperty().asOWLObjectProperty().getIRI().getShortForm();
                String subject = ax.getSubject().asOWLNamedIndividual().getIRI().getShortForm();
                String object = ax.getObject().asOWLNamedIndividual().getIRI().getShortForm();

                result.append(prop + "(\"" + subject + "\", \"" + object + "\") ");
            }
        }

        result.append(".");
        result.append(postfix);

        return result.toString();
    }

    public static void writeFile(OWLOntology ontology, OWLReasoner reasoner, String filename) throws IOException {
        String output = serializeAbox(ontology, reasoner);

        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        writer.write(output);
        writer.close();
    }
}
