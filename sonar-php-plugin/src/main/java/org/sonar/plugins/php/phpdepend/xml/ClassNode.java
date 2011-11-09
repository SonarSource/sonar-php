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
 * The Class representing a phpdepend report file class node.
 */
@XStreamAlias("class")
public final class ClassNode {

  /** The depth in tree line number. */
  @XStreamAsAttribute
  @XStreamAlias("dit")
  private double depthInTreeNumber;

  /** The code lines number. */
  @XStreamAsAttribute
  @XStreamAlias("ncloc")
  private double codeLinesNumber;

  /** The lines number. */
  @XStreamAsAttribute
  @XStreamAlias("loc")
  private double linesNumber;

  /** The comment line number. */
  @XStreamAsAttribute
  @XStreamAlias("cloc")
  private double commentLineNumber;

  /** The complexity. */
  @XStreamAsAttribute
  @XStreamAlias("wmc")
  private double complexity;

  /** The method number. */
  @XStreamAsAttribute
  @XStreamAlias("nom")
  private double methodNumber;

  /** The number of children classes number. */
  @XStreamAsAttribute
  @XStreamAlias("nocc")
  private double numberOfChildrenClassesNumber;

  /** The methodes. */
  @XStreamImplicit
  private List<MethodNode> methodes;

  /**
   * Instantiates a new class node.
   * 
   * @param codeLinesNumber
   *          the code lines number
   * @param linesNumber
   *          the lines number
   * @param complexity
   *          the complexity
   * @param methodNumber
   *          the method number
   * @param methodes
   *          the methodes
   */
  public ClassNode(final double codeLinesNumber, final double commentLineNumber, final double linesNumber, final double complexity,
      final double methodNumber, final double depthInTreeNumber, final List<MethodNode> methodes) {
    super();
    this.codeLinesNumber = codeLinesNumber;
    this.commentLineNumber = commentLineNumber;
    this.complexity = complexity;
    this.methodNumber = methodNumber;
    this.methodes = methodes;
    this.linesNumber = linesNumber;
    this.depthInTreeNumber = depthInTreeNumber;
  }

  /**
   * Adds the method.
   * 
   * @param methode
   *          the methode
   */
  public void addMethod(final MethodNode methode) {
    if (methodes == null) {
      methodes = new ArrayList<MethodNode>();
    }
    methodes.add(methode);
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
   * Gets the complexity.
   * 
   * @return the complexity
   */
  public double getComplexity() {
    return complexity;
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
   * Gets the methodes.
   * 
   * @return the methodes
   */
  public List<MethodNode> getMethodes() {
    return methodes;
  }

  /**
   * Gets the method number.
   * 
   * @return the method number
   */
  public double getMethodNumber() {
    return methodNumber;
  }

  /**
   * Gets the depth in ree
   * 
   * @return the method number
   */
  public double getDepthInTreeNumber() {
    return depthInTreeNumber;
  }

  /**
   * Sets the classes.
   * 
   * @param methodes
   *          the new classes
   */
  public void setClasses(final List<MethodNode> methodes) {
    this.methodes = methodes;
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
   * Sets the complexity.
   * 
   * @param complexity
   *          the new complexity
   */
  public void setComplexity(final double complexity) {
    this.complexity = complexity;
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
   * Sets the method number.
   * 
   * @param methodNumber
   *          the new method number
   */
  public void setMethodNumber(final double methodNumber) {
    this.methodNumber = methodNumber;
  }

  /**
   * Sets the method number.
   * 
   * @param depthInTreeNumber
   *          the depth in tree number
   */
  public void setDepthInTreeNumber(final double depthInTreeNumber) {
    this.depthInTreeNumber = depthInTreeNumber;
  }

  /**
   * @param numberOfChildrenClassesNumber
   *          the numberOfChildrenClassesNumber to set
   */
  public void setNumberOfChildrenClassesNumber(double numberOfChildrenClassesNumber) {
    this.numberOfChildrenClassesNumber = numberOfChildrenClassesNumber;
  }

  /**
   * @return the numberOfChildrenClassesNumber
   */
  public double getNumberOfChildrenClassesNumber() {
    return numberOfChildrenClassesNumber;
  }
}
