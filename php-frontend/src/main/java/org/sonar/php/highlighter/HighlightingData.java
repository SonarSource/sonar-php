/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * sonarqube@googlegroups.com
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
package org.sonar.php.highlighter;

public class HighlightingData {

  private Integer startOffset;
  private Integer endOffset;
  private String highlightCode;

  public HighlightingData(Integer startOffset, Integer endOffset, String highlightCode) {
    this.startOffset = startOffset;
    this.endOffset = endOffset;
    this.highlightCode = highlightCode;
  }

  public Integer startOffset() {
    return startOffset;
  }

  public Integer endOffset() {
    return endOffset;
  }

  public String highlightCode() {
    return highlightCode;
  }

  @Override
  public String toString() {
    return "HighlightingData{" +
      "startOffset=" + startOffset +
      ", endOffset=" + endOffset +
      ", highlightCode='" + highlightCode + '\'' +
      '}';
  }
}
