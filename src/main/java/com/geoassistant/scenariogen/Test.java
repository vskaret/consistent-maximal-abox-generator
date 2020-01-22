package com.geoassistant.scenariogen;

//import org.semanticweb.owlapi.model.OWLClass;
//import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.*;

import java.lang.reflect.Array;
import java.util.HashSet;
import java.util.List;
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
        //t.setDEBUG(debugging);
        //t.loadOntology("geo-test.owl");
        //t.setThing();

        // tests
        /*
        t.testIsLeafClass();
        System.out.println();
        t.testAllLeafClasses();
        System.out.println();
         */
        //t.testUtilPermute();
        //t.testNonEmptySubsetsOf();
        t.testSubsetOf();
    }

    /**
     * Tests Util.subSetOf()
     */
    public void testSubsetOf() {
        Set<String> set1 = new HashSet<>();
        set1.add("a");
        set1.add("b");
        set1.add("c");

        Set<Set<String>> setofsets = new HashSet<>();
        setofsets.add(set1);

        Set<String> set2 = new HashSet<>();
        set2.add("c");

        Set<String> set3 = new HashSet<>();
        set3.add("d");

        System.out.println(Utils.subsetOf(set2, setofsets));
        System.out.println(Utils.subsetOf(set3, setofsets));
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
    public void testAllLeafClasses() throws Exception {
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

    /**
     * Test of the permuteList method in class Utils
     */
    public void testUtilPermute() {
        ArrayList<String> a1 = new ArrayList<>();
        ArrayList<String> a2 = new ArrayList<>();
        ArrayList<String> a3 = new ArrayList<>();

        a1.add("a");
        a1.add("b");
        a2.add("c");
        a2.add("d");
        a2.add("e");
        a3.add("f");
        a3.add("g");
        a3.add("h");

        ArrayList<List<String>> listOfLists = new ArrayList<>();
        listOfLists.add(a1);
        listOfLists.add(a2);
        listOfLists.add(a3);

        System.out.println(Utils.permuteList(a2));
        System.out.println(Utils.permuteLists(listOfLists));

        ArrayList<Integer> b1 = new ArrayList<>();
        ArrayList<Integer> b2 = new ArrayList<>();
        ArrayList<Integer> b3 = new ArrayList<>();

        b1.add(1);
        b1.add(2);
        b1.add(3);
        b1.add(4);
        b2.add(1);
        b2.add(2);
        b2.add(3);
        b2.add(4);
        b3.add(5);
        b3.add(6);

        ArrayList<List<Integer>> l = new ArrayList<>();
        l.add(b1);
        l.add(b2);
        l.add(b3);

        System.out.println(Utils.permuteLists(l));
    }

    public void testNonEmptySubsetsOf() {
        Set<String> test = new HashSet<>();
        test.add("A");
        test.add("B");
        test.add("C");
        test.add("D");
        //test.add("E");

        System.out.println(Utils.nonEmptySubsetsOf(test));
        System.out.println(Utils.nonEmptySubsetsOf(test).size());
    }
}

