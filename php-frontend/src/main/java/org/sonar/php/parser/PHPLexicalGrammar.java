/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
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
  ENUM_DECLARATION,

  CLASS_MEMBER,
  ENUM_MEMBER,

  METHOD_DECLARATION,
  CLASS_VARIABLE_DECLARATION,
  CLASS_CONSTANT_DECLARATION,
  TRAIT_USE_STATEMENT,

  NAMESPACE_NAME,
  NAMESPACE_NAME_WITHOUT_SINGLE_KEYWORD,
  NS_SEPARATOR_WITHOUT_SPACE,
  INTERFACE_LIST,
  PARAMETER_LIST,
  PARAMETER,

  VARIABLE_DECLARATION,
  MEMBER_MODIFIER,
  CLASS_CONST_MODIFIER,
  VISIBILITY_MODIFIER,
  MEMBER_CONST_DECLARATION,
  FUNCTION_CALL_ARGUMENT,

  PROPERTY_HOOK_LIST,
  PROPERTY_HOOK,
  PROPERTY_HOOK_FUNCTION_NAME,

  TRAIT_METHOD_REFERENCE_FULLY_QUALIFIED,
  TRAIT_METHOD_REFERENCE,
  TRAIT_ALIAS,
  TRAIT_PRECEDENCE,

  TYPE,
  TYPE_NAME,
  RETURN_TYPE_CLAUSE,
  UNION_TYPE,
  INTERSECTION_TYPE,
  DECLARED_TYPE,
  DNF_TYPE,
  DNF_INTERSECTION_TYPE,

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
  STRING_CHARACTERS_EXECUTION,
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

  MATCH_EXPRESSION,
  MATCH_CLAUSE,

  SWITCH_STATEMENT,
  SWITCH_CASE_CLAUSE,

  LABEL,
  BREAK_STATEMENT,
  CONTINUE_STATEMENT,
  RETURN_STATEMENT,
  THROW_STATEMENT,
  GOTO_STATEMENT,

  TRY_STATEMENT,
  CATCH_BLOCK,

  GLOBAL_STATEMENT,
  GLOBAL_VAR,
  STATIC_STATEMENT,
  STATIC_VAR,
  UNSET_VARIABLE_STATEMENT,
  UNSET_VARIABLES,

  ENUM_CASE,

  /**
   * Expression
   */
  PRIMARY_EXPRESSION,
  MEMBER_EXPRESSION,
  NEW_OBJECT_EXPRESSION,
  NEW_OBJECT_CLASS_FIELD_ACCESS,
  SPECIAL_CALL,
  VARIABLE_WITHOUT_OBJECTS,
  CLASS_MEMBER_ACCESS,
  OBJECT_MEMBER_ACCESS,
  FUNCTION_CALL_PARAMETER_LIST,
  DIMENSIONAL_OFFSET,
  STATIC_SCALAR,
  ARRAY_INIALIZER,
  COMMON_SCALAR,
  YIELD_SCALAR,
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
  CALLABLE_CONVERT,

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
  BACKTICK,
  REGULAR_STRING_LITERAL,
  VARIABLE_VARIABLE_DOLLAR,
  ENUM,

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

  GET,
  SET,

  SELF,
  PARENT,

  MIXED,
  INTEGER,
  INT,
  DOUBLE,
  FLOAT,
  REAL,
  STRING,
  OBJECT,
  BOOLEAN,
  BOOL,
  BINARY,
  ITERABLE,

  FROM,

  ATTRIBUTE,
  ATTRIBUTE_GROUP;

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
        b.skippedTrivia(b.regexp("[" + LexicalConstant.LINE_TERMINATOR + LexicalConstant.WHITESPACE + "]*+"))))
      .skip();

    // Literals
    b.rule(NOWDOC).is(SPACING, b.regexp(LexicalConstant.NOWDOC));
    b.rule(HEREDOC).is(SPACING, b.regexp(LexicalConstant.HEREDOC));
    b.rule(NUMERIC_LITERAL).is(SPACING, b.regexp(LexicalConstant.NUMERIC_LITERAL));
    b.rule(REGULAR_STRING_LITERAL).is(SPACING, b.regexp(LexicalConstant.STRING_LITERAL));

    b.rule(STRING_WITH_ENCAPS_VAR_CHARACTERS).is(b.regexp(LexicalConstant.STRING_WITH_ENCAPS_VAR_CHARACTERS));
    b.rule(STRING_CHARACTERS_EXECUTION).is(b.regexp(LexicalConstant.STRING_CHARACTERS_EXECUTION));
    b.rule(HEREDOC_STRING_CHARACTERS).is(b.regexp(LexicalConstant.HEREDOC_STRING_CHARACTERS));
    b.rule(DOUBLE_QUOTE).is("\"");
    b.rule(BACKTICK).is("`");
    // FIXME: this recovery is introduce in order to parse ${var}, as expression cannot match keywords.
    b.rule(SEMI_COMPLEX_RECOVERY_EXPRESSION).is(b.regexp("[^}]++"));

    // Identifier
    b.rule(WHITESPACES).is(b.regexp("[" + LexicalConstant.WHITESPACE + "]*+"));
    b.rule(REGULAR_VAR_IDENTIFIER).is(SPACING, VARIABLE_IDENTIFIER).skip();
    b.rule(VARIABLE_IDENTIFIER).is(b.regexp(LexicalConstant.VAR_IDENTIFIER));
    b.rule(IDENTIFIER).is(SPACING, b.firstOf(b.sequence(b.nextNot(KEYWORDS), b.regexp(LexicalConstant.IDENTIFIER)), b.regexp("(?i)match")));
    b.rule(IDENTIFIER_OR_KEYWORD).is(SPACING, b.regexp(LexicalConstant.IDENTIFIER));
    b.rule(NS_SEPARATOR_WITHOUT_SPACE).is(b.regexp("\\\\"));

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
    b.rule(ENUM).is(word(b, "enum")).skip();

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

    b.rule(GET).is(word(b, "GET")).skip();
    b.rule(SET).is(word(b, "SET")).skip();

    b.rule(SELF).is(word(b, "SELF")).skip();
    b.rule(PARENT).is(word(b, "PARENT")).skip();

    b.rule(MIXED).is(word(b, "MIXED")).skip();
    b.rule(INTEGER).is(word(b, "INTEGER")).skip();
    b.rule(INT).is(word(b, "INT")).skip();
    b.rule(DOUBLE).is(word(b, "DOUBLE")).skip();
    b.rule(FLOAT).is(word(b, "FLOAT")).skip();
    b.rule(REAL).is(word(b, "REAL")).skip();
    b.rule(STRING).is(word(b, "STRING")).skip();
    b.rule(OBJECT).is(word(b, "OBJECT")).skip();
    b.rule(BOOLEAN).is(word(b, "BOOLEAN")).skip();
    b.rule(BOOL).is(word(b, "BOOL")).skip();
    b.rule(BINARY).is(word(b, "BINARY")).skip();
    b.rule(ITERABLE).is(word(b, "ITERABLE")).skip();

    b.rule(FROM).is(word(b, "FROM")).skip();

  }

  private static void keywords(LexerlessGrammarBuilder b) {
    Object[] rest = new Object[PHPKeyword.values().length - 2];

    for (int i = 0; i < PHPKeyword.values().length; i++) {
      PHPKeyword tokenType = PHPKeyword.values()[i];

      // PHP keywords are case insensitive
      b.rule(tokenType).is(SPACING, keywordRegexp(b, tokenType.getValue()), b.nextNot(b.regexp(LexicalConstant.IDENTIFIER_PART))).skip();
      if (i > 1) {
        if (tokenType == PHPKeyword.READONLY) {
          // Readonly is only a keyword when it is not used as a function name. SONARPHP-1266
          rest[i - 2] = b.sequence(keywordRegexp(b, "readonly"), b.nextNot(b.regexp("[\\s]*\\(")));
        } else {
          rest[i - 2] = keywordRegexp(b, tokenType.getValue());
        }
      }
    }

    b.rule(KEYWORDS).is(SPACING,
      b.firstOf(
        keywordRegexp(b, PHPKeyword.getKeywordValues()[0]),
        keywordRegexp(b, PHPKeyword.getKeywordValues()[1]),
        rest),
      b.nextNot(b.regexp(LexicalConstant.IDENTIFIER_PART)));
  }

  private static void punctuators(LexerlessGrammarBuilder b) {
    for (PHPPunctuator p : PHPPunctuator.values()) {
      b.rule(p).is(SPACING, p.getValue()).skip();
    }
  }

  private static Object word(LexerlessGrammarBuilder b, String word) {
    return b.sequence(SPACING, b.regexp("(?i)" + word), b.nextNot(b.regexp(LexicalConstant.IDENTIFIER_PART)));
  }

  private static Object keywordRegexp(LexerlessGrammarBuilder b, String keywordValue) {
    return b.regexp("(?i)" + keywordValue);
  }
}
