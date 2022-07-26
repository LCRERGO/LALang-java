package br.ufscar.dc.compiladores;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    public enum TypeLAIdentifier {
        FUNCAO,
        PROCEDIMENTO,
        TIPO,
        VARIAVEL,
        REGISTRO,
        CONSTANTE
    }
    
    public enum TypeLAVariable {
        INTEIRO,
        REAL,
        LITERAL,
        LOGICO,
        INVALIDO,
        PONT_INT,
        PONT_LOG,
        PONT_REA,
        PONT_LIT,
        REGISTRO,
        ENDERECO,
        NAO_DECLARADO
    }
    
    private final Map<String, SymbolTableEntry> table;
    private SymbolTable global;
    
    public SymbolTable() {
        this.table = new HashMap<>();
        this.global = null;
    }
    
    public void setGlobal(SymbolTable global){
        this.global = global;
    }
    
    // add a new variable to symbolTable
    public void add(String name, TypeLAIdentifier identifierType, TypeLAVariable variableType) {
        table.put(name, new SymbolTableEntry(name, identifierType, variableType));
    }
    
    // add a new function frame or register frame to symbolTable
    public void add(String name, TypeLAIdentifier identifierType, TypeLAVariable variableType, SymbolTable argsRegFunc){
        table.put(name, new SymbolTableEntry(name, identifierType, variableType, argsRegFunc));
    }
    
    // add a new return type for functions or registers
    public void add(String name, TypeLAIdentifier identifierType, TypeLAVariable variableType, SymbolTable argsRegFunc, String specialType){
        table.put(name, new SymbolTableEntry(name, identifierType, variableType, argsRegFunc, specialType));
    }
            
    // returns true or false if an identifier exists in the table
    public boolean exists(String name) {
        if(global == null) {
            return table.containsKey(name);
        } else {
            return table.containsKey(name) || global.exists(name);
        }
    }
    
    // returns an entry of the symbol table given a name
    public SymbolTableEntry verify(String name) {
        if(global == null)
            return table.get(name);
        else{
            if(table.containsKey(name))
                return table.get(name);
            else
                return global.verify(name);
        }
    }
    
    // type validation for registers and functions
    public boolean validType(ArrayList<SymbolTable.TypeLAVariable> types){
        int counter = 0;
        
        if(table.size() != types.size())
            return false;
        for(var entry: table.values()){
            if(types.get(counter) != entry.variableType){
                return false;
            }
            counter++;
        }
        
        return true;
    }
}