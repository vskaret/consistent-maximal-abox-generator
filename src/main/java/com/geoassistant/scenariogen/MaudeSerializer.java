package com.geoassistant.scenariogen;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.io.IOException;
import java.util.Set;
import java.io.PrintWriter;

public class MaudeSerializer {

    static final String prefix = "mod OWL-PERMUTATION is\n" +
            //"  PROTECTING OWL-SORTS .\n";
            "  PROTECTING OWL-CONVERTER .\n";

    static final String postfix = "\nendm";

    public MaudeSerializer() {
   }

   public static void writeAboxToFile(OWLOntology ontology, OWLReasoner reasoner, String filename) {
       //String abox = serializeAbox(ontology);
       String abox = serializeAboxLeaves(ontology, reasoner);
       System.out.print(abox);

       try (PrintWriter writer = new PrintWriter(filename)) {
           writer.write(abox);
       } catch (IOException e) {
           System.out.println("exception");
       }

       /*
       try {
            PrintWriter writer = new PrintWriter(filename);
            writer.print(abox);
            writer.close();
        } catch(IOException e) {
           System.out.println("exception");
       } finally {
       }
        */
   }

    public static String serializeAbox(OWLOntology ontology) {
        Set<OWLNamedIndividual> individuals = ontology.getIndividualsInSignature();
        //String result = prefix;
        StringBuffer result = new StringBuffer(prefix);
        result.append("  op unknowns : -> Configuration .\n");
        result.append("  eq unknowns() = ");
        //System.out.println("op unknowns : -> Configuration .");
        //System.out.print("eq unknowns() = ");

        for (OWLNamedIndividual i : individuals) {
            String oid = i.getIRI().getShortForm();
            Set<OWLClassAssertionAxiom> instantiations = ontology.getClassAssertionAxioms(i);

            for (OWLClassAssertionAxiom cax : instantiations) {
                String sort = cax.getClassExpression().asOWLClass().getIRI().getShortForm();
                result.append(sort + "(" + oid + ") ");
                //result += sort + "(" + oid + ") ";
                //System.out.print(sort + "(" + oid + ") ");
            }
        }

        //System.out.println(".");
        //System.out.println(postfix);
        //result += "." + postfix;
        result.append("." + postfix);

        return result.toString();
    }

    //public static void serializeAboxLeaves(OWLOntology ontology, OWLReasoner reasoner) {
    public static String serializeAboxLeaves(OWLOntology ontology, OWLReasoner reasoner) {
        Set<OWLNamedIndividual> individuals = ontology.getIndividualsInSignature();
        StringBuffer result = new StringBuffer(prefix);
        result.append("op unknowns : -> Configuration .\n");
        result.append("eq unknowns() = ");

        for (OWLNamedIndividual i : individuals) {
            String oid = i.getIRI().getShortForm();
            Set<OWLClassAssertionAxiom> instantiations = ontology.getClassAssertionAxioms(i);

            for (OWLClassAssertionAxiom cax : instantiations) {
                if (reasoner.getSubClasses(cax.getClassExpression(), true).isBottomSingleton()) {
                    String sort = cax.getClassExpression().asOWLClass().getIRI().getShortForm();
                    //System.out.print(sort + "(" + oid + ") ");
                    result.append("type(" + sort + ", " + oid + ") ");
                }
            }
        }

        result.append(".");
        result.append(postfix);

        return result.toString();
    }

    public static void serializeTbox() {

    }

    public static void serializeClassHierarchy(OWLOntology ontology, OWLReasoner reasoner, OWLClass root, String unknownClassName) {
        // print current class as sort
        System.out.println("mod OWL-SORTS is");
        System.out.println("sort " + root.getIRI().getShortForm() + " .");
        recSerializeClassHierarchy(ontology, reasoner, root);
        System.out.println("endm");
    }

    public static void serializeClassHierarchy(OWLOntology ontology, OWLReasoner reasoner, OWLClass root) {
        // print current class as sort
        System.out.println("mod OWL-SORTS is");
        //System.out.println("sort " + root.getIRI().getShortForm() + " .");
        //recSerializeClassHierarchy(ontology, reasoner, root);
        //System.out.println();
        recSerializeOperators(ontology, reasoner, root);
        System.out.println("endm");
    }

    private static void recSerializeClassHierarchy(OWLOntology ontology, OWLReasoner reasoner, OWLClass root) {
        for (OWLClass c : reasoner.getSubClasses(root, true).getFlattened()) {
            if (!c.isOWLNothing()) {
                System.out.println("subsort " + "OWL" + root.getIRI().getShortForm() + " > " + "OWL" + c.getIRI().getShortForm() + " .");

                //if (reasoner.getSubClasses(c, true).isBottomSingleton()) {
                    //System.out.print("op ");
                    //System.out.print(c.getIRI().getShortForm());
                    //System.out.println(" : Oid -> " + c.getIRI().getShortForm() + " .");
                //}

                recSerializeClassHierarchy(ontology, reasoner, c);
            }
        }
    }

    // Automatically doing this does not work because we need to specify the Maude sort to set it to
    private static void recSerializeOperators(OWLOntology ontology, OWLReasoner reasoner, OWLClass root) {
        for (OWLClass c : reasoner.getSubClasses(root, true).getFlattened()) {
            //System.out.println(c);

            //if (reasoner.getSubClasses(cax.getClassExpression(), true).isBottomSingleton()) {
                //String sort = cax.getClassExpression().asOWLClass().getIRI().getShortForm();
                //System.out.print(sort + "(" + oid + ") ");
                //result.append("type(" + sort + ", " + oid + ") ");
            //}

            //if (!c.isOWLNothing()) {
            if (reasoner.getSubClasses(c, true).isBottomSingleton()) {
                String op = "";
                //if (reasoner.getSubClasses(c, true).isBottomSingleton()) {
                String cs = c.getIRI().getShortForm();
                //op += "op " + cs + " : Oid -> " + "OWL" + cs + " .\n";
                op = "op " + cs + " : Oid -> LeafClass . \n";
                System.out.print(op);

                //System.out.print("op ");
                //System.out.print(c.getIRI().getShortForm());
                //System.out.println(" : Oid -> " + c.getIRI().getShortForm() + " .");
                //}

            } else {
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
