package org.sonar.php.checks.regex;

import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonarsource.analyzer.commons.regex.RegexParseResult;
import org.sonarsource.analyzer.commons.regex.ast.CharacterClassElementTree;
import org.sonarsource.analyzer.commons.regex.ast.DisjunctionTree;
import org.sonarsource.analyzer.commons.regex.ast.RegexBaseVisitor;

@Rule(key = "S6035")
public class SingleCharacterAlternationCheck extends AbstractRegexCheck {

  public static final String MESSAGE = "Replace this alternation with a character class.";

  @Override
  public void checkRegex(RegexParseResult regexParseResult, FunctionCallTree regexFunctionCall) {
    new SingleCharacterAlternationFinder().visit(regexParseResult);
  }

  class SingleCharacterAlternationFinder extends RegexBaseVisitor {

    @Override
    public void visitDisjunction(DisjunctionTree tree) {
      if (tree.getAlternatives().stream().allMatch(CharacterClassElementTree.class::isInstance)) {
        newIssue(tree, MESSAGE);
      }
      super.visitDisjunction(tree);
    }
  }
}
