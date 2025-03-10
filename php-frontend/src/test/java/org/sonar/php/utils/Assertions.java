/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
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
package org.sonar.php.utils;

import com.sonar.sslr.api.RecognitionException;
import com.sonar.sslr.api.Rule;
import com.sonar.sslr.api.typed.ActionParser;
import java.nio.charset.StandardCharsets;
import org.fest.assertions.GenericAssert;
import org.sonar.api.utils.Preconditions;
import org.sonar.php.parser.PHPGrammar;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.php.parser.PHPNodeBuilder;
import org.sonar.php.parser.TreeFactory;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.sslr.grammar.GrammarRuleKey;
import org.sonar.sslr.grammar.LexerlessGrammarBuilder;
import org.sonar.sslr.tests.ParsingResultComparisonFailure;
import org.sonar.sslr.tests.RuleAssert;

public class Assertions {

  public static RuleAssert assertThat(Rule actual) {
    return new RuleAssert(actual);
  }

  public static ParserAssert assertThat(GrammarRuleKey rule) {
    return assertThat(PHPLexicalGrammar.createGrammarBuilder(), rule);
  }

  public static ParserAssert assertThat(LexerlessGrammarBuilder b, GrammarRuleKey rule) {
    return new ParserAssert(new ActionParser<>(
      StandardCharsets.UTF_8,
      b,
      PHPGrammar.class,
      new TreeFactory(),
      new PHPNodeBuilder(),
      rule));
  }

  public static class ParserAssert extends GenericAssert<ParserAssert, ActionParser<Tree>> {

    public ParserAssert(ActionParser<Tree> actual) {
      super(ParserAssert.class, actual);
    }

    private void parseTillEof(String input) {
      PHPTree tree = (PHPTree) actual.parse(input);
      InternalSyntaxToken lastToken = (InternalSyntaxToken) tree.getLastToken();
      if (lastToken.toIndex() != input.length()) {
        throw new RecognitionException(
          0, "Did not match till EOF, but till line " + lastToken.line() + ": token \"" + lastToken.text() + "\"");
      }
    }

    public ParserAssert matches(String input) {
      isNotNull();
      Preconditions.checkArgument(!hasTrailingWhitespaces(input), "Trailing whitespaces in input are not supported");
      String expected = "Rule '" + getRuleName() + "' should match:\n" + input;
      try {
        parseTillEof(input);
      } catch (RecognitionException e) {
        String actual = e.getMessage();
        throw new ParsingResultComparisonFailure(expected, actual);
      }
      return this;
    }

    private static boolean hasTrailingWhitespaces(String input) {
      return input.endsWith(" ") || input.endsWith("\n") || input.endsWith("\r") || input.endsWith("\t");
    }

    public ParserAssert notMatches(String input) {
      isNotNull();
      try {
        parseTillEof(input);
      } catch (RecognitionException e) {
        // expected
        return this;
      } catch (RuntimeException e) {
        Throwable rootCause = Throwables.getRootCause(e);
        if (rootCause instanceof RecognitionException) {
          return this;
        } else {
          throw e;
        }
      }
      throw new AssertionError("Rule '" + getRuleName() + "' should not match:\n" + input);
    }

    /**
     * Verifies that the actual <code>{@link com.sonar.sslr.api.Rule}</code> partially matches a given input.
     *
     * @param prefixToBeMatched the prefix that must be fully matched
     * @param remainingInput    the remainder of the input, which is not to be matched
     * @return this assertion object.
     */
    public ParserAssert matchesPrefix(String prefixToBeMatched, String remainingInput) {
      isNotNull();
      try {
        PHPTree tree = (PHPTree) actual.parse(prefixToBeMatched + remainingInput);
        SyntaxToken lastToken = tree.getLastToken();

        if (prefixToBeMatched.length() != lastToken.column() + lastToken.text().length()) {
          throw new RecognitionException(0,
            "Rule '" + getRuleName() + "' should match:\n" + prefixToBeMatched + "\nwhen followed by:\n" + remainingInput);
        }
      } catch (RecognitionException e) {
        throw new RecognitionException(0, e.getMessage() + "\n" +
          "Rule '" + getRuleName() + "' should match:\n" + prefixToBeMatched + "\nwhen followed by:\n" + remainingInput);
      }
      return this;
    }

    private String getRuleName() {
      return actual.rootRule().toString();
    }

  }

}
