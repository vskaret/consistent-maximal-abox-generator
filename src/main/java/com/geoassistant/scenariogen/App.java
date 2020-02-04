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
        boolean printInconsistent = true;

        OntologyPermuter op = new OntologyPermuter();

        op.setDEBUG(debugging);
        op.setPrintInconsistent(printInconsistent);
        op.loadOntology("geo-test.owl");
        //op.loadOntology("example.owl");
        //op.loadOntology("example2.owl");
        //op.loadOntology("example3.owl");

        op.permute();


    }
}
