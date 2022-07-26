package br.ufscar.dc.compiladores;

import java.util.LinkedList;
import java.util.List;

public class Scopes {
    private LinkedList<SymbolTable> tableStack;
    
    public Scopes() {
        tableStack = new LinkedList<>();
        // global scope
        createNewScope();
    }
    
    public void createNewScope() {
        tableStack.push(new SymbolTable());
    }
    
    public SymbolTable getCurrentScope() {
        return tableStack.peek();
    }
    
    public List<SymbolTable> runNestedScopes() {
        return tableStack;
    }
    
    public void abandonScope() {
        tableStack.pop();
    }
}
