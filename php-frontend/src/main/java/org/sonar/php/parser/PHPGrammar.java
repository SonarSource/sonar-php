/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * sonarqube@googlegroups.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.php.parser;

import com.sonar.sslr.api.GenericTokenType;
import org.sonar.php.api.PHPKeyword;
import static org.sonar.php.api.PHPKeyword.ABSTRACT;
import static org.sonar.php.api.PHPKeyword.ARRAY;
import static org.sonar.php.api.PHPKeyword.AS;
import static org.sonar.php.api.PHPKeyword.BREAK;
import static org.sonar.php.api.PHPKeyword.CALLABLE;
import static org.sonar.php.api.PHPKeyword.CASE;
import static org.sonar.php.api.PHPKeyword.CATCH;
import static org.sonar.php.api.PHPKeyword.CLASS;
import static org.sonar.php.api.PHPKeyword.CLONE;
import static org.sonar.php.api.PHPKeyword.CONST;
import static org.sonar.php.api.PHPKeyword.CONTINUE;
import static org.sonar.php.api.PHPKeyword.DECLARE;
import static org.sonar.php.api.PHPKeyword.DEFAULT;
import static org.sonar.php.api.PHPKeyword.DIE;
import static org.sonar.php.api.PHPKeyword.DO;
import static org.sonar.php.api.PHPKeyword.ECHO;
import static org.sonar.php.api.PHPKeyword.ELSE;
import static org.sonar.php.api.PHPKeyword.ELSEIF;
import static org.sonar.php.api.PHPKeyword.EMPTY;
import static org.sonar.php.api.PHPKeyword.ENDDECLARE;
import static org.sonar.php.api.PHPKeyword.ENDFOR;
import static org.sonar.php.api.PHPKeyword.ENDFOREACH;
import static org.sonar.php.api.PHPKeyword.ENDIF;
import static org.sonar.php.api.PHPKeyword.ENDSWITCH;
import static org.sonar.php.api.PHPKeyword.ENDWHILE;
import static org.sonar.php.api.PHPKeyword.EVAL;
import static org.sonar.php.api.PHPKeyword.EXIT;
import static org.sonar.php.api.PHPKeyword.EXTENDS;
import static org.sonar.php.api.PHPKeyword.FINAL;
import static org.sonar.php.api.PHPKeyword.FINALLY;
import static org.sonar.php.api.PHPKeyword.FOR;
import static org.sonar.php.api.PHPKeyword.FOREACH;
import static org.sonar.php.api.PHPKeyword.FUNCTION;
import static org.sonar.php.api.PHPKeyword.GLOBAL;
import static org.sonar.php.api.PHPKeyword.GOTO;
import static org.sonar.php.api.PHPKeyword.HALT_COMPILER;
import static org.sonar.php.api.PHPKeyword.IF;
import static org.sonar.php.api.PHPKeyword.IMPLEMENTS;
import static org.sonar.php.api.PHPKeyword.INCLUDE;
import static org.sonar.php.api.PHPKeyword.INCLUDE_ONCE;
import static org.sonar.php.api.PHPKeyword.INSTANCEOF;
import static org.sonar.php.api.PHPKeyword.INSTEADOF;
import static org.sonar.php.api.PHPKeyword.INTERFACE;
import static org.sonar.php.api.PHPKeyword.ISSET;
import static org.sonar.php.api.PHPKeyword.LIST;
import static org.sonar.php.api.PHPKeyword.NAMESPACE;
import static org.sonar.php.api.PHPKeyword.NEW;
import static org.sonar.php.api.PHPKeyword.PRINT;
import static org.sonar.php.api.PHPKeyword.PRIVATE;
import static org.sonar.php.api.PHPKeyword.PROTECTED;
import static org.sonar.php.api.PHPKeyword.PUBLIC;
import static org.sonar.php.api.PHPKeyword.REQUIRE;
import static org.sonar.php.api.PHPKeyword.REQUIRE_ONCE;
import static org.sonar.php.api.PHPKeyword.RETURN;
import static org.sonar.php.api.PHPKeyword.STATIC;
import static org.sonar.php.api.PHPKeyword.SWITCH;
import static org.sonar.php.api.PHPKeyword.THROW;
import static org.sonar.php.api.PHPKeyword.TRAIT;
import static org.sonar.php.api.PHPKeyword.TRY;
import static org.sonar.php.api.PHPKeyword.UNSET;
import static org.sonar.php.api.PHPKeyword.USE;
import static org.sonar.php.api.PHPKeyword.VAR;
import static org.sonar.php.api.PHPKeyword.WHILE;
import static org.sonar.php.api.PHPKeyword.YIELD;
import org.sonar.php.api.PHPPunctuator;
import static org.sonar.php.api.PHPPunctuator.AMPERSAND;
import static org.sonar.php.api.PHPPunctuator.ANDAND;
import static org.sonar.php.api.PHPPunctuator.ANDEQUAL;
import static org.sonar.php.api.PHPPunctuator.ARROW;
import static org.sonar.php.api.PHPPunctuator.BANG;
import static org.sonar.php.api.PHPPunctuator.COLON;
import static org.sonar.php.api.PHPPunctuator.COMMA;
import static org.sonar.php.api.PHPPunctuator.CONCATEQUAL;
import static org.sonar.php.api.PHPPunctuator.DEC;
import static org.sonar.php.api.PHPPunctuator.DIV;
import static org.sonar.php.api.PHPPunctuator.DIVEQUAL;
import static org.sonar.php.api.PHPPunctuator.DOLAR;
import static org.sonar.php.api.PHPPunctuator.DOLAR_LCURLY;
import static org.sonar.php.api.PHPPunctuator.DOT;
import static org.sonar.php.api.PHPPunctuator.DOUBLEARROW;
import static org.sonar.php.api.PHPPunctuator.DOUBLECOLON;
import static org.sonar.php.api.PHPPunctuator.ELIPSIS;
import static org.sonar.php.api.PHPPunctuator.EQU;
import static org.sonar.php.api.PHPPunctuator.EQUAL;
import static org.sonar.php.api.PHPPunctuator.EQUAL2;
import static org.sonar.php.api.PHPPunctuator.GE;
import static org.sonar.php.api.PHPPunctuator.GT;
import static org.sonar.php.api.PHPPunctuator.INC;
import static org.sonar.php.api.PHPPunctuator.LBRACKET;
import static org.sonar.php.api.PHPPunctuator.LCURLYBRACE;
import static org.sonar.php.api.PHPPunctuator.LE;
import static org.sonar.php.api.PHPPunctuator.LPARENTHESIS;
import static org.sonar.php.api.PHPPunctuator.LT;
import static org.sonar.php.api.PHPPunctuator.MINUS;
import static org.sonar.php.api.PHPPunctuator.MINUS_EQU;
import static org.sonar.php.api.PHPPunctuator.MOD;
import static org.sonar.php.api.PHPPunctuator.MOD_EQU;
import static org.sonar.php.api.PHPPunctuator.NOTEQUAL;
import static org.sonar.php.api.PHPPunctuator.NOTEQUAL2;
import static org.sonar.php.api.PHPPunctuator.NOTEQUALBIS;
import static org.sonar.php.api.PHPPunctuator.NS_SEPARATOR;
import static org.sonar.php.api.PHPPunctuator.OR;
import static org.sonar.php.api.PHPPunctuator.OROR;
import static org.sonar.php.api.PHPPunctuator.OR_EQU;
import static org.sonar.php.api.PHPPunctuator.PLUS;
import static org.sonar.php.api.PHPPunctuator.PLUS_EQU;
import static org.sonar.php.api.PHPPunctuator.QUERY;
import static org.sonar.php.api.PHPPunctuator.RBRACKET;
import static org.sonar.php.api.PHPPunctuator.RCURLYBRACE;
import static org.sonar.php.api.PHPPunctuator.RPARENTHESIS;
import static org.sonar.php.api.PHPPunctuator.SEMICOLON;
import static org.sonar.php.api.PHPPunctuator.SL;
import static org.sonar.php.api.PHPPunctuator.SL_EQU;
import static org.sonar.php.api.PHPPunctuator.SR;
import static org.sonar.php.api.PHPPunctuator.SR_EQU;
import static org.sonar.php.api.PHPPunctuator.STAR;
import static org.sonar.php.api.PHPPunctuator.STAR_EQU;
import static org.sonar.php.api.PHPPunctuator.TILDA;
import static org.sonar.php.api.PHPPunctuator.XOR;
import static org.sonar.php.api.PHPPunctuator.XOR_EQU;
import org.sonar.sslr.grammar.GrammarRuleKey;
import org.sonar.sslr.grammar.LexerlessGrammarBuilder;
import org.sonar.sslr.parser.LexerlessGrammar;

