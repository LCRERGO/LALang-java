package br.ufscar.dc.compiladores;

import br.ufscar.dc.compiladores.SymbolTable.TypeLAVariable;

import java.util.ArrayList;

public class LASemantic extends LABaseVisitor<Void> {
    Scopes nestedScopes = new Scopes();

    // Program entrypoint method
    @Override
    public Void visitPrograma(LAParser.ProgramaContext ctx) {
        for (var ctxCmd : ctx.corpo().cmd()) {
            if (ctxCmd.cmdRetorne() != null) {
                LASemanticUtils.addSemanticError(ctxCmd.cmdRetorne().getStart(),
                        "comando retorne nao permitido nesse escopo\n");
            }
        }

        for (var ctxDec : ctx.declaracoes().decl_local_global()) {
            if (ctxDec.declaracao_global() != null && ctxDec.declaracao_global().tipo_estendido() == null) {
                for (var ctxCmd : ctxDec.declaracao_global().cmd()) {
                    if (ctxCmd.cmdRetorne() != null)
                        LASemanticUtils.addSemanticError(ctxCmd.cmdRetorne().getStart(),
                                "comando retorne nao permitido nesse escopo\n");
                }
            }
        }

        return super.visitPrograma(ctx);
    }

    // visitDeclaracao_global builds procedures and functions building it's own stack frame
    // for each subprogram
    @Override
    public Void visitDeclaracao_global(LAParser.Declaracao_globalContext ctx) {
        var identifier = ctx.IDENT().getText();

        // Geting scopes
        var scopes = nestedScopes.runNestedScopes();
        if (scopes.size() > 1) {
            nestedScopes.abandonScope();
        }
        var globalScope = nestedScopes.getCurrentScope();

        if (ctx.tipo_estendido() != null) {
            // Inside a function
            nestedScopes.createNewScope();
            var functionScope = nestedScopes.getCurrentScope();
            functionScope.setGlobal(globalScope);

            if (globalScope.exists(identifier)) {
                LASemanticUtils.addSemanticError(ctx.IDENT().getSymbol(),
                        "identifier " + identifier + " ja declarado anteriormente\n");
            } else {
                var parameters = new SymbolTable();
                globalScope.add(identifier, SymbolTable.TypeLAIdentifier.FUNCAO, null, parameters,
                        ctx.tipo_estendido().getText());

                for (var parameter : ctx.parametros().parametro()) {
                    var variableType = parameter.tipo_estendido().getText();
                    for (var ident : parameter.identificador()) {
                        var parameterName = ident.getText();
                        if (functionScope.exists(parameterName)) {
                            LASemanticUtils.addSemanticError(ident.IDENT(0).getSymbol(),
                                    "identifier " + parameterName + " ja declarado anteriormente\n");
                        } else {
                            switch (variableType) {
                                case "inteiro":
                                    functionScope.add(parameterName,
                                            SymbolTable.TypeLAIdentifier.VARIAVEL, TypeLAVariable.INTEIRO);
                                    parameters.add(parameterName, SymbolTable.TypeLAIdentifier.VARIAVEL,
                                            TypeLAVariable.INTEIRO);
                                    break;
                                case "literal":
                                    functionScope.add(parameterName,
                                            SymbolTable.TypeLAIdentifier.VARIAVEL, TypeLAVariable.LITERAL);
                                    parameters.add(parameterName, SymbolTable.TypeLAIdentifier.VARIAVEL,
                                            TypeLAVariable.LITERAL);
                                    break;
                                case "real":
                                    functionScope.add(parameterName,
                                            SymbolTable.TypeLAIdentifier.VARIAVEL, TypeLAVariable.REAL);
                                    parameters.add(parameterName, SymbolTable.TypeLAIdentifier.VARIAVEL,
                                            TypeLAVariable.REAL);
                                    break;
                                case "logico":
                                    functionScope.add(parameterName,
                                            SymbolTable.TypeLAIdentifier.VARIAVEL, TypeLAVariable.LOGICO);
                                    parameters.add(parameterName, SymbolTable.TypeLAIdentifier.VARIAVEL,
                                            TypeLAVariable.LOGICO);
                                    break;
                                case "^logico":
                                    functionScope.add(parameterName,
                                            SymbolTable.TypeLAIdentifier.VARIAVEL, TypeLAVariable.PONT_LOG);
                                    parameters.add(parameterName, SymbolTable.TypeLAIdentifier.VARIAVEL,
                                            TypeLAVariable.PONT_LOG);
                                    break;
                                case "^real":
                                    functionScope.add(parameterName,
                                            SymbolTable.TypeLAIdentifier.VARIAVEL, TypeLAVariable.PONT_REA);
                                    parameters.add(parameterName, SymbolTable.TypeLAIdentifier.VARIAVEL,
                                            TypeLAVariable.PONT_REA);
                                    break;
                                case "^literal":
                                    functionScope.add(parameterName,
                                            SymbolTable.TypeLAIdentifier.VARIAVEL, TypeLAVariable.PONT_LIT);
                                    parameters.add(parameterName, SymbolTable.TypeLAIdentifier.VARIAVEL,
                                            TypeLAVariable.PONT_LIT);
                                    break;
                                case "^inteiro":
                                    functionScope.add(parameterName,
                                            SymbolTable.TypeLAIdentifier.VARIAVEL, TypeLAVariable.PONT_INT);
                                    parameters.add(parameterName, SymbolTable.TypeLAIdentifier.VARIAVEL,
                                            TypeLAVariable.PONT_INT);
                                    break;
                                default:
                                    if (globalScope.exists(variableType) && globalScope.verify(
                                            variableType).identifierType == SymbolTable.TypeLAIdentifier.TIPO) {
                                        if (functionScope.exists(parameterName)) {
                                            LASemanticUtils.addSemanticError(ident.IDENT(0).getSymbol(),
                                                    "identifier " + parameterName + " ja declarado anteriormente\n");
                                        } else {
                                            var fields = globalScope.verify(variableType);
                                            var nestedTableType = fields.argsRegFunc;

                                            functionScope.add(parameterName,
                                                    SymbolTable.TypeLAIdentifier.REGISTRO,
                                                    SymbolTable.TypeLAVariable.REGISTRO, nestedTableType,
                                                    variableType);
                                            parameters.add(parameterName,
                                                    SymbolTable.TypeLAIdentifier.REGISTRO,
                                                    SymbolTable.TypeLAVariable.REGISTRO, nestedTableType,
                                                    variableType);
                                        }
                                    }
                                    if (!globalScope.exists(variableType)) {
                                        LASemanticUtils.addSemanticError(ident.IDENT(0).getSymbol(),
                                                "tipo " + variableType + " nao declarado\n");
                                        functionScope.add(parameterName,
                                                SymbolTable.TypeLAIdentifier.VARIAVEL,
                                                SymbolTable.TypeLAVariable.INVALIDO);
                                        parameters.add(parameterName,
                                                SymbolTable.TypeLAIdentifier.VARIAVEL,
                                                SymbolTable.TypeLAVariable.INVALIDO);
                                    }
                                    break;
                            }
                        }
                    }
                }
            }
        } else {
            // if a procedure
            nestedScopes.createNewScope();
            var functionScope = nestedScopes.getCurrentScope();
            functionScope.setGlobal(globalScope);
            if (globalScope.exists(identifier)) {
                LASemanticUtils.addSemanticError(ctx.IDENT().getSymbol(),
                        "identifier " + identifier + " ja declarado anteriormente\n");
            } else {
                var parameters = new SymbolTable();
                globalScope.add(identifier, SymbolTable.TypeLAIdentifier.PROCEDIMENTO, null,
                        parameters);
                for (var parameter : ctx.parametros().parametro()) {
                    var variableType = parameter.tipo_estendido().getText();
                    for (var ident : parameter.identificador()) {
                        var parameterName = ident.getText();
                        if (functionScope.exists(parameterName)) {
                            LASemanticUtils.addSemanticError(ident.IDENT(0).getSymbol(),
                                    "identifier " + parameterName + " ja declarado anteriormente\n");
                        } else {
                            switch (variableType) {
                                case "inteiro":
                                    functionScope.add(parameterName,
                                            SymbolTable.TypeLAIdentifier.VARIAVEL, TypeLAVariable.INTEIRO);
                                    parameters.add(parameterName, SymbolTable.TypeLAIdentifier.VARIAVEL,
                                            TypeLAVariable.INTEIRO);
                                    break;
                                case "literal":
                                    functionScope.add(parameterName,
                                            SymbolTable.TypeLAIdentifier.VARIAVEL, TypeLAVariable.LITERAL);
                                    parameters.add(parameterName, SymbolTable.TypeLAIdentifier.VARIAVEL,
                                            TypeLAVariable.LITERAL);
                                    break;
                                case "real":
                                    functionScope.add(parameterName,
                                            SymbolTable.TypeLAIdentifier.VARIAVEL, TypeLAVariable.REAL);
                                    parameters.add(parameterName, SymbolTable.TypeLAIdentifier.VARIAVEL,
                                            TypeLAVariable.REAL);
                                    break;
                                case "logico":
                                    functionScope.add(parameterName,
                                            SymbolTable.TypeLAIdentifier.VARIAVEL, TypeLAVariable.LOGICO);
                                    parameters.add(parameterName, SymbolTable.TypeLAIdentifier.VARIAVEL,
                                            TypeLAVariable.LOGICO);
                                    break;
                                case "^logico":
                                    functionScope.add(parameterName,
                                            SymbolTable.TypeLAIdentifier.VARIAVEL, TypeLAVariable.PONT_LOG);
                                    parameters.add(parameterName, SymbolTable.TypeLAIdentifier.VARIAVEL,
                                            TypeLAVariable.PONT_LOG);
                                    break;
                                case "^real":
                                    functionScope.add(parameterName,
                                            SymbolTable.TypeLAIdentifier.VARIAVEL, TypeLAVariable.PONT_REA);
                                    parameters.add(parameterName, SymbolTable.TypeLAIdentifier.VARIAVEL,
                                            TypeLAVariable.PONT_REA);
                                    break;
                                case "^literal":
                                    functionScope.add(parameterName,
                                            SymbolTable.TypeLAIdentifier.VARIAVEL, TypeLAVariable.PONT_LIT);
                                    parameters.add(parameterName, SymbolTable.TypeLAIdentifier.VARIAVEL,
                                            TypeLAVariable.PONT_LIT);
                                    break;
                                case "^inteiro":
                                    functionScope.add(parameterName,
                                            SymbolTable.TypeLAIdentifier.VARIAVEL, TypeLAVariable.PONT_INT);
                                    parameters.add(parameterName, SymbolTable.TypeLAIdentifier.VARIAVEL,
                                            TypeLAVariable.PONT_INT);
                                    break;
                                default:
                                    if (globalScope.exists(variableType) && globalScope.verify(
                                            variableType).identifierType == SymbolTable.TypeLAIdentifier.TIPO) {
                                        if (functionScope.exists(parameterName)) {
                                            LASemanticUtils.addSemanticError(ident.IDENT(0).getSymbol(),
                                                    "identifier " + parameterName + " ja declarado anteriormente\n");
                                        } else {
                                            SymbolTableEntry fields = globalScope.verify(variableType);
                                            SymbolTable nestedTableType = fields.argsRegFunc;

                                            functionScope.add(parameterName,
                                                    SymbolTable.TypeLAIdentifier.REGISTRO,
                                                    SymbolTable.TypeLAVariable.REGISTRO, nestedTableType,
                                                    variableType);
                                            parameters.add(parameterName,
                                                    SymbolTable.TypeLAIdentifier.REGISTRO,
                                                    SymbolTable.TypeLAVariable.REGISTRO, nestedTableType,
                                                    variableType);
                                        }
                                    }
                                    if (!globalScope.exists(variableType)) {
                                        LASemanticUtils.addSemanticError(ident.IDENT(0).getSymbol(),
                                                "tipo " + variableType + " nao declarado\n");
                                        functionScope.add(parameterName,
                                                SymbolTable.TypeLAIdentifier.VARIAVEL,
                                                SymbolTable.TypeLAVariable.INVALIDO);
                                        parameters.add(parameterName,
                                                SymbolTable.TypeLAIdentifier.VARIAVEL,
                                                SymbolTable.TypeLAVariable.INVALIDO);
                                    }
                                    break;
                            }
                        }
                    }
                }
            }
        }

        return super.visitDeclaracao_global(ctx);
    }


