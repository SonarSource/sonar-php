package org.sonar.plugins.php.pmd;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.ActiveRuleParam;
import org.sonar.api.rules.RuleParam;
import org.sonar.api.rules.RulePriority;
import org.sonar.plugins.php.pmd.xml.Property;
import org.sonar.plugins.php.pmd.xml.Rule;
import org.sonar.plugins.php.pmd.xml.Ruleset;
import org.xml.sax.SAXException;

import com.thoughtworks.xstream.converters.ConversionException;

/**
 * This class is only a copy of the PmdRulesRepositoryTest adapted for the php
 * repository.
 * 
 * @author Administrateur
 */
public class PhpPmdRulesRepositoryTest {

	/** The repository. */
	private PhpPmdRulesRepository repository;

	/**
	 * Setup.
	 */
	@Before
	public void setup() {
		repository = new PhpPmdRulesRepository();
	}

	/**
	 * Rules are defined with the default sonar xml format.
	 */
	@Test
	public void rulesAreDefinedWithTheDefaultSonarXmlFormat() {
		List<org.sonar.api.rules.Rule> rules = repository.getInitialReferential();
		assertTrue(rules.size() > 0);
		for (org.sonar.api.rules.Rule rule : rules) {
			assertNotNull(rule.getKey());
			assertNotNull(rule.getDescription());
			assertNotNull(rule.getConfigKey());
			assertNotNull(rule.getName());
		}
	}

	/**
	 * Should importconfiguration with utf8 character.
	 */
	@Test
	public void shouldImportconfigurationWithUtf8Character() {
		RulesProfile rulesProfile = repository.buildProfile("profile", "/org/sonar/plugins/php/pmd/test_xml_utf8.xml");
		assertThat(rulesProfile, notNullValue());

		String value = rulesProfile.getActiveRules().get(0).getActiveRuleParams().get(0).getValue();
		assertThat(value, is("\u00E9"));
	}

	/**
	 * Should build module with properties.
	 */
	@Test
	public void shouldBuildModuleWithProperties() {
		org.sonar.api.rules.Rule dbRule = new org.sonar.api.rules.Rule();
		dbRule.setKey("rulesets/design.xml/CloseResource");
		dbRule.setPluginName(PhpPmdPlugin.KEY);
		RuleParam ruleParam = new RuleParam(dbRule, "types", null, null);
		ActiveRule activeRule = new ActiveRule(null, dbRule, RulePriority.MAJOR);
		activeRule.setActiveRuleParams(Arrays
				.asList(new ActiveRuleParam(activeRule, ruleParam, "Connection,Statement,ResultSet")));

		Ruleset ruleset = repository.buildRuleset(Arrays.asList(activeRule));

		assertThat(ruleset.getRules().size(), is(1));

		Rule rule = ruleset.getRules().get(0);
		assertThat(rule.getName(), is("rulesets/design.xml/CloseResource"));
		assertThat(rule.getProperties().getProperties().size(), is(1));

		assertThat(rule.getPriority(), is("3"));

		Property property = rule.getProperties().getProperties().get(0);
		assertThat(property.getName(), is("types"));
		assertThat(property.getValue(), is("Connection,Statement,ResultSet"));
	}

	/**
	 * Should build many modules.
	 */
	@Test
	public void shouldBuildManyModules() {

		org.sonar.api.rules.Rule rule1 = new org.sonar.api.rules.Rule();
		rule1.setPluginName(PhpPmdPlugin.KEY);
		rule1.setKey("rulesets/design.xml/CloseResource");
		ActiveRule activeRule1 = new ActiveRule(null, rule1, RulePriority.MAJOR);
		org.sonar.api.rules.Rule rule2 = new org.sonar.api.rules.Rule();
		rule2.setPluginName(PhpPmdPlugin.KEY);
		rule2.setKey("rulesets/braces.xml/IfElseStmtsMustUseBraces");
		ActiveRule activeRule2 = new ActiveRule(null, rule2, RulePriority.MAJOR);

		Ruleset ruleset = repository.buildRuleset(Arrays.asList(activeRule1, activeRule2));

		assertThat(ruleset.getRules().size(), is(2));
		assertThat(ruleset.getRules().get(0).getName(), is("rulesets/design.xml/CloseResource"));
		assertThat(ruleset.getRules().get(1).getName(), is("rulesets/braces.xml/IfElseStmtsMustUseBraces"));
	}

