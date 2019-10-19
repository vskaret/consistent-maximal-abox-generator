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
        to.loadFromFile("geo2.owl");

        WorkerOntology wo = new WorkerOntology(to);
        wo.loadOntology();

        ArrayList<String> queries = new ArrayList<>();
        queries.add("Fault");
        // queries.add("SealingProperty"); // gives exception
        wo.generateScenarios(queries);
    }
}
