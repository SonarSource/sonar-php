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
package org.sonar.plugins.php;

import org.sonar.api.Extension;
import org.sonar.api.SonarPlugin;
import org.sonar.plugins.php.api.Php;
import org.sonar.plugins.php.codesniffer.PhpCodeSnifferConfiguration;
import org.sonar.plugins.php.codesniffer.PhpCodeSnifferExecutor;
import org.sonar.plugins.php.codesniffer.PhpCodeSnifferPriorityMapper;
import org.sonar.plugins.php.codesniffer.PhpCodeSnifferProfileExporter;
import org.sonar.plugins.php.codesniffer.PhpCodeSnifferProfileImporter;
import org.sonar.plugins.php.codesniffer.PhpCodeSnifferRuleRepository;
import org.sonar.plugins.php.codesniffer.PhpCodeSnifferSensor;
import org.sonar.plugins.php.codesniffer.PhpCodeSnifferViolationsXmlParser;
import org.sonar.plugins.php.core.NoSonarAndCommentedOutLocSensor;
import org.sonar.plugins.php.core.PhpSourceCodeColorizer;
import org.sonar.plugins.php.core.PhpSourceImporter;
import org.sonar.plugins.php.core.profiles.AllPhpCSProfile;
import org.sonar.plugins.php.core.profiles.AllPhpmdProfile;
import org.sonar.plugins.php.core.profiles.PearProfile;
import org.sonar.plugins.php.core.profiles.SonarWayProfile;
import org.sonar.plugins.php.core.profiles.ZendProfile;
import org.sonar.plugins.php.duplications.PhpCPDMapping;
import org.sonar.plugins.php.phpdepend.PhpDependConfiguration;
import org.sonar.plugins.php.phpdepend.PhpDependExecutor;
import org.sonar.plugins.php.phpdepend.PhpDependParserSelector;
import org.sonar.plugins.php.phpdepend.PhpDependPhpUnitReportParser;
import org.sonar.plugins.php.phpdepend.PhpDependSensor;
import org.sonar.plugins.php.phpdepend.PhpDependSummaryReportParser;
import org.sonar.plugins.php.phpunit.PhpUnitConfiguration;
import org.sonar.plugins.php.phpunit.PhpUnitCoverageDecorator;
import org.sonar.plugins.php.phpunit.PhpUnitCoverageResultParser;
import org.sonar.plugins.php.phpunit.PhpUnitExecutor;
import org.sonar.plugins.php.phpunit.PhpUnitResultParser;
import org.sonar.plugins.php.phpunit.PhpUnitSensor;
import org.sonar.plugins.php.pmd.PhpmdConfiguration;
import org.sonar.plugins.php.pmd.PhpmdExecutor;
import org.sonar.plugins.php.pmd.PhpmdProfileExporter;
import org.sonar.plugins.php.pmd.PhpmdProfileImporter;
import org.sonar.plugins.php.pmd.PhpmdRuleRepository;
import org.sonar.plugins.php.pmd.PhpmdSensor;
import org.sonar.plugins.php.pmd.PmdRulePriorityMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is the sonar entry point of this plugin. It declares all the extension that can be launched with this plugin.
 */
public class PhpPlugin extends SonarPlugin {

  /**
   * Gets the extensions.
   * 
   * @return the extensions
   * @see org.sonar.api.Plugin#getExtensions()
   */
  public List<Class<? extends Extension>> getExtensions() {
    List<Class<? extends Extension>> extensions = new ArrayList<Class<? extends Extension>>();

    extensions.add(Php.class);

    // Core extensions
    extensions.add(PhpSourceImporter.class);
    extensions.add(PhpSourceCodeColorizer.class);
    extensions.add(NoSonarAndCommentedOutLocSensor.class);

    // Profiles
    extensions.add(SonarWayProfile.class);
    extensions.add(AllPhpmdProfile.class);
    extensions.add(AllPhpCSProfile.class);
    extensions.add(PearProfile.class);
    extensions.add(ZendProfile.class);

    // Duplications
    extensions.add(PhpCPDMapping.class);

    // PhpDepend
    extensions.add(PhpDependExecutor.class);
    extensions.add(PhpDependParserSelector.class);
    extensions.add(PhpDependPhpUnitReportParser.class);
    extensions.add(PhpDependSummaryReportParser.class);
    extensions.add(PhpDependConfiguration.class);
    extensions.add(PhpDependSensor.class);

    // Phpmd
    extensions.add(PhpmdSensor.class);
    extensions.add(PhpmdRuleRepository.class);
    extensions.add(PhpmdConfiguration.class);
    extensions.add(PhpmdExecutor.class);
    extensions.add(PmdRulePriorityMapper.class);
    extensions.add(PhpmdProfileImporter.class);
    extensions.add(PhpmdProfileExporter.class);

    // Code sniffer
    extensions.add(PhpCodeSnifferRuleRepository.class);
    extensions.add(PhpCodeSnifferExecutor.class);
    extensions.add(PhpCodeSnifferViolationsXmlParser.class);
    extensions.add(PhpCodeSnifferSensor.class);
    extensions.add(PhpCodeSnifferConfiguration.class);
    extensions.add(PhpCodeSnifferPriorityMapper.class);
    extensions.add(PhpCodeSnifferProfileExporter.class);
    extensions.add(PhpCodeSnifferProfileImporter.class);

    // PhpUnit
    extensions.add(PhpUnitConfiguration.class);
    extensions.add(PhpUnitSensor.class);
    extensions.add(PhpUnitExecutor.class);
    extensions.add(PhpUnitResultParser.class);
    extensions.add(PhpUnitCoverageResultParser.class);
    extensions.add(PhpUnitCoverageDecorator.class);

    return extensions;
  }
}
