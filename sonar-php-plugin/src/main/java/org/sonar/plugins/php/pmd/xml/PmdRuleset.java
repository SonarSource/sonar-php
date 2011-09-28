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

public class PmdRuleset {

  private String name;

  private String description;

  private List<PmdRule> rules = new ArrayList<PmdRule>();

  public PmdRuleset() {
  }

  public PmdRuleset(String name) {
    this.name = name;
  }

  public List<PmdRule> getPmdRules() {
    return rules;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public void addRule(PmdRule rule) {
    rules.add(rule);
  }

  public void setDescription(String description) {
    this.description = description;
  }

}
