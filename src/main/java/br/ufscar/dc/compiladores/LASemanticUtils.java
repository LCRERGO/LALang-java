package br.ufscar.dc.compiladores;

import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.Token;


public class LASemanticUtils {
    public static List<String> semanticErrors = new ArrayList<>();

    // Adds a new semantic error
    public static void addSemanticError(Token t, String msg) {
        int line = t.getLine();
        semanticErrors.add(String.format("Linha %d: %s", line, msg));
    }

    // Checks if atribution types are valid
    public static boolean verifyType(SymbolTable.TypeLAVariable tipo1, SymbolTable.TypeLAVariable tipo2) {
        if (tipo1 == tipo2)
            return true;
        if (tipo1 == SymbolTable.TypeLAVariable.NAO_DECLARADO
                || tipo2 == SymbolTable.TypeLAVariable.NAO_DECLARADO)
            return true;
        if (tipo1 == SymbolTable.TypeLAVariable.INVALIDO || tipo2 == SymbolTable.TypeLAVariable.INVALIDO)
            return false;
        if ((tipo1 == SymbolTable.TypeLAVariable.INTEIRO || tipo1 == SymbolTable.TypeLAVariable.REAL) &&
                (tipo2 == SymbolTable.TypeLAVariable.INTEIRO || tipo2 == SymbolTable.TypeLAVariable.REAL))
            return true;
        if ((tipo1 == SymbolTable.TypeLAVariable.PONT_INT || tipo1 == SymbolTable.TypeLAVariable.PONT_REA ||
                tipo1 == SymbolTable.TypeLAVariable.PONT_LOG || tipo1 == SymbolTable.TypeLAVariable.PONT_LIT)
                &&
                (tipo2 == SymbolTable.TypeLAVariable.ENDERECO))
            return true;
        if (tipo1 != tipo2)
            return false;

        return true;
    }

    // Gets Symbol type from symbolTable
    public static SymbolTable.TypeLAVariable verifyType(SymbolTable table,
            LAParser.IdentificadorContext ctx) {
        var identifier = ctx.getText();

        // Case register
        if (!identifier.contains("[") && !identifier.contains("]")) {
            // No dimension
            var idParts = identifier.split("\\.");

            if (!table.exists(idParts[0])) {
                // if declared
                addSemanticError(ctx.IDENT(0).getSymbol(), "identificador " + identifier + " nao declarado\n");
            } else {
                SymbolTableEntry maybeRegister = table.verify(idParts[0]);
                if (maybeRegister.identifierType == SymbolTable.TypeLAIdentifier.REGISTRO
                        && idParts.length > 1) {
                    SymbolTable camposRegistro = maybeRegister.argsRegFunc;
                    if (!camposRegistro.exists(idParts[1])) {
                        addSemanticError(ctx.IDENT(0).getSymbol(),
                                "identificador " + identifier + " nao declarado\n");
                    } else {
                        // Exists
                        var registerEntry = camposRegistro.verify(idParts[1]);
                        if (registerEntry.variableType == SymbolTable.TypeLAVariable.INTEIRO)
                            return SymbolTable.TypeLAVariable.INTEIRO;
                        if (registerEntry.variableType == SymbolTable.TypeLAVariable.LITERAL)
                            return SymbolTable.TypeLAVariable.LITERAL;
                        if (registerEntry.variableType == SymbolTable.TypeLAVariable.REAL)
                            return SymbolTable.TypeLAVariable.REAL;
                        if (registerEntry.variableType == SymbolTable.TypeLAVariable.LOGICO)
                            return SymbolTable.TypeLAVariable.LOGICO;
                        if (registerEntry.variableType == SymbolTable.TypeLAVariable.PONT_INT)
                            return SymbolTable.TypeLAVariable.PONT_INT;
                        if (registerEntry.variableType == SymbolTable.TypeLAVariable.PONT_LIT)
                            return SymbolTable.TypeLAVariable.PONT_LIT;
                        if (registerEntry.variableType == SymbolTable.TypeLAVariable.PONT_REA)
                            return SymbolTable.TypeLAVariable.PONT_REA;
                        if (registerEntry.variableType == SymbolTable.TypeLAVariable.PONT_LOG)
                            return SymbolTable.TypeLAVariable.PONT_LOG;
                    }
                }
                if (maybeRegister.identifierType == SymbolTable.TypeLAIdentifier.REGISTRO
                        && idParts.length == 1) {
                    return SymbolTable.TypeLAVariable.REGISTRO;
                }
                if (maybeRegister.variableType == SymbolTable.TypeLAVariable.INTEIRO)
                    return SymbolTable.TypeLAVariable.INTEIRO;
                if (maybeRegister.variableType == SymbolTable.TypeLAVariable.LITERAL)
                    return SymbolTable.TypeLAVariable.LITERAL;
                if (maybeRegister.variableType == SymbolTable.TypeLAVariable.REAL)
                    return SymbolTable.TypeLAVariable.REAL;
                if (maybeRegister.variableType == SymbolTable.TypeLAVariable.LOGICO)
                    return SymbolTable.TypeLAVariable.LOGICO;
                if (maybeRegister.variableType == SymbolTable.TypeLAVariable.PONT_INT)
                    return SymbolTable.TypeLAVariable.PONT_INT;
                if (maybeRegister.variableType == SymbolTable.TypeLAVariable.PONT_LIT)
                    return SymbolTable.TypeLAVariable.PONT_LIT;
                if (maybeRegister.variableType == SymbolTable.TypeLAVariable.PONT_REA)
                    return SymbolTable.TypeLAVariable.PONT_REA;
                if (maybeRegister.variableType == SymbolTable.TypeLAVariable.PONT_LOG)
                    return SymbolTable.TypeLAVariable.PONT_LOG;
            }
        } else {
            // With dimension
            var identifierSemDimensao = "";
            // Ignores dimension and sees if variable already declared
            for (var identCtx : ctx.IDENT())
                identifierSemDimensao += identCtx.getText();
            for (var xp : ctx.dimensao().exp_aritmetica())
                verifyType(table, xp);

            if (!table.exists(identifierSemDimensao)) {
                addSemanticError(ctx.IDENT(0).getSymbol(),
                        "identificador " + identifierSemDimensao + " nao declarado\n");
            } else {
                SymbolTableEntry ident = table.verify(identifierSemDimensao);
                if (ident.variableType == SymbolTable.TypeLAVariable.INTEIRO)
                    return SymbolTable.TypeLAVariable.INTEIRO;
                if (ident.variableType == SymbolTable.TypeLAVariable.LITERAL)
                    return SymbolTable.TypeLAVariable.LITERAL;
                if (ident.variableType == SymbolTable.TypeLAVariable.REAL)
                    return SymbolTable.TypeLAVariable.REAL;
                if (ident.variableType == SymbolTable.TypeLAVariable.LOGICO)
                    return SymbolTable.TypeLAVariable.LOGICO;
                if (ident.identifierType == SymbolTable.TypeLAIdentifier.REGISTRO)
                    return SymbolTable.TypeLAVariable.REGISTRO;

            }
        }

        return SymbolTable.TypeLAVariable.NAO_DECLARADO;
    }

