package org.sonar.php.checks.regex;

import java.util.List;
import java.util.stream.Collectors;
import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonarsource.analyzer.commons.regex.RegexParseResult;
import org.sonarsource.analyzer.commons.regex.ast.BoundaryTree;
import org.sonarsource.analyzer.commons.regex.ast.DisjunctionTree;
import org.sonarsource.analyzer.commons.regex.ast.NonCapturingGroupTree;
import org.sonarsource.analyzer.commons.regex.ast.RegexBaseVisitor;
import org.sonarsource.analyzer.commons.regex.ast.RegexTree;
import org.sonarsource.analyzer.commons.regex.ast.SequenceTree;

@Rule(key = "S5850")
public class AnchorPrecedenceCheck extends AbstractRegexCheck {

  public static final String MESSAGE = "Group parts of the regex together to make the intended operator precedence explicit.";

  @Override
  public void checkRegex(RegexParseResult regexParseResult, FunctionCallTree regexFunctionCall) {
    new Visitor().visit(regexParseResult);
  }

  private enum Position {
    BEGINNING, END
  }

  private class Visitor extends RegexBaseVisitor {

    @Override
    public void visitDisjunction(DisjunctionTree tree) {
      List<RegexTree> alternatives = tree.getAlternatives();
      if ((anchoredAt(alternatives, Position.BEGINNING) || anchoredAt(alternatives, Position.END))
        && notAnchoredElseWhere(alternatives)) {
        newIssue(tree, MESSAGE);
      }
      super.visitDisjunction(tree);
    }

    private boolean anchoredAt(List<RegexTree> alternatives, Position position) {
      int itemIndex = position == Position.BEGINNING ? 0 : (alternatives.size() - 1);
      RegexTree firstOrLast = alternatives.get(itemIndex);
      return isAnchored(firstOrLast, position);
    }

    private boolean notAnchoredElseWhere(List<RegexTree> alternatives) {
      if (isAnchored(alternatives.get(0), Position.END)
        || isAnchored(alternatives.get(alternatives.size() - 1), Position.BEGINNING)) {
        return false;
      }
      for (RegexTree alternative : alternatives.subList(1, alternatives.size() - 1)) {
        if (isAnchored(alternative, Position.BEGINNING) || isAnchored(alternative, Position.END)) {
          return false;
        }
      }
      return true;
    }

    private boolean isAnchored(RegexTree tree, Position position) {
      if (!tree.is(RegexTree.Kind.SEQUENCE)) {
        return false;
      }
      SequenceTree sequence = (SequenceTree) tree;
      List<RegexTree> items = sequence.getItems().stream()
        .filter(item -> !isFlagSetter(item))
        .collect(Collectors.toList());
      if (items.isEmpty()) {
        return false;
      }
      int index = position == Position.BEGINNING ? 0 : (items.size() - 1);
      RegexTree firstOrLast = items.get(index);
      return firstOrLast.is(RegexTree.Kind.BOUNDARY) && isAnchor((BoundaryTree) firstOrLast);
    }

    private boolean isAnchor(BoundaryTree tree) {
      switch (tree.type()) {
        case INPUT_START:
        case LINE_START:
        case INPUT_END:
        case INPUT_END_FINAL_TERMINATOR:
        case LINE_END:
          return true;
        default:
          return false;
      }
    }

    /**
     * Return whether the given regex is a non-capturing group without contents, i.e. one that only sets flags for the
     * rest of the expression
     */
    private boolean isFlagSetter(RegexTree tree) {
      return tree.is(RegexTree.Kind.NON_CAPTURING_GROUP) && ((NonCapturingGroupTree) tree).getElement() == null;
    }

  }
}
