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
package org.sonar.plugins.php.phpdepend.xml;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * The FileNode class represent a phpdepend file node. It's used by XStream to marschall or unmarshall xml files.
 */
@XStreamAlias("file")
public final class FileNode {

  /** The file name. */
  @XStreamAsAttribute
  @XStreamAlias("name")
  private String fileName;

  /** The class number. */
  @XStreamAsAttribute
  @XStreamAlias("classes")
  private double classNumber;

  /** The comment line number. */
  @XStreamAsAttribute
  @XStreamAlias("cloc")
  private double commentLineNumber;

  /** The functions number. */
  @XStreamAsAttribute
  @XStreamAlias("functions")
  private double functionsNumber;

  /** The lines number. */
  @XStreamAsAttribute
  @XStreamAlias("loc")
  private double linesNumber;

  /** The code lines number. */
  @XStreamAsAttribute
  @XStreamAlias("locExecutable")
  private double codeLinesNumber;

  /** The classes. */
  @XStreamImplicit
  private List<ClassNode> classes;

  /** The classes. */
  @XStreamImplicit
  private List<FunctionNode> functions;

  /**
   * Instantiates a new file node.
   * 
   * @param fileName
   *          the file name
   * @param classNumber
   *          the class number
   * @param commentLineNumber
   *          the comment line number
   * @param linesNumber
   *          the lines number
   * @param codeLinesNumber
   *          the code lines number
   * @param classes
   *          the classes
   */
  public FileNode(final String fileName, final double classNumber, final double commentLineNumber, final double linesNumber,
      final double codeLinesNumber, final List<ClassNode> classes) {
    super();
    this.fileName = fileName;
    this.classNumber = classNumber;
    this.commentLineNumber = commentLineNumber;
    this.linesNumber = linesNumber;
    this.codeLinesNumber = codeLinesNumber;
    this.classes = classes;
  }

  /**
   * Adds the class.
   * 
   * @param classNode
   *          the class node
   */
  public void addClass(final ClassNode classNode) {
    if (classes == null) {
      classes = new ArrayList<ClassNode>();
    }
    classes.add(classNode);
  }

  /**
   * Adds the function.
   * 
   * @param classNode
   *          the class node
   */
  public void addClass(final FunctionNode functionNode) {
    if (functions == null) {
      functions = new ArrayList<FunctionNode>();
    }
    functions.add(functionNode);
  }

  /**
   * Gets the classes.
   * 
   * @return the classes
   */
  public List<ClassNode> getClasses() {
    return classes;
  }

  /**
   * Gets the functions.
   * 
   * @return the functions
   */
  public List<FunctionNode> getFunctions() {
    return functions;
  }

  /**
   * Gets the class number.
   * 
   * @return the class number
   */
  public double getClassNumber() {
    return classNumber;
  }

  /**
   * Gets the code lines number.
   * 
   * @return the code lines number
   */
  public double getCodeLinesNumber() {
    return codeLinesNumber;
  }

  /**
   * Gets the comment line number.
   * 
   * @return the comment line number
   */
  public double getCommentLineNumber() {
    return commentLineNumber;
  }

  /**
   * Gets the file name.
   * 
   * @return the file name
   */
  public String getFileName() {
    return fileName;
  }

  /**
   * Gets the lines number.
   * 
   * @return the lines number
   */
  public double getLinesNumber() {
    return linesNumber;
  }

  /**
   * Sets the classes.
   * 
   * @param classes
   *          the new classes
   */
  public void setClasses(final List<ClassNode> classes) {
    this.classes = classes;
  }

  /**
   * Sets the functions.
   * 
   * @param functions
   *          the new functions
   */
  public void setFunctions(final List<FunctionNode> functions) {
    this.functions = functions;
  }

  /**
   * Sets the class number.
   * 
   * @param classNumber
   *          the new class number
   */
  public void setClassNumber(final double classNumber) {
    this.classNumber = classNumber;
  }

  /**
   * Sets the code lines number.
   * 
   * @param codeLinesNumber
   *          the new code lines number
   */
  public void setCodeLinesNumber(final double codeLinesNumber) {
    this.codeLinesNumber = codeLinesNumber;
  }

  /**
   * Sets the comment line number.
   * 
   * @param commentLineNumber
   *          the new comment line number
   */
  public void setCommentLineNumber(final double commentLineNumber) {
    this.commentLineNumber = commentLineNumber;
  }

  /**
   * Sets the file name.
   * 
   * @param fileName
   *          the new file name
   */
  public void setFileName(final String fileName) {
    this.fileName = fileName;
  }

  /**
   * Sets the lines number.
   * 
   * @param linesNumber
   *          the new lines number
   */
  public void setLinesNumber(final double linesNumber) {
    this.linesNumber = linesNumber;
  }

  /**
   * Sets functions number.
   * 
   * @param functionsNumber
   *          the functionsNumber to set
   */
  public void setFunctionsNumber(double functionsNumber) {
    this.functionsNumber = functionsNumber;
  }

  /**
   * Returns functions number
   * 
   * @return the functionsNumber
   */
  public double getFunctionsNumber() {
    return functionsNumber;
  }

}
