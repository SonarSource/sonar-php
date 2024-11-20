/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.php.metrics;

public class FileMeasures {
  private int functionNumber = 0;
  private int classNumber = 0;
  private int statementNumber = 0;
  private int fileComplexity;
  private int fileCognitiveComplexity;

  private int linesOfCodeNumber;
  private int commentLinesNumber;

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
  }

  public int getFileCognitiveComplexity() {
    return fileCognitiveComplexity;
  }

  public void setFileCognitiveComplexity(int fileCognitiveComplexity) {
    this.fileCognitiveComplexity = fileCognitiveComplexity;
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

}
