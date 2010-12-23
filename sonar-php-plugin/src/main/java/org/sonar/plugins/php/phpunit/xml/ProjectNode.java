/*
 * Sonar PHP Plugin
 * Copyright (C) 2010 Akram Ben Aissi
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

/**
 * The ProjectNode represent the analyzed project in the PhpUnit coverage report file.
 */
@XStreamAlias("project")
public class ProjectNode {

  /** The project files. */
  @XStreamImplicit
  @XStreamAlias("file")
  private List<FileNode> files;

  /** The project name. */
  @XStreamAsAttribute
  private String name;

  /** The project metrics. */
  @XStreamAlias("metrics")
  private MetricsNode metrics;

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
   * Instantiates a new project node.
   * 
   * @param files
   *          the files
   * @param name
   *          the name
   * @param metrics
   *          the metrics
   */
  public ProjectNode(List<FileNode> files, String name, MetricsNode metrics) {
    super();
    this.files = files;
    this.name = name;
    this.metrics = metrics;
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

  /**
   * Gets the files.
   * 
   * @return the files
   */
  public List<FileNode> getFiles() {
    return files;
  }

  /**
   * Sets the files.
   * 
   * @param files
   *          the new files
   */
  public void setFiles(List<FileNode> files) {
    this.files = files;
  }
}