	/**
	 * Should build module tree from xml.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void shouldBuildRulesetFromXml() throws IOException {
		InputStream input = getClass().getResourceAsStream("/org/sonar/plugins/php/pmd/test_module_tree.xml");
		Ruleset ruleset = repository.buildRulesetFromXml(IOUtils.toString(input));

		assertThat(ruleset.getRules().size(), is(3));

		Rule rule1 = ruleset.getRules().get(0);
		assertThat(rule1.getName(), is("CyclomaticComplexity"));
		assertThat(rule1.getPriority(), is("2"));
		assertThat(rule1.getProperties().getProperties().size(), is(1));

		Property module1Property = rule1.getProperties().getProperties().get(0);
		assertThat(module1Property.getName(), is("threshold"));
		assertThat(module1Property.getValue(), is("20"));

		Rule rule2 = ruleset.getRules().get(1);
		assertThat(rule2.getName(), is("ExcessiveClassLength"));
		assertThat(rule2.getPriority(), is("3"));
		assertThat(rule2.getProperties().getProperties().size(), is(1));

		Property module2Property = rule2.getProperties().getProperties().get(0);
		assertThat(module2Property.getName(), is("minimum"));
		assertThat(module2Property.getValue(), is("20"));

		Rule rule3 = ruleset.getRules().get(2);
		assertThat(rule3.getName(), is("UnusedFormalParameter"));
		assertThat(rule3.getPriority(), is("4"));
		assertNull(rule3.getProperties().getProperties());
	}

	/**
	 * Should build module tree from xml in utf8.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void shouldBuildRulesetFromXmlInUtf8() throws IOException {
		InputStream input = getClass().getResourceAsStream("/org/sonar/plugins/php/pmd/test_xml_utf8.xml");
		Ruleset ruleset = repository.buildRulesetFromXml(IOUtils.toString(input, "UTF-8"));

		Rule rule1 = ruleset.getRules().get(0);
		assertThat(rule1.getName(), is("CyclomaticComplexity"));
		assertThat(rule1.getProperties().getProperties().get(0).getValue(), is("\u00E9"));
	}

	/**
	 * Should buil xml from module tree.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws SAXException
	 *             the SAX exception
	 */
	@Test
	public void shouldBuilXmlFromRuleset() throws IOException, SAXException {
		Ruleset ruleset = buildRulesetFixture();
		String xml = repository.buildXmlFromRuleset(ruleset);
		assertXmlAreSimilar(xml, "test_module_tree.xml");
	}

	/**
	 * Should import configuration.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void shouldImportConfiguration() throws IOException {
		final List<org.sonar.api.rules.Rule> inputRules = buildRulesFixture();
		List<ActiveRule> activeRulesExpected = buildActiveRulesFixture(inputRules);

		InputStream input = getClass().getResourceAsStream("/org/sonar/plugins/php/pmd/test_module_tree.xml");
		List<ActiveRule> results = repository.importConfiguration(IOUtils.toString(input), inputRules);

		assertThat(results.size(), is(activeRulesExpected.size()));
		assertActiveRulesAreEquals(results, activeRulesExpected);
	}

	/**
	 * Should import pmd levels as sonar levels.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void shouldImportPmdLevelsAsSonarLevels() throws IOException {
		InputStream input = getClass().getResourceAsStream(
				"/org/sonar/plugins/php/pmd/PmdRulesRepositoryTest/shouldImportPmdLevelsAsSonarLevels.xml");
		final List<org.sonar.api.rules.Rule> rules = buildRulesFixture();
		List<ActiveRule> results = repository.importConfiguration(IOUtils.toString(input), rules);

		assertThat(results.size(), is(3));
		assertThat(results.get(0).getPriority(), is(RulePriority.MAJOR));
		assertThat(results.get(1).getPriority(), is(RulePriority.MINOR));
		assertThat(results.get(2).getPriority(), is(RulePriority.INFO));
	}

	/**
	 * Should import with default rule level when no explicit priority.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void shouldImportWithDefaultRuleLevelWhenNoExplicitPriority() throws IOException {
		InputStream input = getClass().getResourceAsStream(
				"/org/sonar/plugins/php/pmd/PmdRulesRepositoryTest/shouldImportWithDefaultRuleLevelWhenNoExplicitPriority.xml");
		final List<org.sonar.api.rules.Rule> rules = buildRulesFixture();
		List<ActiveRule> results = repository.importConfiguration(IOUtils.toString(input), rules);
		assertThat(results.size(), is(1));
		assertThat(results.get(0).getPriority(), is(RulePriority.MAJOR));
	}

	// See http://jira.codehaus.org/browse/XSTR-448 for details
	/**
	 * Should fail to import configuration containing class param because of x
	 * stream limitation.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test(expected = ConversionException.class)
	public void shouldFailToImportConfigurationContainingClassParamBecauseOfXStreamLimitation() throws IOException {
		shouldImportConfiguration("test_xml_with_class_param.xml");
	}

	/**
	 * Should build active rules from module tree.
	 */
	@Test
	public void shouldBuildActiveRulesFromRuleset() {
		final List<org.sonar.api.rules.Rule> inputRules = buildRulesFixture();
		List<ActiveRule> activeRulesExpected = buildActiveRulesFixture(inputRules);

		List<ActiveRule> activeRules = new ArrayList<ActiveRule>();
		Ruleset ruleset = buildRulesetFixture();
		repository.buildActiveRulesFromRuleset(ruleset, activeRules, inputRules);

		assertThat(activeRulesExpected.size(), is(activeRules.size()));
		assertActiveRulesAreEquals(activeRulesExpected, activeRules);
	}

