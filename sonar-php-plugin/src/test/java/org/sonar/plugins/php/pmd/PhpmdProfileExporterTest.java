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

import static org.fest.assertions.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.sonar.plugins.php.pmd.PhpmdProfileImporter.XPATH_CLASS;
import static org.sonar.plugins.php.pmd.PhpmdProfileImporter.XPATH_EXPRESSION_PARAM;
import static org.sonar.plugins.php.pmd.PhpmdProfileImporter.XPATH_MESSAGE_PARAM;

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
  public void testExportProfileWithParam() throws IOException, SAXException {
    ServerFileSystem fileSystem = mock(ServerFileSystem.class);
    PhpmdRuleRepository repository = new PhpmdRuleRepository(fileSystem, new XMLRuleParser());
    List<Rule> rules = repository.createRules();

    RuleFinder ruleFinder = new PhpmdRuleFinder(rules);
    PhpmdProfileImporter importer = new PhpmdProfileImporter(ruleFinder, new PmdRulePriorityMapper());
    String path = "/org/sonar/plugins/php/pmd/simple-ruleset-with-param.xml";
    Reader reader = new StringReader(TestUtils.getResourceContent(path));
    ValidationMessages messages = ValidationMessages.create();
    RulesProfile rulesProfile = importer.importProfile(reader, messages);
    assertThat(messages).isNotNull();
    assertThat(messages.hasErrors()).isFalse();
    assertThat(messages.hasWarnings()).isFalse();
    assertThat(messages.hasInfos()).isFalse();

    StringWriter xmlOutput = new StringWriter();
    exporter.exportProfile(rulesProfile, xmlOutput);
    String exptected = TestUtils.getResourceContent("/org/sonar/plugins/php/pmd/simple-export-with-param.xml");
    assertEquals(StringUtils.remove(exptected, '\r'), StringUtils.remove(xmlOutput.toString(), '\r'));

  }

  @Test
  public void testExportProfile() throws IOException, SAXException {
    ServerFileSystem fileSystem = mock(ServerFileSystem.class);
    PhpmdRuleRepository repository = new PhpmdRuleRepository(fileSystem, new XMLRuleParser());
    List<Rule> rules = repository.createRules();

    RuleFinder ruleFinder = new PhpmdRuleFinder(rules);
    PhpmdProfileImporter importer = new PhpmdProfileImporter(ruleFinder, new PmdRulePriorityMapper());
    Reader reader = new StringReader(TestUtils.getResourceContent("/org/sonar/plugins/php/pmd/simple-ruleset.xml"));
    ValidationMessages messages = ValidationMessages.create();
    RulesProfile rulesProfile = importer.importProfile(reader, messages);
    assertThat(messages).isNotNull();
    assertThat(messages.hasErrors()).isFalse();
    assertThat(messages.hasWarnings()).isTrue();
    assertThat(messages.hasInfos()).isFalse();

    StringWriter xmlOutput = new StringWriter();
    exporter.exportProfile(rulesProfile, xmlOutput);
    String exptected = TestUtils.getResourceContent("/org/sonar/plugins/php/pmd/simple-export.xml");
    assertEquals(StringUtils.remove(exptected, '\r'), StringUtils.remove(xmlOutput.toString(), '\r'));

  }

  @Test
  public void testProcessingXPathRule() {
    String message = "This is bad";
    String xpathExpression = "xpathExpression";

    PmdRule rule = new PmdRule(XPATH_CLASS);
    rule.addProperty(new PmdProperty(XPATH_EXPRESSION_PARAM, xpathExpression));
    rule.addProperty(new PmdProperty(XPATH_MESSAGE_PARAM, message));
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
      return rules;
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

    public Rule findById(int ruleId) {
      // TODO Auto-generated method stub
      return null;
    }
  }
}
