package com.geoassistant.scenariogen;

//import org.semanticweb.owlapi.model.OWLClass;
//import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.*;

import java.util.Set;
import java.util.ArrayList;

/**
 * Class for testing purposes.
 */

public class Test extends OntologyPermuter {

    OWLClass thing;

    public Test() {
    }

    public void setThing() {
        this.thing = factory.getOWLThing();
    }

    public static void main(String args[]) throws Exception {
        boolean debugging = false;

        Test t = new Test();
        t.setDEBUG(debugging);
        t.loadOntology("geo-test.owl");
        t.setThing();

        // tests
        //t.testIsLeafClass();
        System.out.println();
        t.testAllLeafClasses();
        System.out.println();
        //t.testTopSuperClassOf();
    }

    /**
     * For each class, print if true if it's a leaf class otherwise false.
     *
     * @throws Exception
     */
    public void testIsLeafClass() throws Exception {
        Set<OWLClass> classes = ontology.getClassesInSignature();

        System.out.println("Start leaf class test");
        for (OWLClass c : classes) {
            System.out.println(c + " : " + isLeafClass(c));
        }
        System.out.println("End leaf class test");
    }

    /**
     * Prints all leaf classes of the ontology.
     */
    public void testAllLeafClasses() {
        //ArrayList<OWLClassExpression> leafSubClasses = allLeafSubClasses(thing);
        System.out.println("All leaf classes of the ontology:");
        for (OWLClass c : ontology.getClassesInSignature()) {
            ArrayList<ArrayList<OWLClassExpression>> leafLists = allLeafSubClasses(c);

            System.out.println("Leaf classes of " + c);
            for (ArrayList<OWLClassExpression> leafClasses : leafLists) {
                System.out.println(leafClasses);
                //for (OWLClassExpression c : leafSubClasses) {
                //System.out.println(c);
                //}
            }
            System.out.println("End of " + c + " leaf classes");
        }
        System.out.println("End of ontology's leaf classes");
    }

    /**
     * For each leaf class in the ontology, print its top super class.
     * @throws Exception
     */
    /*
    public void testTopSuperClassOf() throws Exception {
        ArrayList<OWLClassExpression> leafSubClasses = allLeafSubClasses(thing);

        for (OWLClassExpression leaf : leafSubClasses) {
            System.out.println(leaf + " : " + topSuperClassOf(leaf.asOWLClass()));
        }
    }
     */
}

