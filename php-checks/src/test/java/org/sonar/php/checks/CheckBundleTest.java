package org.sonar.php.checks;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.sonar.plugins.php.api.symbols.SymbolTable;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.visitors.PHPCheck;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.plugins.php.api.visitors.PhpFile;
import org.sonar.plugins.php.api.visitors.PhpIssue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CheckBundleTest {

  private static final PhpFile FILE = mock(PhpFile.class);
  private static final CompilationUnitTree UNIT_TREE = mock(CompilationUnitTree.class);
  private static final SymbolTable TABLE = mock(SymbolTable.class);

  @Test
  public void test() {
    CheckBundle bundle = new TestCheckBundle();
    assertThat(bundle.analyze(FILE, UNIT_TREE, TABLE)).hasSize(3);
  }

  static class TestCheckBundle extends CheckBundle {

    @Override
    protected List<PHPCheck> checks() {
      PHPCheck checkPart = mock(PHPVisitorCheck.class);
      when(checkPart.analyze(any(PhpFile.class), any(CompilationUnitTree.class), any(SymbolTable.class))).thenReturn(
        Collections.singletonList(mock(PhpIssue.class))
      );
      return Arrays.asList(checkPart, checkPart, checkPart);
    }
  }
}
