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

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * The Class FileNode.
 */
@XStreamAlias("file")
public class FileNode {

  /** The lines. */
  @XStreamImplicit(itemFieldName = "line")
  private List<LineNode> lines;

  /** The ignored nodes. */
  @XStreamOmitField
  @XStreamImplicit(itemFieldName = "class")
  private List<ClassNode> ignoredNodes;
  /** The metrics. */
  @XStreamAlias("metrics")
  private MetricsNode metrics;

  /**
   * Instantiates a new file node.
   * 
   * @param lines
   *          the lines
   * @param metrics
   *          the metrics
   * @param name
   *          the name
   */
  public FileNode(List<LineNode> lines, MetricsNode metrics, String name) {
    super();
    this.lines = lines;
    this.metrics = metrics;
    this.name = name;
  }

  /** The name. */
  @XStreamAsAttribute
  private String name;

  /**
   * Gets the lines.
   * 
   * @return the lines
   */
  public List<LineNode> getLines() {
    return lines;
  }

  /**
   * Sets the lines.
   * 
   * @param lines
   *          the new lines
   */
  public void setLines(List<LineNode> lines) {
    this.lines = lines;
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
   * @param metrics
   *          the new metrics
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
   * @return the ignoredNodes
   */
  public final List<ClassNode> getIgnoredNodes() {
    return ignoredNodes;
  }

  /**
   * @param ignoredNodes
   *          the ignoredNodes to set
   */
  public final void setIgnoredNodes(List<ClassNode> ignoredNodes) {
    this.ignoredNodes = ignoredNodes;
  }

  /**
   * Sets the name.
   * 
   * @param name
   *          the new name
   */
  public void setName(String name) {
    this.name = name;
  }
}
