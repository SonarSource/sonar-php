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

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.sonar.api.BatchExtension;
import org.sonar.api.ServerExtension;
import org.sonar.api.rules.RulePriority;
import org.sonar.plugins.php.core.PriorityMapper;

/**
 * Class that maps Sonar and PHPCS priority/severity ranges.
 */
public final class PhpCodeSnifferPriorityMapper implements ServerExtension, BatchExtension, PriorityMapper {

  private Map<String, RulePriority> from = new TreeMap<String, RulePriority>();
  private Map<RulePriority, String> to = new HashMap<RulePriority, String>();

  /**
   * Creates the PhpCodeSnifferPriorityMapper
   */
  public PhpCodeSnifferPriorityMapper() {
    from.put("10", RulePriority.BLOCKER);
    from.put("9", RulePriority.CRITICAL);
    from.put("8", RulePriority.MAJOR);
    from.put("7", RulePriority.MINOR);
    from.put("6", RulePriority.INFO);
    from.put("5", RulePriority.INFO);
    from.put("4", RulePriority.INFO);
    from.put("3", RulePriority.INFO);
    from.put("2", RulePriority.INFO);
    from.put("1", RulePriority.INFO);

    to.put(RulePriority.BLOCKER, "10");
    to.put(RulePriority.CRITICAL, "9");
    to.put(RulePriority.MAJOR, "8");
    to.put(RulePriority.MINOR, "7");
    to.put(RulePriority.INFO, "6");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sonar.plugins.php.codesniffer.PriorityMapper#from(java.lang.String)
   */
  public RulePriority from(String priority) {
    if (priority == null || from.get(priority) == null) {
      return RulePriority.MAJOR;
    }
    return from.get(priority);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sonar.plugins.php.codesniffer.PriorityMapper#to(org.sonar.api.rules.RulePriority)
   */
  public String to(RulePriority rulePriority) {
    String priority = to.get(rulePriority);
    if (priority == null) {
      throw new IllegalArgumentException("Priority not supported: " + rulePriority);
    }
    return priority;
  }

}