public enum PHPGrammar implements GrammarRuleKey {

  COMPILATION_UNIT,
  SCRIPT,
  TOP_STATEMENT_LIST,

  /**
   * Declaration
   */
  USE_DECLARATIONS,
  USE_DECLARATION,
  USE_FUNCTION_DECLARATION_STATEMENT,
  USE_CONST_DECLARATION_STATEMENT,
  USE_FUNCTION_DECLARATIONS,
  USE_FUNCTION_DECLARATION,
  HALT_COMPILER_STATEMENT,

  NAMESPACE_NAME,
  UNQUALIFIED_NAME,
  QUALIFIED_NAME,
  FULLY_QUALIFIED_NAME,

  REFERENCE,
  FUNCTION_DECLARATION,
  CLASS_DECLARATION,
  CLASS_ENTRY_TYPE,
  CLASS_TYPE,
  FULLY_QUALIFIED_CLASS_NAME,
  INTERFACE_LIST,
  INTERFACE_DECLARATION,
  INTERFACE_EXTENDS_LIST,
  EXTENDS_FROM,
  IMPLEMENTS_LIST,
  CONSTANT_DECLARATION,
  CONSTANT_VAR,

  /**
   * Lexical
   */
  EOF,
  HEREDOC,
  NUMERIC_LITERAL,
  STRING_LITERAL,
  STRING_WITH_ENCAPS_VAR_CHARACTERS,
  ENCAPS_VAR_IDENTIFIER,
  REGULAR_VAR_IDENTIFIER,
  VAR_IDENTIFIER,
  IDENTIFIER,
  FILE_OPENING_TAG,
  INLINE_HTML,
  KEYWORDS,

  /**
   * SPACING
   */
  SPACING,

  /**
   * End of statement.
   */
  EOS,

  /**
   * Statement
   */
  USE_STATEMENT,
  TOP_STATEMENT,
  NAMESPACE_STATEMENT,
  STATEMENT,
  EMPTY_STATEMENT,
  LABEL,
  BLOCK,
  INNER_STATEMENT_LIST,
  CLASS_STATEMENT,
  IF_STATEMENT,
  ELSEIF_LIST,
  ELSEIF_CLAUSE,
  ELSE_CLAUSE,
  ALTERNATIVE_IF_STATEMENT,
  ALTERNATIVE_ELSEIF_LIST,
  ALTERNATIVE_ELSEIF_CLAUSE,
  ALTERNATIVE_ELSE_CLAUSE,
  WHILE_STATEMENT,
  ALTERNATIVE_WHILE_STATEMENT,
  DO_WHILE_STATEMENT,
  ALTERNATIVE_DO_WHILE_STATEMENT,
  FOR_STATEMENT,
  ALTERNATIVE_FOR_STATEMENT,
  FOR_EXRR,
  FOREACH_STATEMENT,
  ALTERNATIVE_FOREACH_STATEMENT,
  FOREACH_EXPR,
  FOREACH_VARIABLE,
  SWITCH_STATEMENT,
  SWITCH_CASE_LIST,
  CASE_LIST,
  CASE_CLAUSE,
  DEFAULT_CLAUSE,
  CASE_SEPARTOR,
  BREAK_STATEMENT,
  CONTINUE_STATEMENT,
  RETURN_STATEMENT,
  DECLARE_STATEMENT,
  ALTERNATIVE_DECLARE_STATEMENT,
  DECLARE_LIST,
  TRY_STATEMENT,
  CATCH_STATEMENT,
  FINALLY_STATEMENT,
  THROW_STATEMENT,
  GOTO_STATEMENT,
  YIELD_STATEMENT,
  GLOBAL_STATEMENT,
  GLOBAL_VAR_LIST,
  GLOBAL_VAR,
  STATIC_STATEMENT,
  STATIC_VAR_LIST,
  STATIC_VAR,
  ECHO_STATEMENT,
  UNSET_VARIABLE_STATEMENT,
  UNSET_VARIABLES,
  PARAMETER_LIST,
  PARAMETER,
  OPTIONAL_CLASS_TYPE,
  METHOD_DECLARATION,
  METHOD_BODY,
  VARIABLE_MODIFIERS,
  CLASS_VARIABLE_DECLARATION,
  VARIABLE_DECLARATION,
  MEMBER_MODIFIER,
  CLASS_CONSTANT_DECLARATION,
  MEMBER_CONST_DECLARATION,
  TRAIT_USE_STATEMENT,
  TRAIT_METHOD_REFERENCE_FULLY_QUALIFIED,
  TRAIT_METHOD_REFERENCE,
  TRAIT_ALIAS,
  TRAIT_PRECEDENCE,
  TRAIT_ADAPTATION_STATEMENT,
  TRAIT_ADAPTATIONS,

