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

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * The Class LineNode.
 */
@XStreamAlias("line")
public class LineNode {

  /** The Constant METHODE_TYPE. */
  public static final String METHODE_TYPE = "method";

  /** The Constant STATEMENT_TYPE. */
  public static final String STATEMENT_TYPE = "stmt";

  /** The count. */
  @XStreamAsAttribute
  private int count;

  /** The num. */
  @XStreamAsAttribute
  private int num;

  /** The type. */
  @XStreamAsAttribute
  private String type;

  /**
   * Instantiates a new line node.
   * 
   * @param num
   *          the num
   * @param type
   *          the type
   * @param count
   *          the count
   */
  public LineNode(int num, String type, int count) {
    super();
    this.num = num;
    this.type = type;
    this.count = count;
  }

  /**
   * Gets the count.
   * 
   * @return the count
   */
  public int getCount() {
    return count;
  }

  /**
   * Gets the num.
   * 
   * @return the num
   */
  public int getNum() {
    return num;
  }

  /**
   * Gets the type.
   * 
   * @return the type
   */
  public String getType() {
    return type;
  }

  /**
   * Sets the count.
   * 
   * @param count
   *          the new count
   */
  public void setCount(int count) {
    this.count = count;
  }

  /**
   * Sets the num.
   * 
   * @param num
   *          the new num
   */
  public void setNum(int num) {
    this.num = num;
  }

  /**
   * Sets the type.
   * 
   * @param type
   *          the new type
   */
  public void setType(String type) {
    this.type = type;
  }
}
