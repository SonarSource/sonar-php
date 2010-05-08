/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2010 SQLi
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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.ActiveRuleParam;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleParam;
import org.sonar.api.rules.RulePriority;
import org.sonar.plugins.checkstyle.xml.Module;
import org.sonar.plugins.checkstyle.xml.Property;
import org.xml.sax.SAXException;

/**
 * The Class PhpCodesnifferRulesRepositoryTest.
 */
public class PhpCodesnifferRulesRepositoryTest {

  /** The repository. */
  private PhpCodesnifferRulesRepository repository;

  /**
   * Assert active rule.
   * 
   * @param activeRule
   *          the active rule
   * @param configKey
   *          the config key
   * @param priority
   *          the priority
   * @param paramsCount
   *          the params count
   */
  private void assertActiveRule(ActiveRule activeRule, String configKey, RulePriority priority, int paramsCount) {
    assertThat(activeRule.getConfigKey(), is(configKey));
    assertThat(activeRule.getPriority(), is(priority));
    assertThat(activeRule.getActiveRuleParams().size(), is(paramsCount));
  }

  /**
   * Assert active rules are equals.
   * 
   * @param activeRules1
   *          the active rules1
   * @param activeRules2
   *          the active rules2
   */
  private void assertActiveRulesAreEquals(List<ActiveRule> activeRules1, List<ActiveRule> activeRules2) {
    for (int i = 0; i < activeRules1.size(); i++) {
      ActiveRule activeRule1 = activeRules1.get(i);
      ActiveRule activeRule2 = activeRules2.get(i);
      assertEquals(activeRule1.getPriority(), activeRule2.getPriority());
      assertEquals(activeRule1.getRule(), activeRule2.getRule());
      assertEquals(activeRule1.getActiveRuleParams().size(), (activeRule2.getActiveRuleParams().size()));
      for (int j = 0; j < activeRule1.getActiveRuleParams().size(); j++) {
        ActiveRuleParam activeRuleParam1 = activeRule1.getActiveRuleParams().get(j);
        ActiveRuleParam activeRuleParam2 = activeRule2.getActiveRuleParams().get(j);
        assertTrue(activeRuleParam1.getRuleParam().equals(activeRuleParam2.getRuleParam())
            && activeRuleParam1.getValue().equals(activeRuleParam2.getValue()));
      }
    }
  }

  /**
   * Assert xml are similar.
   * 
   * @param xml
   *          the xml
   * @param xmlFileToFind
   *          the xml file to find
   * 
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SAXException
   *           the SAX exception
   * @throws ParserConfigurationException
   *           the parser configuration exception
   */
  private void assertXmlAreSimilar(String xml, String xmlFileToFind) throws IOException, SAXException, ParserConfigurationException {
    XMLUnit.setIgnoreWhitespace(true);
    DocumentBuilderFactory bf = DocumentBuilderFactory.newInstance();
    bf.setValidating(false);
    bf.setNamespaceAware(false);
    bf.setFeature("http://apache.org/xml/features/validation/schema", false);
    bf.setFeature("http://xml.org/sax/features/external-general-entities", false);
    bf.setFeature("http://xml.org/sax/features/validation", false);
    bf.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
    bf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
    bf.setFeature("http://apache.org/xml/features/allow-java-encodings", true);

    InputStream input = getClass().getResourceAsStream("/org/sonar/plugins/php/codesniffer/" + xmlFileToFind);
    String xmlToFind = IOUtils.toString(input, "UTF-8");
    Diff diff = XMLUnit.compareXML(XMLUnit.buildDocument(bf.newDocumentBuilder(), new StringReader(xml)), XMLUnit.buildDocument(bf
        .newDocumentBuilder(), new StringReader(xmlToFind)));
    assertTrue(diff.toString(), diff.similar());
  }

  /**
   * Builds the active rules fixture.
   * 
   * @param rules
   *          the rules
   * 
   * @return the list< active rule>
   */
  private List<ActiveRule> buildActiveRulesFixture(List<Rule> rules) {
    List<ActiveRule> activeRules = new ArrayList<ActiveRule>();

    ActiveRule activeRule1 = new ActiveRule(null, rules.get(0), RulePriority.BLOCKER);
    activeRule1.setActiveRuleParams(Arrays.asList(new ActiveRuleParam(activeRule1, rules.get(0).getParams().get(0), "properties")));
    activeRules.add(activeRule1);
    ActiveRule activeRule2 = new ActiveRule(null, rules.get(1), RulePriority.CRITICAL);
    activeRule2.setActiveRuleParams(Arrays.asList(new ActiveRuleParam(activeRule2, rules.get(1).getParams().get(0), "15")));
    activeRules.add(activeRule2);
    ActiveRule activeRule3 = new ActiveRule(null, rules.get(2), RulePriority.MAJOR);
    activeRules.add(activeRule3);
    return activeRules;

  }

