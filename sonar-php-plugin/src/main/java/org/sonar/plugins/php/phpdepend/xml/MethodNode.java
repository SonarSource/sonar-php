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

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * The MethoNode class represent a phpdepend function node. It's used by XStream to marschall or unmarshall xml files.
 */
@XStreamAlias("method")
public final class MethodNode {

  /** The complexity. */
  @XStreamAsAttribute
  @XStreamAlias("ccn")
  private double complexity;

  /**
   * Instantiates a new method node.
   * 
   * @param complexity
   *          the complexity
   */
  public MethodNode(final double complexity) {
    super();
    this.complexity = complexity;
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
   * Sets the complexity.
   * 
   * @param complexity
   *          the new complexity
   */
  public void setComplexity(final double complexity) {
    this.complexity = complexity;
  }
}