	/**
	 * Should provide profiles.
	 */
	@Test
	public void shouldProvideProfiles() {
		List<RulesProfile> profiles = repository.getProvidedProfiles();
		assertThat(profiles.size(), is(1));

		RulesProfile profile1 = profiles.get(0);
		assertThat(profile1.getName(), is("Default Php Profile"));
		assertTrue(profile1.getActiveRules().size() + "", profile1.getActiveRules().size() > 10);
	}

	/**
	 * Should export configuration.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws SAXException
	 *             the SAX exception
	 */
//	@Test
	public void shouldExportConfiguration() throws IOException, SAXException {
		List<ActiveRule> activeRulesExpected = buildActiveRulesFixture(buildRulesFixture());
		RulesProfile activeProfile = new RulesProfile();
		activeProfile.setActiveRules(activeRulesExpected);
		String xml = repository.exportConfiguration(activeProfile);
		assertXmlAreSimilar(xml, "test_xml_complete.xml");
	}

	/**
	 * Should add header to xml.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws SAXException
	 *             the SAX exception
	 */
	@Test
	public void shouldAddHeaderToXml() throws IOException, SAXException {
		String xml = repository.addHeaderToXml("<ruleset/>");
		assertXmlAreSimilar(xml, "test_header.xml");
	}

	/**
	 * Should build only one module when no pmd active rules.
	 */
	@Test
	public void shouldBuildOnlyOneModuleWhenNoPmdActiveRules() {
		org.sonar.api.rules.Rule rule1 = new org.sonar.api.rules.Rule();
		rule1.setPluginName("not-a-pmd-plugin");
		ActiveRule activeRule1 = new ActiveRule(null, rule1, RulePriority.CRITICAL);
		org.sonar.api.rules.Rule rule2 = new org.sonar.api.rules.Rule();
		rule2.setPluginName("not-a-pmd-plugin");
		ActiveRule activeRule2 = new ActiveRule(null, rule1, RulePriority.CRITICAL);

		Ruleset tree = repository.buildRuleset(Arrays.asList(activeRule1, activeRule2));
		assertThat(tree.getRules().size(), is(0));
	}

	/**
	 * Should build only one module when no active rules.
	 */
	@Test
	public void shouldBuildOnlyOneModuleWhenNoActiveRules() {
		Ruleset tree = repository.buildRuleset(Collections.<ActiveRule> emptyList());
		assertThat(tree.getRules().size(), is(0));
	}

	/**
	 * Should build two modules even if same two rules activated.
	 */
	@Test
	public void shouldBuildTwoModulesEvenIfSameTwoRulesActivated() {
		org.sonar.api.rules.Rule dbRule1 = new org.sonar.api.rules.Rule();
		dbRule1.setPluginName(PhpPmdPlugin.KEY);
		dbRule1.setKey("rulesets/coupling.xml/CouplingBetweenObjects");
		ActiveRule activeRule1 = new ActiveRule(null, dbRule1, RulePriority.CRITICAL);
		org.sonar.api.rules.Rule dbRule2 = new org.sonar.api.rules.Rule();
		dbRule2.setPluginName(PhpPmdPlugin.KEY);
		dbRule2.setKey("rulesets/coupling.xml/CouplingBetweenObjects");
		ActiveRule activeRule2 = new ActiveRule(null, dbRule2, RulePriority.CRITICAL);

		Ruleset tree = repository.buildRuleset(Arrays.asList(activeRule1, activeRule2));
		assertThat(tree.getRules().size(), is(2));

		Rule rule1 = tree.getRules().get(0);
		assertThat(rule1.getName(), is("rulesets/coupling.xml/CouplingBetweenObjects"));

		Rule rule2 = tree.getRules().get(1);
		assertThat(rule2.getName(), is("rulesets/coupling.xml/CouplingBetweenObjects"));
	}

	// ------------------------------------------------------------------------
	// -- Private methods
	// ------------------------------------------------------------------------

