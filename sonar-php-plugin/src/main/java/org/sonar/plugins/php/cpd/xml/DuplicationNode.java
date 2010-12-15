/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2010 EchoSource
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

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * Model the <duplication> tag inside the pmd-cpd XML file.
 * 
 * @author akram
 * 
 */
@XStreamAlias("duplication")
public class DuplicationNode {

  /**
   * The number of duplicated lines.
   * 
   */
  @XStreamAlias("lines")
  @XStreamAsAttribute
  private Double lines;

  /**
   * The number of duplicated tokens.
   * 
   */
  @XStreamAlias("tokens")
  @XStreamAsAttribute
  private Double tokens;

  /** The files containing duplication */
  @XStreamImplicit
  @XStreamAlias("file")
  private List<FileNode> files;

  /**
   * The duplicated code fragment.
   */
  @XStreamAlias("codefragment")
  String codeFragment;

  /**
   * @return the files
   */
  public List<FileNode> getFiles() {
    return files;
  }

  /**
   * @return the lines
   */
  public Double getLines() {
    return lines;
  }

  /**
   * @return the tokens
   */
  public Double getTokens() {
    return tokens;
  }

  /**
   * @return the codeFragment
   */
  public String getCodeFragment() {
    return codeFragment;
  }

}
