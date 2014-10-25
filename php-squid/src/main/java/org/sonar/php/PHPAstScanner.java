/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * dev@sonar.codehaus.org
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.php;

import com.google.common.base.Charsets;
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.api.Token;
import com.sonar.sslr.impl.Parser;
import org.sonar.php.api.CharsetAwareVisitor;
import org.sonar.php.api.PHPMetric;
import org.sonar.php.metrics.ComplexityVisitor;
import org.sonar.php.parser.PHPGrammar;
import org.sonar.php.parser.PHPParser;
import org.sonar.php.parser.PHPTokenType;
import org.sonar.squidbridge.*;
import org.sonar.squidbridge.api.*;
import org.sonar.squidbridge.indexer.QueryByType;
import org.sonar.squidbridge.metrics.CommentsVisitor;
import org.sonar.squidbridge.metrics.CounterVisitor;
import org.sonar.squidbridge.metrics.LinesOfCodeVisitor;
import org.sonar.squidbridge.metrics.LinesVisitor;
import org.sonar.sslr.parser.LexerlessGrammar;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Collection;

public class PHPAstScanner {

  /**
   * Line Of Code
   */
  public static class PHPLinesOfCodeVisitor extends LinesOfCodeVisitor<LexerlessGrammar> {

    public PHPLinesOfCodeVisitor() {
      super(PHPMetric.LINES_OF_CODE);
    }

    @Override
    public void visitToken(Token token) {
      if (!token.getType().equals(PHPTokenType.INLINE_HTML) && !token.getType().equals(PHPTokenType.FILE_OPENING_TAG)) {
        super.visitToken(token);
      }
    }
  }

  /**
   * Comment
   */
  private static class PHPCommentAnalyser extends CommentAnalyser {
    @Override
    public boolean isBlank(String line) {
      for (int i = 0; i < line.length(); i++) {
        if (Character.isLetterOrDigit(line.charAt(i))) {
          return false;
        }
      }

      return true;
    }

    @Override
    public String getContents(String comment) {
      if (comment.startsWith("//")) {
        return comment.substring(2);
      } else if (comment.startsWith("#")) {
        return comment.substring(1);
      } else {
        return comment.substring(2, comment.length() - 2);
      }
    }
  }

  /**
   * Function
   */
  public static class FunctionSourceCodeBuilderCallback implements SourceCodeBuilderCallback {
    private int seq = 0;

    @Override
    public SourceCode createSourceCode(SourceCode parentSourceCode, AstNode astNode) {
      seq++;
      SourceFunction function = new SourceFunction("function:" + seq);
      function.setStartAtLine(astNode.getTokenLine());
      return function;
    }
  }

  /**
   * Class
   */
  public static class ClassSourceCodeBuilderCallback implements SourceCodeBuilderCallback {

    @Override
    public SourceCode createSourceCode(SourceCode parentSourceCode, AstNode astNode) {
      AstNode id = astNode.getFirstChild(PHPGrammar.IDENTIFIER);
      SourceClass cls = new SourceClass(id.getTokenOriginalValue());
      cls.setStartAtLine(astNode.getTokenLine());
      return cls;
    }
  }

  /**
   * Package
   */
  public static class PackageVisitor<G extends Grammar> extends SquidAstVisitor<G> {

    @Override
    public void visitFile(@Nullable AstNode astNode) {
      SquidAstVisitorContext<G> context = getContext();
      SourceFile sourceFile = (SourceFile) context.peekSourceCode();
      SourceProject sourceProject = sourceFile.getParent(SourceProject.class);
      SourcePackage sourcePackage = findOrCreateSourcePackage(sourceProject, getPackageKey(astNode));
      if (sourcePackage != null) {
        sourceProject.getChildren().remove(sourceFile);
        context.popSourceCode();
        context.addSourceCode(sourcePackage);
        context.addSourceCode(sourceFile);
      }
    }

    private SourcePackage findOrCreateSourcePackage(SourceProject sourceProject, String packageKey) {
      for (SourceCode sourceCode : sourceProject.getChildren()) {
        if (sourceCode instanceof SourcePackage && sourceCode.getKey() == packageKey) {
          return (SourcePackage)sourceCode;
        }
      }
      SourcePackage sourcePackage = new SourcePackage(packageKey);
      sourceProject.addChild(sourcePackage);
      return sourcePackage;
    }

    private String getPackageKey(AstNode astNode) {
      try {
        AstNode id = astNode.getFirstChild(PHPGrammar.SCRIPT).getFirstChild(PHPGrammar.TOP_STATEMENT_LIST);
        for (AstNode node : id.getChildren()) {
          AstNode nsNode = node.getFirstChild(PHPGrammar.NAMESPACE_STATEMENT);
          if (nsNode != null) {
            return getPackageName(nsNode.getFirstChild(PHPGrammar.NAMESPACE_NAME));
          }
        }
      } catch (NullPointerException ignored) {
      }
      return "";
    }

    private String getPackageName(AstNode expr) {
      StringBuilder builder = new StringBuilder();

      for (Token token : expr.getTokens()) {
        builder.append(token.getOriginalValue());
      }

      return builder.toString();
    }
  }

  private PHPAstScanner() {
  }

