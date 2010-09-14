/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2010 SQLi
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */

package org.sonar.plugins.php.phpunit.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * The Class MetricsNode.
 */
public class MetricsNode {

  /** The The number of covered elements. */
  @XStreamAsAttribute
  @XStreamAlias("coveredelements")
  private int coveredElements;

  /** The number of covered methods. */
  @XStreamAsAttribute
  @XStreamAlias("coveredmethods")
  private int coveredMethods;

  /** The The number of covered statements. */
  @XStreamAsAttribute
  @XStreamAlias("coveredstatements")
  private int coveredStatements;

  /** The total elements count. */
  @XStreamAsAttribute
  @XStreamAlias("elements")
  private int totalElementsCount;

  /** The total methods count. */
  @XStreamAsAttribute
  @XStreamAlias("methods")
  private int totalMethodsCount;

  /** The total statements count. */
  @XStreamAsAttribute
  @XStreamAlias("statements")
  private int totalStatementsCount;

  /**
   * Instantiates a new metrics node.
   * 
   * @param totalMethodsCount
   *          the total methods count
   * @param coveredmethods
   *          the coveredMethods
   * @param totalStatementsCount
   *          the total statements count
   * @param coveredstatements
   *          the coveredstatements
   */
  public MetricsNode(int totalMethodsCount, int coveredmethods, int totalStatementsCount, int coveredstatements, int totalElementsCount,
      int coveredElements) {
    super();
    this.totalMethodsCount = totalMethodsCount;
    this.coveredMethods = coveredmethods;
    this.totalStatementsCount = totalStatementsCount;
    this.coveredStatements = coveredstatements;
    this.totalElementsCount = totalElementsCount;
    this.coveredElements = coveredElements;
  }

  public int getCoveredElements() {
    return coveredElements;
  }

  /**
   * Gets the coveredmethods.
   * 
   * @return the coveredmethods
   */
  public int getCoveredMethods() {
    return coveredMethods;
  }

  /**
   * Gets the coveredstatements.
   * 
   * @return the coveredstatements
   */
  public int getCoveredStatements() {
    return coveredStatements;
  }

  public int getTotalElementsCount() {
    return totalElementsCount;
  }

  /**
   * Gets the total methods count.
   * 
   * @return the total methods count
   */
  public int getTotalMethodsCount() {
    return totalMethodsCount;
  }

  /**
   * Gets the total statements count.
   * 
   * @return the total statements count
   */
  public int getTotalStatementsCount() {
    return totalStatementsCount;
  }

  public void setCoveredElements(int coveredElements) {
    this.coveredElements = coveredElements;
  }

  /**
   * Sets the coveredmethods.
   * 
   * @param coveredmethods
   *          the new coveredmethods
   */
  public void setCoveredMethods(int coveredmethods) {
    this.coveredMethods = coveredmethods;
  }

  /**
   * Sets the coveredstatements.
   * 
   * @param coveredstatements
   *          the new coveredstatements
   */
  public void setCoveredStatements(int coveredstatements) {
    this.coveredStatements = coveredstatements;
  }

  public void setTotalElementsCount(int totalElementsCount) {
    this.totalElementsCount = totalElementsCount;
  }

  /**
   * Sets the total methods count.
   * 
   * @param totalMethodsCount
   *          the new total methods count
   */
  public void setTotalMethodsCount(int totalMethodsCount) {
    this.totalMethodsCount = totalMethodsCount;
  }

  /**
   * Sets the total statements count.
   * 
   * @param totalStatementsCount
   *          the new total statements count
   */
  public void setTotalStatementsCount(int totalStatementsCount) {
    this.totalStatementsCount = totalStatementsCount;
  }
}
