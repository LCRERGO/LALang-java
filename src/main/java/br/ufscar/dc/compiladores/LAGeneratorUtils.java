package br.ufscar.dc.compiladores;

import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.Token;

// Utilities for code generation
public class LAGeneratorUtils {
    public static List<String> semanticErrors = new ArrayList<>();

    public static void addSemanticError(Token t, String msg) {
        var line = t.getLine();
        semanticErrors.add(String.format("Linha %d: %s", line, msg));
    }

    // Checks types for atribuition
    public static boolean verifyType(SymbolTable.TypeLAVariable type1, SymbolTable.TypeLAVariable type2) {
        if (type1 == type2)
            return true;
        if (type1 == SymbolTable.TypeLAVariable.NAO_DECLARADO || type2 == SymbolTable.TypeLAVariable.NAO_DECLARADO)
            return true;
        if (type1 == SymbolTable.TypeLAVariable.INVALIDO || type2 == SymbolTable.TypeLAVariable.INVALIDO)
            return false;
        if ((type1 == SymbolTable.TypeLAVariable.INTEIRO || type1 == SymbolTable.TypeLAVariable.REAL) &&
                (type2 == SymbolTable.TypeLAVariable.INTEIRO || type2 == SymbolTable.TypeLAVariable.REAL))
            return true;
        if ((type1 == SymbolTable.TypeLAVariable.PONT_INT || type1 == SymbolTable.TypeLAVariable.PONT_REA ||
                type1 == SymbolTable.TypeLAVariable.PONT_LOG || type1 == SymbolTable.TypeLAVariable.PONT_LIT)
                && (type2 == SymbolTable.TypeLAVariable.ENDERECO))
            return true;
        if (type1 != type2)
            return false;

        return true;
    }

