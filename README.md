# LALang
--------
This repository is made for the first 5 tasks of the discipline of compiler
construction that occured during the first semester of 2022 at UFSCar
(Universidade Federal de São Carlos) with professor Daniel Lucrédio.

## Objective
Create a lexer for the language LA that is made by the professors
Jander Moreira, Helena de Medeiros Caseli and Daniel Lucrédio, for the
discipline of Algorithm Construction and Programming.

## Dependencies
To build the project I found that old versions of antlr do not tranpile
correctly for the current go antlr runtime, so I sugest using the most recently
version of each dependency.
- OpenJDK (>= 11)
- Antlr4 (>= 4.10.1)
- Maven (>= 3.6.3)

## Build
To build the Java Archive (JAR) simply run the mvn target.
```bash
mvn package
```
After that a *.jar* will be generated and put under the target build.
With the name *PROJECTNAME-VERSION-jar-with-dependencies.jar*. 

## How to Run
After the jar has been built simply run it over the command line passing
the name of the *input* and *output* files, e.g.:
```
java -jar  PROJECTNAME-VERSION-jar-with-dependencies.jar <input> <output>
```

## Information
Built by Lucas Cruz dos Reis (A.K.A. Dante Frostbyte), R.A.: 754757, in 2022.