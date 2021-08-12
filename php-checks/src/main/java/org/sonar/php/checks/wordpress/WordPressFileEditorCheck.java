package org.sonar.php.checks.wordpress;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;

public class WordPressFileEditorCheck extends WordPressConfigVisitor {

  private static final String MESSAGE = "Plugin and theme files editor is activated";

  private FunctionCallTree fileEditConfigTree;
  private FunctionCallTree fileModsConfigTree;

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    fileEditConfigTree = null;
    fileModsConfigTree = null;

    super.visitCompilationUnit(tree);

    if (!fileModsDisallowed()) {
      if (fileEditConfigTree == null) {
        context().newFileIssue(this, MESSAGE);
      } else {
        configValue(fileEditConfigTree).filter(CheckUtils::isFalseValue)
          .ifPresent(value -> newIssue(fileEditConfigTree, MESSAGE));
      }
    }

    fileEditConfigTree = null;
    fileModsConfigTree = null;
  }

  @Override
  protected Set<String> configsToVisit() {
    return new HashSet<>(Arrays.asList("DISALLOW_FILE_EDIT", "DISALLOW_FILE_MODS"));
  }

  @Override
  void visitConfigDeclaration(FunctionCallTree config) {
    if (isConfigKey(config, "DISALLOW_FILE_EDIT")) {
      fileEditConfigTree = config;
    } else {
      // DISALLOW_FILE_MODS
      fileModsConfigTree = config;
    }
  }

  private boolean fileModsDisallowed() {
    return fileModsConfigTree != null && !configValue(fileModsConfigTree).filter(CheckUtils::isFalseValue).isPresent();
  }
}
