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

import static org.sonar.plugins.php.api.Php.PHP;
import static org.sonar.plugins.php.codesniffer.PhpCodeSnifferRuleRepository.PHPCS_REPOSITORY_KEY;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.Violation;

/**
 * The Class PhpCodesnifferPluginSensor.
 */
public class PhpCodeSnifferSensor implements Sensor {

  private static final Logger LOG = LoggerFactory.getLogger(PhpCodeSnifferSensor.class);

  private PhpCodeSnifferConfiguration configuration;
  private PhpCodeSnifferExecutor executor;
  private PhpCodeSnifferViolationsXmlParser parser;
  private RulesProfile profile;

  /**
   * Instantiates a new php codesniffer sensor.
   */
  public PhpCodeSnifferSensor(PhpCodeSnifferConfiguration conf, PhpCodeSnifferExecutor executor, RulesProfile profile,
      PhpCodeSnifferViolationsXmlParser parser) {
    super();
    this.configuration = conf;
    this.executor = executor;
    this.parser = parser;
    this.profile = profile;
  }

  /**
   * {@inheritDoc}
   */
  public void analyse(Project project, SensorContext context) {
    configuration.createWorkingDirectory();

    if (!configuration.isAnalyseOnly()) {
      executor.execute();
    }
    File report = configuration.getReportFile();
    List<PhpCodeSnifferViolation> violations = parser.getViolations(report);

    List<Violation> contextViolations = new ArrayList<Violation>();
    Set<String> unfoundViolations = new HashSet<String>();
    for (PhpCodeSnifferViolation violation : violations) {
      RuleFinder ruleFinder = configuration.getRuleFinder();
      String ruleKey = violation.getRuleKey();
      // get the rule from the repository
      Rule rule = ruleFinder.findByKey(PHPCS_REPOSITORY_KEY, ruleKey);
      if (rule != null) {
        org.sonar.api.resources.File resource = org.sonar.api.resources.File.fromIOFile(new File(violation.getFileName()), project);
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
      LOG.info("No violation found in repository " + PHPCS_REPOSITORY_KEY + " for violation " + key);
    }
    context.saveViolations(contextViolations);
  }

  /**
   * {@inheritDoc}
   */
  public boolean shouldExecuteOnProject(Project project) {
    if (!PHP.equals(project.getLanguage())) {
      return false;
    }

    return !configuration.isSkip() && !profile.getActiveRulesByRepository(PHPCS_REPOSITORY_KEY).isEmpty();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "PHP_CodeSniffer Sensor";
  }
}