	/**
	 * Assert xml are similar.
	 * 
	 * @param xml
	 *            the xml
	 * @param xmlFileToFind
	 *            the xml file to find
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws SAXException
	 *             the SAX exception
	 */
	private void assertXmlAreSimilar(String xml, String xmlFileToFind) throws IOException, SAXException {
		XMLUnit.setIgnoreWhitespace(true);
		InputStream input = getClass().getResourceAsStream("/org/sonar/plugins/php/pmd/" + xmlFileToFind);
		String xmlToFind = IOUtils.toString(input);
		Diff diff = XMLUnit.compareXML(xml, xmlToFind);
		assertTrue(diff.toString(), diff.similar());
	}

	/**
	 * Builds the module tree fixture.
	 * 
	 * @return the ruleset
	 */
	private Ruleset buildRulesetFixture() {
		Ruleset ruleset = new Ruleset("Sonar PHP PMD rules");

		Rule rule1 = new Rule("CyclomaticComplexity", "2");
		rule1.getProperties().add(new Property("threshold", "20"));
		ruleset.getRules().add(rule1);

		Rule rule2 = new Rule("ExcessiveClassLength", "3");
		rule2.getProperties().add(new Property("minimum", "20"));
		ruleset.getRules().add(rule2);

		Rule rule3 = new Rule("UnusedFormalParameter", "4");
		ruleset.getRules().add(rule3);

		return ruleset;
	}

	/**
	 * Builds the rules fixture.
	 * 
	 * @return the list<org.sonar.api.rules. rule>
	 */
	private List<org.sonar.api.rules.Rule> buildRulesFixture() {
		final org.sonar.api.rules.Rule rule1 = new org.sonar.api.rules.Rule("CyclomaticComplexity", "CyclomaticComplexity",
				"CyclomaticComplexity", null, PhpPmdPlugin.KEY, null);
		RuleParam ruleParam1 = new RuleParam(rule1, "threshold", null, "i");
		rule1.setParams(Arrays.asList(ruleParam1));
		rule1.setPriority(RulePriority.CRITICAL);

		final org.sonar.api.rules.Rule rule2 = new org.sonar.api.rules.Rule("ExcessiveClassLength", "ExcessiveClassLength",
				"ExcessiveClassLength", null, PhpPmdPlugin.KEY, null);
		RuleParam ruleParam2 = new RuleParam(rule2, "minimum", null, "i");
		rule2.setParams(Arrays.asList(ruleParam2));
		rule2.setPriority(RulePriority.MAJOR);

		final org.sonar.api.rules.Rule rule3 = new org.sonar.api.rules.Rule("UnusedFormalParameter", "UnusedFormalParameter",
				"UnusedFormalParameter", null, PhpPmdPlugin.KEY, null);
		rule3.setPriority(RulePriority.MINOR);

		return Arrays.asList(rule1, rule2, rule3);
	}

	/**
	 * Builds the active rules fixture.
	 * 
	 * @param rules
	 *            the rules
	 * 
	 * @return the list< active rule>
	 */
	private List<ActiveRule> buildActiveRulesFixture(List<org.sonar.api.rules.Rule> rules) {
		List<ActiveRule> activeRules = new ArrayList<ActiveRule>();
		for (org.sonar.api.rules.Rule rule : rules) {
			ActiveRule activeRule1 = new ActiveRule(null, rule, rule.getPriority());
			if (rule.getParams().size() > 0)
				activeRule1.setActiveRuleParams(Arrays.asList(new ActiveRuleParam(activeRule1, rule.getParams().get(0), "20")));
			activeRules.add(activeRule1);
		}
		return activeRules;

	}

	/**
	 * Assert active rules are equals.
	 * 
	 * @param activeRules1
	 *            the active rules1
	 * @param activeRules2
	 *            the active rules2
	 */
	private void assertActiveRulesAreEquals(List<ActiveRule> activeRules1, List<ActiveRule> activeRules2) {
		for (int i = 0; i < activeRules1.size(); i++) {
			ActiveRule activeRule1 = activeRules1.get(i);
			ActiveRule activeRule2 = activeRules2.get(i);
			assertTrue(activeRule1.getRule().equals(activeRule2.getRule()));
			assertTrue(activeRule1.getPriority().equals(activeRule2.getPriority()));
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
	 * Should import configuration.
	 * 
	 * @param configurationFile
	 *            the configuration file
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void shouldImportConfiguration(String configurationFile) throws IOException {
		final List<org.sonar.api.rules.Rule> inputRules = buildRulesFixture();
		List<ActiveRule> activeRulesExpected = buildActiveRulesFixture(inputRules);

		InputStream input = getClass().getResourceAsStream("/org/sonar/plugins/php/pmd/" + configurationFile);
		List<ActiveRule> results = repository.importConfiguration(IOUtils.toString(input), inputRules);

		assertThat(results.size(), is(activeRulesExpected.size()));
		assertActiveRulesAreEquals(results, activeRulesExpected);
	}

}