  /**
   * Expression
   */
  PRIMARY_EXPRESSION,
  MEMBER_EXPRESSION,
  ALIAS_VARIABLE,
  VARIABLE_NAME,
  COMPUTED_VARIABLE_NAME,
  VARIABLE_WITHOUT_OBJECTS,
  EXPRESSION_STATEMENT,
  REFERENCE_VARIABLE,
  CLASS_MEMBER_ACCESS,
  OBJECT_MEMBER_ACCESS,
  SIMPLE_INDIRECT_REFERENCE,
  STATIC_MEMBER,
  COMPOUND_VARIABLE,
  CLASS_NAME,
  FUNCTION_CALL_PARAMETER_LIST,
  PARAMETER_LIST_FOR_CALL,
  DIMENSIONAL_OFFSET,
  STATIC_SCALAR,
  OBJECT_DIM_LIST,
  PARENTHESIS_EXPRESSION,
  YIELD_EXPRESSION,
  COMBINED_SCALAR,
  COMMON_SCALAR,
  BOOLEAN_LITERAL,
  LEXICAL_VARS,
  LEXICAL_VAR_LIST,
  LEXICAL_VAR,
  LOGICAL_XOR_EXPR,
  LOGICAL_OR_EXPR,
  LOGICAL_OR_OPERATOR,
  BITEWISE_AND_EXPR,
  BITEWISE_XOR_EXPR,
  BITEWISE_OR_EXPR,
  LOGICAL_AND_EXPR,
  LOGICAL_AND_OPERATOR,
  CONDITIONAL_EXPR,
  ASSIGNMENT_EXPR,
  MULTIPLICATIVE_EXPR,
  MULIPLICATIVE_OPERATOR,
  ADDITIVE_EXPR,
  ADDITIVE_OPERATOR,
  SHIFT_EXPR,
  SHIFT_OPERATOR,
  RELATIONAL_EXPR,
  RELATIONAL_OPERATOR,
  EQUALITY_EXPR,
  EQUALITY_OPERATOR,
  CONCATENATION_EXPR,
  POSTFIX_EXPR,
  UNARY_EXPR,
  ASSIGNMENT_BY_REFERENCE,
  ASSIGNMENT_OPERATOR,
  COMPOUND_ASSIGNMENT,
  CAST_TYPE,
  LOGICAL_ASSIGNMENT,
  INTERNAL_FUNCTION,
  NEW_EXPR,
  COMBINED_SCALAR_OFFSET,
  ARRAY_PAIR_LIST,
  ARRAY_PAIR,
  EXIT_EXPR,
  LIST_EXPR,
  LIST_ASSIGNMENT_EXPR,
  ASSIGNMENT_LIST_ELEMENT,
  ASSIGNMENT_LIST,
  FUNCTION_EXPRESSION,
  ENCAPS_STRING_LITERAL,
  COMPLEX_ENCAPS_VARIABLE,
  SEMI_COMPLEX_ENCAPS_VARIABLE,
  SEMI_COMPLEX_RECOVERY_EXPRESSION,
  SIMPLE_ENCAPS_VARIABLE,
  ENCAPS_LIST,
  ENCAPS_VAR_OFFSET,
  ENCAPS_VAR,
  ENCAPS_DIMENSIONAL_OFFSET,
  ENCAPS_OBJECT_MEMBER_ACCESS,

  EXPRESSION;


  public static LexerlessGrammar createGrammar() {
    return createGrammarBuilder().build();
  }

  private static LexerlessGrammarBuilder createGrammarBuilder() {
    LexerlessGrammarBuilder b = LexerlessGrammarBuilder.create();

    b.rule(COMPILATION_UNIT).is(b.optional(SCRIPT), SPACING, EOF);
    b.rule(SCRIPT).is(FILE_OPENING_TAG, b.optional(TOP_STATEMENT_LIST));

    lexical(b);
    punctuators(b);
    keywords(b);
    declaration(b);
    statement(b);
    expression(b);

    b.setRootRule(COMPILATION_UNIT);

    return b;
  }

  public static void lexical(LexerlessGrammarBuilder b) {
    b.rule(SPACING).is(
      b.skippedTrivia(b.regexp("[" + LexicalConstant.LINE_TERMINATOR + LexicalConstant.WHITESPACE + "]*+")),
      b.zeroOrMore(
        b.commentTrivia(b.regexp(LexicalConstant.COMMENT)),
        b.skippedTrivia(b.regexp("[" + LexicalConstant.LINE_TERMINATOR + LexicalConstant.WHITESPACE + "]*+")))
    ).skip();

    // Literals
    b.rule(HEREDOC).is(SPACING, b.regexp(LexicalConstant.HEREDOC));
    b.rule(NUMERIC_LITERAL).is(SPACING, b.regexp(LexicalConstant.NUMERIC_LITERAL));
    b.rule(STRING_LITERAL).is(SPACING, b.firstOf(b.regexp(LexicalConstant.STRING_LITERAL), ENCAPS_STRING_LITERAL));

    b.rule(STRING_WITH_ENCAPS_VAR_CHARACTERS).is(b.regexp(LexicalConstant.STRING_WITH_ENCAPS_VAR_CHARACTERS));
    b.rule(ENCAPS_STRING_LITERAL).is(SPACING, "\"", ENCAPS_LIST, "\"");

    // Identifier
    b.rule(ENCAPS_VAR_IDENTIFIER).is(
      b.regexp("[" + LexicalConstant.WHITESPACE + "]*+"),
      VAR_IDENTIFIER).skip();
    b.rule(REGULAR_VAR_IDENTIFIER).is(SPACING, VAR_IDENTIFIER).skip();
    b.rule(VAR_IDENTIFIER).is(b.regexp(LexicalConstant.VAR_IDENTIFIER));
    b.rule(IDENTIFIER).is(SPACING, b.nextNot(KEYWORDS), b.regexp(LexicalConstant.IDENTIFIER));

    // Tags & Inline HTML
    b.rule(FILE_OPENING_TAG).is(SPACING, b.token(PHPTokenType.FILE_OPENING_TAG, b.regexp(LexicalConstant.PHP_START_TAG))).skip();
    b.rule(INLINE_HTML).is(SPACING, b.token(PHPTokenType.INLINE_HTML, b.regexp(LexicalConstant.PHP_END_TAG))).skip();

    b.rule(EOF).is(b.token(GenericTokenType.EOF, b.endOfInput())).skip();
  }

