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

package org.sonar.plugins.php.core;

import static org.sonar.plugins.php.codesniffer.PhpCodeSnifferConfiguration.PHPCS_ANALYZE_ONLY_DESCRIPTION;
import static org.sonar.plugins.php.codesniffer.PhpCodeSnifferConfiguration.PHPCS_ANALYZE_ONLY_KEY;
import static org.sonar.plugins.php.codesniffer.PhpCodeSnifferConfiguration.PHPCS_ANALYZE_ONLY_MESSAGE;
import static org.sonar.plugins.php.codesniffer.PhpCodeSnifferConfiguration.PHPCS_DEFAULT_ANALYZE_ONLY;
import static org.sonar.plugins.php.codesniffer.PhpCodeSnifferConfiguration.PHPCS_DEFAULT_SHOULD_RUN;
import static org.sonar.plugins.php.codesniffer.PhpCodeSnifferConfiguration.PHPCS_DEFAULT_STANDARD_ARGUMENT;
import static org.sonar.plugins.php.codesniffer.PhpCodeSnifferConfiguration.PHPCS_SHOULD_RUN_KEY;
import static org.sonar.plugins.php.codesniffer.PhpCodeSnifferConfiguration.PHPCS_STANDARD_DESCRIPTION;
import static org.sonar.plugins.php.codesniffer.PhpCodeSnifferConfiguration.PHPCS_STANDARD_MESSAGE;
import static org.sonar.plugins.php.core.PhpPlugin.DEFAULT_SUFFIXES;
import static org.sonar.plugins.php.core.PhpPlugin.FILE_SUFFIXES_KEY;
import static org.sonar.plugins.php.core.PhpPluginConfiguration.PDEPEND_EXECUTE_DESCRIPTION;
import static org.sonar.plugins.php.core.PhpPluginConfiguration.PDEPEND_EXECUTE_MESSAGE;
import static org.sonar.plugins.php.core.PhpPluginConfiguration.PHPCPD_EXECUTE_DESCRIPTION;
import static org.sonar.plugins.php.core.PhpPluginConfiguration.PHPCPD_EXECUTE_MESSAGE;
import static org.sonar.plugins.php.core.PhpPluginConfiguration.PHPCPD_MIN_LINES_DESCRIPTION;
import static org.sonar.plugins.php.core.PhpPluginConfiguration.PHPCPD_MIN_LINES_MESSAGE;
import static org.sonar.plugins.php.core.PhpPluginConfiguration.PHPCPD_MIN_TOKENS_MESSAGE;
import static org.sonar.plugins.php.core.PhpPluginConfiguration.PHPCS_EXECUTE_DESCRIPTION;
import static org.sonar.plugins.php.core.PhpPluginConfiguration.PHPCS_EXECUTE_MESSAGE;
import static org.sonar.plugins.php.core.PhpPluginConfiguration.PHPUNIT_COVERAGE_EXECUTE_MESSAGE;
import static org.sonar.plugins.php.core.PhpPluginConfiguration.PHPUNIT_EXECUTE_MESSAGE;
import static org.sonar.plugins.php.core.PhpPluginConfiguration.PHP_FILE_SUFFIXES_DESCRIPTION;
import static org.sonar.plugins.php.core.PhpPluginConfiguration.PHP_FILE_SUFFIXES_MESSAGE;
import static org.sonar.plugins.php.cpd.PhpCpdConfiguration.PHPCPD_DEFAULT_MINIMUM_NUMBER_OF_IDENTICAL_LINES;
import static org.sonar.plugins.php.cpd.PhpCpdConfiguration.PHPCPD_DEFAULT_MINIMUM_NUMBER_OF_IDENTICAL_TOKENS;
import static org.sonar.plugins.php.cpd.PhpCpdConfiguration.PHPCPD_DEFAULT_SHOULD_RUN;
import static org.sonar.plugins.php.cpd.PhpCpdConfiguration.PHPCPD_MINIMUM_NUMBER_OF_IDENTICAL_LINES_KEY;
import static org.sonar.plugins.php.cpd.PhpCpdConfiguration.PHPCPD_MINIMUM_NUMBER_OF_IDENTICAL_TOKENS_KEY;
import static org.sonar.plugins.php.cpd.PhpCpdConfiguration.PHPCPD_SHOULD_RUN_PROPERTY_KEY;
import static org.sonar.plugins.php.phpdepend.PhpDependConfiguration.PDEPEND_ANALYZE_ONLY_DESCRIPTION;
import static org.sonar.plugins.php.phpdepend.PhpDependConfiguration.PDEPEND_ANALYZE_ONLY_MESSAGE;
import static org.sonar.plugins.php.phpdepend.PhpDependConfiguration.PDEPEND_ANALYZE_ONLY_PROPERTY_KEY;
import static org.sonar.plugins.php.phpdepend.PhpDependConfiguration.PDEPEND_DEFAULT_ANALYZE_ONLY;
import static org.sonar.plugins.php.phpdepend.PhpDependConfiguration.PDEPEND_DEFAULT_SHOULD_RUN;
import static org.sonar.plugins.php.phpdepend.PhpDependConfiguration.PDEPEND_SHOULD_RUN_PROPERTY_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_ANALYZE_ONLY_DESCRIPTION;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_ANALYZE_ONLY_PROPERTY_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_DEFAULT_ANALYZE_ONLY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_DEFAULT_MAIN_TEST_FILE;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_DEFAULT_SHOULD_RUN;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_DEFAULT_SHOULD_RUN_COVERAGE;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_MAIN_TEST_FILE_DESCRIPTION;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_MAIN_TEST_FILE_MESSAGE;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_MAIN_TEST_FILE_PROPERTY_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_SHOULD_RUN_COVERAGE_PROPERTY_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_SHOULD_RUN_PROPERTY_KEY;
import static org.sonar.plugins.php.pmd.PhpmdConfiguration.PHPMD_DEFAULT_SHOULD_RUN;
import static org.sonar.plugins.php.pmd.PhpmdConfiguration.PHPMD_SHOULD_RUN_DESCRIPTION;
import static org.sonar.plugins.php.pmd.PhpmdConfiguration.PHPMD_SHOULD_RUN_KEY;
import static org.sonar.plugins.php.pmd.PhpmdConfiguration.PHPMD_SHOULD_RUN_MESSAGE;

