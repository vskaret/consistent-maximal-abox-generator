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

        OntologyPermuter op = new OntologyPermuter("permutations");

        op.setDEBUG(debugging);
        op.setPrintInconsistent(printInconsistent);
        //op.loadOntology("geo-test.owl");
        //op.loadOntology("geo3.owl");
        //op.loadOntology("example.owl");
        //op.loadOntology("example2.owl");
        //op.loadOntology("example3.owl");
        op.loadOntology("geo-maude3.owl");

        //System.out.println("*********************");
        //op.serializeTbox();
        //System.out.println("*********************");

        op.permute();
        //System.out.println();
        //op.serializeAbox();


    }
}
