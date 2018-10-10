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
package org.sonar.php;

import com.sonar.sslr.api.RecognitionException;
import com.sonar.sslr.api.typed.ActionParser;
import org.sonar.php.parser.PHPParserBuilder;
import org.sonar.php.utils.SourceBuilder;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.sslr.grammar.GrammarRuleKey;

public abstract class PHPTreeModelTest {
  protected ActionParser<Tree> p;

  /**
   * Parse the given string and return the first descendant of the given kind.
   *
   * @param s the string to parse
   * @param rootRule the rule to start parsing from
   * @return the node found for the given kind, null if not found.
   */
  protected <T extends Tree> T parse(String s, GrammarRuleKey rootRule) {
    p = PHPParserBuilder.createParser(rootRule);
    Tree node = p.parse(s);
    checkFullFidelity(node, s.trim());
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
