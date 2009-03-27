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

package org.sonar.plugins.php.cpd;

import net.sourceforge.pmd.cpd.AbstractLanguage;
import net.sourceforge.pmd.cpd.CPD;
import net.sourceforge.pmd.cpd.PHPTokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.plugins.api.maven.model.MavenPom;
import org.sonar.plugins.php.Php;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CpdExecutor {

  private static final Logger LOG = LoggerFactory.getLogger(CpdExecutor.class);
  private MavenPom pom;
  private CPD cpd;

  public CpdExecutor(MavenPom pom) {
    try {
      this.pom = pom;
      initCpd(5, new ArrayList<File>(pom.getSourceFiles(Php.SUFFIXES)));
    } catch (IOException e) {
      throw new CpdExecutionException(e);
    }
  }

  public CpdExecutor(List<File> files) throws IOException {
    initCpd(5, files);
  }

  private void initCpd(int tokens, List<File> files) throws IOException {
    cpd = new CPD(tokens, new PHPLanguage());
    cpd.add(files);
  }

  public void execute() {
    cpd.go();
    cpd.getMatches();
    CpdParser gen = new CpdParser();
    gen.generate(cpd.getMatches());
  }


  class PHPLanguage extends AbstractLanguage {
    public PHPLanguage() {
      super(new PHPTokenizer(), Php.SUFFIXES.clone());
    }
  }

}
