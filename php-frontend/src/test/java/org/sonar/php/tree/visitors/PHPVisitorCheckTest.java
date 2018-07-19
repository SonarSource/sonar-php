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
package org.sonar.php.tree.visitors;

import com.sonar.sslr.api.typed.ActionParser;
import java.io.File;
import org.junit.Test;
import org.sonar.php.FileTestUtils;
import org.sonar.php.parser.PHPParserBuilder;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.lexical.SyntaxTrivia;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.plugins.php.api.visitors.PhpFile;

import static org.assertj.core.api.Assertions.assertThat;

public class PHPVisitorCheckTest {

  @Test
  public void should_visit_tree_elements() {
    ActionParser<Tree> parser = PHPParserBuilder.createParser();
    PhpFile file = FileTestUtils.getFile(new File("src/test/resources/visitors/test.php"));
    CompilationUnitTree tree = (CompilationUnitTree) parser.parse(file.contents());

    TestVisitor testVisitor = new TestVisitor();
    testVisitor.analyze(file, tree);

    assertThat(testVisitor.classCounter).isEqualTo(1);
    assertThat(testVisitor.namespaceNameCounter).isEqualTo(3);
    assertThat(testVisitor.varIdentifierCounter).isEqualTo(2);
    // PHPCheck#init() is called by PHPAnalyzer
    assertThat(testVisitor.initCounter).isEqualTo(0);
    assertThat(testVisitor.literalCounter).isEqualTo(3);
    assertThat(testVisitor.tokenCounter).isEqualTo(29);
    assertThat(testVisitor.triviaCounter).isEqualTo(2);
  }

  @Test
  public void should_have_correct_context() throws Exception {
    ActionParser<Tree> parser = PHPParserBuilder.createParser();
    PhpFile file = FileTestUtils.getFile(new File("src/test/resources/visitors/test.php"));
    CompilationUnitTree tree = (CompilationUnitTree) parser.parse(file.contents());

    ContextTestVisitor testVisitor = new ContextTestVisitor();
    testVisitor.analyze(file, tree);
    assertThat(testVisitor.context().getPhpFile()).isEqualTo(file);
  }


  private class ContextTestVisitor extends PHPVisitorCheck {
  }

  private class TestVisitor extends PHPVisitorCheck {
    int classCounter = 0;
    int namespaceNameCounter = 0;
    int varIdentifierCounter = 0;
    int initCounter = 0;
    int triviaCounter = 0;
    int tokenCounter = 0;
    int literalCounter = 0;

    @Override
    public void visitClassDeclaration(ClassDeclarationTree tree) {
      super.visitClassDeclaration(tree);
      classCounter++;
    }

    @Override
    public void visitNamespaceName(NamespaceNameTree tree) {
      super.visitNamespaceName(tree);
      namespaceNameCounter++;
    }

    @Override
    public void visitFunctionCall(FunctionCallTree tree) {
      tree.callee().accept(this);
      scan(tree.arguments());
    }

    @Override
    public void visitVariableIdentifier(VariableIdentifierTree tree) {
      super.visitVariableIdentifier(tree);
      varIdentifierCounter++;
    }

    @Override
    public void init() {
      initCounter++;
    }

    @Override
    public void visitToken(SyntaxToken token) {
      super.visitToken(token);
      tokenCounter++;
    }

    @Override
    public void visitLiteral(LiteralTree tree) {
      literalCounter++;
      super.visitLiteral(tree);
    }

    @Override
    public void visitTrivia(SyntaxTrivia trivia) {
      triviaCounter++;
    }

  }

}
