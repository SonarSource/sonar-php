/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2017 SonarSource SA
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
package org.sonar.plugins.php.phpunit.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import java.util.List;

/**
 * The Class FileNode.
 */
@XStreamAlias("file")
public class FileNode {

  /**
   * The name.
   */
  @XStreamAsAttribute
  private String name;

  /**
   * The lines.
   */
  @XStreamImplicit(itemFieldName = "line")
  private List<LineNode> lines;

  /**
   * The ignored nodes.
   */
  @XStreamOmitField
  @XStreamImplicit(itemFieldName = "class")
  private List<ClassNode> ignoredNodes;

  /**
   * The metrics.
   */
  @XStreamAlias("metrics")
  private MetricsNode metrics;


  /**
   * Gets the lines.
   *
   * @return the lines
   */
  public List<LineNode> getLines() {
    return lines;
  }

  /**
   * Gets the metrics.
   *
   * @return the metrics
   */
  public MetricsNode getMetrics() {
    return metrics;
  }

  /**
   * Sets the metrics.
   *
   * @param metrics the new metrics
   */
  public void setMetrics(MetricsNode metrics) {
    this.metrics = metrics;
  }

  /**
   * Gets the name.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name.
   *
   * @param name the new name
   */
  public void setName(String name) {
    this.name = name;
  }
}
