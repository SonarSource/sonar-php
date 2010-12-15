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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.sonar.api.platform.ServerFileSystem;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.RuleQuery;
import org.sonar.api.rules.XMLRuleParser;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.plugins.php.pmd.xml.PmdProperty;
import org.sonar.plugins.php.pmd.xml.PmdRule;
import org.sonar.test.TestUtils;
import org.xml.sax.SAXException;

public class PhpmdProfileExporterTest {

  private PhpmdProfileExporter exporter = new PhpmdProfileExporter(new PmdRulePriorityMapper());

  @Test
  public void testExportProfile() throws IOException, SAXException {
    ServerFileSystem fileSystem = mock(ServerFileSystem.class);
    PhpmdRuleRepository repository = new PhpmdRuleRepository(fileSystem, new XMLRuleParser());
    List<Rule> rules = repository.createRules();

    RuleFinder ruleFinder = new PhpmdRuleFinder(rules);
    PhpmdProfileImporter importer = new PhpmdProfileImporter(ruleFinder);
    Reader reader = new StringReader(TestUtils.getResourceContent("/org/sonar/plugins/php/pmd/simple-ruleset.xml"));
    RulesProfile rulesProfile = importer.importProfile(reader, ValidationMessages.create());

    StringWriter xmlOutput = new StringWriter();
    exporter.exportProfile(rulesProfile, xmlOutput);
    String exptected = TestUtils.getResourceContent("/org/sonar/plugins/php/pmd/simple-export.xml");
    assertEquals(StringUtils.remove(exptected, '\r'), StringUtils.remove(xmlOutput.toString(), '\r'));
  }

  @Test
  public void testProcessingXPathRule() {
    String message = "This is bad";
    String xpathExpression = "xpathExpression";

    PmdRule rule = new PmdRule(PhpmdProfileImporter.XPATH_CLASS);
    rule.addProperty(new PmdProperty(PhpmdProfileImporter.XPATH_EXPRESSION_PARAM, xpathExpression));
    rule.addProperty(new PmdProperty(PhpmdProfileImporter.XPATH_MESSAGE_PARAM, message));
    rule.setName("MyOwnRule");

    exporter.processXPathRule("xpathKey", rule);

    assertThat(rule.getMessage(), is(message));
    assertThat(rule.getRef(), is(nullValue()));
    assertThat(rule.getClazz(), is(PhpmdProfileImporter.XPATH_CLASS));
    assertThat(rule.getProperty(PhpmdProfileImporter.XPATH_MESSAGE_PARAM), is(nullValue()));
    assertThat(rule.getName(), is("xpathKey"));
    assertThat(rule.getProperty(PhpmdProfileImporter.XPATH_EXPRESSION_PARAM).getValue(), is(xpathExpression));
  }

  private static class PhpmdRuleFinder implements RuleFinder {

    private List<Rule> rules;

    public PhpmdRuleFinder(List<Rule> rules) {
      this.rules = rules;
    }

    public Rule findByKey(String repositoryKey, String key) {
      throw new UnsupportedOperationException();
    }

    public Collection<Rule> findAll(RuleQuery query) {
      throw new UnsupportedOperationException();
    }

    public Rule find(RuleQuery query) {
      for (Rule rule : rules) {
        if (query.getConfigKey().equals(rule.getConfigKey())) {
          rule.setPluginName(PhpmdRuleRepository.PHPMD_REPOSITORY_KEY);
          return rule;
        }
      }
      return null;
    }
  }
}