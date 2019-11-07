package com.geoassistant.scenariogen;

import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;

import java.util.ArrayList;
import java.util.Set;

public class OWLQuery {

    private String query;
    private int count;

    private ArrayList<OWLClassAssertionAxiom> queryResult = new ArrayList<>();

    public OWLQuery(String query, int count) {
        this.query = query;
        this.count = count;
    }

    public void countDown() {
        this.count--;
    }

    public void countUp() {
        this.count++;
    }

    public void setResult(Set<OWLClassAssertionAxiom> result) {
        Set<OWLClassAssertionAxiom> r = result;

        for (OWLClassAssertionAxiom a : r) {
            this.queryResult.add(a);
        }
    }

    public ArrayList<OWLClassAssertionAxiom> getResults() {
        return this.queryResult;
    }

    public String getQuery() {
        return this.query;
    }

    public int getCount() {
        return this.count;
    }

    public boolean finished() {
        return count <= 0;
    }

}
