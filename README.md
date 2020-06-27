# Readme

## Ontology Description
The default ontology is "Small.owl" which has GeoUnits with the attribute Location, Faults with the attribute SealingCapacity and an object property frontOf used to state that a GeoUnit is directly in front of of another GeoUnit. There are three individuals fi of type Fault and four individuals of type GeoUnit gui instantiated: f1, f2, f3, gu5, gu8, gu11, gu14.

## Run Algorithm
The following two steps will compile the source code and run the consistent maximal ABox generator algorithm with the same ontology as for the results presented in the thesis.

* Compile: mvn compile
* Run: mvn exec:java

Warning: the actual computation might take some time. To test with a smaller 

## Editing Input Ontology
The input for the algorithm is hardcoded into the App.java file in src/main/java/com/geoassistant/scenariogen/. To use another ontology, save the owl file into src/main/java/com/geoassistant/owl/ and change the string on line 15 of App.java to the name of your new file. Note that there is a bug in the implementation that makes it sometimes not fetch the individuals. This has something to do with the URI of the ontology.

## Maude Conversions
The serialized ontologies are stored in src/maude/combinations/. To convert one of the ontologies to a Maude configuration with objects, run the following commands in the src/maude/combinations/ folder.

* maude
* load ../owl2.maude
* load proto-scenario1.maude
* red unknowns .
