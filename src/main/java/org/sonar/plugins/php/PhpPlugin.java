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

package org.sonar.plugins.php;

import org.sonar.plugins.api.Extension;
import org.sonar.plugins.api.Plugin;
import org.sonar.plugins.php.cpd.CpdMavenCollector;
import org.sonar.plugins.php.cpd.jobs.DuplicatedBlocksJob;
import org.sonar.plugins.php.cpd.jobs.DuplicatedFilesJob;
import org.sonar.plugins.php.cpd.jobs.DuplicatedLinesJob;
import org.sonar.plugins.php.cpd.jobs.DuplicatedLinesRatioJob;
import org.sonar.plugins.php.phpcodesniffer.PhpCodeSnifferMavenCollector;
import org.sonar.plugins.php.phpcodesniffer.PhpCodeSnifferRulesRepository;
import org.sonar.plugins.php.phpdepend.PhpDependMavenCollector;

import java.util.ArrayList;
import java.util.List;

public class PhpPlugin implements Plugin {

  public String getKey() {
    return Php.KEY;
  }

  public String getName() {
    return "PHP";
  }

  public String getDescription() {
    return "PHP Plugin.";
  }

  public List<Class<? extends Extension>> getExtensions() {
    List<Class<? extends Extension>> list = new ArrayList<Class<? extends Extension>>();
    list.add(Php.class);
    list.add(PhpImportSourceMavenCollector.class);
    list.add(PhpDependMavenCollector.class);
    list.add(PhpCodeSnifferRulesRepository.class);
    list.add(PhpCodeSnifferMavenCollector.class);
    list.add(CpdMavenCollector.class);

    list.add(DuplicatedBlocksJob.class);
    list.add(DuplicatedFilesJob.class);
    list.add(DuplicatedLinesJob.class);
    list.add(DuplicatedLinesRatioJob.class);
    return list;
  }

  public String toString() {
    return getKey();
  }
}
