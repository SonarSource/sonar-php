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

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.php.regex.ast.PhpRegexBaseVisitor;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonarsource.analyzer.commons.regex.RegexParseResult;
import org.sonarsource.analyzer.commons.regex.ast.AutomatonState;
import org.sonarsource.analyzer.commons.regex.ast.BoundaryTree;
import org.sonarsource.analyzer.commons.regex.ast.DisjunctionTree;
import org.sonarsource.analyzer.commons.regex.ast.EndOfLookaroundState;
import org.sonarsource.analyzer.commons.regex.ast.LookAroundTree;
import org.sonarsource.analyzer.commons.regex.ast.RegexBaseVisitor;
import org.sonarsource.analyzer.commons.regex.ast.RegexTree;
import org.sonarsource.analyzer.commons.regex.helpers.RegexReachabilityChecker;

import static org.sonarsource.analyzer.commons.regex.ast.AutomatonState.TransitionType.CHARACTER;
import static org.sonarsource.analyzer.commons.regex.ast.AutomatonState.TransitionType.EPSILON;
import static org.sonarsource.analyzer.commons.regex.ast.AutomatonState.TransitionType.NEGATION;

@Rule(key = "S5996")
public class ImpossibleBoundariesCheck extends AbstractRegexCheck {

  private static final String MESSAGE = "Remove or replace this boundary that will never match because it appears %s mandatory input.";
  private static final String SOFT_MESSAGE =
    "Remove or replace this boundary that can only match if the previous part matched the empty string because it appears %s mandatory input.";
  private final Set<RegexTree> excluded = new HashSet<>();
  private final RegexReachabilityChecker regexReachabilityChecker = new RegexReachabilityChecker(false);

  @Override
  public void checkRegex(RegexParseResult regexParseResult, FunctionCallTree regexFunctionCall) {
    new ImpossibleBoundaryFinder().visit(regexParseResult);
  }

  private class ImpossibleBoundaryFinder extends PhpRegexBaseVisitor {

    private AutomatonState start;
    private AutomatonState end;

    @Override
    public void visit(RegexParseResult regexParseResult) {
      regexReachabilityChecker.clearCache();
      start = regexParseResult.getStartState();
      end = regexParseResult.getFinalState();
      super.visit(regexParseResult);
    }

    @Override
    public void visitLookAround(LookAroundTree tree) {
      // Inside a lookaround we consider the end/start of the lookahead/behind respectively as if it were the end/start
      // of the regex. This avoids false positives for cases like `(?=.*$)foo` or `foo(?<=^...)`.
      if (tree.getDirection() == LookAroundTree.Direction.BEHIND) {
        AutomatonState oldStart = start;
        start = tree.getElement();
        super.visitLookAround(tree);
        start = oldStart;
      } else {
        AutomatonState oldEnd = end;
        // Set end to the lookaround's end-of-lookaround state
        end = tree.getElement().continuation();
        super.visitLookAround(tree);
        end = oldEnd;
      }
    }

    @Override
    public void visitDisjunction(DisjunctionTree tree) {
      BoundaryInDisjunctionFinder boundaryInDisjunctionFinder = new BoundaryInDisjunctionFinder();
      boundaryInDisjunctionFinder.visit(tree);
      excluded.addAll(boundaryInDisjunctionFinder.foundBoundaries());
      super.visitDisjunction(tree);
    }

    @Override
    public void visitBoundary(BoundaryTree boundaryTree) {
      switch (boundaryTree.type()) {
        case LINE_START:
        case INPUT_START:
          if (!canReachWithoutConsumingInput(start, boundaryTree)) {
            newIssue(boundaryTree, String.format(MESSAGE, "after"));
          } else if (!excluded.contains(boundaryTree) && probablyShouldConsumeInput(start, boundaryTree)) {
            newIssue(boundaryTree, String.format(SOFT_MESSAGE, "after"));
          }
          break;
        case LINE_END:
          if (!boundaryTree.activeFlags().contains(Pattern.MULTILINE)) {
            checkEndBoundary(boundaryTree);
          }
          break;
        case INPUT_END:
        case INPUT_END_FINAL_TERMINATOR:
          checkEndBoundary(boundaryTree);
          break;
        default:
          // Do nothing
      }
    }

    private void checkEndBoundary(BoundaryTree boundaryTree) {
      if (!canReachWithoutConsumingInput(boundaryTree, end)) {
        newIssue(boundaryTree, String.format(MESSAGE, "before"));
      } else if (!excluded.contains(boundaryTree) && probablyShouldConsumeInput(boundaryTree, end)) {
        newIssue(boundaryTree, String.format(SOFT_MESSAGE, "before"));
      }
    }

    private boolean probablyShouldConsumeInput(AutomatonState start, AutomatonState goal) {
      return  canReachWithConsumingInput(start, goal, new HashSet<>());
    }
  }

  private static class BoundaryInDisjunctionFinder extends RegexBaseVisitor {
    private final Set<BoundaryTree> foundBoundaries = new HashSet<>();

    @Override
    public void visitBoundary(BoundaryTree boundaryTree) {
      foundBoundaries.add(boundaryTree);
    }

    public Set<BoundaryTree> foundBoundaries() {
      return new HashSet<>(foundBoundaries);
    }
  }

  private boolean canReachWithConsumingInput(AutomatonState start, AutomatonState goal, Set<AutomatonState> visited) {
    if (start == goal || visited.contains(start)) {
      return false;
    }
    visited.add(start);

    if (start instanceof LookAroundTree) {
      return canReachWithConsumingInput(start.continuation(), goal, visited);
    }

    for (AutomatonState successor : start.successors()) {
      AutomatonState.TransitionType transition = successor.incomingTransitionType();
      if (((transition == CHARACTER) && regexReachabilityChecker.canReach(successor, goal))
        || ((transition != CHARACTER) && canReachWithConsumingInput(successor, goal, visited))) {
        return true;
      }
    }
    return false;
  }

  public static boolean canReachWithoutConsumingInput(AutomatonState start, AutomatonState goal) {
    return canReachWithoutConsumingInput(start, goal, false, new HashSet<>());
  }

  private static boolean canReachWithoutConsumingInput(AutomatonState start, AutomatonState goal, boolean stopAtBoundaries, Set<AutomatonState> visited) {
    if (start == goal) {
      return true;
    }
    if (visited.contains(start) || (stopAtBoundaries && start instanceof BoundaryTree)) {
      return false;
    }
    visited.add(start);
    for (AutomatonState successor : start.successors()) {
      AutomatonState.TransitionType transition = successor.incomingTransitionType();
      // We don't generally consider elements behind backtracking edges to be 0-input reachable because what comes
      // after the edge won't directly follow what's before the edge. However, we do consider the end-of-lookahead
      // state itself reachable (but not any state behind it), so that we can check whether the end of the lookahead
      // can be reached without input from a given place within the lookahead.
      if ((successor instanceof EndOfLookaroundState && successor == goal)
        || ((transition == EPSILON || transition == NEGATION) && canReachWithoutConsumingInput(successor, goal, stopAtBoundaries, visited))) {
        return true;
      }
    }
    return false;
  }
}
