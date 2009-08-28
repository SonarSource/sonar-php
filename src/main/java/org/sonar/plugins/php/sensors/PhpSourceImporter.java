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

package org.sonar.plugins.php.sensors;

import org.sonar.api.resources.Resource;
import org.sonar.api.resources.Language;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.api.batch.AbstractSourceImporter;
import org.sonar.api.batch.SensorContext;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class PhpSourceImporter extends AbstractSourceImporter {

  public PhpSourceImporter(Language language) {
    super(language);
  }

  @Override
  protected void analyse(ProjectFileSystem fileSystem, SensorContext context) throws IOException {
    //parseDirs(context, configuration.getPlsqlFiles(), fileSystem.getSourceDirs(), false, fileSystem.getSourceCharset());
  }

  protected Resource createResource(File file, List<File> sourceDirs, boolean unitTest) {
    org.sonar.api.resources.File resource = org.sonar.api.resources.File.fromIOFile(file, sourceDirs);
    /*if (resource.getKey().contains("sonar") && resource.getKey().contains(FormSensor.GENERATED_SOURCES_DIR)) {
      // occurs when source dir is ${basedir}. It should not get generated sources from target/sonar/plsql-sources
      return null;
    } */

    return resource;
  }

/*  protected String[] getSuffixes() {
    return Php.SUFFIXES;
  }

  protected Resource createSourceResource(File file, String sourceDir) {
    return Php.newFileFromAbsolutePath(file.getAbsolutePath(), Arrays.asList(sourceDir));
  }

  protected Resource createTestResource(File file, String testSourceDir) {
    return Php.newUnitTestFileFromAbsolutePath(file.getAbsolutePath(), Arrays.asList(testSourceDir));
  }
  */

}
