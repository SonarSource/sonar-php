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
package org.sonar.php.metrics;

import org.sonar.api.ce.measure.RangeDistributionBuilder;

public class FileMeasures {
  private int functionNumber;
  private int classNumber;
  private int statementNumber;
  private int classComplexity;
  private int functionComplexity;
  private int fileComplexity;
  private int fileCognitiveComplexity;

  private int linesOfCodeNumber;
  private int commentLinesNumber;

  private RangeDistributionBuilder functionComplexityDistribution;

  private RangeDistributionBuilder fileComplexityDistribution;

  public FileMeasures(Number[] limitsComplexityFunctions, Number[] filesDistributionBottomLimits) {
    functionNumber = 0;
    classNumber = 0;
    statementNumber = 0;
    classComplexity = 0;
    functionComplexity = 0;
    functionComplexityDistribution = new RangeDistributionBuilder(limitsComplexityFunctions);
    fileComplexityDistribution = new RangeDistributionBuilder(filesDistributionBottomLimits);
  }

  public int getCommentLinesNumber() {
    return commentLinesNumber;
  }

  public void setCommentLinesNumber(int commentLinesNumber) {
    this.commentLinesNumber = commentLinesNumber;
  }

  public int getLinesOfCodeNumber() {
    return linesOfCodeNumber;
  }

  public void setLinesOfCodeNumber(int linesOfCodeNumber) {
    this.linesOfCodeNumber = linesOfCodeNumber;
  }

  public int getFileComplexity() {
    return fileComplexity;
  }

  public void setFileComplexity(int fileComplexity) {
    this.fileComplexity = fileComplexity;
    fileComplexityDistribution.add(fileComplexity);
  }

  public RangeDistributionBuilder getFileComplexityDistribution() {
    return fileComplexityDistribution;
  }

  public RangeDistributionBuilder getFunctionComplexityDistribution() {
    return functionComplexityDistribution;
  }

  public int getFileCognitiveComplexity() {
    return fileCognitiveComplexity;
  }

  public void setFileCognitiveComplexity(int fileCognitiveComplexity) {
    this.fileCognitiveComplexity = fileCognitiveComplexity;
  }

  public int getFunctionComplexity() {
    return functionComplexity;
  }

  public int getClassComplexity() {
    return classComplexity;
  }

  public int getClassNumber() {
    return classNumber;
  }

  public int getStatementNumber() {
    return statementNumber;
  }

  public int getFunctionNumber() {
    return functionNumber;
  }

  public void setFunctionNumber(int functionNumber) {
    this.functionNumber = functionNumber;
  }

  public void setStatementNumber(int statementNumber) {
    this.statementNumber = statementNumber;
  }

  public void setClassNumber(int classNumber) {
    this.classNumber = classNumber;
  }

  public void addClassComplexity(int classComplexity) {
    this.classComplexity += classComplexity;
  }

  public void addFunctionComplexity(int functionComplexity) {
    this.functionComplexity += functionComplexity;
    functionComplexityDistribution.add(functionComplexity);
  }

}
