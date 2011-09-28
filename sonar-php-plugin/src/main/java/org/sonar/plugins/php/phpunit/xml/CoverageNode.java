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
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * The php unit report root node..
 */
@XStreamAlias("coverage")
public class CoverageNode {

  /** The projects. */
  @XStreamImplicit
  @XStreamAlias("project")
  private List<ProjectNode> projects;

  /**
   * Instantiates a new coverage node.
   * 
   * @param projects
   *          the projects
   */
  public CoverageNode(List<ProjectNode> projects) {
    super();
    this.projects = projects;
  }

  /**
   * Gets the projects.
   * 
   * @return the projects
   */
  public List<ProjectNode> getProjects() {
    return projects;
  }

  /**
   * Sets the projects.
   * 
   * @param projects
   *          the new projects
   */
  public void setProjects(List<ProjectNode> projects) {
    this.projects = projects;
  }

}