    // visitCorpo tidies the scopes to make only
    // the globalScope visible
    @Override
    public Void visitCorpo(LAParser.CorpoContext ctx) {
        var scopes = nestedScopes.runNestedScopes();
        if (scopes.size() > 1) {
            nestedScopes.abandonScope();
        }

        return super.visitCorpo(ctx);
    }

    // visitDeclaracao_local treats the declaration of variables, constants 
    // and types
    @Override
    public Void visitDeclaracao_local(LAParser.Declaracao_localContext ctx) {
        if (ctx.IDENT() != null) {
            var identifier = ctx.IDENT().getText();
            var currentScope = nestedScopes.getCurrentScope();

            if (ctx.tipo_basico() != null) { 
                // constant declaration
                // 'constante' IDENT ':' tipo_basico '=' valor_constante
                if (currentScope.exists(identifier)) {
                    LASemanticUtils.addSemanticError(ctx.IDENT().getSymbol(),
                            "identifier " + identifier + " ja declarado anteriormente\n");
                } else {
                    var constantType = ctx.tipo_basico().getText();
                    switch (constantType) {
                        case "inteiro":
                            currentScope.add(identifier, SymbolTable.TypeLAIdentifier.CONSTANTE,
                                    TypeLAVariable.INTEIRO);
                            break;
                        case "literal":
                            currentScope.add(identifier, SymbolTable.TypeLAIdentifier.CONSTANTE,
                                    TypeLAVariable.LITERAL);
                            break;
                        case "real":
                            currentScope.add(identifier, SymbolTable.TypeLAIdentifier.CONSTANTE,
                                    TypeLAVariable.REAL);
                            break;
                        case "logico":
                            currentScope.add(identifier, SymbolTable.TypeLAIdentifier.CONSTANTE,
                                    TypeLAVariable.LOGICO);
                            break;
                        default:
                            // never reached
                            break;
                    }
                }
            } else {
                // type declaration
                // 'tipo' IDENT ':' tipo
                if (currentScope.exists(identifier)) {
                    LASemanticUtils.addSemanticError(ctx.IDENT().getSymbol(),
                            "identifier " + identifier + " ja declarado anteriormente\n");
                } else {
                    var fieldsTypes = new SymbolTable();
                    currentScope.add(identifier, SymbolTable.TypeLAIdentifier.TIPO, null, fieldsTypes);
                    for (var variable : ctx.tipo().registro().variavel()) {
                        for (var ctxIdentVariable : variable.identificador()) {
                            var variableIdentifier = ctxIdentVariable.getText();
                            if (fieldsTypes.exists(variableIdentifier)) {
                                LASemanticUtils.addSemanticError(ctxIdentVariable.IDENT(0).getSymbol(),
                                        "identificador " + variableIdentifier + " ja declarado anteriormente\n");
                            } else {
                                var variableType = variable.tipo().getText();
                                switch (variableType) {
                                    case "inteiro":
                                        fieldsTypes.add(variableIdentifier,
                                                SymbolTable.TypeLAIdentifier.VARIAVEL, TypeLAVariable.INTEIRO);
                                        break;
                                    case "literal":
                                        fieldsTypes.add(variableIdentifier,
                                                SymbolTable.TypeLAIdentifier.VARIAVEL, TypeLAVariable.LITERAL);
                                        break;
                                    case "real":
                                        fieldsTypes.add(variableIdentifier,
                                                SymbolTable.TypeLAIdentifier.VARIAVEL, TypeLAVariable.REAL);
                                        break;
                                    case "logico":
                                        fieldsTypes.add(variableIdentifier,
                                                SymbolTable.TypeLAIdentifier.VARIAVEL, TypeLAVariable.LOGICO);
                                        break;
                                    case "^logico":
                                        fieldsTypes.add(variableIdentifier,
                                                SymbolTable.TypeLAIdentifier.VARIAVEL, TypeLAVariable.PONT_LOG);
                                        break;
                                    case "^real":
                                        fieldsTypes.add(variableIdentifier,
                                                SymbolTable.TypeLAIdentifier.VARIAVEL, TypeLAVariable.PONT_REA);
                                        break;
                                    case "^literal":
                                        fieldsTypes.add(variableIdentifier,
                                                SymbolTable.TypeLAIdentifier.VARIAVEL, TypeLAVariable.PONT_LIT);
                                        break;
                                    case "^inteiro":
                                        fieldsTypes.add(variableIdentifier,
                                                SymbolTable.TypeLAIdentifier.VARIAVEL, TypeLAVariable.PONT_INT);
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // variable declaration
            // 'declare' variavel
            if (ctx.variavel().tipo().registro() == null) {
                for (var ctxIdentVariable : ctx.variavel().identificador()) {
                    var variableIdentifier = "";
                    for (var ident : ctxIdentVariable.IDENT())
                        variableIdentifier += ident.getText();
                    var currentScope = nestedScopes.getCurrentScope();

                    if (ctxIdentVariable.dimensao() != null)
                        // dimension exists
                        for (var expDim : ctxIdentVariable.dimensao().exp_aritmetica())
                            LASemanticUtils.verifyType(currentScope, expDim);

                    // Caso o identificador da variavel ja esteja sendo usada.
                    if (currentScope.exists(variableIdentifier)) {
                        LASemanticUtils.addSemanticError(ctxIdentVariable.IDENT(0).getSymbol(),
                                "identificador " + variableIdentifier + " ja declarado anteriormente\n");
                    } else {
                        var variableType = ctx.variavel().tipo().getText();
                        switch (variableType) {
                            case "inteiro":
                                currentScope.add(variableIdentifier,
                                        SymbolTable.TypeLAIdentifier.VARIAVEL, TypeLAVariable.INTEIRO);
                                break;
                            case "literal":
                                currentScope.add(variableIdentifier,
                                        SymbolTable.TypeLAIdentifier.VARIAVEL, TypeLAVariable.LITERAL);
                                break;
                            case "real":
                                currentScope.add(variableIdentifier,
                                        SymbolTable.TypeLAIdentifier.VARIAVEL, TypeLAVariable.REAL);
                                break;
                            case "logico":
                                currentScope.add(variableIdentifier,
                                        SymbolTable.TypeLAIdentifier.VARIAVEL, TypeLAVariable.LOGICO);
                                break;
                            case "^logico":
                                currentScope.add(variableIdentifier,
                                        SymbolTable.TypeLAIdentifier.VARIAVEL, TypeLAVariable.PONT_LOG);
                                break;
                            case "^real":
                                currentScope.add(variableIdentifier,
                                        SymbolTable.TypeLAIdentifier.VARIAVEL, TypeLAVariable.PONT_REA);
                                break;
                            case "^literal":
                                currentScope.add(variableIdentifier,
                                        SymbolTable.TypeLAIdentifier.VARIAVEL, TypeLAVariable.PONT_LIT);
                                break;
                            case "^inteiro":
                                currentScope.add(variableIdentifier,
                                        SymbolTable.TypeLAIdentifier.VARIAVEL, TypeLAVariable.PONT_INT);
                                break;
                            default: 
                                // not a basic/primitive type
                                if (currentScope.exists(variableType) && currentScope.verify(
                                        variableType).identifierType == SymbolTable.TypeLAIdentifier.TIPO) {
                                    if (currentScope.exists(variableIdentifier)) {
                                        LASemanticUtils.addSemanticError(ctxIdentVariable.IDENT(0).getSymbol(),
                                                "identificador "
                                                        + variableIdentifier + " ja declarado anteriormente\n");
                                    } else {
                                        var entry = currentScope.verify(variableType);
                                        var fieldsType = entry.argsRegFunc;
                                        currentScope.add(variableIdentifier,
                                                SymbolTable.TypeLAIdentifier.REGISTRO, null, fieldsType,
                                                variableType);
                                    }
                                }

                                if (!currentScope.exists(variableType)) {
                                    LASemanticUtils.addSemanticError(ctxIdentVariable.IDENT(0).getSymbol(),
                                            "tipo " + variableType + " nao declarado\n");
                                    currentScope.add(variableIdentifier,
                                            SymbolTable.TypeLAIdentifier.VARIAVEL,
                                            SymbolTable.TypeLAVariable.INVALIDO);
                                }
                                break;
                        }
                    }
                }
            } else {
                // Register with type declaration
                ArrayList<String> registerIdentifiers = new ArrayList<>();
                for (var ctxIdentReg : ctx.variavel().identificador()) {
                    var identifierName = ctxIdentReg.getText();
                    var currentScope = nestedScopes.getCurrentScope();

                    if (currentScope.exists(identifierName)) {
                        // identifier must be unique 
                        LASemanticUtils.addSemanticError(ctxIdentReg.IDENT(0).getSymbol(),
                                "identificador " + identifierName + " ja declarado anteriormente\n");
                    } else {
                        var fields = new SymbolTable();
                        currentScope.add(identifierName, SymbolTable.TypeLAIdentifier.REGISTRO, null,
                                fields, null);
                        registerIdentifiers.add(identifierName);
                    }
                }

                for (var ctxVariableRegister : ctx.variavel().tipo().registro().variavel()) {
                    // populate register context
                    for (var ctxVariableRegisterIdent : ctxVariableRegister.identificador()) {
                        var registerFieldName = ctxVariableRegisterIdent.getText();
                        var currentScope = nestedScopes.getCurrentScope();

                        for (var registerIdentifier : registerIdentifiers) {
                            var entry = currentScope.verify(registerIdentifier);
                            var registerFields = entry.argsRegFunc;

                            if (registerFields.exists(registerFieldName)) {
                                LASemanticUtils.addSemanticError(ctxVariableRegisterIdent.IDENT(0).getSymbol(),
                                        "identificador " + registerFieldName + " ja declarado anteriormente\n");
                            } else {
                                var variableType = ctxVariableRegister.tipo().getText();

                                switch (variableType) {
                                    case "inteiro":
                                        registerFields.add(registerFieldName,
                                                SymbolTable.TypeLAIdentifier.VARIAVEL, TypeLAVariable.INTEIRO);
                                        break;
                                    case "literal":
                                        registerFields.add(registerFieldName,
                                                SymbolTable.TypeLAIdentifier.VARIAVEL, TypeLAVariable.LITERAL);
                                        break;
                                    case "real":
                                        registerFields.add(registerFieldName,
                                                SymbolTable.TypeLAIdentifier.VARIAVEL, TypeLAVariable.REAL);
                                        break;
                                    case "logico":
                                        registerFields.add(registerFieldName,
                                                SymbolTable.TypeLAIdentifier.VARIAVEL, TypeLAVariable.LOGICO);
                                        break;
                                    case "^logico":
                                        registerFields.add(registerFieldName,
                                                SymbolTable.TypeLAIdentifier.VARIAVEL, TypeLAVariable.PONT_LOG);
                                        break;
                                    case "^real":
                                        registerFields.add(registerFieldName,
                                                SymbolTable.TypeLAIdentifier.VARIAVEL, TypeLAVariable.PONT_REA);
                                        break;
                                    case "^literal":
                                        registerFields.add(registerFieldName,
                                                SymbolTable.TypeLAIdentifier.VARIAVEL, TypeLAVariable.PONT_LIT);
                                        break;
                                    case "^inteiro":
                                        registerFields.add(registerFieldName,
                                                SymbolTable.TypeLAIdentifier.VARIAVEL, TypeLAVariable.PONT_INT);
                                        break;
                                    default:
                                        // not a basic/primitive type
                                        if (!currentScope.exists(variableType)) {
                                            LASemanticUtils.addSemanticError(
                                                    ctxVariableRegisterIdent.IDENT(0).getSymbol(),
                                                    "tipo " + variableType + " nao declarado\n");
                                            currentScope.add(registerFieldName,
                                                    SymbolTable.TypeLAIdentifier.VARIAVEL,
                                                    SymbolTable.TypeLAVariable.INVALIDO);
                                        }
                                        break;
                                }
                            }
                        }
                    }
                }
            }
        }

        return super.visitDeclaracao_local(ctx);
    }

    // method visitCmd to check builtin functions, atribuitions, ifs and calls
    @Override
    public Void visitCmd(LAParser.CmdContext ctx) {
        if (ctx.cmdLeia() != null) {
            var currentScope = nestedScopes.getCurrentScope();
            for (var ident : ctx.cmdLeia().identificador()) {
                LASemanticUtils.verifyType(currentScope, ident);
            }
        }
        if (ctx.cmdEscreva() != null) {
            var currentScope = nestedScopes.getCurrentScope();
            for (var exp : ctx.cmdEscreva().expressao()) {
                LASemanticUtils.verifyType(currentScope, exp);
            }
        }
        if (ctx.cmdEnquanto() != null) {
            var currentScope = nestedScopes.getCurrentScope();
            LASemanticUtils.verifyType(currentScope, ctx.cmdEnquanto().expressao());
        }
        if (ctx.cmdAtribuicao() != null) {
            var currentScope = nestedScopes.getCurrentScope();
            var leftValue = LASemanticUtils.verifyType(currentScope,
                    ctx.cmdAtribuicao().identificador());
            var rightValue = LASemanticUtils.verifyType(currentScope,
                    ctx.cmdAtribuicao().expressao());
            // for pointers
            var atribuition = ctx.cmdAtribuicao().getText().split("<-");
            if (!LASemanticUtils.verifyType(leftValue, rightValue) && !atribuition[0].contains("^")) {
                LASemanticUtils.addSemanticError(ctx.cmdAtribuicao().identificador().IDENT(0).getSymbol(),
                        "atribuicao nao compativel para " + ctx.cmdAtribuicao().identificador().getText() + "\n");
            }
            
            // Type Checking
            if (atribuition[0].contains("^"))
                if (leftValue == SymbolTable.TypeLAVariable.PONT_INT
                        && rightValue != SymbolTable.TypeLAVariable.INTEIRO)
                    LASemanticUtils.addSemanticError(ctx.cmdAtribuicao().identificador().IDENT(0).getSymbol(),
                            "atribuicao nao compativel para " + atribuition[0] + "\n");
            if (leftValue == SymbolTable.TypeLAVariable.PONT_LOG
                    && rightValue != SymbolTable.TypeLAVariable.LOGICO)
                LASemanticUtils.addSemanticError(ctx.cmdAtribuicao().identificador().IDENT(0).getSymbol(),
                        "atribuicao nao compativel para " + atribuition[0] + "\n");
            if (leftValue == SymbolTable.TypeLAVariable.PONT_REA
                    && rightValue != SymbolTable.TypeLAVariable.REAL)
                LASemanticUtils.addSemanticError(ctx.cmdAtribuicao().identificador().IDENT(0).getSymbol(),
                        "atribuicao nao compativel para " + atribuition[0] + "\n");
            if (leftValue == SymbolTable.TypeLAVariable.PONT_LIT
                    && rightValue != SymbolTable.TypeLAVariable.LITERAL)
                LASemanticUtils.addSemanticError(ctx.cmdAtribuicao().identificador().IDENT(0).getSymbol(),
                        "atribuicao nao compativel para " + atribuition[0] + "\n");
        }
        if (ctx.cmdSe() != null) {
            var currentScope = nestedScopes.getCurrentScope();
            LASemanticUtils.verifyType(currentScope, ctx.cmdSe().expressao());
        }
        if (ctx.cmdChamada() != null) {
            var currentScope = nestedScopes.getCurrentScope();
            var nomeFunProc = ctx.cmdChamada().IDENT().getText();
            if (!currentScope.exists(nomeFunProc)) {
                LASemanticUtils.addSemanticError(ctx.cmdChamada().IDENT().getSymbol(),
                        "identificador " + nomeFunProc + " nao declarado\n");
            } else {
                var funProc = currentScope.verify(nomeFunProc);
                ArrayList<SymbolTable.TypeLAVariable> parameterTypes = new ArrayList<>();
                for (var exp : ctx.cmdChamada().expressao()) {
                    parameterTypes.add(LASemanticUtils.verifyType(currentScope, exp));
                }
                if (!funProc.argsRegFunc.validType(parameterTypes)) {
                    LASemanticUtils.addSemanticError(ctx.cmdChamada().IDENT().getSymbol(),
                            "incompatibilidade de parametros na chamada de " + nomeFunProc + "\n");
                }
            }
        }

        return super.visitCmd(ctx);
    }

}