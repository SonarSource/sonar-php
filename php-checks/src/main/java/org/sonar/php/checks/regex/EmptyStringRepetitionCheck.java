package org.sonar.php.checks.regex;

import java.util.Collections;
import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonarsource.analyzer.commons.regex.RegexParseResult;
import org.sonarsource.analyzer.commons.regex.ast.DisjunctionTree;
import org.sonarsource.analyzer.commons.regex.ast.GroupTree;
import org.sonarsource.analyzer.commons.regex.ast.RegexBaseVisitor;
import org.sonarsource.analyzer.commons.regex.ast.RegexTree;
import org.sonarsource.analyzer.commons.regex.ast.RepetitionTree;
import org.sonarsource.analyzer.commons.regex.ast.SequenceTree;

@Rule(key = "S5842")
public class EmptyStringRepetitionCheck extends AbstractRegexCheck {

  private static final String MESSAGE = "Rework this part of the regex to not match the empty string.";

  @Override
  public void checkRegex(RegexParseResult regexParseResult, FunctionCallTree regexFunctionCall) {
    new Visitor().visit(regexParseResult);
  }

  private class Visitor extends RegexBaseVisitor {

    @Override
    public void visitRepetition(RepetitionTree tree) {
      RegexTree element = tree.getElement();
      if (matchEmptyString(element)) {
        reportIssue(element, MESSAGE, Collections.emptyList());
      }
    }

    private boolean matchEmptyString(RegexTree element) {
      switch (element.kind()) {
        case SEQUENCE:
          return ((SequenceTree) element).getItems().stream().allMatch(this::matchEmptyString);
        case DISJUNCTION:
          return ((DisjunctionTree) element).getAlternatives().stream().anyMatch(this::matchEmptyString);
        case REPETITION:
          return ((RepetitionTree) element).getQuantifier().getMinimumRepetitions() == 0;
        case LOOK_AROUND:
        case BOUNDARY:
          return true;
        default:
          if (element instanceof GroupTree) {
            RegexTree nestedElement = ((GroupTree) element).getElement();
            return nestedElement == null || matchEmptyString(nestedElement);
          }
          return false;
      }
    }

  }
}
