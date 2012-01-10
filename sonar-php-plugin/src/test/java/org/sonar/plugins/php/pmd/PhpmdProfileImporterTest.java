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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.io.Reader;
import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
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
import org.sonar.plugins.php.pmd.xml.PmdRuleset;
import org.sonar.test.TestUtils;

public class PhpmdProfileImporterTest {

  private static final String REPOSITORY_KEY = PhpmdRuleRepository.PHPMD_REPOSITORY_KEY;
  private PhpmdProfileImporter importer;
  private ValidationMessages messages;

  @Before
  public void before() {
    messages = ValidationMessages.create();
    RuleFinder finder = createRuleFinder();
    importer = new PhpmdProfileImporter(finder, new PmdRulePriorityMapper());
  }

  @Test
  public void testBuildPhpmdRuleset() {
    Reader reader = new StringReader(TestUtils.getResourceContent("/org/sonar/plugins/php/pmd/simple-ruleset.xml"));
    PmdRuleset ruleset = importer.parsePmdRuleset(reader, messages);
    assertThat(ruleset.getPmdRules().size(), is(3));
  }

  @Test
  public void testImportingSimpleProfile() {
    Reader reader = new StringReader(TestUtils.getResourceContent("/org/sonar/plugins/php/pmd/simple-ruleset.xml"));
    RulesProfile profile = importer.importProfile(reader, messages);

    assertThat(profile.getActiveRules().size(), is(3));
    assertNotNull(profile.getActiveRuleByConfigKey(REPOSITORY_KEY, "rulesets/codesize.xml/CyclomaticComplexity"));
    assertNotNull(profile.getActiveRuleByConfigKey(REPOSITORY_KEY, "rulesets/codesize.xml/NPathComplexity"));
    assertThat(messages.hasErrors(), is(false));
  }

  @Test
  public void testImportingProfileWithXPathRule() {
    Reader reader = new StringReader(TestUtils.getResourceContent("/org/sonar/plugins/php/pmd/export_xpath_rules.xml"));
    RulesProfile profile = importer.importProfile(reader, messages);

    assertThat(profile.getActiveRules().size(), is(0));
    assertThat(messages.hasWarnings(), is(true));
  }

  @Test
  public void testImportingParameters() {
    Reader reader = new StringReader(TestUtils.getResourceContent("/org/sonar/plugins/php/pmd/simple-ruleset.xml"));
    RulesProfile profile = importer.importProfile(reader, messages);

    ActiveRule activeRule = profile.getActiveRuleByConfigKey(REPOSITORY_KEY, "rulesets/codesize.xml/CyclomaticComplexity");
    assertThat(activeRule.getActiveRuleParams().size(), is(1));
    assertThat(activeRule.getParameter("reportLevel"), is("30"));
  }

  @Test
  public void testImportingDefaultPriority() {
    Reader reader = new StringReader(TestUtils.getResourceContent("/org/sonar/plugins/php/pmd/simple-ruleset.xml"));
    RulesProfile profile = importer.importProfile(reader, messages);

    ActiveRule activeRule = profile.getActiveRuleByConfigKey(REPOSITORY_KEY, "rulesets/codesize.xml/NPathComplexity");
    assertThat(activeRule.getPriority(), is(RulePriority.MAJOR)); // reuse the rule default priority
  }

  @Test
  public void testImportingPriority() {
    Reader reader = new StringReader(TestUtils.getResourceContent("/org/sonar/plugins/php/pmd/simple-ruleset.xml"));
    RulesProfile profile = importer.importProfile(reader, messages);

    ActiveRule activeRule = profile.getActiveRuleByConfigKey(REPOSITORY_KEY, "rulesets/codesize.xml/CyclomaticComplexity");
    assertThat(activeRule.getPriority(), is(RulePriority.MINOR));

    activeRule = profile.getActiveRuleByConfigKey(REPOSITORY_KEY, "rulesets/codesize.xml/ExcessiveMethodLength");
    assertThat(activeRule.getPriority(), is(RulePriority.CRITICAL));
  }