  /**
   * Builds the module tree fixture.
   * 
   * @return the module
   */
  private Module buildModuleTreeFixture() {
    Module root = new Module("Checker", null);
    new Module("CyclomaticComplexity", root, Arrays.asList(new Property("severity", "error"), new Property("minimum", "20")));
    Module child2 = new Module("TreeWalker", root);
    new Module("ExcessiveClassLength", child2, Arrays.asList(new Property("minimum", "15")));
    new Module("NPathComplexity", child2, Arrays.asList(new Property("severity", "warning")));
    return root;
  }

  /**
   * Builds the rules fixture.
   * 
   * @return the list< rule>
   */
  private List<Rule> buildRulesFixture() {
    final Rule rule1 = new Rule("Translation", "com.puppycrawl.tools.checkstyle.checks.TranslationCheck", "Checker/Translation", null,
        PhpCodesnifferPlugin.KEY, null);
    RuleParam ruleParam1 = new RuleParam(rule1, "fileExtensions", null, "s{}");
    rule1.setParams(Arrays.asList(ruleParam1));

    final Rule rule2 = new Rule("AnonInnerLength", "com.puppycrawl.tools.checkstyle.checks.sizes.AnonInnerLengthCheck",
        "Checker/TreeWalker/AnonInnerLength", null, PhpCodesnifferPlugin.KEY, null);
    RuleParam ruleParam2 = new RuleParam(rule2, "max", null, "i");
    rule2.setParams(Arrays.asList(ruleParam2));

    final Rule rule3 = new Rule("Type Name", "com.puppycrawl.tools.checkstyle.checks.naming.TypeNameCheck", "Checker/TreeWalker/TypeName",
        null, PhpCodesnifferPlugin.KEY, null);
    rule3.setPriority(RulePriority.MINOR);
    RuleParam ruleParam3 = new RuleParam(rule3, "format", null, "r");
    rule3.setParams(Arrays.asList(ruleParam3));

    return Arrays.asList(rule1, rule2, rule3);
  }

  /**
   * Do export profile to codesniffer format.
   */
  @Test
  public void doExportProfileToCodesnifferFormat() {
    Rule rule = new Rule();
    rule.setConfigKey("Checker/TreeWalker/Rule");
    rule.setPluginName(PhpCodesnifferPlugin.KEY);
    RuleParam ruleParam = new RuleParam(rule, "a_name", null, null);
    ActiveRule activeRule = new ActiveRule(null, rule, RulePriority.CRITICAL);
    activeRule.setActiveRuleParams(Arrays.asList(new ActiveRuleParam(activeRule, ruleParam, "a_value")));

    Module tree = repository.toXStream(Arrays.asList(activeRule));

    assertThat(tree.getName(), is("Checker"));
    assertThat(tree.getChildren().size(), is(1));

    Module child = tree.getChildren().get(0);
    assertThat(child.getName(), is("TreeWalker"));
    assertThat(child.getChildren().size(), is(1));

    Module grandSon = child.getChildren().get(0);
    assertThat(grandSon.getName(), is("Rule"));
    assertThat(grandSon.getProperties().size(), is(2));

    Property property1 = grandSon.getProperties().get(0);
    assertThat(property1.getName(), is("severity"));
    assertThat(property1.getValue(), is("error"));

    Property property2 = grandSon.getProperties().get(1);
    assertThat(property2.getName(), is("a_name"));
    assertThat(property2.getValue(), is("a_value"));
  }

  /**
   * Do not export parameter when no value.
   */
  @Test
  public void doNotExportParameterWhenNoValue() {
    Rule rule = new Rule();
    rule.setConfigKey("Checker/TreeWalker/Rule");
    rule.setPluginName(PhpCodesnifferPlugin.KEY);
    RuleParam ruleParam = new RuleParam(rule, "a_name", null, null);
    ActiveRule activeRule = new ActiveRule(null, rule, RulePriority.CRITICAL);
    activeRule.setActiveRuleParams(Arrays.asList(new ActiveRuleParam(activeRule, ruleParam, "")));

    Module tree = repository.toXStream(Arrays.asList(activeRule));
    Module child = tree.getChildren().get(0);
    Module grandSon = child.getChildren().get(0);
    assertThat(grandSon.getProperties().size(), is(1));
    Property property1 = grandSon.getProperties().get(0);
    assertThat(property1.getName(), is("severity"));
    assertThat(property1.getValue(), is("error"));
  }

