/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2018 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.php.parser;

import com.sonar.sslr.api.GenericTokenType;
import org.sonar.php.api.PHPKeyword;
import org.sonar.php.api.PHPPunctuator;
import org.sonar.sslr.grammar.GrammarRuleKey;
import org.sonar.sslr.grammar.LexerlessGrammarBuilder;
import org.sonar.sslr.parser.LexerlessGrammar;

public enum PHPLexicalGrammar implements GrammarRuleKey {

  COMPILATION_UNIT,
  SCRIPT,

  /**
   * Declaration
   */

  FUNCTION_DECLARATION,
  CLASS_DECLARATION,
  INTERFACE_DECLARATION,
  TRAIT_DECLARATION,

  CLASS_MEMBER,

  METHOD_DECLARATION,
  CLASS_VARIABLE_DECLARATION,
  CLASS_CONSTANT_DECLARATION,
  TRAIT_USE_STATEMENT,

  NAMESPACE_NAME,
  INTERFACE_LIST,
  PARAMETER_LIST,
  PARAMETER,

  VARIABLE_DECLARATION,
  MEMBER_MODIFIER,
  VISIBILITY_MODIFIER,
  MEMBER_CONST_DECLARATION,

  TRAIT_METHOD_REFERENCE_FULLY_QUALIFIED,
  TRAIT_METHOD_REFERENCE,
  TRAIT_ALIAS,
  TRAIT_PRECEDENCE,

  TYPE,
  TYPE_NAME,
  RETURN_TYPE_CLAUSE,

  /**
   * Lexical
   */
  EOF,
  NOWDOC,
  HEREDOC,
  HEREDOC_BODY,
  NUMERIC_LITERAL,
  STRING_LITERAL,
  STRING_WITH_ENCAPS_VAR_CHARACTERS,
  HEREDOC_STRING_CHARACTERS,
  ENCAPS_VAR_IDENTIFIER,
  REGULAR_VAR_IDENTIFIER,
  VARIABLE_IDENTIFIER,
  IDENTIFIER,
  IDENTIFIER_OR_KEYWORD,
  FILE_OPENING_TAG,
  ANYTHING_BUT_START_TAG,
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
  TOP_STATEMENT,
  STATEMENT,
  INNER_STATEMENT,

  BLOCK,

  EMPTY_STATEMENT,

  INLINE_HTML_STATEMENT,

  NAMESPACE_STATEMENT,
  DECLARE_STATEMENT,

  CONSTANT_DECLARATION,
  CONSTANT_VAR,

  USE_STATEMENT,
  USE_CLAUSE,
  GROUP_USE_CLAUSE,

  EXPRESSION_STATEMENT,
  EXPRESSION_LIST_STATEMENT,
  ECHO_STATEMENT,

  IF_STATEMENT,
  ELSEIF_CLAUSE,
  ELSE_CLAUSE,
  ALTERNATIVE_IF_STATEMENT,
  STANDARD_IF_STATEMENT,
  ALTERNATIVE_ELSEIF_CLAUSE,
  ALTERNATIVE_ELSE_CLAUSE,

  WHILE_STATEMENT,
  ALTERNATIVE_WHILE_STATEMENT,

  DO_WHILE_STATEMENT,

  FOR_STATEMENT,
  FOR_EXPR,

  FOREACH_STATEMENT,
  FOREACH_VARIABLE,

  SWITCH_STATEMENT,
  SWITCH_CASE_CLAUSE,

  LABEL,
  BREAK_STATEMENT,
  CONTINUE_STATEMENT,
  RETURN_STATEMENT,
  THROW_STATEMENT,
  GOTO_STATEMENT,
  YIELD_STATEMENT,

  TRY_STATEMENT,
  CATCH_BLOCK,

  GLOBAL_STATEMENT,
  GLOBAL_VAR,
  STATIC_STATEMENT,
  STATIC_VAR,
  UNSET_VARIABLE_STATEMENT,
  UNSET_VARIABLES,