  public static void expression(LexerlessGrammarBuilder b) {
    b.rule(PARENTHESIS_EXPRESSION).is(LPARENTHESIS, b.firstOf(YIELD_EXPRESSION, EXPRESSION), RPARENTHESIS);

    b.rule(YIELD_EXPRESSION).is(YIELD, EXPRESSION, b.optional(DOUBLEARROW, EXPRESSION));

    b.rule(SIMPLE_INDIRECT_REFERENCE).is(b.oneOrMore(DOLAR, b.nextNot(b.firstOf(IDENTIFIER, KEYWORDS, LCURLYBRACE))));

    b.rule(COMPUTED_VARIABLE_NAME).is(LCURLYBRACE, EXPRESSION, RCURLYBRACE);
    b.rule(REFERENCE_VARIABLE).is(COMPOUND_VARIABLE, b.zeroOrMore(b.firstOf(
      DIMENSIONAL_OFFSET,
      COMPUTED_VARIABLE_NAME)));

    b.rule(VARIABLE_WITHOUT_OBJECTS).is(b.optional(SIMPLE_INDIRECT_REFERENCE), REFERENCE_VARIABLE);

    b.rule(COMPOUND_VARIABLE).is(b.firstOf(
      REGULAR_VAR_IDENTIFIER,
      b.sequence(DOLAR_LCURLY, EXPRESSION, RCURLYBRACE)));

    b.rule(CLASS_NAME).is(b.firstOf(STATIC, FULLY_QUALIFIED_CLASS_NAME));

    b.rule(FUNCTION_CALL_PARAMETER_LIST).is(LPARENTHESIS, b.optional(PARAMETER_LIST_FOR_CALL, b.zeroOrMore(COMMA, PARAMETER_LIST_FOR_CALL)), RPARENTHESIS);
    b.rule(PARAMETER_LIST_FOR_CALL).is(b.firstOf(ALIAS_VARIABLE, b.sequence(b.optional(ELIPSIS), EXPRESSION), YIELD_EXPRESSION));

    b.rule(VARIABLE_NAME).is(b.firstOf(
      IDENTIFIER,
      KEYWORDS,
      COMPUTED_VARIABLE_NAME));

    b.rule(DIMENSIONAL_OFFSET).is(LBRACKET, b.optional(EXPRESSION), RBRACKET);
    b.rule(ALIAS_VARIABLE).is(AMPERSAND, MEMBER_EXPRESSION);

    b.rule(MEMBER_EXPRESSION).is(
      PRIMARY_EXPRESSION,
      b.zeroOrMore(b.firstOf(
        OBJECT_MEMBER_ACCESS,
        CLASS_MEMBER_ACCESS,
        DIMENSIONAL_OFFSET,
        FUNCTION_CALL_PARAMETER_LIST
      ))
    );

    b.rule(PRIMARY_EXPRESSION).is(
      b.firstOf(
        b.sequence(PHPKeyword.NAMESPACE, FULLY_QUALIFIED_NAME),
        CLASS_NAME,
        VARIABLE_WITHOUT_OBJECTS,
        IDENTIFIER,
        PARENTHESIS_EXPRESSION)
    ).skip();

    b.rule(OBJECT_MEMBER_ACCESS).is(ARROW, b.firstOf(VARIABLE_WITHOUT_OBJECTS, OBJECT_DIM_LIST, IDENTIFIER));
    b.rule(CLASS_MEMBER_ACCESS).is(DOUBLECOLON, b.firstOf(
      VARIABLE_WITHOUT_OBJECTS,
      IDENTIFIER,
      PHPKeyword.CLASS,
      COMPUTED_VARIABLE_NAME));

    b.rule(OBJECT_DIM_LIST).is(VARIABLE_NAME, b.zeroOrMore(b.firstOf(
      COMPUTED_VARIABLE_NAME,
      DIMENSIONAL_OFFSET
    )));

    b.rule(STATIC_SCALAR).is(b.firstOf(COMBINED_SCALAR, CONDITIONAL_EXPR));

    b.rule(COMBINED_SCALAR_OFFSET).is(COMBINED_SCALAR, b.zeroOrMore(DIMENSIONAL_OFFSET));

    b.rule(COMBINED_SCALAR).is(b.firstOf(
      b.sequence(ARRAY, LPARENTHESIS, b.optional(ARRAY_PAIR_LIST), RPARENTHESIS),
      b.sequence(LBRACKET, b.optional(ARRAY_PAIR_LIST), RBRACKET)));

    b.rule(ARRAY_PAIR_LIST).is(ARRAY_PAIR, b.zeroOrMore(COMMA, ARRAY_PAIR), b.optional(COMMA));
    b.rule(ARRAY_PAIR).is(b.firstOf(
      b.sequence(EXPRESSION, b.optional(DOUBLEARROW, b.firstOf(ALIAS_VARIABLE, EXPRESSION))),
      ALIAS_VARIABLE));

    b.rule(COMMON_SCALAR).is(b.firstOf(
      HEREDOC,
      NUMERIC_LITERAL,
      STRING_LITERAL,
      BOOLEAN_LITERAL,
      word(b, "NULL"),
      word(b, "__CLASS__"),
      word(b, "__FILE__"),
      word(b, "__DIR__"),
      word(b, "__FUNCTION__"),
      word(b, "__LINE__"),
      word(b, "__METHOD__"),
      word(b, "__NAMESPACE__"),
      word(b, "__TRAIT__")));

    b.rule(BOOLEAN_LITERAL).is(b.firstOf(word(b, "TRUE"), word(b, "FALSE")));

    b.rule(ENCAPS_LIST).is(
      b.oneOrMore(
        b.firstOf(
          ENCAPS_VAR,
          STRING_WITH_ENCAPS_VAR_CHARACTERS)
      )
    );

    b.rule(ENCAPS_VAR).is(
      b.firstOf(
        SEMI_COMPLEX_ENCAPS_VARIABLE,
        SIMPLE_ENCAPS_VARIABLE,
        COMPLEX_ENCAPS_VARIABLE)
    );

    b.rule(COMPLEX_ENCAPS_VARIABLE).is(LCURLYBRACE, b.next(DOLAR), EXPRESSION, RCURLYBRACE);
    b.rule(SEMI_COMPLEX_ENCAPS_VARIABLE).is(
      DOLAR_LCURLY,
      b.firstOf(
        EXPRESSION,
        SEMI_COMPLEX_RECOVERY_EXPRESSION),
      RCURLYBRACE);
    // FIXME: this recovery is introduce in order to parse ${var}, as expression cannot match keywords.
    b.rule(SEMI_COMPLEX_RECOVERY_EXPRESSION).is(b.regexp("[^}]++"));

    b.rule(SIMPLE_ENCAPS_VARIABLE).is(
      ENCAPS_VAR_IDENTIFIER,
      b.optional(b.firstOf(
        ENCAPS_DIMENSIONAL_OFFSET,
        ENCAPS_OBJECT_MEMBER_ACCESS))
    );

    b.rule(ENCAPS_OBJECT_MEMBER_ACCESS).is(ARROW, IDENTIFIER);
    b.rule(ENCAPS_DIMENSIONAL_OFFSET).is(LBRACKET, ENCAPS_VAR_OFFSET, RBRACKET);
    b.rule(ENCAPS_VAR_OFFSET).is(b.firstOf(
      IDENTIFIER,
      NUMERIC_LITERAL,
      ENCAPS_VAR_IDENTIFIER));

    b.rule(CAST_TYPE).is(
      LPARENTHESIS,
      b.firstOf(
        word(b, "INTEGER"),
        word(b, "INT"),
        word(b, "DOUBLE"),
        word(b, "FLOAT"),
        word(b, "STRING"),
        ARRAY,
        word(b, "OBJECT"),
        word(b, "BOOLEAN"),
        word(b, "BOOL"),
        word(b, "BINARY"),
        UNSET),
      RPARENTHESIS);

    b.rule(POSTFIX_EXPR).is(b.firstOf( // TODO martin: to complete
        //YIELD, TODO martin: check
        COMBINED_SCALAR_OFFSET,
        FUNCTION_EXPRESSION,
        COMMON_SCALAR,
        MEMBER_EXPRESSION,
        NEW_EXPR,
        EXIT_EXPR,
        LIST_ASSIGNMENT_EXPR,
        INTERNAL_FUNCTION),
      b.optional(b.firstOf(
        INC,
        DEC,
        b.sequence(INSTANCEOF, MEMBER_EXPRESSION)))
    );

    b.rule(FUNCTION_EXPRESSION).is(b.optional(STATIC), FUNCTION, b.optional(AMPERSAND), LPARENTHESIS, b.optional(PARAMETER_LIST), RPARENTHESIS,
      b.optional(LEXICAL_VARS), BLOCK);
    b.rule(LEXICAL_VARS).is(USE, LPARENTHESIS, LEXICAL_VAR_LIST, RPARENTHESIS);
    b.rule(LEXICAL_VAR_LIST).is(LEXICAL_VAR, b.zeroOrMore(COMMA, LEXICAL_VAR));
    b.rule(LEXICAL_VAR).is(b.optional(AMPERSAND), REGULAR_VAR_IDENTIFIER);

    b.rule(EXIT_EXPR).is(b.firstOf(EXIT, DIE), b.optional(LPARENTHESIS, b.optional(EXPRESSION), RPARENTHESIS));

    b.rule(LIST_ASSIGNMENT_EXPR).is(LIST_EXPR, EQU, EXPRESSION);
    // FIXME: looks like only assignment list is optional not the parenthesis to be checked
    b.rule(LIST_EXPR).is(LIST, b.optional(LPARENTHESIS, ASSIGNMENT_LIST, RPARENTHESIS));
    b.rule(ASSIGNMENT_LIST).is(b.optional(ASSIGNMENT_LIST_ELEMENT), b.zeroOrMore(COMMA, b.optional(ASSIGNMENT_LIST_ELEMENT)));
    b.rule(ASSIGNMENT_LIST_ELEMENT).is(b.firstOf(MEMBER_EXPRESSION, LIST_ASSIGNMENT_EXPR));

    b.rule(INTERNAL_FUNCTION).is(b.firstOf(
      b.sequence(ISSET, LPARENTHESIS, EXPRESSION, b.zeroOrMore(COMMA, EXPRESSION), RPARENTHESIS),
      b.sequence(EMPTY, LPARENTHESIS, EXPRESSION, RPARENTHESIS),
      b.sequence(INCLUDE_ONCE, EXPRESSION),
      b.sequence(INCLUDE, EXPRESSION),
      b.sequence(EVAL, LPARENTHESIS, EXPRESSION, RPARENTHESIS),
      b.sequence(REQUIRE_ONCE, EXPRESSION),
      b.sequence(REQUIRE, EXPRESSION),
      b.sequence(CLONE, EXPRESSION),
      b.sequence(PRINT, EXPRESSION)));

    // FUNCTION_PARAMETERS_CALL_LIST after MEMBER_EXPRESSION has been removed because PEG is greedy and MEMBER_EXPRESSION will always consume
    // FUNCTION_PARAMETERS_CALL_LIST.
    b.rule(NEW_EXPR).is(NEW, MEMBER_EXPRESSION);

    b.rule(ASSIGNMENT_BY_REFERENCE).is(MEMBER_EXPRESSION, EQU, AMPERSAND, b.firstOf(NEW_EXPR, MEMBER_EXPRESSION));
    b.rule(ASSIGNMENT_EXPR).is(b.sequence(MEMBER_EXPRESSION, ASSIGNMENT_OPERATOR, EXPRESSION));
    b.rule(ASSIGNMENT_OPERATOR).is(b.firstOf(EQU, COMPOUND_ASSIGNMENT, LOGICAL_ASSIGNMENT));
    b.rule(COMPOUND_ASSIGNMENT).is(b.firstOf(STAR_EQU, DIVEQUAL, MOD_EQU, PLUS_EQU, MINUS_EQU, SL_EQU, SR_EQU, CONCATEQUAL));
    b.rule(LOGICAL_ASSIGNMENT).is(b.firstOf(ANDEQUAL, XOR_EQU, OR_EQU));

    // Unary expression
    b.rule(UNARY_EXPR).is(b.firstOf(  // TODO martin: re-arrange & complete
      b.sequence(b.firstOf(INC, DEC), UNARY_EXPR),
      b.sequence(b.firstOf(PLUS, MINUS, TILDA, BANG), UNARY_EXPR),
      b.sequence(PHPPunctuator.AT, UNARY_EXPR),
      b.sequence(CAST_TYPE, UNARY_EXPR),
      ASSIGNMENT_EXPR,
      ASSIGNMENT_BY_REFERENCE,
      POSTFIX_EXPR)).skipIfOneChild();
    // Binary expressions
    b.rule(CONCATENATION_EXPR).is(UNARY_EXPR, b.zeroOrMore(DOT, UNARY_EXPR)).skipIfOneChild();
    b.rule(MULTIPLICATIVE_EXPR).is(CONCATENATION_EXPR, b.zeroOrMore(MULIPLICATIVE_OPERATOR, CONCATENATION_EXPR)).skipIfOneChild();
    b.rule(MULIPLICATIVE_OPERATOR).is(b.firstOf(STAR, DIV, MOD));
    b.rule(ADDITIVE_EXPR).is(MULTIPLICATIVE_EXPR, b.zeroOrMore(ADDITIVE_OPERATOR, MULTIPLICATIVE_EXPR)).skipIfOneChild();
    b.rule(ADDITIVE_OPERATOR).is(b.firstOf(PLUS, MINUS));
    b.rule(SHIFT_EXPR).is(ADDITIVE_EXPR, b.zeroOrMore(SHIFT_OPERATOR, ADDITIVE_EXPR)).skipIfOneChild();
    b.rule(SHIFT_OPERATOR).is(b.firstOf(SL, SR));
    b.rule(RELATIONAL_EXPR).is(SHIFT_EXPR, b.zeroOrMore(RELATIONAL_OPERATOR, SHIFT_EXPR)).skipIfOneChild();
    b.rule(RELATIONAL_OPERATOR).is(b.firstOf(LE, GE, LT, GT));
    b.rule(EQUALITY_EXPR).is(RELATIONAL_EXPR, b.zeroOrMore(EQUALITY_OPERATOR, RELATIONAL_EXPR)).skipIfOneChild();
    b.rule(EQUALITY_OPERATOR).is(b.firstOf(NOTEQUAL2, NOTEQUAL, EQUAL2, EQUAL, NOTEQUALBIS));
    b.rule(BITEWISE_AND_EXPR).is(EQUALITY_EXPR, b.zeroOrMore(AMPERSAND, EQUALITY_EXPR)).skipIfOneChild();
    b.rule(BITEWISE_XOR_EXPR).is(BITEWISE_AND_EXPR, b.zeroOrMore(XOR, BITEWISE_AND_EXPR)).skipIfOneChild();
    b.rule(BITEWISE_OR_EXPR).is(BITEWISE_XOR_EXPR, b.zeroOrMore(OR, BITEWISE_XOR_EXPR)).skipIfOneChild();
    b.rule(LOGICAL_AND_EXPR).is(BITEWISE_OR_EXPR, b.zeroOrMore(LOGICAL_AND_OPERATOR, BITEWISE_OR_EXPR)).skipIfOneChild();
    b.rule(LOGICAL_AND_OPERATOR).is(b.firstOf(ANDAND, PHPKeyword.AND));
    b.rule(LOGICAL_XOR_EXPR).is(LOGICAL_AND_EXPR, b.zeroOrMore(PHPKeyword.XOR, LOGICAL_AND_EXPR)).skipIfOneChild();
    b.rule(LOGICAL_OR_EXPR).is(LOGICAL_XOR_EXPR, b.zeroOrMore(LOGICAL_OR_OPERATOR, LOGICAL_XOR_EXPR)).skipIfOneChild();
    b.rule(LOGICAL_OR_OPERATOR).is(b.firstOf(OROR, PHPKeyword.OR));
    b.rule(CONDITIONAL_EXPR).is(LOGICAL_OR_EXPR, b.optional(QUERY, b.optional(CONDITIONAL_EXPR), COLON, CONDITIONAL_EXPR)).skipIfOneChild();
    b.rule(EXPRESSION).is(CONDITIONAL_EXPR);
  }

