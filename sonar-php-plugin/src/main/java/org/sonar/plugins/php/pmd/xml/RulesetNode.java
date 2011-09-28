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
package org.sonar.plugins.php.pmd.xml;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * The Ruleset represent an PHPMD ruleset.
 */
@XStreamAlias("ruleset")
public class RulesetNode {

  /** The name. */
  @XStreamAsAttribute
  private String name;

  /** The description. */
  @XStreamAlias("description")
  private String rulesetDescription;

  /** The rules. */
  @XStreamImplicit(itemFieldName = "rule")
  private List<RuleNode> rules;

  /**
   * Instantiates a new ruleset.
   * 
   * @param description
   *          the description
   */
  public RulesetNode(String description) {
    this.name = description;
    rules = new ArrayList<RuleNode>();
  }

  /**
   * Instantiates a new ruleset.
   * 
   * @param name
   *          the name
   * @param description
   *          the description
   * @param rules
   *          the rules
   */
  public RulesetNode(String name, String description, List<RuleNode> rules) {
    super();
    this.name = name;
    this.rulesetDescription = description;
    this.rules = rules;
  }

  /**
   * Gets the description.
   * 
   * @return the description
   */
  public String getDescription() {
    return rulesetDescription;
  }

  /**
   * Gets the name.
   * 
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the rules.
   * 
   * @return the rules
   */
  public List<RuleNode> getRules() {
    return rules;
  }

  /**
   * Sets the description.
   * 
   * @param description
   *          the new description
   */
  public void setDescription(String description) {
    this.rulesetDescription = description;
  }

  /**
   * Sets the name.
   * 
   * @param name
   *          the new name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Sets the rules.
   * 
   * @param rules
   *          the new rules
   */
  public void setRules(List<RuleNode> rules) {
    this.rules = rules;
  }
}
