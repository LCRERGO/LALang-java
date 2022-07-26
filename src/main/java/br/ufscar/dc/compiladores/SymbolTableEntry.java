package br.ufscar.dc.compiladores;

class SymbolTableEntry {
    String name;
    SymbolTable.TypeLAIdentifier identifierType;
    SymbolTable.TypeLAVariable variableType;
    // Function parameters or Register Fields
    SymbolTable argsRegFunc;
    // Function or Register type
    String specialType;

    // Constructor for a simple variable
    public SymbolTableEntry(String name, SymbolTable.TypeLAIdentifier identifierType,
            SymbolTable.TypeLAVariable variableType) {
        this.name = name;
        this.variableType = variableType;
        this.identifierType = identifierType;
    }

    // Construction for Function parameters or register fields
    public SymbolTableEntry(String name, SymbolTable.TypeLAIdentifier identifierType,
            SymbolTable.TypeLAVariable variableType, SymbolTable argsRegFunc) {
        this.name = name;
        this.variableType = variableType;
        this.identifierType = identifierType;
        this.argsRegFunc = argsRegFunc;
    }

    // Constructor for function return or register type
    public SymbolTableEntry(String name, SymbolTable.TypeLAIdentifier identifierType,
            SymbolTable.TypeLAVariable variableType, SymbolTable argsRegFunc, String specialType) {
        this.name = name;
        this.variableType = variableType;
        this.identifierType = identifierType;
        this.argsRegFunc = argsRegFunc;
        this.specialType = specialType;
    }

}
