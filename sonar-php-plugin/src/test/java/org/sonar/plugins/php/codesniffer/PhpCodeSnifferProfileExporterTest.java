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

package org.sonar.plugins.php.codesniffer;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.sonar.api.platform.ServerFileSystem;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.RuleQuery;
import org.sonar.api.rules.XMLRuleParser;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.test.TestUtils;
import org.xml.sax.SAXException;

public class PhpCodeSnifferProfileExporterTest {

  private PhpCodeSnifferPriorityMapper mapper = new PhpCodeSnifferPriorityMapper();
  private PhpCodeSnifferProfileExporter exporter = new PhpCodeSnifferProfileExporter(mapper);

  @Test
  public void testExportProfile() throws IOException, SAXException {
    ServerFileSystem fileSystem = mock(ServerFileSystem.class);
    PhpCodeSnifferRuleRepository repository = new PhpCodeSnifferRuleRepository(fileSystem, new XMLRuleParser());
    List<Rule> rules = repository.createRules();

    RuleFinder ruleFinder = new MockPhpCodeSnifferRuleFinder(rules);
    PhpCodeSnifferProfileImporter importer = new PhpCodeSnifferProfileImporter(ruleFinder, mapper);
    Reader reader = new StringReader(TestUtils.getResourceContent("/org/sonar/plugins/php/codesniffer/simple-ruleset.xml"));
    RulesProfile rulesProfile = importer.importProfile(reader, ValidationMessages.create());

    StringWriter xmlOutput = new StringWriter();
    exporter.exportProfile(rulesProfile, xmlOutput);
    String exptected = StringUtils.remove(TestUtils.getResourceContent("/org/sonar/plugins/php/codesniffer/simple-export.xml"), '\r');
    assertEquals(exptected, StringUtils.remove(xmlOutput.toString(), '\r'));
  }

  private static class MockPhpCodeSnifferRuleFinder implements RuleFinder {

    private List<Rule> rules;
    private static Map<String, Rule> rulesByKey;

    public MockPhpCodeSnifferRuleFinder(List<Rule> rules) {
      this.rules = rules;
    }

    public Rule findByKey(String repositoryKey, String key) {
      throw new UnsupportedOperationException();
    }

    public Collection<Rule> findAll(RuleQuery query) {
      throw new UnsupportedOperationException();
    }

    public Rule find(RuleQuery query) {
      Map<String, Rule> rulesByKey = getRulesMap();
      String key = query.getKey();
      Rule rule = rulesByKey.get(key);
      rule.setRepositoryKey(PhpCodeSnifferRuleRepository.PHPCS_REPOSITORY_KEY);
      return rule;
    }

    /**
     * @return
     * 
     */
    private Map<String, Rule> getRulesMap() {
      if (rulesByKey == null) {
        rulesByKey = new HashMap<String, Rule>();
        for (Rule rule : rules) {
          rulesByKey.put(rule.getKey(), rule);
        }
      }
      return rulesByKey;
    }
  }
}