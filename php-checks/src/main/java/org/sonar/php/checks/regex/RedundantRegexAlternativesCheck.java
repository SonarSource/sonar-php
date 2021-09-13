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
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.sonar.check.Rule;
import org.sonar.php.regex.ast.PhpRegexBaseVisitor;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonarsource.analyzer.commons.regex.RegexParseResult;
import org.sonarsource.analyzer.commons.regex.ast.CapturingGroupTree;
import org.sonarsource.analyzer.commons.regex.ast.CharacterClassElementTree;
import org.sonarsource.analyzer.commons.regex.ast.DisjunctionTree;
import org.sonarsource.analyzer.commons.regex.ast.RegexBaseVisitor;
import org.sonarsource.analyzer.commons.regex.ast.RegexTree;
import org.sonarsource.analyzer.commons.regex.helpers.RegexTreeHelper;
import org.sonarsource.analyzer.commons.regex.helpers.SubAutomaton;

@Rule(key = "S5855")
public class RedundantRegexAlternativesCheck extends AbstractRegexCheck {

  public static final String MESSAGE = "Remove or rework this redundant alternative.";
  public static final String MESSAGE_KEEP = "Alternative to keep";
  public static final String MESSAGE_REDUNDANT = "Other redundant alternative";

  @Override
  public void checkRegex(RegexParseResult regexParseResult, FunctionCallTree regexFunctionCall) {
    new DisjunctionVisitor().visit(regexParseResult);
  }

  private void reportRedundantIssue(RegexTree supersetAlternative, Set<RegexTree> redundantSubsetAlternatives) {
    List<RegexTree> redundantAlternatives = new ArrayList<>(redundantSubsetAlternatives);
    redundantAlternatives.sort(Comparator.comparing(element -> element.getRange().getBeginningOffset()));
    RegexTree firstRedundantAlternatives = redundantAlternatives.get(0);
    List<RegexIssueLocation> secondaries = new ArrayList<>();
    secondaries.add(new RegexIssueLocation(supersetAlternative, MESSAGE_KEEP));
    redundantAlternatives.stream().skip(1)
      .map(otherRedundantAlternatives -> new RegexIssueLocation(otherRedundantAlternatives, MESSAGE_REDUNDANT))
      .forEach(secondaries::add);
    newIssue(firstRedundantAlternatives, MESSAGE, secondaries);
  }

  private class DisjunctionVisitor extends PhpRegexBaseVisitor {

    @Override
    public void visitDisjunction(DisjunctionTree tree) {
      RedundantAlternativeCollector collector = new RedundantAlternativeCollector();
      List<RegexTree> alternatives = tree.getAlternatives().stream()
        // We don't handle POSIX character classes correctly (SONARPHP-1239)
        .filter(t -> !hasPosixCharacterClass(t))
        .collect(Collectors.toList());
      for (int i = 0; i + 1 < alternatives.size(); i++) {
        for (int j = i + 1; j < alternatives.size(); j++) {
          collector.evaluate(alternatives.get(i), alternatives.get(j));
        }
      }
      collector.supersetSubsetListMap.forEach(RedundantRegexAlternativesCheck.this::reportRedundantIssue);
      super.visitDisjunction(tree);
    }
  }

  private static class RedundantAlternativeCollector {

    private final Map<RegexTree, Set<RegexTree>> supersetSubsetListMap = new LinkedHashMap<>();
    private final Set<RegexTree> allSubsets = new HashSet<>();

    private void evaluate(RegexTree prevAlternative, RegexTree nextAlternative) {
      if (supersetOf(prevAlternative, nextAlternative)) {
        add(prevAlternative, nextAlternative);
      } else {
        if (supersetOf(nextAlternative, prevAlternative) && hasNoCapturingGroup(prevAlternative) && hasNoCapturingGroup(nextAlternative)) {
          add(nextAlternative, prevAlternative);
        }
      }
    }

    private static boolean supersetOf(RegexTree alternative1, RegexTree alternative2) {
      SubAutomaton subAutomaton1 = new SubAutomaton(alternative1, alternative1.continuation(), false);
      SubAutomaton subAutomaton2 = new SubAutomaton(alternative2, alternative2.continuation(), false);
      return RegexTreeHelper.supersetOf(subAutomaton1, subAutomaton2, false);
    }

    private void add(RegexTree superset, RegexTree subset) {
      // When [S1 is a superset of S3] and [S2 is a superset of S3] then [S3 is a subset of S1, S2]
      // But we want to display only one issue about S3 even if it's possible to find more.
      if (allSubsets.contains(subset)) {
        return;
      }
      // When [S1 is a superset of S2] and [S2 is a superset of S3] then [S1 is a superset of S3]
      // But we want one issue about [S1 being superset of S2,S3] and not another one about [S2 being superset of S3]
      // During sequence: add(S1,S2) ... add(S2,S3) => we want to ignore (S2,S3) because another (S1,S3) exists
      if (allSubsets.contains(superset)) {
        return;
      }
      Set<RegexTree> subsetList = supersetSubsetListMap.computeIfAbsent(superset, k -> new HashSet<>());
      // During sequence: add(S2,S3) ... add(S1,S2) => we need to remove S2 from supersets and add(S1,S3)
      Set<RegexTree> subsetOfTheSubset = supersetSubsetListMap.remove(subset);
      if (subsetOfTheSubset != null) {
        subsetList.addAll(subsetOfTheSubset);
      }
      allSubsets.add(subset);
      subsetList.add(subset);
    }

    private static boolean hasNoCapturingGroup(RegexTree tree) {
      CapturingGroupVisitor visitor = new CapturingGroupVisitor();
      tree.accept(visitor);
      return !visitor.hasCapturingGroup;
    }
  }

  private static class CapturingGroupVisitor extends RegexBaseVisitor {
    boolean hasCapturingGroup;

    @Override
    public void visitCapturingGroup(CapturingGroupTree tree) {
      hasCapturingGroup = true;
    }
  }

  private static boolean hasPosixCharacterClass(RegexTree tree) {
    PosixCharacterClassVisitor posixCharacterClassVisitor = new PosixCharacterClassVisitor();
    posixCharacterClassVisitor.visit(tree);
    return posixCharacterClassVisitor.hasPosixCharacterClass;
  }

  private static class PosixCharacterClassVisitor extends RegexBaseVisitor {
    boolean hasPosixCharacterClass = false;

    @Override
    public void visitInCharClass(CharacterClassElementTree tree) {
      if (tree.is(CharacterClassElementTree.Kind.POSIX_CLASS)) {
        hasPosixCharacterClass = true;
      }
      super.visitInCharClass(tree);
    }
  }
}