  /**
   * Helper method for testing checks without having to deploy them on a Sonar instance.
   */
  public static SourceFile scanSingleFile(File file, SquidAstVisitor<LexerlessGrammar>... visitors) {
    if (!file.isFile()) {
      throw new IllegalArgumentException("File '" + file + "' not found.");
    }
    AstScanner<LexerlessGrammar> scanner = create(new PHPConfiguration(Charsets.UTF_8), visitors);
    scanner.scanFile(file);
    Collection<SourceCode> sources = scanner.getIndex().search(new QueryByType(SourceFile.class));
    if (sources.size() != 1) {
      throw new IllegalStateException("Only one SourceFile was expected whereas " + sources.size() + " has been returned.");
    }
    return (SourceFile) sources.iterator().next();
  }

  public static AstScanner<LexerlessGrammar> create(PHPConfiguration conf, SquidAstVisitor<LexerlessGrammar>... visitors) {
    final SquidAstVisitorContextImpl<LexerlessGrammar> context = new SquidAstVisitorContextImpl<LexerlessGrammar>(new SourceProject("PHP Project"));
    final Parser<LexerlessGrammar> parser = PHPParser.create(conf);

    AstScanner.Builder<LexerlessGrammar> builder = new AstScanner.Builder(context).setBaseParser(parser);

    builder.withMetrics(PHPMetric.values());

    /* Comment */
    builder.setCommentAnalyser(new PHPCommentAnalyser());

    /* Files */
    builder.setFilesMetric(PHPMetric.FILES);

    /* Packages */
    builder.withSquidAstVisitor(new PackageVisitor<LexerlessGrammar>());

    /* Classes */
    builder.withSquidAstVisitor(new SourceCodeBuilderVisitor<LexerlessGrammar>(
      new ClassSourceCodeBuilderCallback(),
      PHPGrammar.CLASS_DECLARATION,
      PHPGrammar.INTERFACE_DECLARATION));

    builder.withSquidAstVisitor(CounterVisitor.<LexerlessGrammar>builder().setMetricDef(PHPMetric.CLASSES)
      .subscribeTo(PHPGrammar.CLASS_DECLARATION)
      .subscribeTo(PHPGrammar.INTERFACE_DECLARATION)
      .build());

    /* Functions */
    builder.withSquidAstVisitor(new SourceCodeBuilderVisitor<LexerlessGrammar>(
      new FunctionSourceCodeBuilderCallback(),
      PHPGrammar.METHOD_DECLARATION,
      PHPGrammar.FUNCTION_DECLARATION,
      PHPGrammar.FUNCTION_EXPRESSION));

    builder.withSquidAstVisitor(CounterVisitor.<LexerlessGrammar>builder()
      .setMetricDef(PHPMetric.FUNCTIONS)
      .subscribeTo(PHPGrammar.METHOD_DECLARATION, PHPGrammar.FUNCTION_DECLARATION, PHPGrammar.FUNCTION_EXPRESSION)
      .build());

    /* Metrics */
    builder.withSquidAstVisitor(new LinesVisitor<LexerlessGrammar>(PHPMetric.LINES));
    builder.withSquidAstVisitor(new PHPLinesOfCodeVisitor());

    builder.withSquidAstVisitor(new ComplexityVisitor());
    builder.withSquidAstVisitor(CommentsVisitor.<LexerlessGrammar>builder().withCommentMetric(PHPMetric.COMMENT_LINES)
      .withNoSonar(true)
      .withIgnoreHeaderComment(conf.getIgnoreHeaderComments())
      .build());

    builder.withSquidAstVisitor(CounterVisitor.<LexerlessGrammar>builder()
      .setMetricDef(PHPMetric.STATEMENTS)
      .subscribeTo(
        PHPGrammar.USE_STATEMENT,
        PHPGrammar.NAMESPACE_STATEMENT,
        PHPGrammar.CONSTANT_DECLARATION,
        PHPGrammar.HALT_COMPILER_STATEMENT,
        PHPGrammar.IF_STATEMENT,
        PHPGrammar.ALTERNATIVE_IF_STATEMENT,
        PHPGrammar.FOR_STATEMENT,
        PHPGrammar.FOREACH_STATEMENT,
        PHPGrammar.WHILE_STATEMENT,
        PHPGrammar.DO_WHILE_STATEMENT,
        PHPGrammar.SWITCH_STATEMENT,
        PHPGrammar.BREAK_STATEMENT,
        PHPGrammar.CONTINUE_STATEMENT,
        PHPGrammar.RETURN_STATEMENT,
        PHPGrammar.THROW_STATEMENT,
        PHPGrammar.TRY_STATEMENT,
        PHPGrammar.EMPTY_STATEMENT,
        PHPGrammar.EXPRESSION_STATEMENT,
        PHPGrammar.UNSET_VARIABLE_STATEMENT,
        PHPGrammar.LABEL,
        PHPGrammar.GOTO_STATEMENT,
        PHPGrammar.DECLARE_STATEMENT,
        PHPGrammar.ECHO_STATEMENT,
        PHPGrammar.STATIC_STATEMENT,
        PHPGrammar.YIELD_STATEMENT,
        PHPGrammar.GLOBAL_STATEMENT,
        PHPGrammar.CLASS_VARIABLE_DECLARATION,
        PHPGrammar.CLASS_CONSTANT_DECLARATION,
        PHPGrammar.TRAIT_USE_STATEMENT)
      .build());

    /* External visitors (typically Check ones) */
    for (SquidAstVisitor<LexerlessGrammar> visitor : visitors) {
      if (visitor instanceof CharsetAwareVisitor) {
        ((CharsetAwareVisitor) visitor).setCharset(conf.getCharset());
      }
      builder.withSquidAstVisitor(visitor);
    }

    return builder.build();
  }

}