  /**
   * Import profile from xml.
   * 
   * @param configurationFile
   *          the configuration file
   * 
   * @return the list< active rule>
   * 
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public List<ActiveRule> importProfileFromXml(String configurationFile) throws IOException {
    final List<Rule> inputRules = buildRulesFixture();
    List<ActiveRule> activeRulesExpected = buildActiveRulesFixture(inputRules);

    InputStream input = getClass().getResourceAsStream("/org/sonar/plugins/php/codesniffer/" + configurationFile);
    List<ActiveRule> results = repository.importConfiguration(IOUtils.toString(input), inputRules);

    assertThat(results.size(), is(activeRulesExpected.size()));
    assertActiveRulesAreEquals(results, activeRulesExpected);

    return results;
  }

  /**
   * Import xml profile with defined priorities.
   * 
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  @Test
  public void importXmlProfileWithDefinedPriorities() throws IOException {
    final List<Rule> rules = buildRulesFixture();
    InputStream input = getClass().getResourceAsStream(
        "/org/sonar/plugins/php/codesniffer/PhpCodesnifferRulesRepositoryTest/importXmlProfileWithDefinedPriorities.xml");
    List<ActiveRule> results = repository.importConfiguration(IOUtils.toString(input), rules);
    assertThat(results.size(), is(3));

    assertActiveRule(results.get(0), "Checker/Translation", RulePriority.INFO, 1);
    assertThat(results.get(0).getActiveRuleParams().get(0).getValue(), is("properties"));

    assertActiveRule(results.get(1), "Checker/TreeWalker/AnonInnerLength", RulePriority.BLOCKER, 1);
    assertThat(results.get(1).getActiveRuleParams().get(0).getValue(), is("15"));

    assertActiveRule(results.get(2), "Checker/TreeWalker/TypeName", RulePriority.MAJOR, 0);
  }

  /**
   * Import xml profile with metadata to exclude.
   * 
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  @Test
  public void importXmlProfileWithMetadataToExclude() throws IOException {
    final List<Rule> rules = buildRulesFixture();
    InputStream input = getClass().getResourceAsStream(
        "/org/sonar/plugins/php/codesniffer/PhpCodesnifferRulesRepositoryTest/importXmlProfileWithMetadataToExclude.xml");
    List<ActiveRule> results = repository.importConfiguration(IOUtils.toString(input), rules);
    assertThat(results.size(), is(3));

    assertActiveRule(results.get(0), "Checker/Translation", RulePriority.BLOCKER, 1);
    assertActiveRule(results.get(1), "Checker/TreeWalker/AnonInnerLength", RulePriority.BLOCKER, 1);
    assertActiveRule(results.get(2), "Checker/TreeWalker/TypeName", RulePriority.MAJOR, 0);
  }

  /**
   * Import xml profile with suppression comment filter.
   * 
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  @Test
  public void importXmlProfileWithSuppressionCommentFilter() throws IOException {
    final List<Rule> rules = buildRulesFixture();
    InputStream input = getClass().getResourceAsStream(
        "/org/sonar/plugins/php/codesniffer/PhpCodesnifferRulesRepositoryTest/importXmlProfileWithSuppressionCommentFilter.xml");
    List<ActiveRule> results = repository.importConfiguration(IOUtils.toString(input), rules);
    assertThat(results.size(), is(3));

    assertActiveRule(results.get(0), "Checker/Translation", RulePriority.BLOCKER, 1);
    assertActiveRule(results.get(1), "Checker/TreeWalker/AnonInnerLength", RulePriority.BLOCKER, 1);
    assertActiveRule(results.get(2), "Checker/TreeWalker/TypeName", RulePriority.MAJOR, 0);
  }

  /**
   * Rules are defined with the default sonar xml format.
   */
  @Test
  public void rulesAreDefinedWithTheDefaultSonarXmlFormat() {
    List<Rule> rules = repository.getInitialReferential();
    assertTrue(rules.size() > 0);
    for (Rule rule : rules) {
      assertNotNull(rule.getKey());
      assertNotNull(rule.getDescription());
      assertNotNull(rule.getConfigKey());
      assertNotNull(rule.getName());
      assertNotNull(rule.getPriority());
    }
  }

