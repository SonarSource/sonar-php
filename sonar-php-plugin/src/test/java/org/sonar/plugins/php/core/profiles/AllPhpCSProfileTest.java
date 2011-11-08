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
package org.sonar.plugins.php.core.profiles;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.Test;
import org.sonar.api.platform.ServerFileSystem;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.profiles.XMLProfileParser;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.XMLRuleParser;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.plugins.php.codesniffer.PhpCodeSnifferProfileExporterTest.MockPhpCodeSnifferRuleFinder;
import org.sonar.plugins.php.codesniffer.PhpCodeSnifferRuleRepository;

public class AllPhpCSProfileTest {

  @Test
  public void testCreateProfileValidationMessages() {
    ServerFileSystem fileSystem = mock(ServerFileSystem.class);
    PhpCodeSnifferRuleRepository repository = new PhpCodeSnifferRuleRepository(fileSystem, new XMLRuleParser());
    List<Rule> rules = repository.createRules();
    RuleFinder ruleFinder = new MockPhpCodeSnifferRuleFinder(rules);

    XMLProfileParser parser = new XMLProfileParser(ruleFinder);
    AllPhpCSProfile profile = new AllPhpCSProfile(parser);
    ValidationMessages messages = ValidationMessages.create();
    RulesProfile rulesProfile = profile.createProfile(messages);
    assertNotNull(rulesProfile);
    assertEquals("All PHP CodeSniffer Rules", rulesProfile.getName());
  }
}
