package com.geoassistant.scenariogen;

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
        to.loadFromFile("geo.owl");

        WorkerOntology wo = new WorkerOntology(to);
        wo.loadOntology();
        wo.generateScenarios();
    }
}
