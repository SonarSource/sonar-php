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
package org.sonar.plugins.php.bridges;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.CoreProperties;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.utils.SonarException;
import org.sonar.squidbridge.api.*;
import org.sonar.squidbridge.indexer.QueryByType;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class ResourceIndex extends HashMap<SourceCode, Resource> {

  private static final long serialVersionUID = -918346378374943773L;

  private static final Logger LOG = LoggerFactory.getLogger(ResourceIndex.class);
  private final boolean skipPackageDesignAnalysis;

  public ResourceIndex(boolean skipPackageDesignAnalysis) {
    this.skipPackageDesignAnalysis = skipPackageDesignAnalysis;
  }

  public ResourceIndex loadSquidResources(SourceCodeSearchEngine index, SensorContext context, Project project) {
    loadSquidProject(index, project);
    loadSquidFilesAndPackages(index, context, project);
    return this;
  }

  private void loadSquidProject(SourceCodeSearchEngine index, Project project) {
    put(index.search(new QueryByType(SourceProject.class)).iterator().next(), project);
  }

  private void loadSquidFilesAndPackages(SourceCodeSearchEngine index, SensorContext context, Project project) {
    Map<Resource, SourceCode> directoryReverseMap = Maps.newHashMap();

    Collection<SourceCode> files = index.search(new QueryByType(SourceFile.class));
    for (SourceCode squidFile : files) {
      String filePath = squidFile.getKey();

      File file = new File(filePath);
      Resource sonarFile = org.sonar.api.resources.File.fromIOFile(file, project);

      // resource is reloaded to get the id:
      put(squidFile, context.getResource(sonarFile));
      SourceCode squidPackage = squidFile.getParent(SourcePackage.class);

      // resource is reloaded to get the id:
      Resource sonarDirectory = context.getResource(sonarFile.getParent());

      SourceCode previousDirectoryMapping = directoryReverseMap.get(sonarDirectory);
      if (previousDirectoryMapping == null) {
        directoryReverseMap.put(sonarDirectory, squidPackage);
        put(squidPackage, sonarDirectory);
      } else if (!previousDirectoryMapping.equals(squidPackage)) {
        String message = "Directory contains files belonging to different packages";
        String warning = " - some metrics could be reported incorrectly: {}";
        if (skipPackageDesignAnalysis) {
          LOG.warn(message + warning, file.getParentFile());
        } else {
          LOG.error(message + warning, file.getParentFile());
          throw new SonarException(message + " : " + file.getParentFile() +
            " Please fix your source code or use " + CoreProperties.DESIGN_SKIP_PACKAGE_DESIGN_PROPERTY + "=true to continue the analysis.");
        }
      }
    }
  }

}