  @Test
  public void testImportingPhpmdConfigurationWithUnknownNodes() {
    Reader reader = new StringReader(TestUtils.getResourceContent("/org/sonar/plugins/php/pmd/complex-with-unknown-nodes.xml"));
    RulesProfile profile = importer.importProfile(reader, messages);

    assertThat(profile.getActiveRules().size(), is(3));
  }

  @Test
  public void testUnsupportedProperty() {
    Reader reader = new StringReader(TestUtils.getResourceContent("/org/sonar/plugins/php/pmd/simple-ruleset.xml"));
    RulesProfile profile = importer.importProfile(reader, messages);

    ActiveRule check = profile.getActiveRuleByConfigKey(REPOSITORY_KEY, "rulesets/codesize.xml/CyclomaticComplexity");
    // The mock rulefinder contains only one param for the rule, but the ruleset file contains 2, so we should get a warning about that.
    assertThat(check.getParameter("threshold"), nullValue());
    assertThat(messages.getWarnings().size(), is(1));
  }

  @Test
  public void testInvalidXML() {
    Reader reader = new StringReader("not xml");
    importer.importProfile(reader, messages);
    assertThat(messages.getErrors().size(), is(1));
  }

  @Test
  public void testImportingUnknownRules() {
    Reader reader = new StringReader(TestUtils.getResourceContent("/org/sonar/plugins/php/pmd/simple-ruleset.xml"));
    importer = new PhpmdProfileImporter(mock(RuleFinder.class), new PmdRulePriorityMapper());
    RulesProfile profile = importer.importProfile(reader, messages);

    assertThat(profile.getActiveRules().size(), is(0));
    assertThat(messages.getWarnings().size(), is(3));
  }

  @Test
  public void testImportingRulesetWithEntireUnusedCodeRuleset() {
    Reader reader = new StringReader(TestUtils.getResourceContent("/org/sonar/plugins/php/pmd/entire-unusedcode-ruleset.xml"));
    importer = new PhpmdProfileImporter(createRuleFinder(), new PmdRulePriorityMapper());
    RulesProfile profile = importer.importProfile(reader, messages);

    assertThat(messages.getWarnings().size(), is(0));
    assertThat(profile.getActiveRules().size(), is(4));
  }

  private RuleFinder createRuleFinder() {
    ServerFileSystem fileSystem = mock(ServerFileSystem.class);
    PhpmdRuleRepository repository = new PhpmdRuleRepository(fileSystem, new XMLRuleParser());
    List<Rule> rules = repository.createRules();
    return new MockPhpmdRuleFinder(rules);
  }

  public static class MockPhpmdRuleFinder implements RuleFinder {

    private List<Rule> rules;
    private static Map<String, Rule> rulesByConfigKey = new HashMap<String, Rule>();

    public MockPhpmdRuleFinder(List<Rule> rules) {
      this.rules = rules;
    }

    public Rule findByKey(String repositoryKey, String key) {
      throw new UnsupportedOperationException();
    }

    public Collection<Rule> findAll(RuleQuery query) {
      return rules;
    }

    public Rule find(RuleQuery query) {
      Map<String, Rule> rulesByConfigKey = getRulesMap();
      String configKey = query.getConfigKey();
      Rule rule = rulesByConfigKey.get(configKey);
      if (rule != null) {
        rule.setRepositoryKey(PhpmdRuleRepository.PHPMD_REPOSITORY_KEY);
      }
      return rule;
    }

    private Map<String, Rule> getRulesMap() {
      if (rulesByConfigKey == null) {
        rulesByConfigKey = new HashMap<String, Rule>();
      }
      for (Rule rule : rules) {
        rulesByConfigKey.put(rule.getConfigKey(), rule);
      }
      return rulesByConfigKey;
    }

    public Rule findById(int ruleId) {
      return null;
    }
  }
}
