/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.php.tree.visitors;

import com.sonar.sslr.api.typed.ActionParser;
import java.io.File;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.sonar.php.FileTestUtils;
import org.sonar.php.parser.PHPParserBuilder;
import org.sonar.plugins.php.api.cache.CacheContext;
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.AttributeGroupTree;
import org.sonar.plugins.php.api.tree.declaration.AttributeTree;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.DnfIntersectionTypeTree;
import org.sonar.plugins.php.api.tree.declaration.DnfTypeTree;
import org.sonar.plugins.php.api.tree.declaration.IntersectionTypeTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.declaration.PropertyHookTree;
import org.sonar.plugins.php.api.tree.declaration.UnionTypeTree;
import org.sonar.plugins.php.api.tree.expression.BinaryExpressionTree;
import org.sonar.plugins.php.api.tree.expression.CallableConvertTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.lexical.SyntaxTrivia;
import org.sonar.plugins.php.api.tree.statement.EnumCaseTree;
import org.sonar.plugins.php.api.visitors.PHPCheck;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.plugins.php.api.visitors.PhpFile;
import org.sonar.plugins.php.api.visitors.PhpInputFileContext;
import org.sonar.plugins.php.api.visitors.PhpIssue;
import org.sonar.plugins.php.api.visitors.PreciseIssue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class PHPVisitorCheckTest {

  private static final String WORKDIR = "src/test/resources/visitors/";

  private PhpInputFileContext fileContext;
  private CacheContext cacheContext;

  @BeforeEach
  public void setUp() throws Exception {
    PhpFile file = FileTestUtils.getFile(new File(WORKDIR + "test.php"));
    cacheContext = mock(CacheContext.class);
    fileContext = new PhpInputFileContext(file, new File(WORKDIR), cacheContext);
  }

  @Test
  void shouldVisitTreeElements() {
    ActionParser<Tree> parser = PHPParserBuilder.createParser();
    PhpFile file = FileTestUtils.getFile(new File("src/test/resources/visitors/test.php"));
    CompilationUnitTree tree = (CompilationUnitTree) parser.parse(file.contents());

    TestVisitor testVisitor = new TestVisitor();
    testVisitor.analyze(file, tree);

    assertThat(testVisitor.classCounter).isEqualTo(2);
    assertThat(testVisitor.namespaceNameCounter).isEqualTo(9);
    assertThat(testVisitor.varIdentifierCounter).isEqualTo(12);
    // PHPCheck#init() is called by PHPAnalyzer
    assertThat(testVisitor.initCounter).isZero();
    assertThat(testVisitor.literalCounter).isEqualTo(7);
    assertThat(testVisitor.tokenCounter).isEqualTo(122);
    assertThat(testVisitor.triviaCounter).isEqualTo(2);
    assertThat(testVisitor.unionTypesCounter).isEqualTo(1);
    assertThat(testVisitor.intersectionTypeCounter).isEqualTo(1);
    assertThat(testVisitor.dnfTypeCounter).isEqualTo(1);
    assertThat(testVisitor.dnfIntersectionTypeCounter).isEqualTo(1);
    assertThat(testVisitor.propertyHookCounter).isEqualTo(2);
    assertThat(testVisitor.attributeGroupsCounter).isEqualTo(2);
    assertThat(testVisitor.attributesCounter).isEqualTo(3);
    assertThat(testVisitor.enumsCounter).isEqualTo(1);
    assertThat(testVisitor.enumCasesCounter).isEqualTo(2);
  }

  @Test
  void shouldHaveCorrectContext() {
    ActionParser<Tree> parser = PHPParserBuilder.createParser();
    PhpFile file = FileTestUtils.getFile(new File("src/test/resources/visitors/test.php"));
    CompilationUnitTree tree = (CompilationUnitTree) parser.parse(file.contents());

    ContextTestVisitor testVisitor = new ContextTestVisitor();
    testVisitor.analyze(file, tree);
    assertThat(testVisitor.context().getPhpFile()).isEqualTo(file);
    assertThat(testVisitor.context().getWorkingDirectory()).isNull();

    File workingDir = new File("working_dir");
    testVisitor.analyze(new PHPCheckContext(file, tree, workingDir));
    assertThat(testVisitor.context().getWorkingDirectory()).isEqualTo(workingDir);
  }

  @Test
  void testGetFullyQualifiedName() {
    ActionParser<Tree> parser = PHPParserBuilder.createParser();
    CompilationUnitTree tree = (CompilationUnitTree) parser.parse("<?php namespace n { function foo() {}; foo(); }");
    PHPVisitorCheck visitor = new PHPVisitorCheck() {
      @Override
      public void visitFunctionCall(FunctionCallTree tree) {
        QualifiedName qualifiedName = getFullyQualifiedName(((NamespaceNameTree) tree.callee()));
        assertThat(qualifiedName.simpleName()).isEqualTo("foo");
        assertThat(qualifiedName).hasToString("n\\foo");
      }
    };
    PHPCheckContext phpCheckContext = new PHPCheckContext(mock(PhpFile.class), tree, null);
    visitor.analyze(phpCheckContext);
  }

  @Test
  void testNewIssue() {
    ActionParser<Tree> parser = PHPParserBuilder.createParser();
    CompilationUnitTree tree = (CompilationUnitTree) parser.parse("<?php phpinfo();");
    PHPVisitorCheck testVisitor = new PHPVisitorCheck() {
      @Override
      public void visitCompilationUnit(CompilationUnitTree tree) {
        newIssue(tree, "testIssue");
      }
    };
    testVisitor.analyze(new PHPCheckContext(mock(PhpFile.class), tree, null));

    List<PhpIssue> issues = testVisitor.context().getIssues();

    assertThat(issues).hasSize(1);
    assertThat(issues.get(0)).isInstanceOf(PreciseIssue.class);
    assertThat(((PreciseIssue) issues.get(0)).primaryLocation().message()).isEqualTo("testIssue");
  }

  @Test
  @Timeout(5000)
  void visitingDepthIsLimited() {
    ActionParser<Tree> parser = PHPParserBuilder.createParser();
    PhpFile file = FileTestUtils.getFile(new File("src/test/resources/visitors/long-concat.php"));
    CompilationUnitTree tree = (CompilationUnitTree) parser.parse(file.contents());

    class BinaryExpressionVisitor extends PHPVisitorCheck {
      public int visitedBinaryExpressions = 0;

      @Override
      public void visitBinaryExpression(BinaryExpressionTree tree) {
        visitedBinaryExpressions++;
        super.visitBinaryExpression(tree);
      }
    }

    BinaryExpressionVisitor visitor = new BinaryExpressionVisitor();
    visitor.analyze(file, tree);

    // We subtract 3 as that is the depth to get to the binary expressions.
    assertThat(visitor.visitedBinaryExpressions).isEqualTo(PHPVisitorCheck.MAX_DEPTH - 3);
  }

  @Test
  void defaultScanWithoutParsing() {
    PHPCheck check = new PHPVisitorCheck() {
    };
    assertThat(check.scanWithoutParsing(fileContext)).isTrue();
  }

  @Test
  void overrideScanWithoutParsing() {
    PHPCheck check = new PHPVisitorCheck() {
      @Override
      public boolean scanWithoutParsing(PhpInputFileContext phpInputFileContext) {
        return phpInputFileContext.cacheContext() == cacheContext;
      }
    };
    assertThat(check.scanWithoutParsing(fileContext)).isTrue();
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
    int unionTypesCounter = 0;
    int attributeGroupsCounter = 0;
    int attributesCounter = 0;
    int enumsCounter = 0;
    int enumCasesCounter = 0;
    int callableConvertCounter = 0;
    int intersectionTypeCounter = 0;
    int dnfTypeCounter = 0;
    int dnfIntersectionTypeCounter = 0;
    int propertyHookCounter = 0;

    @Override
    public void visitClassDeclaration(ClassDeclarationTree tree) {
      super.visitClassDeclaration(tree);
      classCounter++;
      if (tree.is(Tree.Kind.ENUM_DECLARATION)) {
        enumsCounter++;
      }
    }

    @Override
    public void visitNamespaceName(NamespaceNameTree tree) {
      super.visitNamespaceName(tree);
      namespaceNameCounter++;
    }

    @Override
    public void visitFunctionCall(FunctionCallTree tree) {
      tree.callee().accept(this);
      scan(tree.callArguments());
    }

    @Override
    public void visitCallableConvert(CallableConvertTree tree) {
      super.visitCallableConvert(tree);
      callableConvertCounter++;
    }

    @Override
    public void visitVariableIdentifier(VariableIdentifierTree tree) {
      super.visitVariableIdentifier(tree);
      varIdentifierCounter++;
    }

    @Override
    public void visitUnionType(UnionTypeTree tree) {
      super.visitUnionType(tree);
      unionTypesCounter++;
    }

    @Override
    public void visitIntersectionType(IntersectionTypeTree tree) {
      super.visitIntersectionType(tree);
      intersectionTypeCounter++;
    }

    @Override
    public void visitDnfType(DnfTypeTree tree) {
      super.visitDnfType(tree);
      dnfTypeCounter++;
    }

    @Override
    public void visitDnfIntersectionType(DnfIntersectionTypeTree tree) {
      super.visitDnfIntersectionType(tree);
      dnfIntersectionTypeCounter++;
    }

    @Override
    public void visitPropertyHook(PropertyHookTree tree) {
      super.visitPropertyHook(tree);
      propertyHookCounter++;
    }

    @Override
    public void visitAttributeGroup(AttributeGroupTree tree) {
      super.visitAttributeGroup(tree);
      attributeGroupsCounter++;
    }

    @Override
    public void visitAttribute(AttributeTree tree) {
      super.visitAttribute(tree);
      attributesCounter++;
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
      super.visitLiteral(tree);
      literalCounter++;
    }

    @Override
    public void visitTrivia(SyntaxTrivia trivia) {
      triviaCounter++;
    }

    @Override
    public void visitEnumCase(EnumCaseTree tree) {
      super.visitEnumCase(tree);
      enumCasesCounter++;
    }
  }

}
