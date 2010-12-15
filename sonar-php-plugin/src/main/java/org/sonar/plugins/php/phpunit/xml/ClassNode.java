/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2010 Akram Ben Aissi
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
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * The Class ClassNode.
 */
@XStreamAlias("class")
public class ClassNode {

  /** The ignored node. */
  @XStreamOmitField
  @XStreamAlias("metrics")
  private Object ignoredNode;

  /**
   * @return the ignoredNode
   */
  public final Object getIgnoredNode() {
    return ignoredNode;
  }

  /**
   * @param ignoredNode
   *          the ignoredNode to set
   */
  public final void setIgnoredNode(Object ignoredNode) {
    this.ignoredNode = ignoredNode;
  }

}
