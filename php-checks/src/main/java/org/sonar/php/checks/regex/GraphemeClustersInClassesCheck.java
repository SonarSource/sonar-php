/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2021 SonarSource SA
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
package org.sonar.php.checks.regex;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.php.regex.RegexCheck;
import org.sonar.php.regex.ast.PhpRegexBaseVisitor;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonarsource.analyzer.commons.regex.RegexParseResult;
import org.sonarsource.analyzer.commons.regex.ast.CharacterClassTree;
import org.sonarsource.analyzer.commons.regex.ast.CharacterClassUnionTree;
import org.sonarsource.analyzer.commons.regex.ast.CharacterTree;
import org.sonarsource.analyzer.commons.regex.ast.RegexSyntaxElement;

@Rule(key = "S5868")
public class GraphemeClustersInClassesCheck extends AbstractRegexCheck {

  private static final String MESSAGE = "Extract %d Grapheme Cluster(s) from this character class.";

  @Override
  public void checkRegex(RegexParseResult regexParseResult, FunctionCallTree regexFunctionCall) {
    new GraphemeInClassVisitor().visit(regexParseResult);
  }

  private class GraphemeInClassVisitor extends PhpRegexBaseVisitor {

    private final List<RegexIssueLocation> graphemeClusters = new ArrayList<>();

    @Override
    public void visitCharacterClass(CharacterClassTree tree) {
      super.visitCharacterClass(tree);
      if (!graphemeClusters.isEmpty()) {
        newIssue(tree, String.format(MESSAGE, graphemeClusters.size()), graphemeClusters);
      }
      graphemeClusters.clear();
    }

    @Override
    public void visitCharacterClassUnion(CharacterClassUnionTree tree) {
      graphemeClusters.addAll(GraphemeHelper.getGraphemeInList(tree.getCharacterClasses()));
      super.visitCharacterClassUnion(tree);
    }

  }

  private static class GraphemeHelper {

    // M (Mark) is "a character intended to be combined with another character (e.g. accents, umlauts, enclosing boxes, etc.)."
    // See https://www.regular-expressions.info/unicode.html
    private static final Pattern MARK_PATTERN = Pattern.compile("\\p{M}");

    private GraphemeHelper() {
    }

    private static List<RegexCheck.RegexIssueLocation> getGraphemeInList(List<? extends RegexSyntaxElement> trees) {
      List<RegexCheck.RegexIssueLocation> result = new ArrayList<>();
      List<RegexSyntaxElement> codePoints = new ArrayList<>();
      for (RegexSyntaxElement child : trees) {
        if (child instanceof CharacterTree) {
          CharacterTree currentCharacter = (CharacterTree) child;
          if (!currentCharacter.isEscapeSequence()) {
            if (!isMark(currentCharacter)) {
              addCurrentGrapheme(result, codePoints);
              codePoints.clear();
              codePoints.add(currentCharacter);
            } else if (!codePoints.isEmpty()) {
              codePoints.add(currentCharacter);
            }
            continue;
          }
        }
        addCurrentGrapheme(result, codePoints);
        codePoints.clear();
      }
      addCurrentGrapheme(result, codePoints);
      return result;
    }

    private static boolean isMark(CharacterTree currentChar) {
      return MARK_PATTERN.matcher(currentChar.characterAsString()).matches();
    }

    private static void addCurrentGrapheme(List<RegexCheck.RegexIssueLocation> result, List<RegexSyntaxElement> codePoints) {
      if (codePoints.size() > 1) {
        result.add(new RegexCheck.RegexIssueLocation(codePoints, ""));
      }
    }

  }
}
