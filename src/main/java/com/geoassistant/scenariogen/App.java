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
        OntologyPermuter op = new OntologyPermuter();

        op.setDEBUG(debugging);
        op.loadOntology("Small.owl");
        op.permute();
    }
}
