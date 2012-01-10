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
package org.sonar.plugins.php.codesniffer;

/**
 * @author Akram Ben Aissi
 * 
 */
public final class PhpCodeSnifferViolation {

  /**
   * The ruleKey of the violated rule.
   */
  private String ruleKey;

  /**
   * The ruleKey of the violated rule.
   */
  private String ruleName;

  /**
     * 
     */
  private String type;
  /**
     * 
     */
  private String longMessage;
  /**
    * 
    */
  private Integer line;
  /**
   * 
   */
  private Integer comlumn;
  /**
     * 
     */
  private String fileName;
  /**
   * 
   * 
   */
  private String sourcePath;

  /**
   * @return
   */
  public String getType() {
    return type;
  }

  /**
   * @return
   */
  public String getLongMessage() {
    return longMessage;
  }

  /**
   * @return
   */
  public Integer getLine() {
    return line;
  }

  /**
   * @return
   */
  public String getFileName() {
    return fileName;
  }

  /**
   * @return
   */
  public String getSourcePath() {
    return sourcePath;
  }

  /**
   * @param type
   *          the type to set
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * @param longMessage
   *          the longMessage to set
   */
  public void setLongMessage(String longMessage) {
    this.longMessage = longMessage;
  }

  /**
   * @param line
   *          the line to set
   */
  public void setLine(Integer line) {
    this.line = line;
  }

  /**
   * @param fileName
   *          the fileName to set
   */
  public void setFileName(String className) {
    this.fileName = className;
  }

  /**
   * @param sourcePath
   *          the sourcePath to set
   */
  public void setSourcePath(String sourcePath) {
    this.sourcePath = sourcePath;
  }

  /**
   * @return the ruleKey
   */
  public String getRuleKey() {
    return ruleKey;
  }

  /**
   * @param ruleKey
   *          the ruleKey to set
   */
  public void setRuleKey(String key) {
    this.ruleKey = key;
  }

  /**
   * @return the ruleName
   */
  public String getRuleName() {
    return ruleName;
  }

  /**
   * @param ruleName
   *          the ruleName to set
   */
  public void setRuleName(String sourceKey) {
    this.ruleName = sourceKey;
  }

  /**
   * @return the comlumn
   */
  public Integer getComlumn() {
    return comlumn;
  }

  /**
   * @param comlumn
   *          the comlumn to set
   */
  public void setComlumn(Integer comlumn) {
    this.comlumn = comlumn;
  }

}