  /**
   * Expression
   */
  PRIMARY_EXPRESSION,
  MEMBER_EXPRESSION,
  VARIABLE_WITHOUT_OBJECTS,
  CLASS_MEMBER_ACCESS,
  OBJECT_MEMBER_ACCESS,
  FUNCTION_CALL_PARAMETER_LIST,
  DIMENSIONAL_OFFSET,
  STATIC_SCALAR,
  OBJECT_DIM_LIST,
  ARRAY_INIALIZER,
  COMMON_SCALAR,
  BOOLEAN_LITERAL,
  LEXICAL_VARIABLE,
  ASSIGNMENT_EXPRESSION,
  MULTIPLICATIVE_EXPR,
  POWER_EXPR,
  ADDITIVE_EXPR,
  SHIFT_EXPR,
  RELATIONAL_EXPR,
  EQUALITY_EXPR,
  POSTFIX_EXPR,
  UNARY_EXPR,
  ASSIGNMENT_BY_REFERENCE,
  ASSIGNMENT_OPERATOR,
  CAST_TYPE,
  INTERNAL_FUNCTION,
  ARRAY_PAIR_LIST,
  LIST_EXPRESSION_ASSIGNMENT,
  ARRAY_DESTRUCTURING_ASSIGNMENT,
  ARRAY_ASSIGNMENT_PATTERN_ELEMENT,
  COMPLEX_ENCAPS_VARIABLE,
  SEMI_COMPLEX_ENCAPS_VARIABLE,
  SEMI_COMPLEX_RECOVERY_EXPRESSION,
  SIMPLE_ENCAPS_VARIABLE,
  ENCAPS_VAR,
  EXIT_EXPRESSION,

  NULL,
  CLASS_CONSTANT,
  FILE_CONSTANT,
  DIR_CONSTANT,
  FUNCTION_CONSTANT,
  LINE_CONSTANT,
  METHOD_CONSTANT,
  NAMESPACE_CONSTANT,
  TRAIT_CONSTANT,
  NEXT_IS_DOLLAR,
  DOUBLE_QUOTE,
  REGULAR_STRING_LITERAL,
  VARIABLE_VARIABLE_DOLLAR,

  ISSET,
  EMPTY,
  WHITESPACES,
  EXPRESSION,
  INCLUDE_ONCE,
  INCLUDE,
  EVAL,
  REQUIRE_ONCE,
  REQUIRE,
  CLONE,
  PRINT,

  SELF,

  INTEGER,
  INT,
  DOUBLE,
  FLOAT,
  STRING,
  OBJECT,
  BOOLEAN,
  BOOL,
  BINARY,

  FROM
  ;

  public static LexerlessGrammar createGrammar() {
    return createGrammarBuilder().build();
  }

