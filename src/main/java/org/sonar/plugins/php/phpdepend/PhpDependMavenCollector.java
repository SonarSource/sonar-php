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

package org.sonar.plugins.php.phpdepend;

import org.sonar.plugins.api.maven.MavenCollector;
import org.sonar.plugins.api.maven.MavenPluginHandler;
import org.sonar.plugins.api.maven.ProjectContext;
import org.sonar.plugins.api.maven.model.MavenPom;
import org.sonar.plugins.php.Php;

public class PhpDependMavenCollector implements MavenCollector {

  public Class<? extends MavenPluginHandler> dependsOnMavenPlugin(MavenPom pom) {
    return null;
  }

  public boolean shouldCollectOn(MavenPom pom) {
    return Php.KEY.equals(pom.getLanguageKey());
  }

  public void collect(MavenPom pom, ProjectContext context) {
    PhpDependConfiguration config = new PhpDependConfiguration(pom);
    PhpDependExecutor executor = new PhpDependExecutor(config);
    executor.execute();

    PhpDependResultsParser parser = new PhpDependResultsParser(config, context);
    parser.parse();
  }
}
