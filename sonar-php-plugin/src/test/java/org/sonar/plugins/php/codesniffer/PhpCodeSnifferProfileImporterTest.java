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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.sonar.api.platform.ServerFileSystem;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.RulePriority;
import org.sonar.api.rules.RuleQuery;
import org.sonar.api.rules.XMLRuleParser;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.test.TestUtils;
import org.xml.sax.SAXException;

public class PhpCodeSnifferProfileImporterTest {

  private PhpCodeSnifferPriorityMapper mapper = new PhpCodeSnifferPriorityMapper();

  @Test
  public void testImportProfile() throws IOException, SAXException {
    PhpCodeSnifferProfileImporter importer = createImporter();
    Reader reader = new StringReader(TestUtils.getResourceContent("/org/sonar/plugins/php/codesniffer/simple-ruleset.xml"));
    RulesProfile rulesProfile = importer.importProfile(reader, ValidationMessages.create());
    assertThat(rulesProfile.getActiveRules().size(), is(2));
  }

  @Test
  public void testImportProfileWithNoPriority() throws IOException, SAXException {
    PhpCodeSnifferProfileImporter importer = createImporter();
    Reader reader = new StringReader(TestUtils.getResourceContent("/org/sonar/plugins/php/codesniffer/simple-ruleset-with-no-priority.xml"));
    RulesProfile rulesProfile = importer.importProfile(reader, ValidationMessages.create());
    assertThat(rulesProfile.getActiveRules().size(), is(1));
  }

  @Test
  public void testImportComplexProfile() throws IOException, SAXException {
    PhpCodeSnifferProfileImporter importer = createImporter();
    Reader reader = new StringReader(TestUtils.getResourceContent("/org/sonar/plugins/php/codesniffer/complex-ruleset.xml"));
    RulesProfile rulesProfile = importer.importProfile(reader, ValidationMessages.create());
    assertThat(rulesProfile.getActiveRules().size(), is(3));

    ActiveRule rule = rulesProfile.getActiveRule(PhpCodeSnifferRuleRepository.PHPCS_REPOSITORY_KEY,
        "Generic.Files.LineEndings.InvalidEOLChar");
    assertThat(rule.getSeverity(), is(RulePriority.INFO));
    assertThat(rule.getParameter("eolChar"), is("FOO"));

    rule = rulesProfile.getActiveRule(PhpCodeSnifferRuleRepository.PHPCS_REPOSITORY_KEY,
        "Generic.CodeAnalysis.ForLoopShouldBeWhileLoop.CanSimplify");
    assertThat(rule.getSeverity(), is(RulePriority.CRITICAL));

    rule = rulesProfile.getActiveRule(PhpCodeSnifferRuleRepository.PHPCS_REPOSITORY_KEY,
        "Generic.CodeAnalysis.ForLoopWithTestFunctionCall.NotAllowed");
    assertThat(rule.getSeverity(), is(RulePriority.MAJOR));

    // priority to "0" means the rule should be muted, so it should not be present in the imported profile
    assertThat(
        rulesProfile.getActiveRule(PhpCodeSnifferRuleRepository.PHPCS_REPOSITORY_KEY, "Generic.CodeAnalysis.JumbledIncrementer.Found"),
        nullValue());
  }

  @Test
  public void testImportPearProfileWithGenericSniffs() throws IOException, SAXException {
    PhpCodeSnifferProfileImporter importer = createImporter();
    Reader reader = new StringReader(TestUtils.getResourceContent("/org/sonar/plugins/php/codesniffer/pear-ruleset.xml"));
    RulesProfile rulesProfile = importer.importProfile(reader, ValidationMessages.create());

    List<ActiveRule> activeRules = rulesProfile.getActiveRules();
    assertThat(activeRules.size(), is(16));
    // just check that the "eolChar" param has been passed to the "Generic.Files.LineEndings.InvalidEOLChar" rule
    // NOTE : be carefull, this test may be broken if this sniff does not exist anymore in the PHPCS rules.xml file
    for (ActiveRule activeRule : activeRules) {
      if ("Generic.Files.LineEndings.InvalidEOLChar".equals(activeRule.getRuleKey())) {
        assertThat(activeRule.getParameter("eolChar"), is("\\n"));
      }
    }
  }

  @Test
  public void testImportWithInvalidFile() throws IOException, SAXException {
    PhpCodeSnifferProfileImporter importer = createImporter();
    ValidationMessages messages = ValidationMessages.create();

    Reader reader = new StringReader(TestUtils.getResourceContent("/org/sonar/plugins/php/codesniffer/invalid-ruleset.xml"));
    importer.importProfile(reader, messages);
    assertThat(messages.getErrors().get(0), startsWith("The PhpCodeSniffer configuration file is not valid"));
  }

  private PhpCodeSnifferProfileImporter createImporter() {
    ServerFileSystem fileSystem = mock(ServerFileSystem.class);
    PhpCodeSnifferRuleRepository repository = new PhpCodeSnifferRuleRepository(fileSystem, new XMLRuleParser());
    List<Rule> rules = repository.createRules();

    RuleFinder ruleFinder = new MockPhpCodeSnifferRuleFinder(rules);
    PhpCodeSnifferProfileImporter importer = new PhpCodeSnifferProfileImporter(ruleFinder, mapper);
    return importer;
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
      if (rule != null) {
        rule.setRepositoryKey(PhpCodeSnifferRuleRepository.PHPCS_REPOSITORY_KEY);
      }
      return rule;
    }

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
      return null;
    }
  }

}