    // Gets Symbol type from symbolTable
    public static SymbolTable.TypeLAVariable verifyType(StringBuilder output, SymbolTable table,
            LAParser.IdentificadorContext ctx) {
        var identifier = ctx.getText();

        if (!identifier.contains("[") && !identifier.contains("]")) {
            var idParts = identifier.split("\\.");

            if (!table.exists(idParts[0])) {
                addSemanticError(ctx.IDENT(0).getSymbol(), "identificador " + identifier + " nao declarado\n");
            } else {
                var maybeRegister = table.verify(idParts[0]);
                if (maybeRegister.identifierType == SymbolTable.TypeLAIdentifier.REGISTRO
                        && idParts.length > 1) {
                    var registerFields = maybeRegister.argsRegFunc;
                    if (!registerFields.exists(idParts[1])) {
                        addSemanticError(ctx.IDENT(0).getSymbol(),
                                "identificador " + identifier + " nao declarado\n");
                    } else {
                        var registerEntry = registerFields.verify(idParts[1]);
                        if (registerEntry.variableType == SymbolTable.TypeLAVariable.INTEIRO) {
                            output.append(identifier);
                            return SymbolTable.TypeLAVariable.INTEIRO;
                        }
                        if (registerEntry.variableType == SymbolTable.TypeLAVariable.LITERAL) {
                            output.append(identifier);
                            return SymbolTable.TypeLAVariable.LITERAL;
                        }
                        if (registerEntry.variableType == SymbolTable.TypeLAVariable.REAL) {
                            output.append(identifier);
                            return SymbolTable.TypeLAVariable.REAL;
                        }
                        if (registerEntry.variableType == SymbolTable.TypeLAVariable.LOGICO) {
                            output.append(identifier);
                            return SymbolTable.TypeLAVariable.LOGICO;
                        }
                        if (registerEntry.variableType == SymbolTable.TypeLAVariable.PONT_INT) {
                            output.append(identifier);
                            return SymbolTable.TypeLAVariable.PONT_INT;
                        }
                        if (registerEntry.variableType == SymbolTable.TypeLAVariable.PONT_LIT) {
                            output.append(identifier);
                            return SymbolTable.TypeLAVariable.PONT_LIT;
                        }
                        if (registerEntry.variableType == SymbolTable.TypeLAVariable.PONT_REA) {
                            output.append(identifier);
                            return SymbolTable.TypeLAVariable.PONT_REA;
                        }
                        if (registerEntry.variableType == SymbolTable.TypeLAVariable.PONT_LOG) {
                            output.append(identifier);
                            return SymbolTable.TypeLAVariable.PONT_LOG;
                        }
                    }
                }

                if (maybeRegister.identifierType == SymbolTable.TypeLAIdentifier.REGISTRO
                        && idParts.length == 1) {
                    return SymbolTable.TypeLAVariable.REGISTRO;
                }

                if (maybeRegister.variableType == SymbolTable.TypeLAVariable.INTEIRO) {
                    output.append(maybeRegister.name);
                    return SymbolTable.TypeLAVariable.INTEIRO;
                }
                if (maybeRegister.variableType == SymbolTable.TypeLAVariable.LITERAL) {
                    output.append(maybeRegister.name);
                    return SymbolTable.TypeLAVariable.LITERAL;
                }
                if (maybeRegister.variableType == SymbolTable.TypeLAVariable.REAL) {
                    output.append(maybeRegister.name);
                    return SymbolTable.TypeLAVariable.REAL;
                }
                if (maybeRegister.variableType == SymbolTable.TypeLAVariable.LOGICO) {
                    return SymbolTable.TypeLAVariable.LOGICO;
                }
                if (maybeRegister.variableType == SymbolTable.TypeLAVariable.PONT_INT) {
                    return SymbolTable.TypeLAVariable.PONT_INT;
                }
                if (maybeRegister.variableType == SymbolTable.TypeLAVariable.PONT_LIT) {
                    return SymbolTable.TypeLAVariable.PONT_LIT;
                }
                if (maybeRegister.variableType == SymbolTable.TypeLAVariable.PONT_REA) {
                    return SymbolTable.TypeLAVariable.PONT_REA;
                }
                if (maybeRegister.variableType == SymbolTable.TypeLAVariable.PONT_LOG) {
                    return SymbolTable.TypeLAVariable.PONT_LOG;
                }
            }
        } else {
            var identifierNoDimension = "";

            for (var identCtx : ctx.IDENT())
                identifierNoDimension += identCtx.getText();
            if (!table.exists(identifierNoDimension)) {
                addSemanticError(ctx.IDENT(0).getSymbol(),
                        "identificador " + identifierNoDimension + " nao declarado\n");
            } else {
                var ident = table.verify(identifierNoDimension);
                output.append(ctx.getText());
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
    public static SymbolTable.TypeLAVariable verifyType(StringBuilder output, SymbolTable table,
            LAParser.ExpressaoContext ctx) {
        SymbolTable.TypeLAVariable ret = null;
        var counter = 0;
        for (var tl : ctx.termo_logico()) {
            SymbolTable.TypeLAVariable aux = verifyType(output, table, tl);
            if (ctx.op_logico_1(counter) != null) {
                output.append(" || ");
            }

            if (ret == null) {
                ret = aux;
            } else if (!verifyType(ret, aux)) {
                ret = SymbolTable.TypeLAVariable.INVALIDO;
            }
            counter++;
        }

        return ret;
    }

    // verifyType in context of logic term
    public static SymbolTable.TypeLAVariable verifyType(StringBuilder output, SymbolTable table,
            LAParser.Termo_logicoContext ctx) {
        SymbolTable.TypeLAVariable ret = null;
        var counter = 0;
        for (var fl : ctx.fator_logico()) {
            SymbolTable.TypeLAVariable aux = verifyType(output, table, fl);
            if (ctx.op_logico_2(counter) != null) {
                output.append(" && ");
            }

            if (ret == null) {
                ret = aux;
            } else if (!verifyType(ret, aux)) {
                ret = SymbolTable.TypeLAVariable.INVALIDO;
            }
            counter++;
        }

        return ret;
    }

    // verifyType in context of logic factor
    public static SymbolTable.TypeLAVariable verifyType(StringBuilder output, SymbolTable table,
            LAParser.Fator_logicoContext ctx) {
        return verifyType(output, table, ctx.parcela_logica());
    }

    // verifyType in context of logic parcel
    public static SymbolTable.TypeLAVariable verifyType(StringBuilder output, SymbolTable table,
            LAParser.Parcela_logicaContext ctx) {
        if (ctx.exp_relacional() != null)
            return verifyType(output, table, ctx.exp_relacional());
        else {
            return SymbolTable.TypeLAVariable.LOGICO;
        }
    }

    // verifyType in context of relational expression
    public static SymbolTable.TypeLAVariable verifyType(StringBuilder output, SymbolTable table,
            LAParser.Exp_relacionalContext ctx) {
        SymbolTable.TypeLAVariable ret = null;

        if (ctx.exp_aritmetica().size() == 1)
            for (var ea : ctx.exp_aritmetica()) {
                var aux = verifyType(output, table, ea);
                if (ret == null) {
                    ret = aux;
                } else if (!verifyType(ret, aux)) {
                    ret = SymbolTable.TypeLAVariable.INVALIDO;
                }
            }
        else {
            // > 1 ">"
            var lock = false;
            for (var ea : ctx.exp_aritmetica()) {
                verifyType(output, table, ea);
                if (ctx.op_relacional() != null && !lock) {
                    if (ctx.op_relacional().getText().equals(">"))
                        output.append(">");
                    if (ctx.op_relacional().getText().equals("="))
                        output.append("==");
                    if (ctx.op_relacional().getText().equals("<>"))
                        output.append("!=");
                    if (ctx.op_relacional().getText().equals("<"))
                        output.append("<");
                    if (ctx.op_relacional().getText().equals("<="))
                        output.append("<=");
                    if (ctx.op_relacional().getText().equals(">="))
                        output.append(">=");
                    lock = true;
                }
            }
            return SymbolTable.TypeLAVariable.LOGICO;
        }

        return ret;
    }

    // verifyType in context of arithmetic expression
    public static SymbolTable.TypeLAVariable verifyType(StringBuilder output, SymbolTable table,
            LAParser.Exp_aritmeticaContext ctx) {
        SymbolTable.TypeLAVariable ret = null;
        var counter = 0;
        for (var te : ctx.termo()) {
            var aux = verifyType(output, table, te);
            if (ctx.op1(counter) != null) {
                if (ctx.op1(counter).getText().equals("+"))
                    output.append("+");
                else
                    output.append("-");
            }

            if (ret == null) {
                ret = aux;
            } else if (!verifyType(ret, aux)) {
                ret = SymbolTable.TypeLAVariable.INVALIDO;
            }
            counter++;
        }
        return ret;
    }

    // verifyType in context of term
    public static SymbolTable.TypeLAVariable verifyType(StringBuilder output, SymbolTable table,
            LAParser.TermoContext ctx) {
        SymbolTable.TypeLAVariable ret = null;
        var counter = 0;
        for (var fa : ctx.fator()) {
            var aux = verifyType(output, table, fa);
            if (ctx.op2(counter) != null) {
                if (ctx.op2(counter).getText().equals("*"))
                    output.append("*");
                else
                    output.append("/");
            }
            if (ret == null) {
                ret = aux;
            } else if (!verifyType(ret, aux)) {
                ret = SymbolTable.TypeLAVariable.INVALIDO;
            }
            counter++;
        }
        return ret;
    }

    // verifyType in context of factor
    public static SymbolTable.TypeLAVariable verifyType(StringBuilder output, SymbolTable table,
            LAParser.FatorContext ctx) {
        SymbolTable.TypeLAVariable ret = null;
        var counter = 0;
        for (var pa : ctx.parcela()) {
            var aux = verifyType(output, table, pa);
            if (ctx.op3(counter) != null) {
                output.append("%");
            }
            if (ret == null) {
                ret = aux;
            } else if (!verifyType(ret, aux)) {
                ret = SymbolTable.TypeLAVariable.INVALIDO;
            }
            counter++;
        }
        return ret;
    }

    // verifyType in context of parcel
    public static SymbolTable.TypeLAVariable verifyType(StringBuilder output, SymbolTable table,
            LAParser.ParcelaContext ctx) {

        if (ctx.parcela_unario() != null) {
            return verifyType(output, table, ctx.parcela_unario());
        } else {
            return verifyType(output, table, ctx.parcela_nao_unario());
        }
    }

    // verifyType in context of unary parcel
    public static SymbolTable.TypeLAVariable verifyType(StringBuilder output, SymbolTable table,
            LAParser.Parcela_unarioContext ctx) {
        SymbolTable.TypeLAVariable ret = null;

        if (ctx.NUM_INT() != null) {
            output.append(ctx.NUM_INT().getText());
            return SymbolTable.TypeLAVariable.INTEIRO;
        }
        if (ctx.NUM_REAL() != null) {
            output.append(ctx.NUM_REAL().getText());
            return SymbolTable.TypeLAVariable.REAL;
        }
        if (ctx.IDENT() != null) {
            // if function
            if (!table.exists(ctx.IDENT().getText())) {
                addSemanticError(ctx.identificador().IDENT(0).getSymbol(),
                        "identificador " + ctx.IDENT().getText() + " nao declarado\n");
            }

            if (table.exists(ctx.IDENT().getText())) {
                var function = table.verify(ctx.IDENT().getText());
                switch (function.specialType) {
                    // return value
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
                        // not a basic/primitive type
                        ret = SymbolTable.TypeLAVariable.REGISTRO;
                        break;
                }
                // Parameter type and number
                var nameFun = ctx.IDENT().getText();
                // var funProc = table.verify(nameFun); // FIXME: necessary?
                output.append(nameFun + "(");

                var flag = true;
                for (var exp : ctx.expressao()) {
                    verifyType(output, table, exp);
                    if (!flag)
                        output.append(",");
                    else
                        flag = false;
                }
                output.append(")");
            }

        }

        if (ctx.identificador() != null) {
            return verifyType(output, table, ctx.identificador());
        }

        if (ctx.IDENT() == null && ctx.expressao() != null) {
            for (var exp : ctx.expressao()) {
                return verifyType(output, table, ctx.expressao(0));
            }
        }

        return ret;
    }

    // verifyType in context of parcel non unary
    public static SymbolTable.TypeLAVariable verifyType(StringBuilder output, SymbolTable table,
            LAParser.Parcela_nao_unarioContext ctx) {
        SymbolTable.TypeLAVariable ret = null;

        if (ctx.CADEIA() != null) {
            output.append(ctx.CADEIA().getText());
            ret = SymbolTable.TypeLAVariable.LITERAL;
        } else {
            ret = verifyType(output, table, ctx.identificador());
            if (ctx.getText().contains("&")) {
                return SymbolTable.TypeLAVariable.ENDERECO;
            }
        }

        return ret;
    }
}