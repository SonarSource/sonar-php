/*
 * Sonar PHP Plugin
 * Copyright (C) 2010 Akram Ben Aissi or Jerome Tama or Frederic Leroy
 * mailto: akram.benaissi@free.fr or jerome.tama@codehaus.org
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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Phase;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.checks.NoSonarFilter;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.resources.Project;
import org.sonar.api.utils.SonarException;
import org.sonar.squid.measures.Metric;
import org.sonar.squid.recognizer.CamelCaseDetector;
import org.sonar.squid.recognizer.CodeRecognizer;
import org.sonar.squid.recognizer.ContainsDetector;
import org.sonar.squid.recognizer.Detector;
import org.sonar.squid.recognizer.EndWithDetector;
import org.sonar.squid.recognizer.KeywordsDetector;
import org.sonar.squid.recognizer.LanguageFootprint;
import org.sonar.squid.text.Source;

@Phase(name = Phase.Name.PRE)
// The NoSonarFilter must be fed before launching the violation engines
public class NoSonarAndCommentedOutLocSensor implements Sensor {

  private final static Logger log = LoggerFactory.getLogger(NoSonarAndCommentedOutLocSensor.class);

  /**
   * 
   */
  private final NoSonarFilter filter;

  /**
   * @param noSonarFilter
   */
  public NoSonarAndCommentedOutLocSensor(NoSonarFilter noSonarFilter) {
    this.filter = noSonarFilter;
  }

  /**
   * @see org.sonar.api.batch.Sensor#analyse(org.sonar.api.resources.Project, org.sonar.api.batch.SensorContext)
   */
  public void analyse(Project project, SensorContext context) {
    List<File> sourceFiles = project.getFileSystem().getSourceFiles();
    List<File> directories = project.getFileSystem().getSourceDirs();
    for (File file : sourceFiles) {
      PhpFile phpFile = PhpFile.fromIOFile(file, directories, false);
      if (phpFile == null) {
        continue;
      }
      Source source = analyseSourceCode(file);
      if (source != null) {
        filter.addResource(phpFile, source.getNoSonarTagLines());
        context.saveMeasure(phpFile, CoreMetrics.COMMENTED_OUT_CODE_LINES, (double) source.getMeasure(Metric.COMMENTED_OUT_CODE_LINES));
      }
    }
  }

  protected static Source analyseSourceCode(File file) {
    Source result = null;
    try {

      result = new Source(new FileReader(file), new CodeRecognizer(0.9, new PhpLanguageFootprint()));
    } catch (FileNotFoundException e) {
      throw new SonarException("Unable to open file '" + file.getAbsolutePath() + "'", e);
    } catch (RuntimeException rEx) {
      log.error("error while parsing file '" + file.getAbsolutePath() + "'", rEx);
    }
    return result;
  }

  /**
   * @see org.sonar.api.batch.CheckProject#shouldExecuteOnProject(org.sonar.api.resources.Project)
   */
  public boolean shouldExecuteOnProject(Project project) {
    return Php.INSTANCE.equals(project.getLanguage());
  }

  private static class PhpLanguageFootprint implements LanguageFootprint {

    private final Set<Detector> detectors = new HashSet<Detector>();

    public PhpLanguageFootprint() {
      detectors.add(new EndWithDetector(0.95, '}', ';', '{'));
      detectors.add(new KeywordsDetector(0.7, "||", "&&"));
      detectors.add(new KeywordsDetector(0.3, Php.PHP_KEYWORDS_ARRAY));
      detectors.add(new ContainsDetector(0.95, "++", "for(", "if(", "while(", "catch(", "switch(", "try{", "else{"));
      detectors.add(new CamelCaseDetector(0.5));
    }

    /**
     * @see org.sonar.squid.recognizer.LanguageFootprint#getDetectors()
     */
    public Set<Detector> getDetectors() {
      return detectors;
    }
  }
}