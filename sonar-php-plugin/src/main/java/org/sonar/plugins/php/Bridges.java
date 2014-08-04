/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
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

import org.sonar.api.CoreProperties;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.checks.CheckFactory;
import org.sonar.api.config.Settings;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.plugins.php.bridges.Bridge;
import org.sonar.plugins.php.bridges.BridgeFactory;
import org.sonar.plugins.php.bridges.ResourceIndex;
import org.sonar.squidbridge.api.*;
import org.sonar.squidbridge.indexer.QueryByType;

import java.util.Collection;
import java.util.List;

public class Bridges {

  private final PHPSquid squid;
  private final Settings settings;
  private final SourceCodeSearchEngine index;

  public Bridges(PHPSquid squid, SourceCodeSearchEngine index, Settings settings) {
    this.squid = squid;
    this.index = index;
    this.settings = settings;
  }

  public void save(SensorContext context, Project project, CheckFactory checkFactory, RulesProfile profile) {
    boolean skipPackageDesignAnalysis = settings.getBoolean(CoreProperties.DESIGN_SKIP_PACKAGE_DESIGN_PROPERTY);
    ResourceIndex resourceIndex = new ResourceIndex(skipPackageDesignAnalysis).loadSquidResources(index, context, project);
    List<Bridge> bridges = BridgeFactory.create(
      skipPackageDesignAnalysis,
      context,
      checkFactory,
      resourceIndex,
      squid,
      profile);
    saveProject(resourceIndex, bridges);
    savePackages(resourceIndex, bridges);
    saveFiles(resourceIndex, bridges);
  }

  private void saveProject(ResourceIndex resourceIndex, List<Bridge> bridges) {
    SourceProject squidProject = (SourceProject) index.search(new QueryByType(SourceProject.class)).iterator().next();
    Resource sonarResource = resourceIndex.get(squidProject);
    for (Bridge bridge : bridges) {
      bridge.onProject(squidProject, (Project) sonarResource);
    }
  }

  private void savePackages(ResourceIndex resourceIndex, List<Bridge> bridges) {
    Collection<SourceCode> packages = index.search(new QueryByType(SourcePackage.class));
    for (SourceCode squidPackage : packages) {
      Resource sonarPackage = resourceIndex.get(squidPackage);
      if (sonarPackage != null) {
        for (Bridge bridge : bridges) {
          bridge.onPackage((SourcePackage) squidPackage, sonarPackage);
        }
      }
    }
  }

  private void saveFiles(ResourceIndex resourceIndex, List<Bridge> bridges) {
    /**
     * todo exclude test files
     */
    Collection<SourceCode> squidFiles = index.search(
      new QueryByType(SourceFile.class)
    );
    for (SourceCode squidFile : squidFiles) {
      Resource sonarFile = resourceIndex.get(squidFile);
      if (sonarFile != null) {
        for (Bridge bridge : bridges) {
          bridge.onFile((SourceFile) squidFile, sonarFile);
        }
      }
    }
  }

}