  public static LexerlessGrammarBuilder createGrammarBuilder() {
    LexerlessGrammarBuilder b = LexerlessGrammarBuilder.create();

    lexical(b);
    punctuators(b);
    keywords(b);

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
    b.rule(NOWDOC).is(SPACING, b.regexp(LexicalConstant.NOWDOC));
    b.rule(HEREDOC).is(SPACING, b.regexp(LexicalConstant.HEREDOC));
    b.rule(NUMERIC_LITERAL).is(SPACING, b.regexp(LexicalConstant.NUMERIC_LITERAL));
    b.rule(REGULAR_STRING_LITERAL).is(SPACING, b.regexp(LexicalConstant.STRING_LITERAL));

    b.rule(STRING_WITH_ENCAPS_VAR_CHARACTERS).is(b.regexp(LexicalConstant.STRING_WITH_ENCAPS_VAR_CHARACTERS));
    b.rule(HEREDOC_STRING_CHARACTERS).is(b.regexp(LexicalConstant.HEREDOC_STRING_CHARACTERS));
    b.rule(DOUBLE_QUOTE).is("\"");
    // FIXME: this recovery is introduce in order to parse ${var}, as expression cannot match keywords.
    b.rule(SEMI_COMPLEX_RECOVERY_EXPRESSION).is(b.regexp("[^}]++"));

    // Identifier
    b.rule(WHITESPACES).is(b.regexp("[" + LexicalConstant.WHITESPACE + "]*+"));
    b.rule(REGULAR_VAR_IDENTIFIER).is(SPACING, VARIABLE_IDENTIFIER).skip();
    b.rule(VARIABLE_IDENTIFIER).is(b.regexp(LexicalConstant.VAR_IDENTIFIER));
    b.rule(IDENTIFIER).is(SPACING, b.nextNot(KEYWORDS), b.regexp(LexicalConstant.IDENTIFIER));
    b.rule(IDENTIFIER_OR_KEYWORD).is(SPACING, b.regexp(LexicalConstant.IDENTIFIER));

    // Tags & Inline HTML
    b.rule(FILE_OPENING_TAG).is(SPACING, b.regexp(LexicalConstant.PHP_START_TAG)).skip();
    b.rule(INLINE_HTML).is(SPACING, b.regexp(LexicalConstant.PHP_END_TAG)).skip();
    b.rule(ANYTHING_BUT_START_TAG).is(SPACING, b.regexp(LexicalConstant.ANYTHING_BUT_START_TAG)).skip();

    b.rule(EOF).is(b.token(GenericTokenType.EOF, b.endOfInput())).skip();

    b.rule(NULL).is(word(b, "NULL")).skip();
    b.rule(CLASS_CONSTANT).is(word(b, "__CLASS__")).skip();
    b.rule(FILE_CONSTANT).is(word(b, "__FILE__")).skip();
    b.rule(DIR_CONSTANT).is(word(b, "__DIR__")).skip();
    b.rule(FUNCTION_CONSTANT).is(word(b, "__FUNCTION__")).skip();
    b.rule(LINE_CONSTANT).is(word(b, "__LINE__")).skip();
    b.rule(METHOD_CONSTANT).is(word(b, "__METHOD__")).skip();
    b.rule(NAMESPACE_CONSTANT).is(word(b, "__NAMESPACE__")).skip();
    b.rule(TRAIT_CONSTANT).is(word(b, "__TRAIT__")).skip();

    b.rule(BOOLEAN_LITERAL).is(b.firstOf(word(b, "TRUE"), word(b, "FALSE")));

    b.rule(NEXT_IS_DOLLAR).is(b.next(PHPPunctuator.DOLLAR));
    b.rule(VARIABLE_VARIABLE_DOLLAR).is(PHPPunctuator.DOLLAR, b.nextNot(b.firstOf(IDENTIFIER, KEYWORDS, PHPPunctuator.LCURLYBRACE)));

    b.rule(ISSET).is(word(b, "ISSET")).skip();
    b.rule(EMPTY).is(word(b, "EMPTY")).skip();
    b.rule(INCLUDE_ONCE).is(word(b, "INCLUDE_ONCE")).skip();
    b.rule(INCLUDE).is(word(b, "INCLUDE")).skip();
    b.rule(EVAL).is(word(b, "EVAL")).skip();
    b.rule(REQUIRE_ONCE).is(word(b, "REQUIRE_ONCE")).skip();
    b.rule(REQUIRE).is(word(b, "REQUIRE")).skip();
    b.rule(CLONE).is(word(b, "CLONE")).skip();
    b.rule(PRINT).is(word(b, "PRINT")).skip();

    b.rule(SELF).is(word(b, "SELF")).skip();

    b.rule(INTEGER).is(word(b, "INTEGER")).skip();
    b.rule(INT).is(word(b, "INT")).skip();
    b.rule(DOUBLE).is(word(b, "DOUBLE")).skip();
    b.rule(FLOAT).is(word(b, "FLOAT")).skip();
    b.rule(STRING).is(word(b, "STRING")).skip();
    b.rule(OBJECT).is(word(b, "OBJECT")).skip();
    b.rule(BOOLEAN).is(word(b, "BOOLEAN")).skip();
    b.rule(BOOL).is(word(b, "BOOL")).skip();
    b.rule(BINARY).is(word(b, "BINARY")).skip();

    b.rule(FROM).is(word(b, "FROM")).skip();

  }

  private static void keywords(LexerlessGrammarBuilder b) {
    Object[] rest = new Object[PHPKeyword.values().length - 2];

    for (int i = 0; i < PHPKeyword.values().length; i++) {
      PHPKeyword tokenType = PHPKeyword.values()[i];

      // PHP keywords are case insensitive
      b.rule(tokenType).is(SPACING, b.regexp("(?i)" + tokenType.getValue()), b.nextNot(b.regexp(LexicalConstant.IDENTIFIER_PART))).skip();
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
      b.rule(p).is(SPACING, p.getValue()).skip();
    }
  }

  private static Object word(LexerlessGrammarBuilder b, String word) {
    return b.sequence(SPACING, b.regexp("(?i)" + word), b.nextNot(b.regexp(LexicalConstant.IDENTIFIER_PART)));
  }

}
