/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2009 SonarSource SA
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

package org.sonar.plugins.php.phpcodesniffer;

import org.apache.commons.io.FileUtils;
import org.sonar.commons.rules.RulesProfile;
import org.sonar.plugins.api.maven.MavenCollector;
import org.sonar.plugins.api.maven.MavenPluginHandler;
import org.sonar.plugins.api.maven.ProjectContext;
import org.sonar.plugins.api.maven.model.MavenPom;
import org.sonar.plugins.api.rules.RulesManager;
import org.sonar.plugins.php.Php;

import java.io.File;
import java.io.IOException;

public class PhpCodeSnifferMavenCollector implements MavenCollector {

  private RulesManager rulesManager;
  private RulesProfile rulesProfile;
  private PhpCodeSnifferRulesRepository rulesRepository;

  public PhpCodeSnifferMavenCollector(RulesManager rulesManager, RulesProfile rulesProfile,
                                PhpCodeSnifferRulesRepository rulesRepository) {
    this.rulesManager = rulesManager;
    this.rulesProfile = rulesProfile;
    this.rulesRepository = rulesRepository;
  }

  protected PhpCodeSnifferMavenCollector(RulesProfile rulesProfile, PhpCodeSnifferRulesRepository rulesRepository){
    this.rulesProfile = rulesProfile;
    this.rulesRepository = rulesRepository;
  }

  public Class<? extends MavenPluginHandler> dependsOnMavenPlugin(MavenPom pom) {
    return null;
  }

  public boolean shouldCollectOn(MavenPom pom) {
    return Php.KEY.equals(pom.getLanguageKey());
  }

  public void collect(MavenPom pom, ProjectContext context) {
    PhpCodeSnifferConfiguration config = new PhpCodeSnifferConfiguration(pom, rulesProfile.getName());
    PhpCodeSnifferExecutor executor = new PhpCodeSnifferExecutor(config);
    createRulesConfigurationFolderAndFile(config);
    executor.execute();

    PhpCodeSnifferResultsParser parser = new PhpCodeSnifferResultsParser(config, context, rulesManager);
    parser.parse();
  }

  protected void createRulesConfigurationFolderAndFile(PhpCodeSnifferConfiguration config) {
    try {
      String rulesConfiguration = rulesRepository.exportConfiguration(rulesProfile);
      String profileName = rulesProfile.getName();
      File profileDir = config.getProfileDir();
      File configurationFile = new File(profileDir, profileName +"CodingStandard.php");

      FileUtils.forceMkdir(profileDir);
      FileUtils.writeStringToFile(configurationFile, rulesConfiguration, "UTF-8");
    } catch (IOException e) {
      throw new PhpCodeSnifferExecutionException(e);
    }
  }
}