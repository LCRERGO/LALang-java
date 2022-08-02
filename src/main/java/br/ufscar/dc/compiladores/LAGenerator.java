package br.ufscar.dc.compiladores;

import java.util.ArrayList;
import java.util.List;

import com.ibm.icu.util.TimeZone.SystemTimeZoneType;

public class LAGenerator extends LABaseVisitor<Void> {
    StringBuilder output;
    SymbolTable table;
    Scopes nestedScopes = new Scopes();

    public LAGenerator() {
        output = new StringBuilder();
        table = new SymbolTable();
    }

    public StringBuilder getOutput() {
        return output;
    }

    // Program entrypoint method
    @Override
    public Void visitPrograma(LAParser.ProgramaContext ctx) {
        output.append("#include <stdio.h>\n");
        output.append("#include <stdlib.h>\n");
        output.append("\n");

        ctx.declaracoes().decl_local_global().forEach(dec -> visitDecl_local_global(dec));

        for (var decLocGlo : ctx.declaracoes().decl_local_global()) {
            if (decLocGlo.declaracao_global() != null) {
                visitDeclaracao_global(decLocGlo.declaracao_global());
                decLocGlo.declaracao_global().cmd().forEach(cmd -> visitCmd(cmd));
                output.append("}\n");
            }
        }

        var scopes = nestedScopes.runNestedScopes();
        if (scopes.size() > 1) {
            nestedScopes.abandonScope();
        }

        output.append("\n");
        output.append("int main(){\n");
        ctx.corpo().declaracao_local().forEach(dec -> visitDeclaracao_local(dec));
        ctx.corpo().cmd().forEach(cmd -> visitCmd(cmd));
        output.append("    return 0;\n");
        output.append("}\n");
        return null;
    }

    // visitDeclaracao_local treats the declaration of variables, constants
    // and types
    @Override
    public Void visitDecl_local_global(LAParser.Decl_local_globalContext ctx) {
        if (ctx.declaracao_local() != null) {
            if (ctx.declaracao_local().IDENT() != null) {
                var identifier = ctx.declaracao_local().IDENT().getText();
                var currentScope = nestedScopes.getCurrentScope();

                if (ctx.declaracao_local().tipo_basico() != null) {
                    // constant declaration
                    // 'constante' IDENT ':' tipo_basico '=' valor_constante
                    if (currentScope.exists(identifier))
                        LASemanticUtils.addSemanticError(ctx.declaracao_local().IDENT().getSymbol(),
                                "identificador " + identifier + " ja declarado anteriormente\n");
                    else {
                        var constantType = ctx.declaracao_local().tipo_basico().getText();
                        output.append(
                                "#define " + identifier + " " + ctx.declaracao_local().valor_constante().getText());
                        switch (constantType) {
                            case "inteiro":
                                currentScope.add(identifier, SymbolTable.TypeLAIdentifier.CONSTANTE,
                                        SymbolTable.TypeLAVariable.INTEIRO);
                                break;
                            case "literal":
                                currentScope.add(identifier, SymbolTable.TypeLAIdentifier.CONSTANTE,
                                        SymbolTable.TypeLAVariable.LITERAL);
                                break;
                            case "real":
                                currentScope.add(identifier, SymbolTable.TypeLAIdentifier.CONSTANTE,
                                        SymbolTable.TypeLAVariable.REAL);
                                break;
                            case "logico":
                                currentScope.add(identifier, SymbolTable.TypeLAIdentifier.CONSTANTE,
                                        SymbolTable.TypeLAVariable.LOGICO);
                                break;
                            default:
                                // Never reached
                                break;
                        }
                    }
                }
            }
        }
        return null;
    }

