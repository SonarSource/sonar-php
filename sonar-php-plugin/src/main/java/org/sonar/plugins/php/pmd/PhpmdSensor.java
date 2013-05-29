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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.PropertyType;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.Violation;
import org.sonar.plugins.php.api.PhpConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.sonar.plugins.php.core.AbstractPhpConfiguration.DEFAULT_TIMEOUT;
import static org.sonar.plugins.php.pmd.PhpmdConfiguration.PHPMD_ANALYZE_ONLY_KEY;
import static org.sonar.plugins.php.pmd.PhpmdConfiguration.PHPMD_ARGUMENT_LINE_KEY;
import static org.sonar.plugins.php.pmd.PhpmdConfiguration.PHPMD_CUSTOM_RULES_PROP_KEY;
import static org.sonar.plugins.php.pmd.PhpmdConfiguration.PHPMD_LEVEL_ARGUMENT_DEFVALUE;
import static org.sonar.plugins.php.pmd.PhpmdConfiguration.PHPMD_LEVEL_ARGUMENT_KEY;
import static org.sonar.plugins.php.pmd.PhpmdConfiguration.PHPMD_REPORT_FILE_NAME_DEFVALUE;
import static org.sonar.plugins.php.pmd.PhpmdConfiguration.PHPMD_REPORT_FILE_NAME_KEY;
import static org.sonar.plugins.php.pmd.PhpmdConfiguration.PHPMD_REPORT_FILE_RELATIVE_PATH_DEFVALUE;
import static org.sonar.plugins.php.pmd.PhpmdConfiguration.PHPMD_REPORT_FILE_RELATIVE_PATH_KEY;
import static org.sonar.plugins.php.pmd.PhpmdConfiguration.PHPMD_REPORT_PATH_KEY;
import static org.sonar.plugins.php.pmd.PhpmdConfiguration.PHPMD_SKIP_KEY;
import static org.sonar.plugins.php.pmd.PhpmdConfiguration.PHPMD_TIMEOUT_KEY;
import static org.sonar.plugins.php.pmd.PhpmdRuleRepository.PHPMD_REPOSITORY_KEY;

/**
 * The plugin entry point.
 */
@Properties({
  // ------------------ Phpmd configuration ------------------
  @Property(key = PHPMD_SKIP_KEY, defaultValue = "false", name = "Disable PHPMD", project = true, global = true,
    description = "Disabling PHPMD is not a good idea because almost all metrics rely on it.", category = PhpmdSensor.CATEGORY_PHP_PHPMD,
    type = PropertyType.BOOLEAN),
  @Property(key = PHPMD_ANALYZE_ONLY_KEY, defaultValue = "false", name = "Only analyze existing Phpmd report files", project = true,
    global = true, description = "By default, the plugin will launch PHPMD and parse the generated result file."
      + "If this option is set to true, the plugin will only reuse an existing report file.", category = PhpmdSensor.CATEGORY_PHP_PHPMD,
    type = PropertyType.BOOLEAN),
  @Property(key = PHPMD_REPORT_PATH_KEY,
    name = "Report file path", project = true, global = true, description = "Path of the report file to analyse.",
    category = PhpmdSensor.CATEGORY_PHP_PHPMD),
  @Property(key = PHPMD_REPORT_FILE_RELATIVE_PATH_KEY, defaultValue = PHPMD_REPORT_FILE_RELATIVE_PATH_DEFVALUE,
    name = "Report file path", project = true, global = true, description = "[DEPRECATED] Relative path of the report file to analyse. "
      + "This property is deprecated: location of PHPMD report should be defined using the \"" + PHPMD_REPORT_PATH_KEY + "\" property only.",
    category = PhpmdSensor.CATEGORY_PHP_PHPMD),
  @Property(key = PHPMD_REPORT_FILE_NAME_KEY, defaultValue = PHPMD_REPORT_FILE_NAME_DEFVALUE, name = "Report file name", project = true,
    global = true, description = "[DEPRECATED] Name of the report file to analyse. "
      + "This property is deprecated: location of PHPMD report should be defined using the \"" + PHPMD_REPORT_PATH_KEY + "\" property only.",
    category = PhpmdSensor.CATEGORY_PHP_PHPMD),
  @Property(key = PHPMD_LEVEL_ARGUMENT_KEY, defaultValue = PHPMD_LEVEL_ARGUMENT_DEFVALUE, name = "Mimimum priority", project = true,
    global = true, description = "The lowest level events won't be included in report file. "
      + "Values goes from 1(Strong) to 5(Weak) (only integers)", category = PhpmdSensor.CATEGORY_PHP_PHPMD),
  @Property(key = PHPMD_ARGUMENT_LINE_KEY, defaultValue = "", name = "Additional arguments", project = true, global = true,
    description = "Additionnal parameters that can be passed to PHPMD tool.", category = PhpmdSensor.CATEGORY_PHP_PHPMD),
  @Property(key = PHPMD_TIMEOUT_KEY, defaultValue = "" + DEFAULT_TIMEOUT, name = "Timeout", project = true, global = true,
    description = "Maximum number of minutes that the execution of the tool should take.", category = PhpmdSensor.CATEGORY_PHP_PHPMD),
  @Property(key = PHPMD_CUSTOM_RULES_PROP_KEY,
    defaultValue = "", name = "PHPMD custom rules",
    description = "XML description of PHPMD custom rules", type = PropertyType.TEXT,
    global = true, project = false, category = PhpmdSensor.CATEGORY_PHP_PHPMD)
})
public class PhpmdSensor implements Sensor {

