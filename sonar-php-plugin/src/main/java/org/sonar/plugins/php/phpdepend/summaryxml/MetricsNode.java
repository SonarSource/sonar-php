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
 * The MetricsNode class represent a Php Depend summary-xml metrics node.
 * It's used by XStream to marschall or unmarshall xml files.
 */
@XStreamAlias("metrics")
public final class MetricsNode {
  @XStreamAlias("files")
  private List<FileNode> files;

  @XStreamImplicit
  private List<PackageNode> packages;

  public List<FileNode> getFiles() {
    return files;
  }

  /**
   * Matches metrics in packages with the global file nodes
   */
  public void findMatchingMetrics() {
    for (PackageNode packageNode : packages) {
      List<ClassNode> classes = packageNode.getClasses();
      if (classes != null) {
        for (ClassNode classNode : classes) {
          FileNode fileNode = getFileNodeByFileName(classNode.getFile().getFileName());
          if (fileNode == null) {
            continue;
          }
          fileNode.increaseClassNumber();
          List<MethodNode> methods = classNode.getMethods();
          if (methods != null) {
            fileNode.increaseMethodNumber(methods.size());
          }
        }
      }
      List<FunctionNode> functions = packageNode.getFunctions();
      if (functions != null) {
        for (FunctionNode functionNode : functions) {
          FileNode fileNode = getFileNodeByFileName(functionNode.getFile().getFileName());
          if (fileNode == null) {
            continue;
          }
          fileNode.increaseFunctionNumber();
        }
      }
    }
  }

  /**
   * Searches through global file list in the xml report for a specific file
   *
   * @param filename
   */
  private FileNode getFileNodeByFileName(String filename) {
    for (FileNode fileNode : files) {
      if (fileNode.getFileName().equals(filename)) {
        return fileNode;
      }
    }

    return null;
  }
}