  /**
   * Setup.
   */
  @Before
  public void setup() {
    repository = new PhpCodesnifferRulesRepository();
  }

  /**
   * Should add header to xml.
   * 
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SAXException
   *           the SAX exception
   * @throws ParserConfigurationException
   *           the parser configuration exception
   */
  @Test
  public void shouldAddHeaderToXml() throws IOException, SAXException, ParserConfigurationException {
    String xml = repository.addXmlHeader("<module/>");

    assertXmlAreSimilar(xml, "test_header.xml");
  }

  /**
   * Should build many modules.
   */
  @Test
  public void shouldBuildManyModules() {
    Rule rule1 = new Rule();
    rule1.setPluginName(PhpCodesnifferPlugin.KEY);
    rule1.setConfigKey("Checker/TreeWalker/Rule");
    ActiveRule activeRule1 = new ActiveRule(null, rule1, RulePriority.CRITICAL);
    Rule rule2 = new Rule();
    rule2.setPluginName(PhpCodesnifferPlugin.KEY);
    rule2.setConfigKey("Checker/TreeWalker/AnonInnerLength");
    ActiveRule activeRule2 = new ActiveRule(null, rule2, RulePriority.CRITICAL);

    Module tree = repository.toXStream(Arrays.asList(activeRule1, activeRule2));

    assertThat(tree.getName(), is("Checker"));
    assertThat(tree.getChildren().size(), is(1));

    Module child = tree.getChildren().get(0);
    assertThat(child.getName(), is("TreeWalker"));
    assertThat(child.getChildren().size(), is(2));

    Module grandSon1 = child.getChildren().get(0);
    assertThat(grandSon1.getName(), is("Rule"));

    Module grandSon2 = child.getChildren().get(1);
    assertThat(grandSon2.getName(), is("AnonInnerLength"));
  }

  /**
   * Should build module with config key with only two level config.
   */
  @Test
  public void shouldBuildModuleWithConfigKeyWithOnlyTwoLevelConfig() {
    Rule rule1 = new Rule();
    rule1.setPluginName(PhpCodesnifferPlugin.KEY);
    rule1.setConfigKey("Checker/Rule");
    ActiveRule activeRule1 = new ActiveRule(null, rule1, RulePriority.CRITICAL);

    Module tree = repository.toXStream(Arrays.asList(activeRule1));

    assertThat(tree.getName(), is("Checker"));
    assertThat(tree.getChildren().size(), is(1));

    Module child = tree.getChildren().get(0);
    assertThat(child.getName(), is("Rule"));
  }

  /**
   * Should build only one module when no active rules.
   */
  @Test
  public void shouldBuildOnlyOneModuleWhenNoActiveRules() {
    Module tree = repository.toXStream(Collections.<ActiveRule> emptyList());
    assertThat(tree.getName(), is("Checker"));
    assertThat(tree.getChildren().size(), is(0));
  }

  /**
   * Should build only one module when no codesniffer active rules.
   */
  @Test
  public void shouldBuildOnlyOneModuleWhenNoCodesnifferActiveRules() {
    Rule rule1 = new Rule();
    rule1.setPluginName("not-a-checktyle-plugin");
    ActiveRule activeRule1 = new ActiveRule(null, rule1, RulePriority.CRITICAL);
    Rule rule2 = new Rule();
    rule2.setPluginName("not-a-checktyle-plugin");
    ActiveRule activeRule2 = new ActiveRule(null, rule1, RulePriority.CRITICAL);

    Module tree = repository.toXStream(Arrays.asList(activeRule1, activeRule2));
    assertThat(tree.getName(), is("Checker"));
    assertThat(tree.getChildren().size(), is(0));
  }

