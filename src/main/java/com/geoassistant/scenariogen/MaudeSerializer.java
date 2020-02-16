package com.geoassistant.scenariogen;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.util.Set;

public class MaudeSerializer {
    public MaudeSerializer() {
   }

    public static void serializeAbox(OWLOntology ontology) {
        Set<OWLNamedIndividual> individuals = ontology.getIndividualsInSignature();

        for (OWLNamedIndividual i : individuals) {
            String oid = i.getIRI().getShortForm();
            Set<OWLClassAssertionAxiom> instantiations = ontology.getClassAssertionAxioms(i);

            for (OWLClassAssertionAxiom cax : instantiations) {
                String sort = cax.getClassExpression().asOWLClass().getIRI().getShortForm();
                System.out.print(sort + "(" + oid + ") ");
            }
        }

        System.out.println(".");
    }

    public static void serializeAboxLeaves(OWLOntology ontology, OWLReasoner reasoner) {
        Set<OWLNamedIndividual> individuals = ontology.getIndividualsInSignature();

        System.out.println("op unknowns : -> Configuration .");
        System.out.print("eq unknowns() = ");

        for (OWLNamedIndividual i : individuals) {
            String oid = i.getIRI().getShortForm();
            Set<OWLClassAssertionAxiom> instantiations = ontology.getClassAssertionAxioms(i);

            for (OWLClassAssertionAxiom cax : instantiations) {
                if (reasoner.getSubClasses(cax.getClassExpression(), true).isBottomSingleton()) {
                    String sort = cax.getClassExpression().asOWLClass().getIRI().getShortForm();
                    System.out.print(sort + "(" + oid + ") ");
                }
            }
        }

        System.out.println(".");
    }

    public static void serializeTbox() {

    }

    public static void serializeClassHierarchy(OWLOntology ontology, OWLReasoner reasoner, OWLClass root, String unknownClassName) {
        // print current class as sort
        System.out.println("sort " + root.getIRI().getShortForm() + " .");
        recSerializeClassHierarchy(ontology, reasoner, root);
    }

    public static void serializeClassHierarchy(OWLOntology ontology, OWLReasoner reasoner, OWLClass root) {
        // print current class as sort
        System.out.println("sort " + root.getIRI().getShortForm() + " .");
        recSerializeClassHierarchy(ontology, reasoner, root);
        System.out.println();
        recSerializeOperators(ontology, reasoner, root);
    }

    private static void recSerializeClassHierarchy(OWLOntology ontology, OWLReasoner reasoner, OWLClass root) {
        for (OWLClass c : reasoner.getSubClasses(root, true).getFlattened()) {
            if (!c.isOWLNothing()) {
                System.out.println("subsort " + root.getIRI().getShortForm() + " > " + c.getIRI().getShortForm() + " .");

                //if (reasoner.getSubClasses(c, true).isBottomSingleton()) {
                    //System.out.print("op ");
                    //System.out.print(c.getIRI().getShortForm());
                    //System.out.println(" : Oid -> " + c.getIRI().getShortForm() + " .");
                //}

                recSerializeClassHierarchy(ontology, reasoner, c);
            }
        }
    }

    private static void recSerializeOperators(OWLOntology ontology, OWLReasoner reasoner, OWLClass root) {
        for (OWLClass c : reasoner.getSubClasses(root, true).getFlattened()) {
            //System.out.println(c);
            if (!c.isOWLNothing()) {
                String op = "";
                if (reasoner.getSubClasses(c, true).isBottomSingleton()) {
                    String cs = c.getIRI().getShortForm();
                    op += "op " + cs + " : Oid -> " + cs + " .\n";
                    System.out.print(op);

                    //System.out.print("op ");
                    //System.out.print(c.getIRI().getShortForm());
                    //System.out.println(" : Oid -> " + c.getIRI().getShortForm() + " .");
                }

                recSerializeOperators(ontology, reasoner, c);
            }
        }
    }

    public static void serializeObjectProperties(OWLOntology ontology, OWLReasoner reasoner) {
        for (OWLObjectProperty p : ontology.getObjectPropertiesInSignature()) {
            Set<OWLClassExpression> domains = p.getDomains(ontology);
            Set<OWLClassExpression> ranges = p.getRanges(ontology);


            for (OWLClassExpression d : domains) {
                for (OWLClassExpression r : ranges) {
                    System.out.print("op ");
                    System.out.print(p.getIRI().getShortForm());
                    System.out.print(" : ");

                    System.out.print(d.asOWLClass().getIRI().getShortForm());
                    System.out.print(" ");
                    System.out.print(r.asOWLClass().getIRI().getShortForm());

                    System.out.println(" -> ObjectProperty .");
                }
            }
        }
    }
}
