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

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * The PackegeNode class represent a phpdepend metrics node. It's used by XStream to marschall or unmarshall xml files.
 */
@XStreamAlias("package")
public class PackageNode {

  /** The files. */
  @XStreamImplicit
  private List<FileNode> files;

  /**
   * Default constructor with a list of classes.
   * 
   * @param files
   */
  public PackageNode(List<FileNode> files) {
    super();
    this.files = files;
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