  /**
   * Should build two modules even if same two rules activated.
   */
  @Test
  public void shouldBuildTwoModulesEvenIfSameTwoRulesActivated() {
    Rule rule1 = new Rule();
    rule1.setPluginName(PhpCodesnifferPlugin.KEY);
    rule1.setConfigKey("Checker/TreeWalker/Rule");
    ActiveRule activeRule1 = new ActiveRule(null, rule1, RulePriority.CRITICAL);
    Rule rule2 = new Rule();
    rule2.setPluginName(PhpCodesnifferPlugin.KEY);
    rule2.setConfigKey("Checker/TreeWalker/Rule");
    ActiveRule activeRule2 = new ActiveRule(null, rule2, RulePriority.CRITICAL);

    Module tree = repository.toXStream(Arrays.asList(activeRule1, activeRule2));
    assertThat(tree.getName(), is("Checker"));
    assertThat(tree.getChildren().size(), is(1));

    Module child = tree.getChildren().get(0);
    assertThat(child.getName(), is("TreeWalker"));
    assertThat(child.getChildren().size(), is(2));

    Module grandSon1 = child.getChildren().get(0);
    assertThat(grandSon1.getName(), is("Rule"));

    Module grandSon2 = child.getChildren().get(1);
    assertThat(grandSon2.getName(), is("Rule"));
  }

  /**
   * Should build xml from module tree.
   * 
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SAXException
   *           the SAX exception
   * @throws ParserConfigurationException
   *           the parser configuration exception
   */
  @Test
  public void shouldBuildXmlFromModuleTree() throws IOException, SAXException, ParserConfigurationException {
    Module root = buildModuleTreeFixture();
    assertXmlAreSimilar(root.toXml(), "test_module_tree.xml");
  }

  /**
   * Should build x stream from xml.
   * 
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  @Test
  public void shouldBuildXStreamFromXml() throws IOException {
    InputStream input = getClass().getResourceAsStream("/org/sonar/plugins/php/codesniffer/xml/ModuleTest/shouldBuildXStreamFromXml.xml");
    Module module = Module.fromXml(IOUtils.toString(input));

    assertThat(module.getName(), is("Checker"));
    assertThat(module.getChildren().size(), is(2));

    Module child1 = module.getChildren().get(0);
    assertThat(child1.getName(), is("Translation"));
    assertThat(child1.getProperties().size(), is(2));
    assertThat(child1.getProperties().get(0).getName(), is("severity"));
    assertThat(child1.getProperties().get(0).getValue(), is("error"));
    assertThat(child1.getProperties().get(1).getName(), is("fileExtensions"));
    assertThat(child1.getProperties().get(1).getValue(), is("properties"));

    Module child2 = module.getChildren().get(1);
    assertThat(child2.getName(), is("TreeWalker"));
    assertThat(child2.getChildren().size(), is(2));

    Module grandSon1 = child2.getChildren().get(0);
    assertThat(grandSon1.getName(), is("AnonInnerLength"));
    assertThat(grandSon1.getProperties().size(), is(1));
    assertThat(grandSon1.getProperties().get(0).getName(), is("max"));
    assertThat(grandSon1.getProperties().get(0).getValue(), is("15"));

    Module grandSon2 = child2.getChildren().get(1);
    assertThat(grandSon2.getName(), is("TypeName"));
    assertThat(grandSon2.getProperties().size(), is(1));
    assertThat(grandSon2.getProperties().get(0).getName(), is("severity"));
    assertThat(grandSon2.getProperties().get(0).getValue(), is("warning"));
  }

  /**
   * Should export configuration to xml.
   * 
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SAXException
   *           the SAX exception
   * @throws ParserConfigurationException
   *           the parser configuration exception
   */
  @Test
  public void shouldExportConfigurationToXml() throws IOException, SAXException, ParserConfigurationException {
    List<Rule> rules = buildRulesFixture();
    List<ActiveRule> activeRulesExpected = buildActiveRulesFixture(rules);
    // add a parameter without value
    activeRulesExpected.get(2).setActiveRuleParams(
        Arrays.asList(new ActiveRuleParam(activeRulesExpected.get(2), rules.get(2).getParams().get(0), "")));

    RulesProfile activeProfile = new RulesProfile();
    activeProfile.setActiveRules(activeRulesExpected);
    String xml = repository.exportConfiguration(activeProfile);
    assertXmlAreSimilar(xml, "PhpCodesnifferRulesRepositoryTest/shouldExportConfigurationToXml.xml");
  }

  // ------------------------------------------------------------------------
  // -- Private methods
  // ------------------------------------------------------------------------

