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

import static org.sonar.plugins.php.api.Php.PHP;
import static org.sonar.plugins.php.pmd.PhpmdRuleRepository.PHPMD_REPOSITORY_KEY;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.Violation;

/**
 * The plugin entry point.
 */
public class PhpmdSensor implements Sensor {

  /** The logger. */
  private static final Logger LOG = LoggerFactory.getLogger(PhpmdSensor.class);

  /** The rules profile. */
  private RulesProfile profile;

  /** The plugin configuration. */
  private PhpmdConfiguration configuration;

  /** The plugin configuration. */
  private PhpmdExecutor executor;

  /**
   * /** Instantiates a new php pmd sensor.
   * 
   * @param rulesManager
   *          the rules manager
   */
  public PhpmdSensor(PhpmdConfiguration conf, PhpmdExecutor executor, RulesProfile profile) {
    super();
    this.configuration = conf;
    this.profile = profile;
    this.executor = executor;
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
      Rule rule = Rule.create(PHPMD_REPOSITORY_KEY, violation.getRuleKey());
      org.sonar.api.resources.File resource = org.sonar.api.resources.File.fromIOFile(new File(violation.getFileName()), project);
      if (context.getResource(resource) != null) {
        Violation v = Violation.create(rule, resource).setLineId(violation.getBeginLine()).setMessage(violation.getLongMessage());
        contextViolations.add(v);
        LOG.debug("Violation found: " + v);
      }
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

    return !configuration.isSkip() && !profile.getActiveRulesByRepository(PHPMD_REPOSITORY_KEY).isEmpty();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "PHP Mess Detector Sensor";
  }

}
