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
        //System.out.println( "Hello World!" );
        boolean debugging = false;

        OntologyPermutator op = new OntologyPermutator(debugging);
        op.loadOntology("geo-test.owl");
        op.permutate();
    }
}
