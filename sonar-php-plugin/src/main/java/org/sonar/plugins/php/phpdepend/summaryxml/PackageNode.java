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
package org.sonar.plugins.php.phpdepend.summaryxml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.List;

/**
 * The FileNode class represent a Php Depend summary-xml files node.
 * It's used by XStream to marschall or unmarshall xml files.
 */
@XStreamAlias("package")
public class PackageNode {
  @XStreamImplicit
  private List<ClassNode> classes;

  @XStreamImplicit
  private List<FunctionNode> functions;

  @XStreamImplicit
  private List<MethodNode> methods;

  /**
   * Returns ClassNodes in the current PackageNode
   */
  public List<ClassNode> getClasses() {
    return classes;
  }

  /**
   * Returns FunctionNodes in the current PackageNode
   */
  public List<FunctionNode> getFunctions() {
    return functions;
  }
}