    // verifyType in context of expression
    public static SymbolTable.TypeLAVariable verifyType(SymbolTable table,
            LAParser.ExpressaoContext ctx) {
        SymbolTable.TypeLAVariable ret = null;
        for (var tl : ctx.termo_logico()) {
            SymbolTable.TypeLAVariable aux = verifyType(table, tl);
            if (ret == null) {
                ret = aux;
            } else if (!verifyType(ret, aux)) {
                ret = SymbolTable.TypeLAVariable.INVALIDO;
            }
        }

        return ret;
    }

    // verifyType in context of logic term
    public static SymbolTable.TypeLAVariable verifyType(SymbolTable table,
            LAParser.Termo_logicoContext ctx) {
        SymbolTable.TypeLAVariable ret = null;
        for (var fl : ctx.fator_logico()) {
            var aux = verifyType(table, fl);
            if (ret == null) {
                ret = aux;
            } else if (!verifyType(ret, aux)) {
                ret = SymbolTable.TypeLAVariable.INVALIDO;
            }
        }

        return ret;
    }

    // verifyType in context of logic factor
    public static SymbolTable.TypeLAVariable verifyType(SymbolTable table,
            LAParser.Fator_logicoContext ctx) {
        return verifyType(table, ctx.parcela_logica());
    }

    // verifyType in context of logic parcel
    public static SymbolTable.TypeLAVariable verifyType(SymbolTable table,
            LAParser.Parcela_logicaContext ctx) {
        if (ctx.exp_relacional() != null) {
            return verifyType(table, ctx.exp_relacional());
        } else {
            return SymbolTable.TypeLAVariable.LOGICO;
        }
    }

    // verifyType in context of relational expression
    public static SymbolTable.TypeLAVariable verifyType(SymbolTable table,
            LAParser.Exp_relacionalContext ctx) {
        SymbolTable.TypeLAVariable ret = null;
        if (ctx.exp_aritmetica().size() == 1)
            for (var ea : ctx.exp_aritmetica()) {
                var aux = verifyType(table, ea);
                if (ret == null) {
                    ret = aux;
                } else if (!verifyType(ret, aux)) {
                    ret = SymbolTable.TypeLAVariable.INVALIDO;
                }
            } else {
            for (var ea : ctx.exp_aritmetica()) {
                verifyType(table, ea);
            }

            return SymbolTable.TypeLAVariable.LOGICO;
        }

        return ret;
    }

    // verifyType in context of arithmetic expression
    public static SymbolTable.TypeLAVariable verifyType(SymbolTable table,
            LAParser.Exp_aritmeticaContext ctx) {
        SymbolTable.TypeLAVariable ret = null;

        for (var te : ctx.termo()) {
            var aux = verifyType(table, te);
            if (ret == null) {
                ret = aux;
            } else if (!verifyType(ret, aux)) {
                ret = SymbolTable.TypeLAVariable.INVALIDO;
            }
        }
        return ret;
    }

