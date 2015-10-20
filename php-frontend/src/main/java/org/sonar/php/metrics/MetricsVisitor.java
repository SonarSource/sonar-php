/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * sonarqube@googlegroups.com
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
package org.sonar.php.metrics;

import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.visitors.PHPSubscriptionCheck;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class MetricsVisitor extends PHPSubscriptionCheck {

  private static final Number[] LIMITS_COMPLEXITY_FUNCTIONS = {1, 2, 4, 6, 8, 10, 12, 20, 30};
  private static final Number[] FILES_DISTRIBUTION_BOTTOM_LIMITS = {0, 5, 10, 20, 30, 60, 90};

  public static final Kind[] FUNCTION_NODES = {
    Kind.FUNCTION_DECLARATION,
    Kind.FUNCTION_EXPRESSION,
    Kind.METHOD_DECLARATION,
  };

  public static final Kind[] CLASS_NODES = {
    Kind.CLASS_DECLARATION,
    Kind.INTERFACE_DECLARATION,
    Kind.TRAIT_DECLARATION
  };


  private FileMeasures fileMeasures;
  private FileLinesContext fileLinesContext;

  @Override
  public List<Kind> nodesToVisit() {
    List<Kind> result = new ArrayList<>(Arrays.asList(FUNCTION_NODES));
    result.addAll(Arrays.asList(CLASS_NODES));
    result.add(Kind.COMPILATION_UNIT);
    return result;
  }

  @Override
  public void visitNode(Tree tree) {
    if (tree.is(Kind.COMPILATION_UNIT)) {
      fileMeasures.setFileComplexity(NewComplexityVisitor.complexity(tree));

    } else if (tree.is(Kind.CLASS_DECLARATION)) {
      fileMeasures.addClassComplexity(NewComplexityVisitor.complexity(tree));

    } else if (tree.is(FUNCTION_NODES)) {
      fileMeasures.addFunctionComplexity(NewComplexityVisitor.complexityWithoutNestedFunctions(tree));
    }
  }

  public FileMeasures getFileMeasures(File file, CompilationUnitTree tree, FileLinesContext fileLinesContext) {
    this.fileMeasures = new FileMeasures(LIMITS_COMPLEXITY_FUNCTIONS, FILES_DISTRIBUTION_BOTTOM_LIMITS);
    this.fileLinesContext = fileLinesContext;

    super.analyze(file, tree);

    setCounterMeasures();
    setLineAndCommentMeasures();
    return this.fileMeasures;
  }

  private void setCounterMeasures() {
    CounterVisitor counter = new CounterVisitor(context().tree());
    fileMeasures.setClassNumber(counter.getClassNumber());
    fileMeasures.setFunctionNumber(counter.getFunctionNumber());
    fileMeasures.setStatementNumber(counter.getStatementNumber());
  }

  private void setLineAndCommentMeasures() {
    LineVisitor lineVisitor = new LineVisitor(context().tree());
    CommentLineVisitor commentVisitor = new CommentLineVisitor(context().tree());

    int linesNumber = lineVisitor.getLinesNumber();

    fileMeasures.setLinesNumber(linesNumber);
    fileMeasures.setLinesOfCodeNumber(lineVisitor.getLinesOfCodeNumber());
    fileMeasures.setCommentLinesNumber(commentVisitor.getCommentLineNumber());
    fileMeasures.setNoSonarLines(commentVisitor.getNoSonarLines());

    Set<Integer> linesOfCode = lineVisitor.getLinesOfCode();
    Set<Integer> commentLines = commentVisitor.getCommentLines();

    for (int line = 1; line <= linesNumber; line++) {
      fileLinesContext.setIntValue(CoreMetrics.NCLOC_DATA_KEY, line, linesOfCode.contains(line) ? 1 : 0);
      fileLinesContext.setIntValue(CoreMetrics.COMMENT_LINES_DATA_KEY, line, commentLines.contains(line) ? 1 : 0);
    }

    // fixme (is it compliant with our idea of separate logic?)
    fileLinesContext.save();
  }

}
