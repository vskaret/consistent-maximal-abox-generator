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
        op.loadOntology("geo-test.owl");
        op.permutate();
    }
}
