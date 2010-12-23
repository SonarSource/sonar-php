/*
 * Sonar PHP Plugin
 * Copyright (C) 2010 Akram Ben Aissi or Jerome Tama or Frederic Leroy
 * mailto: akram.benaissi@free.fr or jerome.tama@codehaus.org
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

package org.sonar.plugins.php.codesniffer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.sonar.plugins.php.codesniffer.PhpCodeSnifferRuleRepository.PHPCS_REPOSITORY_NAME;

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

public class PhpCodeSnifferProfileTest {

  @Test
  public void testCreateProfileValidationMessages() {
    ServerFileSystem fileSystem = mock(ServerFileSystem.class);
    PhpCodeSnifferRuleRepository repository = new PhpCodeSnifferRuleRepository(fileSystem, new XMLRuleParser());
    List<Rule> rules = repository.createRules();
    RuleFinder ruleFinder = new MockPhpCodeSnifferRuleFinder(rules);

    XMLProfileParser parser = new XMLProfileParser(ruleFinder);
    PhpCodeSnifferProfile profile = new PhpCodeSnifferProfile(parser);
    ValidationMessages messages = ValidationMessages.create();
    RulesProfile rulesProfile = profile.createProfile(messages);
    assertNotNull(rulesProfile);
    assertEquals(PHPCS_REPOSITORY_NAME, rulesProfile.getName());
  }
}