  public static void declaration(LexerlessGrammarBuilder b) {
    // TODO: use declaration should be simplified and merged with use_statement
    // FIXME: also USE_CONST_DECLARATION_STATEMENT is never used...
    b.rule(USE_CONST_DECLARATION_STATEMENT).is(USE, CONST, USE_FUNCTION_DECLARATIONS, EOS);
    b.rule(USE_FUNCTION_DECLARATION_STATEMENT).is(USE, FUNCTION, USE_FUNCTION_DECLARATIONS, EOS); // TODO martin: to check
    b.rule(USE_FUNCTION_DECLARATIONS).is(USE_FUNCTION_DECLARATION, b.zeroOrMore(COMMA, USE_FUNCTION_DECLARATION));
    b.rule(USE_FUNCTION_DECLARATION).is(NAMESPACE_NAME, b.optional(AS, IDENTIFIER));

    b.rule(USE_DECLARATIONS).is(USE_DECLARATION, b.zeroOrMore(COMMA, USE_DECLARATION));
    b.rule(USE_DECLARATION).is(b.firstOf(
      b.sequence(NAMESPACE_NAME, AS, IDENTIFIER),
      NAMESPACE_NAME));

    // Class declaration
    b.rule(CLASS_DECLARATION).is(CLASS_ENTRY_TYPE, IDENTIFIER, b.optional(EXTENDS_FROM), b.optional(IMPLEMENTS_LIST),
      LCURLYBRACE, b.zeroOrMore(CLASS_STATEMENT), RCURLYBRACE);
    b.rule(CLASS_ENTRY_TYPE).is(b.firstOf(
      b.sequence(b.optional(CLASS_TYPE), CLASS),
      TRAIT));
    b.rule(CLASS_TYPE).is(b.firstOf(ABSTRACT, FINAL));

    b.rule(EXTENDS_FROM).is(EXTENDS, FULLY_QUALIFIED_CLASS_NAME);
    b.rule(IMPLEMENTS_LIST).is(IMPLEMENTS, INTERFACE_LIST);
    b.rule(INTERFACE_LIST).is(FULLY_QUALIFIED_CLASS_NAME, b.zeroOrMore(COMMA, FULLY_QUALIFIED_CLASS_NAME));
    b.rule(FULLY_QUALIFIED_CLASS_NAME).is(b.firstOf(
      b.sequence(NAMESPACE, FULLY_QUALIFIED_NAME),
      NAMESPACE_NAME));

    // Interface declaration
    b.rule(INTERFACE_DECLARATION).is(INTERFACE, IDENTIFIER, b.optional(INTERFACE_EXTENDS_LIST),
      LCURLYBRACE, b.zeroOrMore(CLASS_STATEMENT), RCURLYBRACE);
    b.rule(INTERFACE_EXTENDS_LIST).is(EXTENDS, INTERFACE_LIST);

    b.rule(CONSTANT_DECLARATION).is(CONST, CONSTANT_VAR, b.zeroOrMore(COMMA, CONSTANT_VAR), EOS);
    b.rule(CONSTANT_VAR).is(IDENTIFIER, EQU, STATIC_SCALAR);

    b.rule(METHOD_DECLARATION).is(b.zeroOrMore(MEMBER_MODIFIER), FUNCTION, b.optional(REFERENCE), IDENTIFIER,
      LPARENTHESIS, b.optional(PARAMETER_LIST), RPARENTHESIS, METHOD_BODY);
    b.rule(METHOD_BODY).is(b.firstOf(EOS, BLOCK));

    b.rule(PARAMETER_LIST).is(PARAMETER, b.zeroOrMore(COMMA, PARAMETER));
    b.rule(PARAMETER).is(b.optional(OPTIONAL_CLASS_TYPE), b.optional(AMPERSAND), b.optional(ELIPSIS), REGULAR_VAR_IDENTIFIER, b.optional(EQU, STATIC_SCALAR));
    b.rule(OPTIONAL_CLASS_TYPE).is(b.firstOf(ARRAY, CALLABLE, FULLY_QUALIFIED_CLASS_NAME));

    b.rule(CLASS_VARIABLE_DECLARATION).is(VARIABLE_MODIFIERS, VARIABLE_DECLARATION, b.zeroOrMore(COMMA, VARIABLE_DECLARATION), EOS);
    b.rule(VARIABLE_DECLARATION).is(REGULAR_VAR_IDENTIFIER, b.optional(EQU, STATIC_SCALAR));
    b.rule(VARIABLE_MODIFIERS).is(b.firstOf(VAR, b.oneOrMore(MEMBER_MODIFIER)));

    b.rule(CLASS_CONSTANT_DECLARATION).is(CONST, MEMBER_CONST_DECLARATION, b.zeroOrMore(COMMA, MEMBER_CONST_DECLARATION), EOS);
    b.rule(MEMBER_CONST_DECLARATION).is(IDENTIFIER, b.optional(EQU, STATIC_SCALAR));

    b.rule(MEMBER_MODIFIER).is(b.firstOf(
      PUBLIC,
      PROTECTED,
      PRIVATE,
      STATIC,
      ABSTRACT,
      FINAL));

    b.rule(REFERENCE).is(AMPERSAND);
    b.rule(FUNCTION_DECLARATION).is(FUNCTION, b.optional(REFERENCE), IDENTIFIER,
      LPARENTHESIS, b.optional(PARAMETER_LIST), RPARENTHESIS, BLOCK);

  }