import java.util.ArrayList;
import java.util.List;

import org.sonar.api.Extension;
import org.sonar.api.Plugin;
import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.plugins.php.codesniffer.PhpCodeSnifferConfiguration;
import org.sonar.plugins.php.codesniffer.PhpCodeSnifferExecutor;
import org.sonar.plugins.php.codesniffer.PhpCodeSnifferPriorityMapper;
import org.sonar.plugins.php.codesniffer.PhpCodeSnifferProfile;
import org.sonar.plugins.php.codesniffer.PhpCodeSnifferProfileExporter;
import org.sonar.plugins.php.codesniffer.PhpCodeSnifferProfileImporter;
import org.sonar.plugins.php.codesniffer.PhpCodeSnifferRuleRepository;
import org.sonar.plugins.php.codesniffer.PhpCodeSnifferViolationsXmlParser;
import org.sonar.plugins.php.codesniffer.PhpCodesnifferSensor;
import org.sonar.plugins.php.cpd.PhpCpdConfiguration;
import org.sonar.plugins.php.cpd.PhpCpdExecutor;
import org.sonar.plugins.php.cpd.PhpCpdResultParser;
import org.sonar.plugins.php.cpd.PhpCpdSensor;
import org.sonar.plugins.php.phpdepend.PhpDependConfiguration;
import org.sonar.plugins.php.phpdepend.PhpDependExecutor;
import org.sonar.plugins.php.phpdepend.PhpDependResultsParser;
import org.sonar.plugins.php.phpdepend.PhpDependSensor;
import org.sonar.plugins.php.phpunit.PhpUnitConfiguration;
import org.sonar.plugins.php.phpunit.PhpUnitCoverageResultParser;
import org.sonar.plugins.php.phpunit.PhpUnitExecutor;
import org.sonar.plugins.php.phpunit.PhpUnitResultParser;
import org.sonar.plugins.php.phpunit.PhpUnitSensor;
import org.sonar.plugins.php.pmd.PhpmdConfiguration;
import org.sonar.plugins.php.pmd.PhpmdProfile;
import org.sonar.plugins.php.pmd.PhpmdProfileExporter;
import org.sonar.plugins.php.pmd.PhpmdProfileImporter;
import org.sonar.plugins.php.pmd.PhpmdRuleRepository;
import org.sonar.plugins.php.pmd.PhpmdSensor;
import org.sonar.plugins.php.pmd.PmdRulePriorityMapper;

/**
 * This class is the sonar entry point of this plugin. It declares all the extension that can be launched with this plugin
 */
