package org.sonar.php.filters;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.AttributeTree;
import org.sonar.plugins.php.api.tree.declaration.CallArgumentTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.lexical.SyntaxTrivia;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

public class SuppressWarningFilter extends PHPVisitorCheck implements FilterPhpIssue {

  private final Map<String, Map<Integer, Set<String>>> suppressedRulesPerLinePerFile = new HashMap<>();
  private String currentComponent;

  private static final String ARGUMENT_FORMAT = "\"[a-zA-Z0-9:]++\"";
  private static final String ARGUMENTS_FORMAT = ARGUMENT_FORMAT + "(?:\\s*+,\\s*+"+ARGUMENT_FORMAT+")*+";
  private static final Pattern SUPPRESS_WARNING_COMMENT_PATTERN = Pattern.compile("@SuppressWarnings\\s*+\\(\\s*+(?<arguments>"+ARGUMENTS_FORMAT+")\\s*+\\)");

  public void reset() {
    suppressedRulesPerLinePerFile.clear();
  }

  public void scanCompilationUnit(String componentName, CompilationUnitTree tree) {
    this.currentComponent = componentName;
    super.visitCompilationUnit(tree);
  }

  @Override
  public boolean accept(String fileName, String ruleName, int line) {
    return !suppressedRulesPerLinePerFile.getOrDefault(fileName, Collections.emptyMap())
      .getOrDefault(line, Collections.emptySet()).contains(ruleName);
  }

  @Override
  public void visitAttribute(AttributeTree tree) {
    int lineSuppressed = tree.closeParenthesisToken().line() + 1; // TODO: replace this "ignore next line issue" by "the scope of parent group attribute"
    Set<String> rulesSuppressed = tree.arguments().stream()
      .map(CallArgumentTree::value)
      .filter(expr -> expr.is(Tree.Kind.REGULAR_STRING_LITERAL)).map(LiteralTree.class::cast) // consider only string literal
      .map(LiteralTree::value)
      .map(SuppressWarningFilter::stripDoubleQuotes)
      .collect(Collectors.toSet());
    suppressedRulesPerLinePerFile.computeIfAbsent(currentComponent, key -> new HashMap<>()).put(lineSuppressed, rulesSuppressed);
    super.visitAttribute(tree);
  }

  @Override
  public void visitToken(SyntaxToken token) {
    for (SyntaxTrivia trivia : token.trivias()) {
      String comment = getContents(trivia.text());
      processSuppressedWarningsInComment(token, comment);
    }
    super.visitToken(token);
  }

  private void processSuppressedWarningsInComment(SyntaxToken token, String comment) {
    Matcher matcher = SUPPRESS_WARNING_COMMENT_PATTERN.matcher(comment);
    while (matcher.find()) {
      String arguments = matcher.group("arguments");
      Arrays.stream(arguments.split(","))
        .map(str -> stripDoubleQuotes(str.trim()))
        .forEach(ruleName -> {
          for (int line = token.line(); line <= token.endLine(); line++) {
            suppressedRulesPerLinePerFile.computeIfAbsent(currentComponent, key -> new HashMap<>())
              .computeIfAbsent(line, key -> new HashSet<>()).add(ruleName);
          }
        });
    }
  }

  private static String getContents(String comment) {
    if (comment.startsWith("//")) {
      return comment.substring(2);
    } else if (comment.startsWith("#")) {
      return comment.substring(1);
    } else {
      return comment.substring(2, comment.length() - 2);
    }
  }

  private static String stripDoubleQuotes(String str) {
    if (str.startsWith("\"") && str.endsWith("\"")) {
      return str.substring(1, str.length()-1);
    }
    return str;
  }
}