    // verifyType in context of term
    public static SymbolTable.TypeLAVariable verifyType(SymbolTable table,
            LAParser.TermoContext ctx) {
        SymbolTable.TypeLAVariable ret = null;

        for (var fa : ctx.fator()) {
            var aux = verifyType(table, fa);
            if (ret == null) {
                ret = aux;
            } else if (!verifyType(ret, aux)) {
                ret = SymbolTable.TypeLAVariable.INVALIDO;
            }
        }
        return ret;
    }

    // verifyType in context of factor
    public static SymbolTable.TypeLAVariable verifyType(SymbolTable table,
            LAParser.FatorContext ctx) {
        SymbolTable.TypeLAVariable ret = null;

        for (var pa : ctx.parcela()) {
            var aux = verifyType(table, pa);
            if (ret == null) {
                ret = aux;
            } else if (!verifyType(ret, aux)) {
                ret = SymbolTable.TypeLAVariable.INVALIDO;
            }
        }

        return ret;
    }

    // verifyType in context of parcel
    public static SymbolTable.TypeLAVariable verifyType(SymbolTable table,
            LAParser.ParcelaContext ctx) {

        if (ctx.parcela_unario() != null) {
            return verifyType(table, ctx.parcela_unario());
        } else {
            return verifyType(table, ctx.parcela_nao_unario());
        }
    }

    // verifyType in context of unary parcel
    public static SymbolTable.TypeLAVariable verifyType(SymbolTable tabela,
            LAParser.Parcela_unarioContext ctx) {
        SymbolTable.TypeLAVariable ret = null;

        if (ctx.NUM_INT() != null) {
            return SymbolTable.TypeLAVariable.INTEIRO;
        }
        if (ctx.NUM_REAL() != null) {
            return SymbolTable.TypeLAVariable.REAL;
        }
        if (ctx.IDENT() != null) {
            // function
            if (!tabela.exists(ctx.IDENT().getText())) {
                addSemanticError(ctx.identificador().IDENT(0).getSymbol(),
                        "identificador " + ctx.IDENT().getText() + " nao declarado\n");
            }

            for (var exp : ctx.expressao()) {
                var aux = verifyType(tabela, exp);
                if (ret == null) {
                    ret = aux;
                } else if (!verifyType(ret, aux)) {
                    ret = SymbolTable.TypeLAVariable.INVALIDO;
                }
            }

            if (tabela.exists(ctx.IDENT().getText())) {
                // return type
                var function = tabela.verify(ctx.IDENT().getText());
                switch (function.specialType) {
                    case "inteiro":
                        ret = SymbolTable.TypeLAVariable.INTEIRO;
                        break;
                    case "literal":
                        ret = SymbolTable.TypeLAVariable.LITERAL;
                        break;
                    case "real":
                        ret = SymbolTable.TypeLAVariable.REAL;
                        break;
                    case "logico":
                        ret = SymbolTable.TypeLAVariable.LOGICO;
                        break;
                    case "^logico":
                        ret = SymbolTable.TypeLAVariable.PONT_LOG;
                        break;
                    case "^real":
                        ret = SymbolTable.TypeLAVariable.PONT_REA;
                        break;
                    case "^literal":
                        ret = SymbolTable.TypeLAVariable.PONT_LIT;
                        break;
                    case "^inteiro":
                        ret = SymbolTable.TypeLAVariable.PONT_INT;
                        break;
                    default:
                        ret = SymbolTable.TypeLAVariable.REGISTRO;
                        break;
                }

                // Parameter type and number
                var nameFun = ctx.IDENT().getText();
                var funProc = tabela.verify(nameFun);

                ArrayList<SymbolTable.TypeLAVariable> parameterTypes = new ArrayList<>();

                for (var exp : ctx.expressao()) {
                    parameterTypes.add(verifyType(tabela, exp));
                }

                if (!funProc.argsRegFunc.validType(parameterTypes)) {
                    addSemanticError(ctx.IDENT().getSymbol(),
                            "incompatibilidade de parametros na chamada de " + nameFun + "\n");
                }
            }

        }

        if (ctx.identificador() != null) {
            return verifyType(tabela, ctx.identificador());
        }

        if (ctx.IDENT() == null && ctx.expressao() != null) {
            for (var exp : ctx.expressao()) {
                return verifyType(tabela, exp);
            }
        }

        return ret;
    }

    // verifyType in context of parcel non unary
    public static SymbolTable.TypeLAVariable verifyType(SymbolTable table,
            LAParser.Parcela_nao_unarioContext ctx) {
        SymbolTable.TypeLAVariable ret = null;

        if (ctx.CADEIA() != null) {
            ret = SymbolTable.TypeLAVariable.LITERAL;
        } else {
            ret = verifyType(table, ctx.identificador());
            if (ctx.getText().contains("&")) {
                // case address
                return SymbolTable.TypeLAVariable.ENDERECO;
            }
        }

        return ret;
    }
}