  public static void statement(LexerlessGrammarBuilder b) {
    b.rule(TOP_STATEMENT_LIST).is(b.oneOrMore(TOP_STATEMENT));

    b.rule(TOP_STATEMENT).is(b.firstOf(
      CLASS_DECLARATION,
      FUNCTION_DECLARATION,
      INTERFACE_DECLARATION,
      NAMESPACE_STATEMENT,
      USE_STATEMENT,
      USE_FUNCTION_DECLARATION_STATEMENT,
      CONSTANT_DECLARATION,
      HALT_COMPILER_STATEMENT,
      STATEMENT
    ));

    b.rule(NAMESPACE_NAME).is(b.firstOf(
      FULLY_QUALIFIED_NAME,
      QUALIFIED_NAME,
      UNQUALIFIED_NAME));

    b.rule(UNQUALIFIED_NAME).is(IDENTIFIER);
    b.rule(QUALIFIED_NAME).is(IDENTIFIER, b.oneOrMore(NS_SEPARATOR, IDENTIFIER));
    b.rule(FULLY_QUALIFIED_NAME).is(NS_SEPARATOR, IDENTIFIER, b.zeroOrMore(NS_SEPARATOR, IDENTIFIER));

    b.rule(NAMESPACE_STATEMENT).is(b.firstOf(
      b.sequence(NAMESPACE, NAMESPACE_NAME, EOS),
      b.sequence(NAMESPACE, b.optional(NAMESPACE_NAME), LCURLYBRACE, b.optional(TOP_STATEMENT_LIST), RCURLYBRACE)));

    b.rule(USE_STATEMENT).is(USE, USE_DECLARATIONS, EOS);

    b.rule(HALT_COMPILER_STATEMENT).is(HALT_COMPILER, LPARENTHESIS, RPARENTHESIS, EOS);

    b.rule(STATEMENT).is(b.firstOf(
      BLOCK,
      ALTERNATIVE_IF_STATEMENT,
      THROW_STATEMENT,
      IF_STATEMENT,
      WHILE_STATEMENT,
      DO_WHILE_STATEMENT,
      FOREACH_STATEMENT,
      FOR_STATEMENT,
      SWITCH_STATEMENT,
      BREAK_STATEMENT,
      CONTINUE_STATEMENT,
      RETURN_STATEMENT,
      EMPTY_STATEMENT,
      YIELD_STATEMENT,
      GLOBAL_STATEMENT,
      STATIC_STATEMENT,
      ECHO_STATEMENT,
      TRY_STATEMENT,
      DECLARE_STATEMENT,
      GOTO_STATEMENT,
      INLINE_HTML,
      UNSET_VARIABLE_STATEMENT,
      EXPRESSION_STATEMENT,
      LABEL
    ));

    b.rule(EOS).is(b.firstOf(SEMICOLON, INLINE_HTML)).skip();

    b.rule(EMPTY_STATEMENT).is(SEMICOLON);

    b.rule(LABEL).is(IDENTIFIER, COLON);
    b.rule(BLOCK).is(LCURLYBRACE, b.optional(INNER_STATEMENT_LIST), RCURLYBRACE);

    b.rule(IF_STATEMENT).is(IF, PARENTHESIS_EXPRESSION, STATEMENT, b.optional(ELSEIF_LIST), b.optional(ELSE_CLAUSE));
    b.rule(ELSEIF_LIST).is(b.oneOrMore(ELSEIF_CLAUSE));
    b.rule(ELSEIF_CLAUSE).is(ELSEIF, PARENTHESIS_EXPRESSION, STATEMENT);
    b.rule(ELSE_CLAUSE).is(ELSE, STATEMENT);

    b.rule(ALTERNATIVE_IF_STATEMENT).is(IF, PARENTHESIS_EXPRESSION, COLON, b.optional(INNER_STATEMENT_LIST),
      b.optional(ALTERNATIVE_ELSEIF_LIST), b.optional(ALTERNATIVE_ELSE_CLAUSE), ENDIF, EOS);
    b.rule(ALTERNATIVE_ELSEIF_LIST).is(b.oneOrMore(ALTERNATIVE_ELSEIF_CLAUSE));
    b.rule(ALTERNATIVE_ELSEIF_CLAUSE).is(ELSEIF, PARENTHESIS_EXPRESSION, COLON, b.optional(INNER_STATEMENT_LIST));
    b.rule(ALTERNATIVE_ELSE_CLAUSE).is(ELSE, COLON, b.optional(INNER_STATEMENT_LIST));

    b.rule(WHILE_STATEMENT).is(WHILE, PARENTHESIS_EXPRESSION, b.firstOf(ALTERNATIVE_WHILE_STATEMENT, STATEMENT));
    b.rule(ALTERNATIVE_WHILE_STATEMENT).is(COLON, b.optional(INNER_STATEMENT_LIST), ENDWHILE, EOS);

    b.rule(DO_WHILE_STATEMENT).is(DO, STATEMENT, WHILE, PARENTHESIS_EXPRESSION, EOS);

    b.rule(FOR_STATEMENT).is(FOR, LPARENTHESIS, b.optional(FOR_EXRR), SEMICOLON, b.optional(FOR_EXRR), SEMICOLON, b.optional(FOR_EXRR),
      RPARENTHESIS, b.firstOf(ALTERNATIVE_FOR_STATEMENT, STATEMENT));
    b.rule(FOR_EXRR).is(EXPRESSION, b.zeroOrMore(COMMA, EXPRESSION));
    b.rule(ALTERNATIVE_FOR_STATEMENT).is(COLON, b.optional(INNER_STATEMENT_LIST), ENDFOR, EOS);

    b.rule(SWITCH_STATEMENT).is(SWITCH, PARENTHESIS_EXPRESSION, SWITCH_CASE_LIST);
    b.rule(SWITCH_CASE_LIST).is(b.firstOf(
      b.sequence(LCURLYBRACE, b.optional(SEMICOLON), b.optional(CASE_LIST), RCURLYBRACE),
      b.sequence(COLON, b.optional(SEMICOLON), b.optional(CASE_LIST), ENDSWITCH, EOS)));

    b.rule(CASE_LIST).is(b.oneOrMore(b.firstOf(CASE_CLAUSE, DEFAULT_CLAUSE)));
    b.rule(CASE_CLAUSE).is(CASE, EXPRESSION, CASE_SEPARTOR, b.optional(INNER_STATEMENT_LIST));
    b.rule(DEFAULT_CLAUSE).is(DEFAULT, CASE_SEPARTOR, b.optional(INNER_STATEMENT_LIST));
    b.rule(CASE_SEPARTOR).is(b.firstOf(COLON, SEMICOLON));

    b.rule(BREAK_STATEMENT).is(BREAK, b.optional(EXPRESSION), EOS);
    b.rule(CONTINUE_STATEMENT).is(CONTINUE, b.optional(EXPRESSION), EOS);

    b.rule(RETURN_STATEMENT).is(RETURN, b.optional(EXPRESSION), EOS);
    b.rule(EXPRESSION_STATEMENT).is(EXPRESSION, EOS);

    b.rule(FOREACH_STATEMENT).is(FOREACH, LPARENTHESIS, FOREACH_EXPR, RPARENTHESIS, b.firstOf(ALTERNATIVE_FOREACH_STATEMENT, STATEMENT));
    b.rule(FOREACH_EXPR).is(EXPRESSION, AS, FOREACH_VARIABLE, b.optional(DOUBLEARROW, FOREACH_VARIABLE));
    b.rule(FOREACH_VARIABLE).is(b.firstOf(
      b.sequence(b.optional(AMPERSAND), MEMBER_EXPRESSION),
      b.sequence(LIST, LPARENTHESIS, ASSIGNMENT_LIST, RPARENTHESIS)));
    b.rule(ALTERNATIVE_FOREACH_STATEMENT).is(COLON, b.optional(INNER_STATEMENT_LIST), ENDFOREACH, EOS);

    b.rule(DECLARE_STATEMENT).is(DECLARE, LPARENTHESIS, DECLARE_LIST, RPARENTHESIS, b.firstOf(
      ALTERNATIVE_DECLARE_STATEMENT,
      EOS,
      STATEMENT));
    b.rule(DECLARE_LIST).is(MEMBER_CONST_DECLARATION, b.zeroOrMore(COMMA, MEMBER_CONST_DECLARATION));
    b.rule(ALTERNATIVE_DECLARE_STATEMENT).is(COLON, b.optional(INNER_STATEMENT_LIST), ENDDECLARE, EOS);

    b.rule(TRY_STATEMENT).is(TRY, BLOCK, b.zeroOrMore(CATCH_STATEMENT), b.optional(FINALLY_STATEMENT));
    b.rule(CATCH_STATEMENT).is(CATCH, LPARENTHESIS, FULLY_QUALIFIED_CLASS_NAME, REGULAR_VAR_IDENTIFIER, RPARENTHESIS, BLOCK);
    b.rule(FINALLY_STATEMENT).is(FINALLY, BLOCK);

    b.rule(THROW_STATEMENT).is(THROW, EXPRESSION, EOS);
    b.rule(GOTO_STATEMENT).is(GOTO, IDENTIFIER, EOS);

    b.rule(YIELD_STATEMENT).is(YIELD_EXPRESSION, EOS);

    b.rule(GLOBAL_STATEMENT).is(GLOBAL, GLOBAL_VAR_LIST, EOS);
    b.rule(GLOBAL_VAR_LIST).is(GLOBAL_VAR, b.zeroOrMore(COMMA, GLOBAL_VAR));
    b.rule(GLOBAL_VAR).is(b.optional(SIMPLE_INDIRECT_REFERENCE), COMPOUND_VARIABLE);

    b.rule(STATIC_STATEMENT).is(STATIC, STATIC_VAR_LIST, EOS);
    b.rule(STATIC_VAR_LIST).is(STATIC_VAR, b.zeroOrMore(COMMA, STATIC_VAR));
    b.rule(STATIC_VAR).is(REGULAR_VAR_IDENTIFIER, b.optional(EQU, STATIC_SCALAR));

    b.rule(ECHO_STATEMENT).is(ECHO, EXPRESSION, b.zeroOrMore(COMMA, EXPRESSION), EOS);

    b.rule(UNSET_VARIABLE_STATEMENT).is(UNSET, LPARENTHESIS, UNSET_VARIABLES, RPARENTHESIS, EOS);
    b.rule(UNSET_VARIABLES).is(MEMBER_EXPRESSION, b.zeroOrMore(COMMA, MEMBER_EXPRESSION));

    b.rule(CLASS_STATEMENT).is(b.firstOf(
      METHOD_DECLARATION,
      CLASS_VARIABLE_DECLARATION,
      CLASS_CONSTANT_DECLARATION,
      TRAIT_USE_STATEMENT));

    b.rule(TRAIT_USE_STATEMENT).is(USE, INTERFACE_LIST, TRAIT_ADAPTATIONS);
    b.rule(TRAIT_ADAPTATIONS).is(b.firstOf(
      b.sequence(LCURLYBRACE, b.zeroOrMore(TRAIT_ADAPTATION_STATEMENT), RCURLYBRACE),
      EOS));
    b.rule(TRAIT_ADAPTATION_STATEMENT).is(b.firstOf(TRAIT_PRECEDENCE, TRAIT_ALIAS), EOS);
    b.rule(TRAIT_PRECEDENCE).is(TRAIT_METHOD_REFERENCE_FULLY_QUALIFIED, INSTEADOF, INTERFACE_LIST);
    b.rule(TRAIT_ALIAS).is(TRAIT_METHOD_REFERENCE, AS, b.firstOf(
      b.sequence(b.optional(MEMBER_MODIFIER), IDENTIFIER),
      MEMBER_MODIFIER));
    b.rule(TRAIT_METHOD_REFERENCE).is(b.firstOf(TRAIT_METHOD_REFERENCE_FULLY_QUALIFIED, IDENTIFIER));
    b.rule(TRAIT_METHOD_REFERENCE_FULLY_QUALIFIED).is(FULLY_QUALIFIED_CLASS_NAME, DOUBLECOLON, IDENTIFIER);

    b.rule(INNER_STATEMENT_LIST).is(b.oneOrMore(b.firstOf(
      FUNCTION_DECLARATION,
      CLASS_DECLARATION,
      INTERFACE_DECLARATION,
      HALT_COMPILER_STATEMENT,
      STATEMENT)));
  }

