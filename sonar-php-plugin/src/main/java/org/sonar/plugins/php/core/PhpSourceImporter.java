/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2010 Akram Ben Aissi
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

package org.sonar.plugins.php.core;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.AbstractSourceImporter;
import org.sonar.api.batch.Phase;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Language;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.api.utils.SonarException;

/**
 * The PhpSourceImporter is in charge of analysing and loading valid php files. All source files under source folder and test source folder
 * will be imported. The extension will only execute on php project
 */
@Phase(name = Phase.Name.PRE)
public class PhpSourceImporter extends AbstractSourceImporter {

  /** The logger. */
  private static final Logger LOG = LoggerFactory.getLogger(PhpSourceImporter.class);

  /**
   * Instantiates a new php source importer.
   */
  public PhpSourceImporter() {
    super(Php.INSTANCE);
  }

  /**
   * Analyse the project source dirs.
   * 
   * @param project
   *          the project to be analyzed
   * @param context
   *          the context the execution context
   * @see org.sonar.api.batch.AbstractSourceImporter#analyse(org.sonar.api.resources.Project, org.sonar.api.batch.SensorContext)
   */
  @Override
  public void analyse(Project project, SensorContext context) {
    try {
      LOG.info("Importing files from project " + project.getName());
      doAnalyse(project, context);
    } catch (IOException e) {
      throw new SonarException("Parsing source files ended abnormaly", e);
    }
  }

  /**
   * Creates the resource.
   * 
   * @param file
   *          the file
   * @param sourceDirs
   *          the source dirs
   * @param unitTest
   *          the unit test
   * @return the php file
   * @see org.sonar.api.batch.AbstractSourceImporter#createResource(java.io.File, java.util.List, boolean)
   */
  @Override
  protected PhpFile createResource(File file, List<File> sourceDirs, boolean unitTest) {
    return (file != null && !file.getName().contains("$")) ? PhpFile.fromIOFile(file, sourceDirs, unitTest) : null;
  }

  /**
   * Imports php file contains in the source and tests folders.
   * 
   * @param project
   *          the project
   * @param context
   *          the context
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  protected void doAnalyse(Project project, SensorContext context) throws IOException {
    // Importing source files
    ProjectFileSystem fileSystem = project.getFileSystem();
    List<File> sourceDirs = fileSystem.getSourceDirs();
    Language[] language = new Language[] { Php.INSTANCE };
    List<File> sourceFiles = fileSystem.getSourceFiles(language);
    parseDirs(context, sourceFiles, sourceDirs, false, fileSystem.getSourceCharset());

    // Importing tests files
    List<File> testDirs = fileSystem.getTestDirs();
    List<File> testFiles = fileSystem.getTestFiles(language);
    parseDirs(context, testFiles, testDirs, true, fileSystem.getSourceCharset());

    // Display source dirs and tests directories if info level is enabled.
    if (LOG.isInfoEnabled()) {
      for (File directory : sourceDirs) {
        LOG.info(directory.getName());
      }
      for (File directory : testDirs) {
        LOG.info(directory.getName());
      }
    }
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
    builder.append("getLanguage()", getLanguage());
    builder.append("getClass()", getClass());
    return builder.toString();
  }

}
