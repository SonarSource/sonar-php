/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2010 Akram Ben Aissi
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

package org.sonar.plugins.php.pmd;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.platform.ServerFileSystem;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.XMLRuleParser;

public class PhpmdRulesRepositoryTest {

  private PhpmdRuleRepository repository;

  @Before
  public void setup() {
    ServerFileSystem fs = mock(ServerFileSystem.class);
    XMLRuleParser parser = new XMLRuleParser();
    repository = new PhpmdRuleRepository(fs, parser);
  }

  @Test
  public void testCreateRules() {
    List<Rule> rules = repository.createRules();
    for (Rule rule : rules) {
      assertNotNull(rule.getKey());
      assertNotNull(rule.getDescription());
      assertNotNull(rule.getName());
      assertNotNull(rule.getRulesCategory());
      assertNotNull(rule.getPriority());
      // assertNotNull(rule.getPluginName());
    }
  }

}