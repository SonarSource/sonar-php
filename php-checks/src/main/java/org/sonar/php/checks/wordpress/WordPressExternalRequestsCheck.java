package org.sonar.php.checks.wordpress;

import java.util.Collections;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;

@Rule(key = "S6345")
public class WordPressExternalRequestsCheck extends WordPressConfigVisitor {

  private static final String MESSAGE = "Make sure allowing external requests is intended.";
  private boolean configOccurred;

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    configOccurred = false;
    super.visitCompilationUnit(tree);
    if (!configOccurred && isWpConfigFile()) {
      context().newFileIssue(this, MESSAGE);
    }
  }

  @Override
  protected Set<String> configsToVisit() {
    return Collections.singleton("WP_HTTP_BLOCK_EXTERNAL");
  }

  @Override
  void visitConfigDeclaration(FunctionCallTree config) {
    configOccurred = true;
    configValue(config).filter(CheckUtils::isFalseValue).ifPresent(v -> newIssue(config, MESSAGE));
  }
}
