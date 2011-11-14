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

/**
 * The PackageNode represents the analyzed packages in the PhpUnit coverage report file.
 */
@XStreamAlias("project")
public class PackageNode {

  /** The project files. */
  @XStreamImplicit
  @XStreamAlias("file")
  private List<FileNode> files;

  /** The project name. */
  @XStreamAsAttribute
  private String name;

  /**
   * Instantiates a new package node.
   * 
   * @param files
   *          the files
   * @param name
   *          the name
   */
  public PackageNode(List<FileNode> files, String name) {
    super();
    this.files = files;
    this.name = name;
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
