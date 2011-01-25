/*
 * Sonar PHP Plugin
 * Copyright (C) 2010 Akram Ben Aissi
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

import static java.lang.Boolean.parseBoolean;
import static org.sonar.plugins.php.codesniffer.PhpCodeSnifferConfiguration.PHPCS_DEFAULT_SHOULD_RUN;
import static org.sonar.plugins.php.codesniffer.PhpCodeSnifferConfiguration.PHPCS_SHOULD_RUN_KEY;
import static org.sonar.plugins.php.codesniffer.PhpCodeSnifferRuleRepository.PHPCS_REPOSITORY_KEY;
import static org.sonar.plugins.php.core.Php.PHP;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Language;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.Violation;
import org.sonar.plugins.php.core.PhpFile;

/**
 * The Class PhpCodesnifferPluginSensor.
 */
public class PhpCodesnifferSensor implements Sensor {

  /** The logger. */
  private static final Logger LOG = LoggerFactory.getLogger(PhpCodesnifferSensor.class);

  private PhpCodeSnifferExecutor executor;

  private PhpCodeSnifferViolationsXmlParser parser;
  /**   */
  private RulesProfile profile;

  /**
   * Instantiates a new php codesniffer sensor.
   * 
   * @param rulesManager
   *          the rules manager
   */
  public PhpCodesnifferSensor(PhpCodeSnifferExecutor executor, PhpCodeSnifferViolationsXmlParser parser, RulesProfile profile) {
    super();
    this.executor = executor;
    this.parser = parser;
    this.profile = profile;
  }

  /**
   * Launches the external tool (if configured so) and analyze result file.
   * 
   * @param project
   *          the project
   * @param context
   *          the context
   * 
   * @see org.sonar.api.batch.Sensor#analyse(org.sonar.api.resources.Project, org.sonar.api.batch.SensorContext)
   */
  public void analyse(Project project, SensorContext context) {
    PhpCodeSnifferConfiguration configuration = executor.getConfiguration();

    if ( !configuration.isAnalyseOnly()) {
      executor.execute();
    }
    File report = configuration.getReportFile();
    LOG.info("PhpCodeSniffer  report file: " + report.getAbsolutePath());
    List<PhpCodeSnifferViolation> violations = parser.getViolations(report);

    // TODO Check that we can't get a rule form other repositories
    String repositoryKey = PhpCodeSnifferRuleRepository.PHPCS_REPOSITORY_KEY;
    List<Violation> contextViolations = new ArrayList<Violation>();
    Set<String> unfoundViolations = new HashSet<String>();
    for (PhpCodeSnifferViolation violation : violations) {
      RuleFinder ruleFinder = configuration.getRuleFinder();
      String ruleKey = violation.getRuleKey();
      Rule rule = ruleFinder.findByKey(repositoryKey, ruleKey);
      if (rule != null) {
        PhpFile resource = (PhpFile) context.getResource(PhpFile.fromAbsolutePath(violation.getFileName(), project));
        if (context.getResource(resource) != null) {
          Violation v = Violation.create(rule, resource).setLineId(violation.getLine()).setMessage(violation.getLongMessage());
          contextViolations.add(v);
          LOG.debug("Violation found: " + v);
        }
      } else {
        unfoundViolations.add(ruleKey);
      }
    }
    for (String key : unfoundViolations) {
      LOG.info("No violation found in repository " + repositoryKey + " for violation " + key);
    }
    context.saveViolations(contextViolations);
  }

  /**
   * Returns <code>true</code> if the given project language is PHP and the project configuration is set to allow plugin to run.
   * 
   * @param project
   *          the project
   * 
   * @return true, if should execute on project
   * 
   * @see org.sonar.api.batch.CheckProject#shouldExecuteOnProject(org.sonar.api .resources.Project)
   */
  public boolean shouldExecuteOnProject(Project project) {
    Configuration configuration = project.getConfiguration();
    Language language = project.getLanguage();
    return (project.getPom() != null) && PHP.equals(language)
        && configuration.getBoolean(PHPCS_SHOULD_RUN_KEY, parseBoolean(PHPCS_DEFAULT_SHOULD_RUN))
        && (project.getReuseExistingRulesConfig() || !profile.getActiveRulesByRepository(PHPCS_REPOSITORY_KEY).isEmpty());
  }

  /**
   * To string.
   * 
   * @return the string
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}