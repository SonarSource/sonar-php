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

import java.util.HashMap;
import java.util.Map;

import org.sonar.api.BatchExtension;
import org.sonar.api.ServerExtension;
import org.sonar.api.rules.RulePriority;
import org.sonar.plugins.php.core.PriorityMapper;

/**
 * Class that maps Sonar and PHPMD priority/severity ranges.
 */
public final class PmdRulePriorityMapper implements ServerExtension, BatchExtension, PriorityMapper {

  private Map<String, RulePriority> from = new HashMap<String, RulePriority>();
  private Map<RulePriority, String> to = new HashMap<RulePriority, String>();

  private static final RulePriority DEFAULT_RULE_PRIORITY = RulePriority.MAJOR;

  /**
   * Creates a PmdRulePriorityMapper
   */
  public PmdRulePriorityMapper() {
    from.put("1", RulePriority.BLOCKER);
    from.put("2", RulePriority.CRITICAL);
    from.put("3", RulePriority.MAJOR);
    from.put("4", RulePriority.MINOR);
    from.put("5", RulePriority.INFO);
    from.put(null, DEFAULT_RULE_PRIORITY);
    from.put("", DEFAULT_RULE_PRIORITY);

    to.put(RulePriority.BLOCKER, "1");
    to.put(RulePriority.CRITICAL, "2");
    to.put(RulePriority.MAJOR, "3");
    to.put(RulePriority.MINOR, "4");
    to.put(RulePriority.INFO, "5");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sonar.plugins.php.codesniffer.PriorityMapper#from(java.lang.String)
   */
  public RulePriority from(String level) {
    return from.get(level);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sonar.plugins.php.codesniffer.PriorityMapper#to(org.sonar.api.rules.RulePriority)
   */
  public String to(RulePriority priority) {
    String level = to.get(priority);
    if (level == null) {
      throw new IllegalArgumentException("Level not supported: " + priority);
    }
    return level;
  }

}
