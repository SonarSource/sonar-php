/*
 * Sonar PHP Plugin
 * Copyright (C) 2010 Codehaus Sonar Plugins
 * dev@sonar.codehaus.org
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

import org.sonar.sslr.grammar.GrammarRuleKey;
import org.sonar.sslr.grammar.LexerfulGrammarBuilder;

import static com.sonar.sslr.api.GenericTokenType.EOF;
import static com.sonar.sslr.api.GenericTokenType.IDENTIFIER;
import static org.sonar.php.lexer.PHPKeyword.ABSTRACT;
import static org.sonar.php.lexer.PHPKeyword.AS;
import static org.sonar.php.lexer.PHPKeyword.BREAK;
import static org.sonar.php.lexer.PHPKeyword.CATCH;
import static org.sonar.php.lexer.PHPKeyword.CLASS;
import static org.sonar.php.lexer.PHPKeyword.CONTINUE;
import static org.sonar.php.lexer.PHPKeyword.DECLARE;
import static org.sonar.php.lexer.PHPKeyword.DO;
import static org.sonar.php.lexer.PHPKeyword.ELSE;
import static org.sonar.php.lexer.PHPKeyword.ELSEIF;
import static org.sonar.php.lexer.PHPKeyword.ENDDECLARE;
import static org.sonar.php.lexer.PHPKeyword.ENDFOR;
import static org.sonar.php.lexer.PHPKeyword.ENDFOREACH;
import static org.sonar.php.lexer.PHPKeyword.ENDIF;
import static org.sonar.php.lexer.PHPKeyword.ENDSWITCH;
import static org.sonar.php.lexer.PHPKeyword.ENDWHILE;
import static org.sonar.php.lexer.PHPKeyword.EXTENDS;
import static org.sonar.php.lexer.PHPKeyword.FINAL;
import static org.sonar.php.lexer.PHPKeyword.FINALLY;
import static org.sonar.php.lexer.PHPKeyword.FOR;
import static org.sonar.php.lexer.PHPKeyword.FOREACH;
import static org.sonar.php.lexer.PHPKeyword.FUNCTION;
import static org.sonar.php.lexer.PHPKeyword.GOTO;
import static org.sonar.php.lexer.PHPKeyword.HALT_COMPILER;
import static org.sonar.php.lexer.PHPKeyword.IF;
import static org.sonar.php.lexer.PHPKeyword.IMPLEMENTS;
import static org.sonar.php.lexer.PHPKeyword.INTERFACE;
import static org.sonar.php.lexer.PHPKeyword.NAMESPACE;
import static org.sonar.php.lexer.PHPKeyword.RETURN;
import static org.sonar.php.lexer.PHPKeyword.SWITCH;
import static org.sonar.php.lexer.PHPKeyword.THROW;
import static org.sonar.php.lexer.PHPKeyword.TRAIT;
import static org.sonar.php.lexer.PHPKeyword.TRY;
import static org.sonar.php.lexer.PHPKeyword.USE;
import static org.sonar.php.lexer.PHPKeyword.WHILE;
import static org.sonar.php.lexer.PHPPunctuator.AND;
import static org.sonar.php.lexer.PHPPunctuator.COLON;
import static org.sonar.php.lexer.PHPPunctuator.COMMA;
import static org.sonar.php.lexer.PHPPunctuator.LCURLYBRACE;
import static org.sonar.php.lexer.PHPPunctuator.LPARENTHESIS;
import static org.sonar.php.lexer.PHPPunctuator.NS_SEPARATOR;
import static org.sonar.php.lexer.PHPPunctuator.RCURLYBRACE;
import static org.sonar.php.lexer.PHPPunctuator.RPARENTHESIS;
import static org.sonar.php.lexer.PHPPunctuator.SEMICOLON;
import static org.sonar.php.lexer.PHPTokenType.CLOSE_TAG;
import static org.sonar.php.lexer.PHPTokenType.OPEN_TAG;
import static org.sonar.php.lexer.PHPTokenType.VAR_IDENTIFIER;

public enum PHPGrammar implements GrammarRuleKey {

  COMPILATION_UNIT,
  DIRECTIVES,
  DIRECTIVE,
  NAMESPACE_DIRECTIVE,
  USE_DIRECTIVE,
  USE_DECLARATIONS,
  USE_DECLARATION,
  HALT_COMPILER_STATMENT,

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


  STATEMENT,
  EMPTY_STATEMENT,
  LABEL,
  BLOCK,
  INNER_STATEMENT_LIST,
  IF_STATEMENT,
  ELSIF_LIST,
  ELSE_CLAUSE,
  ALTERNATIVE_IF_STATEMENT,
  ALTERNATIVE_ELSIF_LIST,
  ALTERNATIVE_ELSE_CLAUSE,
  WHILE_STATEMENT,
  INNER_WHILE_STATEMENT,
  DO_WHILE_STATEMENT,
  FOR_STATEMENT,
  INNER_FOR_STATEMENT,
  FOREACH_STATEMENT,
  INNER_FOREACH_STATEMENT,
  SWITCH_STATEMENT,
  SWITCH_CASE_LIST,
  CASE_LIST,
  BREAK_STATEMENT,
  CONTINUE_STATEMENT,
  RETURN_STATEMENT,
  DECLARE_STATEMENT,
  INNER_DECLARE_STATEMENT,
  TRY_STATEMENT,
  CATCH_STATEMENT,
  FINALLY_STATEMENT,
  THROW_STATEMENT,
  GOTO_STATEMENT,
  EXPRESSION_STATEMENT,

  EXPRESSION,
  PARENTHESIS_EXPRESSION;

  public static LexerfulGrammarBuilder create() {
    LexerfulGrammarBuilder b = LexerfulGrammarBuilder.create();

    b.rule(COMPILATION_UNIT).is(b.firstOf(
      b.sequence(OPEN_TAG, DIRECTIVES, b.optional(CLOSE_TAG)),
      EOF));

    directives(b);
    statements(b);
    expression(b);

    b.setRootRule(COMPILATION_UNIT);

    return b;
  }

  public static void expression(LexerfulGrammarBuilder b) {
    b.rule(EXPRESSION).is(b.nothing()); // TODO
    b.rule(PARENTHESIS_EXPRESSION).is(b.bridge(LPARENTHESIS, /*TODO: -> expr | yield_expr */ RPARENTHESIS));
  }

  public static void directives(LexerfulGrammarBuilder b) {
    // Zend: top_statement_list
    b.rule(DIRECTIVES).is(b.zeroOrMore(DIRECTIVE));

    // Zend: top_statement
    b.rule(DIRECTIVE).is(b.firstOf(
      STATEMENT,
      FUNCTION_DECLARATION,
      CLASS_DECLARATION,
      // TODO: CONSTANT_DECLARATION,
      NAMESPACE_DIRECTIVE,
      USE_DIRECTIVE,
      HALT_COMPILER_STATMENT
    ));

    b.rule(NAMESPACE_NAME).is(b.firstOf(
      FULLY_QUALIFIED_NAME,
      QUALIFIED_NAME,
      UNQUALIFIED_NAME));

    b.rule(UNQUALIFIED_NAME).is(IDENTIFIER);
    b.rule(QUALIFIED_NAME).is(IDENTIFIER, b.oneOrMore(NS_SEPARATOR, IDENTIFIER));
    b.rule(FULLY_QUALIFIED_NAME).is(NS_SEPARATOR, IDENTIFIER, b.zeroOrMore(NS_SEPARATOR, IDENTIFIER));

    b.rule(NAMESPACE_DIRECTIVE).is(b.firstOf(
      b.sequence(NAMESPACE, NAMESPACE_NAME, SEMICOLON),
      b.sequence(NAMESPACE, b.optional(NAMESPACE_NAME), b.bridge(LCURLYBRACE, /*TODO: -> DIRECTIVES*/ RCURLYBRACE))));

    b.rule(USE_DIRECTIVE).is(USE, USE_DECLARATIONS, SEMICOLON);
    b.rule(USE_DECLARATIONS).is(USE_DECLARATION, b.zeroOrMore(COMMA, USE_DECLARATION));
    b.rule(USE_DECLARATION).is(b.firstOf(
      b.sequence(NAMESPACE_NAME, AS, IDENTIFIER),
      NAMESPACE_NAME));

    b.rule(HALT_COMPILER_STATMENT).is(HALT_COMPILER, LPARENTHESIS, RPARENTHESIS, SEMICOLON);

    b.rule(REFERENCE).is(AND);
    b.rule(FUNCTION_DECLARATION).is(FUNCTION, b.optional(REFERENCE), IDENTIFIER,
      b.bridge(LPARENTHESIS, /*TODO: -> parameter_list*/ RPARENTHESIS), b.bridge(LCURLYBRACE, /*TODO: -> inner_stmt_list*/ RCURLYBRACE));

    // Class declaration
    b.rule(CLASS_DECLARATION).is(CLASS_ENTRY_TYPE, IDENTIFIER, b.optional(EXTENDS_FROM), b.optional(IMPLEMENTS_LIST),
      b.bridge(LCURLYBRACE, /*TODO: -> class_statement_list*/ RCURLYBRACE));
    b.rule(CLASS_ENTRY_TYPE).is(b.firstOf(
      b.sequence(b.optional(CLASS_TYPE), CLASS),
      TRAIT));
    b.rule(CLASS_TYPE).is(b.firstOf(ABSTRACT, FINAL));

    b.rule(EXTENDS_FROM).is(EXTENDS, FULLY_QUALIFIED_CLASS_NAME);
    b.rule(IMPLEMENTS_LIST).is(IMPLEMENTS, INTERFACE_LIST);
    b.rule(INTERFACE_LIST).is(FULLY_QUALIFIED_CLASS_NAME, b.zeroOrMore(COMMA, FULLY_QUALIFIED_CLASS_NAME));
    b.rule(FULLY_QUALIFIED_CLASS_NAME).is(b.firstOf(
      b.sequence(NAMESPACE, NS_SEPARATOR, NAMESPACE_NAME),
      NAMESPACE_NAME));

    b.rule(INTERFACE_DECLARATION).is(INTERFACE, IDENTIFIER, b.optional(INTERFACE_EXTENDS_LIST),
      b.bridge(LCURLYBRACE, /*TODO: -> class_statement_list*/ RCURLYBRACE));
    b.rule(INTERFACE_EXTENDS_LIST).is(EXTENDS, INTERFACE_LIST);
  }


  public static void statements(LexerfulGrammarBuilder b) {
    b.rule(STATEMENT).is(b.firstOf(
      BLOCK,
      LABEL,
      ALTERNATIVE_IF_STATEMENT,
      IF_STATEMENT,
      WHILE_STATEMENT,
      DO_WHILE_STATEMENT,
      FOR_STATEMENT,
      SWITCH_STATEMENT,
      BREAK_STATEMENT,
      CONTINUE_STATEMENT,
      RETURN_STATEMENT,
      EXPRESSION_STATEMENT,
      EMPTY_STATEMENT
      // TODO: yield
      // TODO: global
      // TODO: static
      // TODO: echo
      // TODO: INLINE_HTML ?
      // TODO: unset
    ));

    b.rule(EMPTY_STATEMENT).is(SEMICOLON);

    b.rule(LABEL).is(IDENTIFIER, COLON);
    b.rule(BLOCK).is(LCURLYBRACE, b.optional(INNER_STATEMENT_LIST), RCURLYBRACE);

    b.rule(IF_STATEMENT).is(IF, PARENTHESIS_EXPRESSION, STATEMENT, b.optional(ELSIF_LIST), b.optional(ELSE_CLAUSE));
    b.rule(ELSIF_LIST).is(b.oneOrMore(ELSEIF, PARENTHESIS_EXPRESSION, STATEMENT));
    b.rule(ELSE_CLAUSE).is(ELSE, STATEMENT);

    b.rule(ALTERNATIVE_IF_STATEMENT).is(IF, PARENTHESIS_EXPRESSION, COLON, b.optional(INNER_STATEMENT_LIST),
      b.optional(ALTERNATIVE_ELSIF_LIST), b.optional(ALTERNATIVE_ELSE_CLAUSE), ENDIF, SEMICOLON);
    b.rule(ALTERNATIVE_ELSIF_LIST).is(b.oneOrMore(ELSEIF, PARENTHESIS_EXPRESSION, COLON, b.optional(INNER_STATEMENT_LIST)));
    b.rule(ALTERNATIVE_ELSE_CLAUSE).is(ELSE, COLON, b.optional(INNER_STATEMENT_LIST));

    b.rule(WHILE_STATEMENT).is(WHILE, PARENTHESIS_EXPRESSION, INNER_WHILE_STATEMENT);
    b.rule(INNER_WHILE_STATEMENT).is(b.firstOf(
      b.sequence(COLON, b.optional(INNER_STATEMENT_LIST), ENDWHILE, SEMICOLON),
      STATEMENT));

    b.rule(DO_WHILE_STATEMENT).is(DO, STATEMENT, WHILE, PARENTHESIS_EXPRESSION, SEMICOLON);

    b.rule(FOR_STATEMENT).is(FOR, b.bridge(LPARENTHESIS, /*TODO: for_expr;..;..*/ RPARENTHESIS), b.optional(INNER_FOR_STATEMENT));
    b.rule(INNER_FOR_STATEMENT).is(b.firstOf(
      b.sequence(COLON, INNER_STATEMENT_LIST, ENDFOR, SEMICOLON),
      STATEMENT));

    b.rule(SWITCH_STATEMENT).is(SWITCH, PARENTHESIS_EXPRESSION, SWITCH_CASE_LIST);
    b.rule(SWITCH_CASE_LIST).is(b.firstOf(
      b.bridge(LCURLYBRACE, /*TODO: -> semicolon | case_list*/ RCURLYBRACE),
      b.sequence(COLON, b.optional(SEMICOLON), /*TODO: b.optional(CASE_LIST)*/ ENDSWITCH, SEMICOLON)));

    b.rule(BREAK_STATEMENT).is(BREAK, b.optional(EXPRESSION), SEMICOLON);
    b.rule(CONTINUE_STATEMENT).is(CONTINUE, b.optional(EXPRESSION), SEMICOLON);

    b.rule(RETURN_STATEMENT).is(RETURN, /*TODO: expr_without_variable | variable*/ SEMICOLON);
    b.rule(EXPRESSION_STATEMENT).is(EXPRESSION, SEMICOLON);

    b.rule(FOREACH_STATEMENT).is(FOREACH, b.bridge(LPARENTHESIS, /*TODO: -> 2 version of foreach*/ RPARENTHESIS), b.optional(INNER_FOREACH_STATEMENT));
    b.rule(INNER_FOREACH_STATEMENT).is(b.firstOf(
      b.sequence(COLON, INNER_STATEMENT_LIST, ENDFOREACH, SEMICOLON),
      STATEMENT));

    b.rule(DECLARE_STATEMENT).is(DECLARE, b.bridge(LPARENTHESIS, /*TODO: -> declare_list*/ RPARENTHESIS), INNER_DECLARE_STATEMENT);
    b.rule(INNER_DECLARE_STATEMENT).is(b.firstOf(
      b.sequence(COLON, b.optional(INNER_STATEMENT_LIST), ENDDECLARE, SEMICOLON),
      STATEMENT));

    b.rule(TRY_STATEMENT).is(TRY, LCURLYBRACE, b.optional(INNER_STATEMENT_LIST), RCURLYBRACE, b.zeroOrMore(CATCH_STATEMENT), b.optional(FINALLY_STATEMENT));
    b.rule(CATCH_STATEMENT).is(CATCH, LPARENTHESIS, FULLY_QUALIFIED_CLASS_NAME, VAR_IDENTIFIER, RPARENTHESIS, LCURLYBRACE, b.optional(INNER_STATEMENT_LIST), RCURLYBRACE);
    b.rule(FINALLY_STATEMENT).is(FINALLY, LCURLYBRACE, b.optional(INNER_STATEMENT_LIST), RCURLYBRACE);

    b.rule(THROW_STATEMENT).is(THROW, EXPRESSION, SEMICOLON); // TODO: TEST when expr complete
    b.rule(GOTO_STATEMENT).is(GOTO, IDENTIFIER, SEMICOLON);

    b.rule(INNER_STATEMENT_LIST).is(b.oneOrMore(b.firstOf(
      FUNCTION_DECLARATION,
      CLASS_DECLARATION,
      HALT_COMPILER_STATMENT,
      STATEMENT)));
  }

}
