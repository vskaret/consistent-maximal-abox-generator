package com.geoassistant.scenariogen;

import java.util.ArrayList;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws Exception
    {
        System.out.println( "Hello World!" );

        OntologyPermutator op = new OntologyPermutator();
        op.loadOntology("geo3.owl");

        //ArrayList<String> queries = new ArrayList<>();
        ArrayList<OWLQuery> queries = new ArrayList<>();
        //queries.add(new OWLQuery("Fault", 3));
        //queries.add(new OWLQuery("SealingProperty", 1));
        //queries.add(new OWLQuery("Rock", 2));

        queries.add(new OWLQuery("InDistributaryChannel", 2));
        queries.add(new OWLQuery("InFeederChannel", 2));
        op.permutate();
    }
}
