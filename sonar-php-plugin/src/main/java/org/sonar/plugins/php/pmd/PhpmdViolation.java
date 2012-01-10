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
package org.sonar.plugins.php.pmd;

/**
 * @author Akram Ben Aissi
 * 
 */
public final class PhpmdViolation {

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
  private String priority;
  /**
     * 
     */
  private String longMessage;
  /**
   * 
   */
  private Integer beginLine;
  /**
    * 
    */
  private Integer endLine;

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
   * @return the ruleKey
   */
  public String getRuleKey() {
    return ruleKey;
  }

  /**
   * @param ruleKey
   *          the ruleKey to set
   */
  public void setRuleKey(String ruleKey) {
    this.ruleKey = ruleKey;
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
  public void setRuleName(String ruleName) {
    this.ruleName = ruleName;
  }

  /**
   * @return the priority
   */
  public String getPriority() {
    return priority;
  }

  /**
   * @param priority
   *          the priority to set
   */
  public void setPriority(String type) {
    this.priority = type;
  }

  /**
   * @return the longMessage
   */
  public String getLongMessage() {
    return longMessage;
  }

  /**
   * @param longMessage
   *          the longMessage to set
   */
  public void setLongMessage(String longMessage) {
    this.longMessage = longMessage;
  }

  /**
   * @return the beginLine
   */
  public Integer getBeginLine() {
    return beginLine;
  }

  /**
   * @param beginLine
   *          the beginLine to set
   */
  public void setBeginLine(Integer beginLine) {
    this.beginLine = beginLine;
  }

  /**
   * @return the endLine
   */
  public Integer getEndLine() {
    return endLine;
  }

  /**
   * @param endLine
   *          the endLine to set
   */
  public void setEndLine(Integer endLine) {
    this.endLine = endLine;
  }

  /**
   * @return the fileName
   */
  public String getFileName() {
    return fileName;
  }

  /**
   * @param fileName
   *          the fileName to set
   */
  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  /**
   * @return the sourcePath
   */
  public String getSourcePath() {
    return sourcePath;
  }

  /**
   * @param sourcePath
   *          the sourcePath to set
   */
  public void setSourcePath(String sourcePath) {
    this.sourcePath = sourcePath;
  }

}
