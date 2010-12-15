/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2010 SQLi
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

/**
 * 
 */
package org.sonar.plugins.php.cpd.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * Model a <file> tag in the pmd-cpd XML file.
 * 
 * @author akram
 * 
 */
@XStreamAlias("file")
public class FileNode {

  /**
   * The for the file containing duplication;
   */
  @XStreamAlias("path")
  @XStreamAsAttribute
  private String path;
  /**
   * The line number containing duplication.
   */
  @XStreamAlias("line")
  @XStreamAsAttribute
  private Double lineNumber;

  /**
   * @return the path
   */
  public String getPath() {
    return path;
  }

  /**
   * @return the lineNumber
   */
  public Double getLineNumber() {
    return lineNumber;
  }

}
