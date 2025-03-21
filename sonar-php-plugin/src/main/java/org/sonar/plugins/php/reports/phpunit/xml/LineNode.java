/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.plugins.php.reports.phpunit.xml;

/**
 * The Class LineNode.
 */
public class LineNode {

  /**
   * The count.
   */
  private int count;

  /**
   * The num.
   */
  private int num;

  /**
   * The type.
   */
  private String type;

  public LineNode(int count, int num, String type) {
    this.count = count;
    this.num = num;
    this.type = type;
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
   * Sets the type.
   *
   * @param type the new type
   */
  public void setType(String type) {
    this.type = type;
  }
}
