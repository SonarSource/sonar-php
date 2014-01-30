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
import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.SonarPlugin;
import org.sonar.plugins.php.api.Php;
import org.sonar.plugins.php.core.NoSonarAndCommentedOutLocSensor;
import org.sonar.plugins.php.core.PhpCommonRulesEngineProvider;
import org.sonar.plugins.php.core.PhpLexerSensor;
import org.sonar.plugins.php.core.PhpSourceCodeColorizer;
import org.sonar.plugins.php.core.PhpSourceImporter;
import org.sonar.plugins.php.core.profiles.SonarWayProfile;
import org.sonar.plugins.php.duplications.PhpCPDMapping;
import org.sonar.plugins.php.phpunit.PhpUnitConfiguration;
import org.sonar.plugins.php.phpunit.PhpUnitCoverageDecorator;
import org.sonar.plugins.php.phpunit.PhpUnitCoverageResultParser;
import org.sonar.plugins.php.phpunit.PhpUnitExecutor;
import org.sonar.plugins.php.phpunit.PhpUnitResultParser;
import org.sonar.plugins.php.phpunit.PhpUnitSensor;

import java.util.ArrayList;
import java.util.List;

@Properties({
  @Property(
    key = PhpPlugin.FILE_SUFFIXES_KEY,
    defaultValue = Php.DEFAULT_FILE_SUFFIXES,
    name = "File suffixes",
    description = "Comma-separated list of suffixes for files to analyze. To not filter, leave the list empty.",
    global = true,
    project = true)
})
public class PhpPlugin extends SonarPlugin {

  public static final String FILE_SUFFIXES_KEY = "sonar.php.file.suffixes";

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
  //extensions.add(PhpLexerSensor.class); TODO: safely delete
    extensions.add(PhpSourceCodeColorizer.class);
    extensions.add(NoSonarAndCommentedOutLocSensor.class);

    // Duplications
    extensions.add(PhpCPDMapping.class);

    // Common rules
    extensions.add(PhpCommonRulesEngineProvider.class);

    extensions.add(PHPSquidSensor.class);

    extensions.add(PHPRuleRepository.class);
    extensions.add(SonarWayProfile.class);
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
