package com.geoassistant.scenariogen;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws Exception
    {
        boolean debugging = false;
        boolean printInconsistent = true;

        OntologyPermuter op = new OntologyPermuter();

        op.setDEBUG(debugging);
        op.setPrintInconsistent(printInconsistent);
        op.loadOntology("geo-maude3.owl");
        op.permute();
    }
}
