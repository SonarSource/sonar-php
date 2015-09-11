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
package org.sonar.php;

import com.google.common.base.Charsets;
import com.sonar.sslr.api.RecognitionException;
import com.sonar.sslr.api.typed.ActionParser;
import com.sonar.sslr.impl.ast.AstXmlPrinter;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.php.parser.PHPParserBuilder;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.php.utils.SourceBuilder;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.sslr.internal.matchers.InputBuffer;
import org.sonar.sslr.parser.ParseError;
import org.sonar.sslr.parser.ParseErrorFormatter;

import java.util.Iterator;

import static org.fest.assertions.Assertions.assertThat;

public class PHPTreeModelTest {
  protected ActionParser<Tree> p;

  /**
   * Parse the given string and return the first descendant of the given kind.
   *
   * @param s the string to parse
   * @param rootRule the rule to start parsing from
   * @return the node found for the given kind, null if not found.
   */
  protected <T extends Tree> T parse(String s, PHPLexicalGrammar rootRule) throws Exception {
    p = PHPParserBuilder.createParser(rootRule, Charsets.UTF_8);
    Tree node = p.parse(s);
    checkFullFidelity(node, s);
    return (T) node;
  }

  /**
   * Return the concatenation of all the given node tokens value.
   */
  protected static String expressionToString(Tree node) {
    return SourceBuilder.build(node).trim();
  }

  private static void checkFullFidelity(Tree tree, String inputString) {
    String resultString = expressionToString(tree);
    if (!inputString.equals(resultString)) {
      if (inputString.startsWith(resultString)) {
        String message = "Only beginning of the input string is parsed: " + resultString;
        throw new RecognitionException(0, message);
      } else {
        String message = "Some tokens are lost. See result tree string: " + resultString;
        throw new RecognitionException(0, message);
      }
    }
  }
}