  /**
   * Should export utf8 characters.
   * 
   * @throws Exception
   *           the exception
   */
  @Test
  public void shouldExportUtf8Characters() throws Exception {
    Rule rule = new Rule("Translation", "com.puppycrawl.tools.checkstyle.checks.TranslationCheck", "Checker/Translation", null,
        PhpCodesnifferPlugin.KEY, null);
    RuleParam ruleParam1 = new RuleParam(rule, "fileExtensions", null, null);
    rule.setParams(Arrays.asList(ruleParam1));
    List<Rule> rules = Arrays.asList(rule);

    ActiveRule activeRule = new ActiveRule(null, rules.get(0), RulePriority.CRITICAL);
    activeRule.setActiveRuleParams(Arrays.asList(new ActiveRuleParam(activeRule, rules.get(0).getParams().get(0), "\u00E9")));
    List<ActiveRule> activeRulesExpected = Arrays.asList(activeRule);

    RulesProfile activeProfile = new RulesProfile();
    activeProfile.setActiveRules(activeRulesExpected);
    String xml = repository.exportConfiguration(activeProfile);
    assertXmlAreSimilar(xml, "PhpCodesnifferRulesRepositoryTest/shouldExportUtf8Characters.xml");
  }

  /**
   * Should import profile with a param value equal to id.
   * 
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  @Test
  public void shouldImportProfileWithAParamValueEqualToId() throws IOException {
    final List<Rule> rules = new ArrayList<Rule>();
    Rule rule = new Rule("Test", "checks.Test", "Checker/Test", null, PhpCodesnifferPlugin.KEY, null);
    RuleParam ruleParam1 = new RuleParam(rule, "id", null, "s");
    rule.setParams(Arrays.asList(ruleParam1));
    rules.add(rule);

    InputStream input = getClass().getResourceAsStream("/org/sonar/plugins/php/codesniffer/test_xml_with_param_value_id.xml");
    List<ActiveRule> results = repository.importConfiguration(IOUtils.toString(input), rules);

    assertThat(results.size(), is(1));
    assertThat(results.get(0).getActiveRuleParams().get(0).getRuleParam().getKey(), is("id"));
    assertThat(results.get(0).getActiveRuleParams().get(0).getValue(), is("[QP-COD-0000]"));
  }

  /**
   * Should import when importing many metadata.
   * 
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  @Test
  public void shouldImportWhenImportingManyMetadata() throws IOException {

    final List<Rule> rules = buildRulesFixture();
    InputStream input = getClass().getResourceAsStream(
        "/org/sonar/plugins/php/codesniffer/PhpCodesnifferRulesRepositoryTest/shouldImportWhenImportingMetadataNodes.xml");
    List<ActiveRule> activeRules = repository.importConfiguration(IOUtils.toString(input), rules);

    assertThat(activeRules.size(), is(1));
    assertThat(activeRules.get(0).getPriority(), is(RulePriority.BLOCKER));
    assertThat(activeRules.get(0).getRuleKey(), is("com.puppycrawl.tools.checkstyle.checks.sizes.AnonInnerLengthCheck"));
    assertThat(activeRules.get(0).getActiveRuleParams().size(), is(1)); // param
    // "max"

  }

  /**
   * Should provide profiles.
   */
  @Test
  public void shouldProvideProfiles() {
    // FIXME implement when php temas gives me ruleset and profiles
    // List<RulesProfile> profiles = repository.getProvidedProfiles();
    // assertThat(profiles.size(), is(3));
    //
    // RulesProfile profile1 = profiles.get(0);
    // assertThat(profile1.getName(), is("SQLI_WAY"));
    // assertTrue(profile1.getActiveRules().size() + "",
    // profile1.getActiveRules().size() > 30);
  }

  /**
   * Use rule priority when xml profile does not define priorities.
   * 
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  @Test
  public void useRulePriorityWhenXmlProfileDoesNotDefinePriorities() throws IOException {
    // FIXME when php teams gives me rulesets.
    // final List<Rule> rules = buildRulesFixture();
    // InputStream input =
    // getClass().getResourceAsStream("/org/sonar/plugins/php/codesniffer/PhpCodesnifferRulesRepositoryTest/useRulePriorityWhenXmlProfileDoesNotDefinePriorities.xml");
    // List<ActiveRule> results =
    // repository.importConfiguration(IOUtils.toString(input), rules);
    // assertThat(results.size(), is(3));
    //
    // assertActiveRule(results.get(0), "Checker/Translation",
    // RulePriority.MAJOR, 0);
    //
    // assertActiveRule(results.get(1),
    // "Checker/TreeWalker/AnonInnerLength", RulePriority.MAJOR, 1);
    // assertThat(results.get(1).getActiveRuleParams().get(0).getValue(),
    // is("15"));
    //
    // assertActiveRule(results.get(2), "Checker/TreeWalker/TypeName",
    // RulePriority.MINOR, 0);
  }
}