    // visitDeclaracao_global builds procedures and functions building it's own
    // stack frame
    // for each subprogram
    @Override
    public Void visitDeclaracao_global(LAParser.Declaracao_globalContext ctx) {
        var identifier = ctx.IDENT().getText();
        var scopes = nestedScopes.runNestedScopes();
        if (scopes.size() > 1) {
            nestedScopes.abandonScope();
        }

        var globalScope = nestedScopes.getCurrentScope();
        if (ctx.tipo_estendido() != null) {
            // function scope
            nestedScopes.createNewScope();
            var funScope = nestedScopes.getCurrentScope();
            funScope.setGlobal(globalScope);

            if (globalScope.exists(identifier))
                LASemanticUtils.addSemanticError(ctx.IDENT().getSymbol(),
                        "identificador " + identifier + " ja declarado anteriormente\n");
            else {
                var returnType = ctx.tipo_estendido().getText();
                var parameters = new SymbolTable();
                globalScope.add(identifier, SymbolTable.TypeLAIdentifier.FUNCAO, null, parameters,
                        returnType);

                switch (returnType) {
                    case "inteiro":
                        output.append("int " + identifier + "(");
                        break;
                    case "literal":
                        output.append("char* " + identifier + "(");
                        break;
                    case "real":
                        output.append("float " + identifier + "(");
                        break;
                    case "logico":
                        output.append("boolean " + identifier + "(");
                        break;
                    case "^logico":
                        output.append("boolean* " + identifier + "(");
                        break;
                    case "^real":
                        output.append("float* " + identifier + "(");
                        break;
                    case "^literal":
                        output.append("char** " + identifier + "(");
                        break;
                    case "^inteiro":
                        output.append("int* " + identifier + "(");
                        break;
                    default:
                        break;
                }

                var flag = true;
                for (var parameter : ctx.parametros().parametro()) {
                    var variableType = parameter.tipo_estendido().getText();
                    for (var ident : parameter.identificador()) {
                        var parameterName = ident.getText();
                        if (funScope.exists(parameterName))
                            LASemanticUtils.addSemanticError(ident.IDENT(0).getSymbol(),
                                    "identificador " + parameterName + " ja declarado anteriormente\n");
                        else {
                            switch (variableType) {
                                case "inteiro":
                                    funScope.add(parameterName,
                                            SymbolTable.TypeLAIdentifier.VARIAVEL,
                                            SymbolTable.TypeLAVariable.INTEIRO);
                                    parameters.add(parameterName, SymbolTable.TypeLAIdentifier.VARIAVEL,
                                            SymbolTable.TypeLAVariable.INTEIRO);
                                    if (flag) {
                                        output.append("int " + parameterName);
                                        flag = false;
                                    } else
                                        output.append(",int" + " " + parameterName);
                                    break;
                                case "literal":
                                    funScope.add(parameterName,
                                            SymbolTable.TypeLAIdentifier.VARIAVEL,
                                            SymbolTable.TypeLAVariable.LITERAL);
                                    parameters.add(parameterName, SymbolTable.TypeLAIdentifier.VARIAVEL,
                                            SymbolTable.TypeLAVariable.LITERAL);
                                    if (flag) {
                                        output.append("char* " + parameterName);
                                        flag = false;
                                    } else
                                        output.append(",char*" + " " + parameterName);
                                    break;
                                case "real":
                                    funScope.add(parameterName,
                                            SymbolTable.TypeLAIdentifier.VARIAVEL,
                                            SymbolTable.TypeLAVariable.REAL);
                                    parameters.add(parameterName, SymbolTable.TypeLAIdentifier.VARIAVEL,
                                            SymbolTable.TypeLAVariable.REAL);
                                    if (flag) {
                                        output.append("float " + parameterName);
                                        flag = false;
                                    } else
                                        output.append(",float " + parameterName);
                                    break;
                                case "logico":
                                    funScope.add(parameterName,
                                            SymbolTable.TypeLAIdentifier.VARIAVEL,
                                            SymbolTable.TypeLAVariable.LOGICO);
                                    parameters.add(parameterName, SymbolTable.TypeLAIdentifier.VARIAVEL,
                                            SymbolTable.TypeLAVariable.LOGICO);
                                    if (flag) {
                                        output.append("boolean " + parameterName);
                                        flag = false;
                                    } else
                                        output.append(",boolean " + parameterName);
                                    break;
                                case "^logico":
                                    funScope.add(parameterName,
                                            SymbolTable.TypeLAIdentifier.VARIAVEL,
                                            SymbolTable.TypeLAVariable.PONT_LOG);
                                    parameters.add(parameterName, SymbolTable.TypeLAIdentifier.VARIAVEL,
                                            SymbolTable.TypeLAVariable.PONT_LOG);
                                    if (flag) {
                                        output.append("boolean* " + parameterName);
                                        flag = false;
                                    } else
                                        output.append(",boolean* " + parameterName);
                                    break;
                                case "^real":
                                    funScope.add(parameterName,
                                            SymbolTable.TypeLAIdentifier.VARIAVEL,
                                            SymbolTable.TypeLAVariable.PONT_REA);
                                    parameters.add(parameterName, SymbolTable.TypeLAIdentifier.VARIAVEL,
                                            SymbolTable.TypeLAVariable.PONT_REA);
                                    if (flag) {
                                        output.append("float* " + parameterName);
                                        flag = false;
                                    } else
                                        output.append(",float* " + parameterName);
                                    break;
                                case "^literal":
                                    funScope.add(parameterName,
                                            SymbolTable.TypeLAIdentifier.VARIAVEL,
                                            SymbolTable.TypeLAVariable.PONT_LIT);
                                    parameters.add(parameterName, SymbolTable.TypeLAIdentifier.VARIAVEL,
                                            SymbolTable.TypeLAVariable.PONT_LIT);
                                    if (flag) {
                                        output.append("boolean* " + parameterName);
                                        flag = false;
                                    } else
                                        output.append(",boolean* " + parameterName);
                                    break;
                                case "^inteiro":
                                    funScope.add(parameterName,
                                            SymbolTable.TypeLAIdentifier.VARIAVEL,
                                            SymbolTable.TypeLAVariable.PONT_INT);
                                    parameters.add(parameterName, SymbolTable.TypeLAIdentifier.VARIAVEL,
                                            SymbolTable.TypeLAVariable.PONT_INT);
                                    if (flag) {
                                        output.append("int* " + parameterName);
                                        flag = false;
                                    } else
                                        output.append(",int* " + parameterName);
                                    break;
                                default:
                                    // not a basic/primitive type
                                    if (globalScope.exists(variableType) && globalScope.verify(
                                            variableType).identifierType == SymbolTable.TypeLAIdentifier.TIPO) {

                                        if (funScope.exists(parameterName))
                                            LASemanticUtils.addSemanticError(ident.IDENT(0).getSymbol(),
                                                    "identificador " + parameterName + " ja declarado anteriormente\n");
                                        else {
                                            // declaring register
                                            var fields = globalScope.verify(variableType);
                                            var nestedTableType = fields.argsRegFunc;

                                            funScope.add(parameterName,
                                                    SymbolTable.TypeLAIdentifier.REGISTRO,
                                                    SymbolTable.TypeLAVariable.REGISTRO, nestedTableType,
                                                    variableType);
                                            parameters.add(parameterName,
                                                    SymbolTable.TypeLAIdentifier.REGISTRO,
                                                    SymbolTable.TypeLAVariable.REGISTRO, nestedTableType,
                                                    variableType);
                                            if (flag) {
                                                output.append(variableType + " " + parameterName);
                                                flag = false;
                                            } else
                                                output.append("," + variableType + " " + parameterName);
                                        }
                                    }

                                    if (!globalScope.exists(variableType)) {
                                        LASemanticUtils.addSemanticError(ident.IDENT(0).getSymbol(),
                                                "tipo " + variableType + " nao declarado\n");
                                        funScope.add(parameterName,
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
                output.append(") {\n");
            }
        } else {
            // procedure
            nestedScopes.createNewScope();
            var funScope = nestedScopes.getCurrentScope();
            funScope.setGlobal(globalScope);
            output.append("void " + identifier + "(");

            var flag = true;
            if (globalScope.exists(identifier))
                LASemanticUtils.addSemanticError(ctx.IDENT().getSymbol(),
                        "identificador " + identifier + " ja declarado anteriormente\n");
            else {
                var parameters = new SymbolTable();
                globalScope.add(identifier, SymbolTable.TypeLAIdentifier.PROCEDIMENTO, null,
                        parameters);

                for (var parameter : ctx.parametros().parametro()) {
                    var variableType = parameter.tipo_estendido().getText();
                    for (var ident : parameter.identificador()) {
                        var parameterName = ident.getText();
                        if (funScope.exists(parameterName))
                            LASemanticUtils.addSemanticError(ident.IDENT(0).getSymbol(),
                                    "identificador " + parameterName + " ja declarado anteriormente\n");
                        else {
                            switch (variableType) {
                                case "inteiro":
                                    funScope.add(parameterName,
                                            SymbolTable.TypeLAIdentifier.VARIAVEL,
                                            SymbolTable.TypeLAVariable.INTEIRO);
                                    parameters.add(parameterName, SymbolTable.TypeLAIdentifier.VARIAVEL,
                                            SymbolTable.TypeLAVariable.INTEIRO);
                                    if (flag) {
                                        output.append("int " + parameterName);
                                        flag = false;
                                    } else
                                        output.append(",int" + " " + parameterName);
                                    break;
                                case "literal":
                                    funScope.add(parameterName,
                                            SymbolTable.TypeLAIdentifier.VARIAVEL,
                                            SymbolTable.TypeLAVariable.LITERAL);
                                    parameters.add(parameterName, SymbolTable.TypeLAIdentifier.VARIAVEL,
                                            SymbolTable.TypeLAVariable.LITERAL);
                                    if (flag) {
                                        output.append("char* " + parameterName);
                                        flag = false;
                                    } else
                                        output.append(",char*" + " " + parameterName);
                                    break;
                                case "real":
                                    funScope.add(parameterName,
                                            SymbolTable.TypeLAIdentifier.VARIAVEL,
                                            SymbolTable.TypeLAVariable.REAL);
                                    parameters.add(parameterName, SymbolTable.TypeLAIdentifier.VARIAVEL,
                                            SymbolTable.TypeLAVariable.REAL);
                                    if (flag) {
                                        output.append("float " + parameterName);
                                        flag = false;
                                    } else
                                        output.append(",float " + parameterName);
                                    break;
                                case "logico":
                                    funScope.add(parameterName,
                                            SymbolTable.TypeLAIdentifier.VARIAVEL,
                                            SymbolTable.TypeLAVariable.LOGICO);
                                    parameters.add(parameterName, SymbolTable.TypeLAIdentifier.VARIAVEL,
                                            SymbolTable.TypeLAVariable.LOGICO);
                                    if (flag) {
                                        output.append("boolean " + parameterName);
                                        flag = false;
                                    } else
                                        output.append(",boolean " + parameterName);
                                    break;
                                case "^logico":
                                    funScope.add(parameterName,
                                            SymbolTable.TypeLAIdentifier.VARIAVEL,
                                            SymbolTable.TypeLAVariable.PONT_LOG);
                                    parameters.add(parameterName, SymbolTable.TypeLAIdentifier.VARIAVEL,
                                            SymbolTable.TypeLAVariable.PONT_LOG);
                                    if (flag) {
                                        output.append("boolean* " + parameterName);
                                        flag = false;
                                    } else
                                        output.append(",boolean* " + parameterName);
                                    break;
                                case "^real":
                                    funScope.add(parameterName,
                                            SymbolTable.TypeLAIdentifier.VARIAVEL,
                                            SymbolTable.TypeLAVariable.PONT_REA);
                                    parameters.add(parameterName, SymbolTable.TypeLAIdentifier.VARIAVEL,
                                            SymbolTable.TypeLAVariable.PONT_REA);
                                    if (flag) {
                                        output.append("float* " + parameterName);
                                        flag = false;
                                    } else
                                        output.append(",float* " + parameterName);
                                    break;
                                case "^literal":
                                    funScope.add(parameterName,
                                            SymbolTable.TypeLAIdentifier.VARIAVEL,
                                            SymbolTable.TypeLAVariable.PONT_LIT);
                                    parameters.add(parameterName, SymbolTable.TypeLAIdentifier.VARIAVEL,
                                            SymbolTable.TypeLAVariable.PONT_LIT);
                                    if (flag) {
                                        output.append("boolean* " + parameterName);
                                        flag = false;
                                    } else
                                        output.append(",boolean* " + parameterName);
                                    break;
                                case "^inteiro":
                                    funScope.add(parameterName,
                                            SymbolTable.TypeLAIdentifier.VARIAVEL,
                                            SymbolTable.TypeLAVariable.PONT_INT);
                                    parameters.add(parameterName, SymbolTable.TypeLAIdentifier.VARIAVEL,
                                            SymbolTable.TypeLAVariable.PONT_INT);
                                    if (flag) {
                                        output.append("int* " + parameterName);
                                        flag = false;
                                    } else
                                        output.append(",int* " + parameterName);
                                    break;
                                default:
                                    // not a basic/primitive type
                                    if (globalScope.exists(variableType) && globalScope.verify(
                                            variableType).identifierType == SymbolTable.TypeLAIdentifier.TIPO) {

                                        if (funScope.exists(parameterName)) {
                                            LASemanticUtils.addSemanticError(ident.IDENT(0).getSymbol(),
                                                    "identificador " + parameterName + " ja declarado anteriormente\n");
                                        } else { 
                                            var fields = globalScope.verify(variableType);
                                            var nestedTableType = fields.argsRegFunc;

                                            funScope.add(parameterName,
                                                    SymbolTable.TypeLAIdentifier.REGISTRO,
                                                    SymbolTable.TypeLAVariable.REGISTRO, nestedTableType,
                                                    variableType);
                                            parameters.add(parameterName,
                                                    SymbolTable.TypeLAIdentifier.REGISTRO,
                                                    SymbolTable.TypeLAVariable.REGISTRO, nestedTableType,
                                                    variableType);
                                            if (flag) {
                                                output.append(variableType + " " + parameterName);
                                                flag = false;
                                            } else
                                                output.append("," + variableType + " " + parameterName);
                                        }
                                    }

                                    if (!globalScope.exists(variableType)) { 
                                        LASemanticUtils.addSemanticError(ident.IDENT(0).getSymbol(),
                                                "tipo " + variableType + " nao declarado\n");
                                        funScope.add(parameterName,
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
                output.append(") {\n");
            }
        }

        return null;
    }

    // visitDeclaracao_local treats the declaration of variables, constants 
    // and types
    @Override
    public Void visitDeclaracao_local(LAParser.Declaracao_localContext ctx) {

        if (ctx.IDENT() != null) {
            // constant declaration
            // 'constante' IDENT ':' tipo_basico '=' valor_constante
            var identifier = ctx.IDENT().getText();
            var currentScope = nestedScopes.getCurrentScope();

            if (currentScope.exists(identifier)) {
                LASemanticUtils.addSemanticError(ctx.IDENT().getSymbol(),
                        "identificador " + identifier + " ja declarado anteriormente\n");
            } else {
                var fieldsType = new SymbolTable();
                currentScope.add(identifier, SymbolTable.TypeLAIdentifier.TIPO, null, fieldsType);

                output.append("    typedef struct {\n");

                for (var variable : ctx.tipo().registro().variavel()) {
                    for (var ctxIdentVariable : variable.identificador()) {
                        var variableIdentifier = ctxIdentVariable.getText();

                        if (fieldsType.exists(variableIdentifier))
                            LASemanticUtils.addSemanticError(ctxIdentVariable.IDENT(0).getSymbol(),
                                    "identificador " + variableIdentifier + " ja declarado anteriormente\n");
                        else {
                            var variableType = variable.tipo().getText();

                            switch (variableType) {
                                case "inteiro":
                                    fieldsType.add(variableIdentifier,
                                            SymbolTable.TypeLAIdentifier.VARIAVEL,
                                            SymbolTable.TypeLAVariable.INTEIRO);
                                    output.append("        int " + variableIdentifier + ";\n");
                                    break;
                                case "literal":
                                    fieldsType.add(variableIdentifier,
                                            SymbolTable.TypeLAIdentifier.VARIAVEL,
                                            SymbolTable.TypeLAVariable.LITERAL);
                                    output.append("        char " + variableIdentifier + "[80];\n");
                                    break;
                                case "real":
                                    fieldsType.add(variableIdentifier,
                                            SymbolTable.TypeLAIdentifier.VARIAVEL,
                                            SymbolTable.TypeLAVariable.REAL);
                                    output.append("        float " + variableIdentifier + ";\n");
                                    break;
                                case "logico":
                                    fieldsType.add(variableIdentifier,
                                            SymbolTable.TypeLAIdentifier.VARIAVEL,
                                            SymbolTable.TypeLAVariable.LOGICO);
                                    output.append("        boolean " + variableIdentifier + ";\n");
                                    break;
                                case "^logico":
                                    fieldsType.add(variableIdentifier,
                                            SymbolTable.TypeLAIdentifier.VARIAVEL,
                                            SymbolTable.TypeLAVariable.PONT_LOG);
                                    output.append("        boolean* " + variableIdentifier + ";\n");
                                    break;
                                case "^real":
                                    fieldsType.add(variableIdentifier,
                                            SymbolTable.TypeLAIdentifier.VARIAVEL,
                                            SymbolTable.TypeLAVariable.PONT_REA);
                                    output.append("        float* " + variableIdentifier + ";\n");
                                    break;
                                case "^literal":
                                    fieldsType.add(variableIdentifier,
                                            SymbolTable.TypeLAIdentifier.VARIAVEL,
                                            SymbolTable.TypeLAVariable.PONT_LIT);
                                    output.append("        char* " + variableIdentifier + "[80];\n");
                                    break;
                                case "^inteiro":
                                    fieldsType.add(variableIdentifier,
                                            SymbolTable.TypeLAIdentifier.VARIAVEL,
                                            SymbolTable.TypeLAVariable.PONT_INT);
                                    output.append("        int* " + variableIdentifier + ";\n");
                                    break;
                                default: 
                                    break;
                            }
                        }
                    }
                }
                output.append("    } " + identifier + ";\n");
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

                    if (ctxIdentVariable.dimensao() != null) {
                        for (var expDim : ctxIdentVariable.dimensao().exp_aritmetica()) {
                            LASemanticUtils.verifyType(currentScope, expDim);
                        }
                    }

                    if (currentScope.exists(variableIdentifier)) {
                        LASemanticUtils.addSemanticError(ctxIdentVariable.IDENT(0).getSymbol(),
                                "identificador " + variableIdentifier + " ja declarado anteriormente\n");
                    } else {
                        var variableType = ctx.variavel().tipo().getText();
                        switch (variableType) {
                            case "inteiro":
                                currentScope.add(variableIdentifier,
                                        SymbolTable.TypeLAIdentifier.VARIAVEL,
                                        SymbolTable.TypeLAVariable.INTEIRO);
                                output.append("    int " + ctxIdentVariable.getText() + ";\n");
                                break;
                            case "literal":
                                currentScope.add(variableIdentifier,
                                        SymbolTable.TypeLAIdentifier.VARIAVEL,
                                        SymbolTable.TypeLAVariable.LITERAL);
                                output.append("    char " + variableIdentifier + "[80];\n");
                                break;
                            case "real":
                                currentScope.add(variableIdentifier,
                                        SymbolTable.TypeLAIdentifier.VARIAVEL,
                                        SymbolTable.TypeLAVariable.REAL);
                                output.append("    float " + ctxIdentVariable.getText() + ";\n");
                                break;
                            case "logico":
                                currentScope.add(variableIdentifier,
                                        SymbolTable.TypeLAIdentifier.VARIAVEL,
                                        SymbolTable.TypeLAVariable.LOGICO);
                                break;
                            case "^logico":
                                currentScope.add(variableIdentifier,
                                        SymbolTable.TypeLAIdentifier.VARIAVEL,
                                        SymbolTable.TypeLAVariable.PONT_LOG);
                                break;
                            case "^real":
                                currentScope.add(variableIdentifier,
                                        SymbolTable.TypeLAIdentifier.VARIAVEL,
                                        SymbolTable.TypeLAVariable.PONT_REA);
                                output.append("    float* " + ctxIdentVariable.getText() + ";\n");
                                break;
                            case "^literal":
                                currentScope.add(variableIdentifier,
                                        SymbolTable.TypeLAIdentifier.VARIAVEL,
                                        SymbolTable.TypeLAVariable.PONT_LIT);
                                output.append("    char* " + variableIdentifier + "[80];\n");
                                break;
                            case "^inteiro":
                                currentScope.add(variableIdentifier,
                                        SymbolTable.TypeLAIdentifier.VARIAVEL,
                                        SymbolTable.TypeLAVariable.PONT_INT);
                                output.append("    int* " + ctxIdentVariable.getText() + ";\n");
                                break;
                            default:
                                // not a basic/primitive type
                                if (currentScope.exists(variableType) && currentScope.verify(
                                        variableType).identifierType == SymbolTable.TypeLAIdentifier.TIPO) {
                                    if (currentScope.exists(variableIdentifier)) {
                                        LASemanticUtils.addSemanticError(ctxIdentVariable.IDENT(0).getSymbol(),
                                                "identificador " + variableIdentifier
                                                        + " ja declarado anteriormente\n");
                                    } else {
                                        var entry = currentScope.verify(variableType);
                                        var fieldsType = entry.argsRegFunc;
                                        currentScope.add(variableIdentifier,
                                                SymbolTable.TypeLAIdentifier.REGISTRO, null, fieldsType,
                                                variableType);
                                        output.append(
                                                "    " + variableType + " " + ctxIdentVariable.getText() + ";\n");
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
                output.append("    struct {\n");
                ArrayList<String> identsRegisters = new ArrayList<>();
                for (var ctxIdentReg : ctx.variavel().identificador()) {
                    var identifierName = ctxIdentReg.getText();
                    var currentScope = nestedScopes.getCurrentScope();

                    if (currentScope.exists(identifierName)) {
                        LASemanticUtils.addSemanticError(ctxIdentReg.IDENT(0).getSymbol(),
                                "identificador " + identifierName + " ja declarado anteriormente\n");
                    } else {
                        var fields = new SymbolTable();
                        currentScope.add(identifierName, SymbolTable.TypeLAIdentifier.REGISTRO, null,
                                fields, null);
                        identsRegisters.add(identifierName);
                    }
                }

                var lock = false;
                for (var ctxVariableRegister : ctx.variavel().tipo().registro().variavel()) { 
                    for (var ctxVariableRegisterIdent : ctxVariableRegister.identificador()) {
                        lock = false;
                        var registerFieldName = ctxVariableRegisterIdent.getText();
                        var currentScope = nestedScopes.getCurrentScope();

                        for (var identRegister : identsRegisters) {
                            var entry = currentScope.verify(identRegister);
                            var registerFields = entry.argsRegFunc;

                            if (registerFields.exists(registerFieldName)) {
                                LASemanticUtils.addSemanticError(ctxVariableRegisterIdent.IDENT(0).getSymbol(),
                                        "identificador " + registerFieldName + " ja declarado anteriormente\n");
                            } else { 
                                var variableType = ctxVariableRegister.tipo().getText();

                                switch (variableType) {
                                    case "inteiro":
                                        registerFields.add(registerFieldName,
                                                SymbolTable.TypeLAIdentifier.VARIAVEL,
                                                SymbolTable.TypeLAVariable.INTEIRO);
                                        if (!lock)
                                            output.append("    int " + registerFieldName + ";\n");
                                        break;
                                    case "literal":
                                        registerFields.add(registerFieldName,
                                                SymbolTable.TypeLAIdentifier.VARIAVEL,
                                                SymbolTable.TypeLAVariable.LITERAL);
                                        if (!lock)
                                            output.append("    char " + registerFieldName + "[80];\n");
                                        break;
                                    case "real":
                                        registerFields.add(registerFieldName,
                                                SymbolTable.TypeLAIdentifier.VARIAVEL,
                                                SymbolTable.TypeLAVariable.REAL);
                                        if (!lock)
                                            output.append("    float " + registerFieldName + ";\n");
                                        break;
                                    case "logico":
                                        registerFields.add(registerFieldName,
                                                SymbolTable.TypeLAIdentifier.VARIAVEL,
                                                SymbolTable.TypeLAVariable.LOGICO);
                                        break;
                                    case "^logico":
                                        registerFields.add(registerFieldName,
                                                SymbolTable.TypeLAIdentifier.VARIAVEL,
                                                SymbolTable.TypeLAVariable.PONT_LOG);
                                        break;
                                    case "^real":
                                        registerFields.add(registerFieldName,
                                                SymbolTable.TypeLAIdentifier.VARIAVEL,
                                                SymbolTable.TypeLAVariable.PONT_REA);
                                        if (!lock)
                                            output.append("    float* " + registerFieldName + ";\n");
                                        break;
                                    case "^literal":
                                        registerFields.add(registerFieldName,
                                                SymbolTable.TypeLAIdentifier.VARIAVEL,
                                                SymbolTable.TypeLAVariable.PONT_LIT);
                                        if (!lock)
                                            output.append("    char* " + registerFieldName + "[80];\n");
                                        break;
                                    case "^inteiro":
                                        registerFields.add(registerFieldName,
                                                SymbolTable.TypeLAIdentifier.VARIAVEL,
                                                SymbolTable.TypeLAVariable.PONT_INT);
                                        if (!lock)
                                            output.append("    int* " + registerFieldName + ";\n");
                                        break;
                                    default:
                                        // Not a basic/primitive type
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
                        lock = true;
                    }
                }
                output.append("    }");
                var flag = true;
                for (var identRegister : identsRegisters) {
                    if (flag)
                        output.append(identRegister);
                    else
                        output.append("," + identRegister);
                }
                output.append(";\n");
            }
        }

        return null;
    }

    // visitCmdLeia treats 'leia' command
    @Override
    public Void visitCmdLeia(LAParser.CmdLeiaContext ctx) {
        for (var identifier : ctx.identificador()) {
            var identifierName = "";

            for (var namePart : identifier.IDENT())
                identifierName += namePart.getText();

            var currentScope = nestedScopes.getCurrentScope();
            var variable = currentScope.verify(identifierName);

            if (variable.variableType == SymbolTable.TypeLAVariable.INTEIRO)
                output.append("    scanf(\"%d\", &" + identifierName + ");\n");
            if (variable.variableType == SymbolTable.TypeLAVariable.REAL)
                output.append("    scanf(\"%f\", &" + identifierName + ");\n");
            if (variable.variableType == SymbolTable.TypeLAVariable.LITERAL)
                output.append("    gets(" + identifierName + ");\n");
        }
        return null;
    }

    // visitCmdEscreva treats 'leia' command
    @Override
    public Void visitCmdEscreva(LAParser.CmdEscrevaContext ctx) {
        var currentScope = nestedScopes.getCurrentScope();

        output.append("    printf(\"");
        var expContent = new StringBuilder();
        ArrayList<String> variableNames = new ArrayList<>();

        for (var expression : ctx.expressao()) {
            var variableType = LAGeneratorUtils.verifyType(expContent, currentScope, expression);
            System.out.println("Tipo: " + variableType + "\n" + "Conteudo: " + expContent.toString());

            if (variableType == SymbolTable.TypeLAVariable.LITERAL
                    && expContent.toString().contains("\""))
                output.append(expContent.toString().replaceAll("\"", ""));
            if (variableType == SymbolTable.TypeLAVariable.INTEIRO) {
                output.append("%d");
                variableNames.add(expContent.toString());
            }
            if (variableType == SymbolTable.TypeLAVariable.REAL) {
                output.append("%f");
                variableNames.add(expContent.toString());
            }
            if (variableType == SymbolTable.TypeLAVariable.LITERAL
                    && !expContent.toString().contains("\"")) {
                output.append("%s");
                variableNames.add(expContent.toString());
            }

            expContent.setLength(0);
        }
        output.append("\"");
        for (var variableName : variableNames) {
            output.append("," + variableName);
        }

        output.append(");\n");
        return null;
    }

    // visitCmdAtribuicao treats '=' command
    @Override
    public Void visitCmdAtribuicao(LAParser.CmdAtribuicaoContext ctx) {
        var currentScope = nestedScopes.getCurrentScope();
        var buf = new StringBuilder();
        var atribution = ctx.getText().split("<-");
        var lValue = ctx.identificador().getText();

        if (!(LAGeneratorUtils.verifyType(buf, currentScope,
                ctx.identificador()) == SymbolTable.TypeLAVariable.LITERAL)) {
            if (atribution[0].contains("^")) {
                output.append("    *" + lValue + " = ");
            } else {
                output.append("    " + lValue + " = ");
            }

            var expContent = new StringBuilder();

            if (atribution[1].contains("&")) {
                output.append("&");
            }
            LAGeneratorUtils.verifyType(expContent, currentScope, ctx.expressao());

            output.append(expContent + ";\n");
        } else {
            output.append("    strcpy(" + lValue + ",");

            var expContent = new StringBuilder();
            LAGeneratorUtils.verifyType(expContent, currentScope, ctx.expressao());

            output.append(expContent + ");\n");
        }
        return null;
    }

    // visitCmdSe treats 'se' command
    @Override
    public Void visitCmdSe(LAParser.CmdSeContext ctx) {
        var currentScope = nestedScopes.getCurrentScope();
        var expContent = new StringBuilder();

        output.append("    if(");

        LAGeneratorUtils.verifyType(expContent, currentScope, ctx.expressao());
        output.append(expContent);
        output.append("){\n");

        ctx.cmdIf.forEach(cmd -> visitCmd(cmd));

        output.append("    }\n");

        if (ctx.getText().contains("senao")) {
            output.append("    else{\n");
            ctx.cmdElse.forEach(cmd -> visitCmd(cmd));
            output.append("    }\n");
        }

        return null;
    }

    // visitCmdCaso treats 'caso' command
    @Override
    public Void visitCmdCaso(LAParser.CmdCasoContext ctx) {
        var currentScope = nestedScopes.getCurrentScope();
        var expContent = new StringBuilder();

        output.append("    switch(");
        LAGeneratorUtils.verifyType(expContent, currentScope, ctx.exp_aritmetica());
        output.append(expContent);
        output.append("){\n");

        for (var item : ctx.selecao().item_selecao()) {
            for (var intervalNum : item.constantes().numero_intervalo()) {
                // Intervals
                var startNum = "";
                var endNum = "";

                if (intervalNum.op_unarioPrimeiro != null)
                    startNum += intervalNum.op_unarioPrimeiro.getText();
                startNum += intervalNum.numeroPrimeiro.getText();

                if (intervalNum.op_unariosSegundo != null)
                    endNum += intervalNum.op_unariosSegundo.getText();
                if (intervalNum.numeroSegundo != null)
                    endNum += intervalNum.numeroSegundo.getText();

                if (!endNum.equals(""))
                    for (int i = Integer.parseInt(startNum); i <= Integer.parseInt(endNum); i++)
                        output.append("case " + String.valueOf(i) + ":\n");
                else
                    output.append("case " + startNum + ":\n");
            }

            item.cmd().forEach(cmd -> visitCmd(cmd));
            output.append("break;\n");
        }

        if (ctx.getText().contains("senao")) {
            output.append("    default:\n");
            ctx.cmd().forEach(cmd -> visitCmd(cmd));
            output.append("    }\n");
        }

        return null;
    }

    // visitCmdPara treats 'para' command
    @Override
    public Void visitCmdPara(LAParser.CmdParaContext ctx) {
        var currentScope = nestedScopes.getCurrentScope();
        var expContent = new StringBuilder();

        output.append("for(");
        output.append(ctx.IDENT().getText());
        output.append(" = ");

        LAGeneratorUtils.verifyType(expContent, currentScope, ctx.exp_aritmetica(0));
        output.append(expContent);
        output.append("; ");

        output.append(ctx.IDENT().getText());
        output.append(" <= ");
        expContent = new StringBuilder();
        LAGeneratorUtils.verifyType(expContent, currentScope, ctx.exp_aritmetica(1));
        output.append(expContent);
        output.append("; ");
        output.append(ctx.IDENT().getText());
        output.append("++){\n");

        ctx.cmd().forEach(cmd -> visitCmd(cmd));
        output.append("}\n");

        return null;
    }

    // visitCmdEnquanto treats 'enquanto' command
    @Override
    public Void visitCmdEnquanto(LAParser.CmdEnquantoContext ctx) {
        var currentScope = nestedScopes.getCurrentScope();
        var expContent = new StringBuilder();

        output.append("while(");
        LAGeneratorUtils.verifyType(expContent, currentScope, ctx.expressao());
        output.append(expContent);
        output.append("){\n");

        ctx.cmd().forEach(cmd -> visitCmd(cmd));
        output.append("}\n");

        return null;
    }

    // vistCmdFaca treats 'faca' command
    @Override
    public Void visitCmdFaca(LAParser.CmdFacaContext ctx) {
        var currentScope = nestedScopes.getCurrentScope();
        var expContent = new StringBuilder();
        var flag = false;

        output.append("do{\n");
        ctx.cmd().forEach(cmd -> visitCmd(cmd));

        output.append("}while(");

        if (ctx.expressao().termo_logico(0).getText().contains("nao")) {
            flag = true;
            output.append("!(");
        }
        LAGeneratorUtils.verifyType(expContent, currentScope, ctx.expressao());
        output.append(expContent);
        if (flag) {
            output.append(")");
        }
        output.append(");\n");

        return null;
    }

    // vistCmdRetorne treats function returs
    @Override
    public Void visitCmdRetorne(LAParser.CmdRetorneContext ctx) {
        var currentScope = nestedScopes.getCurrentScope();
        var expContent = new StringBuilder();

        LAGeneratorUtils.verifyType(expContent, currentScope, ctx.expressao());
        output.append("    return " + expContent + ";\n");
        return null;
    }

    // vistCmdChamada treats function calls
    @Override
    public Void visitCmdChamada(LAParser.CmdChamadaContext ctx) {
        output.append("    " + ctx.getText() + ";\n");

        return null;
    }
}