@Properties({
  // Global Php Configuration
  @Property(key = FILE_SUFFIXES_KEY, defaultValue = DEFAULT_SUFFIXES, name = PHP_FILE_SUFFIXES_MESSAGE, project = true, global = true,
      description = PHP_FILE_SUFFIXES_DESCRIPTION),

  // Phpmd configuration: Disabling Phpmd is not a good idea cause almost all metrics rely on it.
  @Property(key = PHPMD_SHOULD_RUN_KEY, defaultValue = PHPMD_DEFAULT_SHOULD_RUN, name = PHPMD_SHOULD_RUN_MESSAGE, project = true,
      global = true, description = PHPMD_SHOULD_RUN_DESCRIPTION),
  @Property(key = PhpmdConfiguration.PHPMD_ANALYZE_ONLY_KEY, defaultValue = PhpmdConfiguration.PHPMD_DEFAULT_ANALYZE_ONLY,
      name = PhpmdConfiguration.PHPMD_ANALYZE_ONLY_MESSAGE, project = true, global = true,
      description = PhpmdConfiguration.PHPMD_ANALYZE_ONLY_DESCRIPTION),

  // PhpCodeSniffer configuration
  @Property(key = PHPCS_SHOULD_RUN_KEY, defaultValue = PHPCS_DEFAULT_SHOULD_RUN, name = PHPCS_EXECUTE_MESSAGE, project = true,
      global = true, description = PHPCS_EXECUTE_DESCRIPTION),
  @Property(key = PHPCS_ANALYZE_ONLY_KEY, defaultValue = PHPCS_DEFAULT_ANALYZE_ONLY, name = PHPCS_ANALYZE_ONLY_MESSAGE, project = true,
      global = true, description = PHPCS_ANALYZE_ONLY_DESCRIPTION),
  @Property(key = PhpCodeSnifferConfiguration.PHPCS_STANDARD_ARGUMENT_KEY, defaultValue = PHPCS_DEFAULT_STANDARD_ARGUMENT,
      name = PHPCS_STANDARD_MESSAGE, project = true, global = true, description = PHPCS_STANDARD_DESCRIPTION),

  // PhPdepend configuration
  @Property(key = PDEPEND_SHOULD_RUN_PROPERTY_KEY, defaultValue = PDEPEND_DEFAULT_SHOULD_RUN, name = PDEPEND_EXECUTE_MESSAGE,
      project = true, global = true, description = PDEPEND_EXECUTE_DESCRIPTION),
  @Property(key = PDEPEND_ANALYZE_ONLY_PROPERTY_KEY, defaultValue = PDEPEND_DEFAULT_ANALYZE_ONLY, name = PDEPEND_ANALYZE_ONLY_MESSAGE,
      project = true, global = true, description = PDEPEND_ANALYZE_ONLY_DESCRIPTION),

  // Phpunit Configuration
  @Property(key = PHPUNIT_SHOULD_RUN_PROPERTY_KEY, defaultValue = PHPUNIT_DEFAULT_SHOULD_RUN, name = PHPUNIT_EXECUTE_MESSAGE,
      project = true, global = true, description = PHPUNIT_EXECUTE_MESSAGE),
  @Property(key = PHPUNIT_SHOULD_RUN_COVERAGE_PROPERTY_KEY, defaultValue = PHPUNIT_DEFAULT_SHOULD_RUN_COVERAGE,
      name = PHPUNIT_COVERAGE_EXECUTE_MESSAGE, project = true, global = true, description = PHPUNIT_COVERAGE_EXECUTE_MESSAGE),
  @Property(key = PHPUNIT_ANALYZE_ONLY_PROPERTY_KEY, defaultValue = PHPUNIT_DEFAULT_ANALYZE_ONLY, name = PHPUNIT_ANALYZE_ONLY_DESCRIPTION,
      project = true, global = true, description = PHPUNIT_COVERAGE_EXECUTE_MESSAGE),
  @Property(key = PHPUNIT_MAIN_TEST_FILE_PROPERTY_KEY, defaultValue = PHPUNIT_DEFAULT_MAIN_TEST_FILE,
      name = PHPUNIT_MAIN_TEST_FILE_MESSAGE, project = true, global = true, description = PHPUNIT_MAIN_TEST_FILE_DESCRIPTION),

  // PhpCpd configuration
  @Property(key = PHPCPD_SHOULD_RUN_PROPERTY_KEY, defaultValue = PHPCPD_DEFAULT_SHOULD_RUN, name = PHPCPD_EXECUTE_MESSAGE, project = true,
      global = true, description = PHPCPD_EXECUTE_DESCRIPTION),
  @Property(key = PHPCPD_MINIMUM_NUMBER_OF_IDENTICAL_LINES_KEY, defaultValue = PHPCPD_DEFAULT_MINIMUM_NUMBER_OF_IDENTICAL_LINES,
      name = PHPCPD_MIN_LINES_MESSAGE, project = true, global = true, description = PHPCPD_MIN_LINES_DESCRIPTION),
  @Property(key = PHPCPD_MINIMUM_NUMBER_OF_IDENTICAL_TOKENS_KEY, defaultValue = PHPCPD_DEFAULT_MINIMUM_NUMBER_OF_IDENTICAL_TOKENS,
      name = PHPCPD_MIN_TOKENS_MESSAGE, project = true, global = true, description = PHPCPD_MIN_TOKENS_MESSAGE) })
