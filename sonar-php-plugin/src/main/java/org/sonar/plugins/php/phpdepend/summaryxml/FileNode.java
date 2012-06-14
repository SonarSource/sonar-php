/*
 * Sonar PHP Plugin
 * Copyright (C) 2010 Codehaus Sonar Plugins
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
package org.sonar.plugins.php.phpdepend.summaryxml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * The FileNode class represent a Php Depend summary-xml files node.
 * It's used by XStream to marschall or unmarshall xml files.
 */
@XStreamAlias("file")
public final class FileNode {
  /** The file name. */
  @XStreamAsAttribute
  @XStreamAlias("name")
  private String fileName;

  /** The lines number. */
  @XStreamAsAttribute
  @XStreamAlias("loc")
  private double linesNumber;

  /** The lines number. */
  @XStreamAsAttribute
  @XStreamAlias("eloc")
  private double executableLinesNumber;

  /** The comment line number. */
  @XStreamAsAttribute
  @XStreamAlias("cloc")
  private double commentLineNumber;

  private int classNumber = 0;

  private int methodNumber = 0;

  private int functionNumber = 0;

  /**
   * Returns filename of the current FileNode
   */
  public String getFileName() {
    return fileName;
  }

  /**
   * Returns number of lines of code (loc) in the current FileNode
   */
  public double getLinesNumber()
  {
    return linesNumber;
  }

  /**
   * Returns number of executable lines of code (eloc) in the current FileNode
   */
  public double getExecutableLinesNumber()
  {
    return executableLinesNumber;
  }

  /**
   * Returns number of comment lines (cloc) in the current FileNode
   */
  public double getCommentLinesNumber()
  {
    return commentLineNumber;
  }

  /**
   * Increases number of classes in the current FileNode by the given number
   *
   * int classNumber Number of classes
   */
  public void increaseClassNumber(int classNumber) {
    this.classNumber = this.classNumber + classNumber;
  }

  /**
   * Increases number of classes in the current FileNode by 1
   */
  public void increaseClassNumber() {
    this.classNumber++;
  }

  /**
   * Returns number of classes in the current FileNode
   */
  public int getClassNumber() {
    return classNumber;
  }

  /**
   * Increases number of methods in the current FileNode by the given number
   *
   * int methodNumber Number of methods
   */
  public void increaseMethodNumber(int methodNumber) {
    this.methodNumber = this.methodNumber + methodNumber;
  }

  /**
   * Increases number of methods in the current FileNode by 1
   */
  public void increaseMethodNumber() {
    this.methodNumber++;
  }

  /**
   * Returns number of methods in the current FileNode
   */
  public int getMethodNumber() {
    return methodNumber;
  }

  /**
   * Increases number of functions in the current FileNode by the given number
   *
   * int functionNumber Number of functions
   */
  public void increaseFunctionNumber(int functionNumber) {
    this.functionNumber = this.functionNumber + functionNumber;
  }

  /**
   * Increases number of functions in the current FileNode by 1
   */
  public void increaseFunctionNumber() {
    this.functionNumber++;
  }

  /**
   * Returns number of functions in the current FileNode
   */
  public int getFunctionNumber() {
    return functionNumber;
  }
}
