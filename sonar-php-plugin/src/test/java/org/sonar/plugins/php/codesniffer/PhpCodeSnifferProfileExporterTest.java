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

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
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
import org.sonar.api.utils.SonarException;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.test.TestUtils;
import org.xml.sax.SAXException;

public class PhpCodeSnifferProfileExporterTest {

  private PhpCodeSnifferPriorityMapper mapper = new PhpCodeSnifferPriorityMapper();
  private PhpCodeSnifferProfileExporter exporter = new PhpCodeSnifferProfileExporter(mapper);

  @Test(expected = SonarException.class)
  public void testExportProfileFail() throws IOException, SAXException {
    ServerFileSystem fileSystem = mock(ServerFileSystem.class);
    PhpCodeSnifferRuleRepository repository = new PhpCodeSnifferRuleRepository(fileSystem, new XMLRuleParser());
    List<Rule> rules = repository.createRules();

    RuleFinder ruleFinder = new MockPhpCodeSnifferRuleFinder(rules);
    PhpCodeSnifferProfileImporter importer = new PhpCodeSnifferProfileImporter(ruleFinder, mapper);
    Reader reader = new StringReader(TestUtils.getResourceContent("/org/sonar/plugins/php/codesniffer/simple-ruleset.xml"));
    RulesProfile rulesProfile = importer.importProfile(reader, ValidationMessages.create());

    exporter.exportProfile(rulesProfile, xmlOutput);
  }

  @Test
  public void testExportComplexProfile() throws IOException, SAXException {
    ServerFileSystem fileSystem = mock(ServerFileSystem.class);
    PhpCodeSnifferRuleRepository repository = new PhpCodeSnifferRuleRepository(fileSystem, new XMLRuleParser());
    List<Rule> rules = repository.createRules();

    RuleFinder ruleFinder = new MockPhpCodeSnifferRuleFinder(rules);
    PhpCodeSnifferProfileImporter importer = new PhpCodeSnifferProfileImporter(ruleFinder, mapper);
    Reader reader = new StringReader(TestUtils.getResourceContent("/org/sonar/plugins/php/codesniffer/complex-ruleset.xml"));
    RulesProfile rulesProfile = importer.importProfile(reader, ValidationMessages.create());

    StringWriter xmlOutput = new StringWriter();
    exporter.exportProfile(rulesProfile, xmlOutput);
    String expected = StringUtils.remove(TestUtils.getResourceContent("/org/sonar/plugins/php/codesniffer/complex-export.xml"), '\r');
    String filteredOuput = StringUtils.remove(xmlOutput.toString(), '\r');
    assertThat(filteredOuput).isEqualTo(expected);
  }

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
    String expected = StringUtils.remove(TestUtils.getResourceContent("/org/sonar/plugins/php/codesniffer/simple-export.xml"), '\r');
    String filteredOuput = StringUtils.remove(xmlOutput.toString(), '\r');
    assertThat(filteredOuput).isEqualTo(expected);
  }

  public static class MockPhpCodeSnifferRuleFinder implements RuleFinder {

    private List<Rule> rules;
    private static Map<String, Rule> rulesByKey = new HashMap<String, Rule>();

    public MockPhpCodeSnifferRuleFinder(List<Rule> rules) {
      this.rules = rules;
    }

    public Rule findByKey(String repositoryKey, String key) {
      return rulesByKey.get(key);
    }

    public Collection<Rule> findAll(RuleQuery query) {
      return rules;
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
      }
      for (Rule rule : rules) {
        rulesByKey.put(rule.getKey(), rule);
      }
      return rulesByKey;
    }

    public Rule findById(int ruleId) {
      // TODO Auto-generated method stub
      return null;
    }
  }

  static Writer xmlOutput = new Writer() {

    public Writer append(CharSequence s) throws IOException {
      throw new IOException("");
    }

    @Override
    public void close() throws IOException {
      // TODO Auto-generated method stub

    }

    @Override
    public void flush() throws IOException {
      // TODO Auto-generated method stub

    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
      // TODO Auto-generated method stub

    };
  };

}
