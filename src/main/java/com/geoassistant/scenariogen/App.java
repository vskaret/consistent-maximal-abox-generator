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

        TemplateOntology to = new TemplateOntology();
        to.loadFromFile("geo3.owl");

        //to.getObjectProperties();

        WorkerOntology wo = new WorkerOntology(to);
        wo.loadOntology();

        //ArrayList<String> queries = new ArrayList<>();
        ArrayList<OWLQuery> queries = new ArrayList<>();
        //queries.add(new OWLQuery("Fault", 3));
        //queries.add(new OWLQuery("SealingProperty", 1));
        //queries.add(new OWLQuery("Rock", 2));

        queries.add(new OWLQuery("InDistributaryChannel", 2));
        queries.add(new OWLQuery("InFeederChannel", 2));
        //wo.generateScenario(queries);
        wo.permutate();
    }
}
