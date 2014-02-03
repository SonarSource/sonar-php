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
package org.sonar.plugins.php.phpunit.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * The Class MetricsNode.
 */
public final class MetricsNode {

  /** The The number of covered statements. */
  @XStreamAsAttribute
  @XStreamAlias("coveredstatements")
  private double coveredStatements;

  /** The total statements count. */
  @XStreamAsAttribute
  @XStreamAlias("statements")
  private double totalStatementsCount;

  /**
   * Instantiates a new metrics node.
   *
   * @param totalStatementsCount
   *          the total statements count
   * @param coveredstatements
   *          the coveredstatements
   */
  public MetricsNode(double totalStatementsCount, double coveredstatements) {
    super();
    this.totalStatementsCount = totalStatementsCount;
    this.coveredStatements = coveredstatements;
  }

  /**
   * Gets the coveredstatements.
   *
   * @return the coveredstatements
   */
  public double getCoveredStatements() {
    return coveredStatements;
  }

  /**
   * Gets the total statements count.
   *
   * @return the total statements count
   */
  public double getTotalStatementsCount() {
    return totalStatementsCount;
  }
}