  protected static final String CATEGORY_PHP_PHPMD = "PHP PMD";

  /** The logger. */
  private static final Logger LOG = LoggerFactory.getLogger(PhpmdSensor.class);

  /** The rules profile. */
  private RulesProfile profile;

  /** The plugin configuration. */
  private PhpmdConfiguration configuration;

  /** The plugin configuration. */
  private PhpmdExecutor executor;

  /** The rule finder */
  private RuleFinder ruleFinder;

  /**
   * /** Instantiates a new php pmd sensor.
   * 
   * @param rulesManager
   *          the rules manager
   */
  public PhpmdSensor(PhpmdConfiguration conf, PhpmdExecutor executor, RulesProfile profile, RuleFinder ruleFinder) {
    super();
    this.configuration = conf;
    this.profile = profile;
    this.executor = executor;
    this.ruleFinder = ruleFinder;
  }

  /**
   * {@inheritDoc}
   */
  public void analyse(Project project, SensorContext context) {
    if (!configuration.isAnalyseOnly()) {
      configuration.createWorkingDirectory();
      executor.execute();
    }
    File report = configuration.getReportFile();
    PhpmdViolationsXmlParser reportParser = new PhpmdViolationsXmlParser(report);
    List<PhpmdViolation> violations = reportParser.getViolations();
    List<Violation> contextViolations = new ArrayList<Violation>();
    for (PhpmdViolation violation : violations) {
      Rule rule = ruleFinder.findByKey(PHPMD_REPOSITORY_KEY, violation.getRuleKey());
      if (rule != null) {
        org.sonar.api.resources.File resource = org.sonar.api.resources.File.fromIOFile(new File(violation.getFileName()), project);
        if (context.getResource(resource) != null) {
          Violation v = Violation.create(rule, resource).setLineId(violation.getBeginLine()).setMessage(violation.getLongMessage());
          contextViolations.add(v);
          LOG.debug("Violation found: " + v);
        }
      }
    }
    context.saveViolations(contextViolations);
  }

  /**
   * {@inheritDoc}
   */
  public boolean shouldExecuteOnProject(Project project) {
    return PhpConstants.LANGUAGE_KEY.equals(project.getLanguageKey())
      && !configuration.isSkip()
      && !project.getFileSystem().mainFiles(PhpConstants.LANGUAGE_KEY).isEmpty()
      && !profile.getActiveRulesByRepository(PHPMD_REPOSITORY_KEY).isEmpty();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "PHP Mess Detector Sensor";
  }

}
