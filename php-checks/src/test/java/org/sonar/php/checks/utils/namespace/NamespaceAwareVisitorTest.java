/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2018 SonarSource SA
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
package org.sonar.php.checks.utils.namespace;

import com.sonar.sslr.api.typed.ActionParser;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sonar.php.parser.PHPParserBuilder;
import org.sonar.plugins.php.TestUtils;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.statement.NamespaceStatementTree;
import org.sonar.plugins.php.api.tree.statement.UseClauseTree;
import org.sonar.plugins.php.api.tree.statement.UseStatementTree;
import org.sonar.plugins.php.api.visitors.PhpFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.php.checks.utils.namespace.QualifiedName.create;

public class NamespaceAwareVisitorTest {
  private static final ActionParser<Tree> parser = PHPParserBuilder.createParser();

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Test
  public void test_simple() throws IOException {
    NamespaceAwareVisitorChecker checker = new NamespaceAwareVisitorChecker();
    visit("new A(); new A\\B(); new \\A\\C();", checker);

    assertThat(checker.fullyQualifiedNames).containsExactly(
      create("A"),
      create("A", "B"),
      create("A", "C"));
  }

  @Test
  public void test_with_simple_namespaces() throws IOException {
    NamespaceAwareVisitorChecker checker = new NamespaceAwareVisitorChecker();
    visit("namespace N1; new A(); new A\\B(); new \\A\\C(); namespace N2; new A(); new A\\B(); new \\A\\D();", checker);

    assertThat(checker.fullyQualifiedNames).containsExactly(
      create("N1", "A"),
      create("N1", "A", "B"),
      create("A", "C"),
      create("N2", "A"),
      create("N2", "A", "B"),
      create("A", "D"));
  }

  @Test
  public void test_with_simple_bracketed_namespaces() throws IOException {
    NamespaceAwareVisitorChecker checker = new NamespaceAwareVisitorChecker();
    visit("namespace N1 { new A(); new A\\B(); new \\A\\C(); } namespace N2 { new A(); new A\\B(); new \\A\\D(); }", checker);

    assertThat(checker.fullyQualifiedNames).containsExactly(
      create("N1", "A"),
      create("N1", "A", "B"),
      create("A", "C"),
      create("N2", "A"),
      create("N2", "A", "B"),
      create("A", "D"));
  }

  @Test
  public void test_with_bracketed_namespaces() throws IOException {
    NamespaceAwareVisitorChecker checker = new NamespaceAwareVisitorChecker();
    visit("namespace N1 { new A(); new A\\B(); new \\A\\C(); } namespace { new A(); new A\\B(); new \\A\\D(); }", checker);

    assertThat(checker.fullyQualifiedNames).containsExactly(
      create("N1", "A"),
      create("N1", "A", "B"),
      create("A", "C"),
      create("A"),
      create("A", "B"),
      create("A", "D"));
  }

  @Test
  public void use_without_alias() throws IOException {
    NamespaceAwareVisitorChecker checker = new NamespaceAwareVisitorChecker();
    // 'use A\B'; is equivalent to 'use A\B as B', and the original name is always relative to global scope
    visit("use A\\B; new A(); new A\\B(); new \\A\\B(); new B();", checker);

    assertThat(checker.fullyQualifiedNames).containsExactly(
      create("A"),
      create("A", "B"),
      create("A", "B"),
      create("A", "B"));
  }

  @Test
  public void use_with_alias() throws IOException {
    NamespaceAwareVisitorChecker checker = new NamespaceAwareVisitorChecker();
    visit("use A\\B as AB; new A(); new AB(); new AB\\C();", checker);

    assertThat(checker.fullyQualifiedNames).containsExactly(
      create("A"),
      create("A", "B"),
      create("A", "B", "C"));
  }

  @Test
  public void use_with_multi_aliases() throws IOException {
    NamespaceAwareVisitorChecker checker = new NamespaceAwareVisitorChecker();
    visit("use AB as AA, A\\B as AB; new A(); new AA(); new AB();", checker);

    assertThat(checker.fullyQualifiedNames).containsExactly(
      create("A"),
      create("AB"),
      create("A", "B"));
  }

  @Test
  public void use_with_aliases_grouped() throws IOException {
    NamespaceAwareVisitorChecker checker = new NamespaceAwareVisitorChecker();
    visit("use A\\B\\{C, D as E}; new A(); new B(); new C(); new D(); new E();", checker);

    assertThat(checker.fullyQualifiedNames).containsExactly(
      create("A"),
      create("B"),
      create("A", "B", "C"),
      create("D"),
      create("A", "B", "D"));
  }

  @Test
  public void aliases_in_namespaces_context() throws IOException {
    NamespaceAwareVisitorChecker checker = new NamespaceAwareVisitorChecker();
    visit("namespace N1; new A(); use A; new A(); new B(); use A as B; new B();", checker);

    assertThat(checker.fullyQualifiedNames).containsExactly(
      create("N1", "A"),
      create("A"),
      create("N1", "B"),
      create("A"));
  }

  @Test
  public void test_mix_1() throws IOException {
    NamespaceAwareVisitorChecker checker = new NamespaceAwareVisitorChecker();
    visit("namespace N1 { use A as B; new A(); new B(); new \\A(); new \\B(); }", checker);

    assertThat(checker.fullyQualifiedNames).containsExactly(
      create("N1", "A"),
      create("A"),
      create("A"),
      create("B"));
  }

  @Test
  public void test_mix_2() throws IOException {
    NamespaceAwareVisitorChecker checker = new NamespaceAwareVisitorChecker();
    visit("namespace N1 { use A as B; new B(); } namespace N2 { new B(); new \\N1\\B(); }", checker);

    assertThat(checker.fullyQualifiedNames).containsExactly(
      create("A"),
      create("N2", "B"),
      create("N1", "B"));
  }

  @Test
  public void consecutive_visits() throws IOException {
    NamespaceAwareVisitorChecker checker = new NamespaceAwareVisitorChecker();
    visit("namespace N1 { new B(); use A as B; new B(); }", checker);
    visit("new B();", checker);
    visit("namespace N2; new B();", checker);
    visit("new B();", checker);

    assertThat(checker.fullyQualifiedNames).containsExactly(
      create("N1", "B"),
      create("A"),
      create("B"),
      create("N2", "B"),
      create("B"));
  }

  private void visit(String content, NamespaceAwareVisitor visitor) throws IOException {
    String fileContent = "<?php\n" + content;
    CompilationUnitTree tree = (CompilationUnitTree) parser.parse(fileContent);
    PhpFile inputFile = TestUtils.getFile(temporaryFolder.newFile(), fileContent); // "<?php $foo = 1; \n");
    visitor.init();
    visitor.analyze(inputFile, tree);
  }

  private class NamespaceAwareVisitorChecker extends NamespaceAwareVisitor {
    private List<QualifiedName> fullyQualifiedNames = new ArrayList<>();

    @Override
    public void visitNamespaceName(NamespaceNameTree tree) {
      super.visitNamespaceName(tree);
      // ignore NamespaceNameTree coming from namespace or use statements
      if (!(tree.getParent() instanceof NamespaceStatementTree)
        && !(tree.getParent() instanceof UseStatementTree)
        && !(tree.getParent() instanceof UseClauseTree)) {
        fullyQualifiedNames.add(getFullyQualifiedName(tree));
      }
    }
  }
}
