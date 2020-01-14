package com.geoassistant.scenariogen;

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

        OntologyPermuter op = new OntologyPermuter(debugging);
        op.loadOntology("geo-test.owl");
        op.permute();
    }
}