public class PhpPlugin implements Plugin, PhpPluginConfiguration {

  /** All the valid php files suffixes. */
  public static final String DEFAULT_SUFFIXES = "php,php3,php4,php5,phtml,inc";

  public static final String FILE_SUFFIXES_KEY = "sonar.php.file.suffixes";

  /** The php plugin key. */
  public static final String KEY = "PHP Language";

  /** The PHPMD plugin KEY. */
  public static final String PHPMD_PLUGIN_KEY = "PHPMD";
  /** The CodeSniffer plugin KEY. */
  public static final String CODESNIFFER_PLUGIN_KEY = "PHP_CodeSniffer";

  public static final String PLUGIN_NAME = "PHP";

  /**
   * Gets the description.
   * 
   * @return the description
   * @see org.sonar.api.Plugin#getDescription()
   */
  public final String getDescription() {
    return "Sonar PHP Plugin is set of tool that brings PHP support to sonar. It rely on Sonar core, PDepend, Phpmd, PHP_CodeSniffer, Phpunit and Phpcpd tools.";
  }

  /**
   * Gets the extensions.
   * 
   * @return the extensions
   * @see org.sonar.api.Plugin#getExtensions()
   */
  public List<Class<? extends Extension>> getExtensions() {
    List<Class<? extends Extension>> extensions = new ArrayList<Class<? extends Extension>>(); // Adds the language

    extensions.add(Php.class);

    // Source importer
    extensions.add(PhpSourceImporter.class);
    // Php Source Code Colorizer
    extensions.add(PhpSourceCodeColorizer.class);

    // Code sniffer
    extensions.add(NoSonarAndCommentedOutLocSensor.class);
    extensions.add(PhpCodeSnifferRuleRepository.class);
    extensions.add(PhpCodeSnifferExecutor.class);
    extensions.add(PhpCodeSnifferViolationsXmlParser.class);
    extensions.add(PhpCodesnifferSensor.class);
    extensions.add(PhpCodeSnifferProfile.class);
    extensions.add(PhpCodeSnifferConfiguration.class);
    extensions.add(PhpCodeSnifferPriorityMapper.class);
    extensions.add(PhpCodeSnifferProfileExporter.class);
    extensions.add(PhpCodeSnifferProfileImporter.class);

    // PhpDepend
    extensions.add(PhpDependExecutor.class);
    extensions.add(PhpDependResultsParser.class);
    extensions.add(PhpDependConfiguration.class);
    extensions.add(PhpDependSensor.class);

    // Phpmd
    extensions.add(PhpmdSensor.class);
    extensions.add(PhpmdRuleRepository.class);
    extensions.add(PhpmdProfile.class);
    extensions.add(PmdRulePriorityMapper.class);
    extensions.add(PhpmdProfileImporter.class);
    extensions.add(PhpmdProfileExporter.class);

    // PhpUnit
    extensions.add(PhpUnitConfiguration.class);
    extensions.add(PhpUnitSensor.class);
    extensions.add(PhpUnitExecutor.class);
    extensions.add(PhpUnitResultParser.class);
    extensions.add(PhpUnitCoverageResultParser.class);

    // Phpcpd
    extensions.add(PhpCpdConfiguration.class);
    extensions.add(PhpCpdExecutor.class);
    extensions.add(PhpCpdResultParser.class);
    extensions.add(PhpCpdSensor.class);

    return extensions;
  }

  /**
   * Gets the key.
   * 
   * @return the key
   * @see org.sonar.api.Plugin#getKey()
   */
  public final String getKey() {
    return KEY;
  }

  /**
   * Gets the name.
   * 
   * @return the name
   * @see org.sonar.api.Plugin#getName()
   */
  public final String getName() {
    return PLUGIN_NAME;
  }

  /**
   * To string.
   * 
   * @return the string
   * @see java.lang.Object#toString()
   */
  @Override
  public final String toString() {
    return getKey();
  }
}
