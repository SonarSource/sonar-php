/*
 * Sonar PHP Plugin
 * Copyright (C) 2010 Sonar PHP Plugin
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

package org.sonar.plugins.php.core;

import java.io.File;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.AbstractSourceImporter;
import org.sonar.api.batch.Phase;
import org.sonar.api.resources.Project;
import org.sonar.plugins.php.api.Php;
import org.sonar.plugins.php.api.PhpFile;

/**
 * The PhpSourceImporter is in charge of analysing and loading valid php files. All source files under source folder and test source folder
 * will be imported. The extension will only execute on php project
 */
@Phase(name = Phase.Name.PRE)
public class PhpSourceImporter extends AbstractSourceImporter {

  /** The logger. */
  private static final Logger LOG = LoggerFactory.getLogger(PhpSourceImporter.class);
  private Project project;

  /**
   * Instantiates a new php source importer.
   */
  public PhpSourceImporter(Project project) {
    super(Php.PHP);
    this.project = project;
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
    PhpFile phpFile = null;
    if (file != null && !file.getName().contains("$")) {
      phpFile = PhpFile.getInstance(project).fromIOFile(file, sourceDirs, unitTest);
    }
    return phpFile;
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
