/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * sonarqube@googlegroups.com
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

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Phase;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.resources.Project;
import org.sonar.api.utils.SonarException;
import org.sonar.php.api.PHPKeyword;
import org.sonar.plugins.php.api.Php;
import org.sonar.squidbridge.measures.Metric;
import org.sonar.squidbridge.recognizer.CamelCaseDetector;
import org.sonar.squidbridge.recognizer.CodeRecognizer;
import org.sonar.squidbridge.recognizer.ContainsDetector;
import org.sonar.squidbridge.recognizer.Detector;
import org.sonar.squidbridge.recognizer.EndWithDetector;
import org.sonar.squidbridge.recognizer.KeywordsDetector;
import org.sonar.squidbridge.recognizer.LanguageFootprint;
import org.sonar.squidbridge.text.Source;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;

@Phase(name = Phase.Name.PRE)
// The NoSonarFilter must be fed before launching the violation engines
public class NoSonarAndCommentedOutLocSensor implements Sensor {

  private static final double CODE_RECOGNIZER_SENSITIVITY = 0.9;

  private static final Logger LOG = LoggerFactory.getLogger(NoSonarAndCommentedOutLocSensor.class);

  private final NoSonarFilter filter;
  private final FileSystem filesystem;
  private final FilePredicates filePredicates;

  public NoSonarAndCommentedOutLocSensor(FileSystem filesystem, NoSonarFilter noSonarFilter) {
    this.filter = noSonarFilter;
    this.filesystem = filesystem;
    this.filePredicates = filesystem.predicates();

  }

  /**
   * @see org.sonar.api.batch.Sensor#analyse(org.sonar.api.resources.Project, org.sonar.api.batch.SensorContext)
   */
  public void analyse(Project project, SensorContext context) {
    Iterable<InputFile> sourceFiles = filesystem.inputFiles(filePredicates.and(filePredicates.hasLanguage(Php.KEY), filePredicates.hasType(InputFile.Type.MAIN)));
    for (InputFile file : sourceFiles) {
      // TODO: remove when deprecated NoSonarFilter will be replaced.
      org.sonar.api.resources.File phpFile = context.getResource(org.sonar.api.resources.File.create(file.relativePath()));
      if (phpFile != null) {
        Source source = analyseSourceCode(file.file());
        if (source != null) {
          filter.addComponent(phpFile.getEffectiveKey(), source.getNoSonarTagLines());
          double measure = source.getMeasure(Metric.COMMENTED_OUT_CODE_LINES);
          context.saveMeasure(phpFile, CoreMetrics.COMMENTED_OUT_CODE_LINES, measure);
        }
      }
    }
  }

  @VisibleForTesting
  org.sonar.api.resources.File getSonarResource(Project project, File file) {
    return org.sonar.api.resources.File.fromIOFile(file, project);
  }

  protected static Source analyseSourceCode(File file) {
    Source result = null;
    FileReader reader = null;
    try {
      reader = new FileReader(file);
      result = new Source(reader, new CodeRecognizer(CODE_RECOGNIZER_SENSITIVITY, new PhpLanguageFootprint()));
    } catch (FileNotFoundException e) {
      throw new SonarException("Unable to open file '" + file.getAbsolutePath() + "'", e);
    } catch (RuntimeException rEx) {
      LOG.error("Error while parsing file '" + file.getAbsolutePath() + "'", rEx);
    } finally {
      IOUtils.closeQuietly(reader);
    }

    return result;
  }

  /**
   * @see org.sonar.api.batch.CheckProject#shouldExecuteOnProject(org.sonar.api.resources.Project)
   */
  @Override
  public boolean shouldExecuteOnProject(Project project) {
    return filesystem.hasFiles(filePredicates.and(filePredicates.hasLanguage(Php.KEY), filePredicates.hasType(InputFile.Type.MAIN)));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "NoSonar and Commented out LOC Sensor";
  }

  private static class PhpLanguageFootprint implements LanguageFootprint {

    private static final double CAMEL_CASE_PROBABILITY = 0.5;
    private static final double CONDITIONAL_PROBABILITY = 0.95;
    private static final double PHP_KEYWORDS_PROBABILITY = 0.3;
    private static final double BOOLEAN_OPERATOR_PROBABILITY = 0.7;
    private static final double END_WITH_DETECTOR_PROBABILITY = 0.95;
    private final Set<Detector> detectors = new HashSet<Detector>();

    public PhpLanguageFootprint() {
      detectors.add(new EndWithDetector(END_WITH_DETECTOR_PROBABILITY, '}', ';', '{'));
      detectors.add(new KeywordsDetector(BOOLEAN_OPERATOR_PROBABILITY, "||", "&&"));
      detectors.add(new KeywordsDetector(PHP_KEYWORDS_PROBABILITY, PHPKeyword.getKeywordValues()));
      detectors.add(new ContainsDetector(CONDITIONAL_PROBABILITY, "++", "for(", "if(", "while(", "catch(", "switch(", "try{", "else{"));
      detectors.add(new CamelCaseDetector(CAMEL_CASE_PROBABILITY));
    }

    /**
     * @see org.sonar.squidbridge.recognizer.LanguageFootprint#getDetectors()
     */
    public Set<Detector> getDetectors() {
      return detectors;
    }
  }
}