  private static void keywords(LexerlessGrammarBuilder b) {
    Object[] rest = new Object[PHPKeyword.values().length - 2];

    for (int i = 0; i < PHPKeyword.values().length; i++) {
      PHPKeyword tokenType = PHPKeyword.values()[i];

      // PHP keywords are case insensitive
      b.rule(tokenType).is(SPACING, b.token(tokenType, b.regexp("(?i)" + tokenType.getValue())), b.nextNot(b.regexp(LexicalConstant.IDENTIFIER_PART))).skip();
      if (i > 1) {
        rest[i - 2] = b.regexp("(?i)" + tokenType.getValue());
      }
    }

    b.rule(KEYWORDS).is(SPACING,
      b.firstOf(
        PHPKeyword.getKeywordValues()[0],
        PHPKeyword.getKeywordValues()[1],
        rest),
      b.nextNot(b.regexp(LexicalConstant.IDENTIFIER_PART))
    );
  }

  private static void punctuators(LexerlessGrammarBuilder b) {
    for (PHPPunctuator p : PHPPunctuator.values()) {
      b.rule(p).is(SPACING, b.token(p, p.getValue())).skip();
    }
  }

  private static Object word(LexerlessGrammarBuilder b, String word) {
    return b.sequence(SPACING, b.regexp("(?i)" + word), b.nextNot(b.regexp(LexicalConstant.IDENTIFIER_PART)));
  }
}
