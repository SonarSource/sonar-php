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

import net.sourceforge.pmd.cpd.Match;
import net.sourceforge.pmd.cpd.TokenEntry;
import org.sonar.commons.Metric;
import org.sonar.commons.resources.Resource;
import org.sonar.plugins.api.maven.ProjectContext;
import org.sonar.plugins.api.metrics.CoreMetrics;
import org.sonar.plugins.php.Php;
import org.sonar.plugins.php.ResourcesBag;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;


public class CpdAnalyser {
  private ProjectContext context;
  private ResourcesBag resourcesBag;
  private List<String> sourceDirs;

  public CpdAnalyser(ProjectContext context, List<String> sourceDirs) {
    this.sourceDirs = sourceDirs;
    this.context = context;
    resourcesBag = new ResourcesBag();
  }

  public void generate(Iterator<Match> matches) throws IOException, ParseException {
    Map<Resource, ClassDuplicationData> duplicationsData = new HashMap<Resource, ClassDuplicationData>();
    while (matches.hasNext()) {
      Match match = matches.next();
      String code = match.getSourceCodeSlice();
      int duplicatedLines = match.getLineCount();

//      TokenEntry mark;
//      for (Iterator<TokenEntry> iterator = match.iterator(); iterator.hasNext();) {
//        mark = iterator.next();
//        int line = mark.getBeginLine();
//        String filename = match.getFirstMark().getTokenSrcID();
//        Resource file = Php.newFileFromAbsolutePath(filename, sourceDirs);
//
//        addFileMeasure(1d, CoreMetrics.DUPLICATED_FILES, file);
//        addFileMeasure((double) duplicatedLines, CoreMetrics.DUPLICATED_LINES, file);
//      }

      TokenEntry firstMark = match.getFirstMark();
      String filename1 = firstMark.getTokenSrcID();
      int line1 = firstMark.getBeginLine();
      Resource file1 = Php.newFileFromAbsolutePath(filename1, sourceDirs);

      TokenEntry secondMark = match.getSecondMark();
      String filename2 = secondMark.getTokenSrcID();
      int line2 = secondMark.getBeginLine();
      Resource file2 = Php.newFileFromAbsolutePath(filename2, sourceDirs);

      processClassMeasure(duplicationsData, file2, line2, file1, line1, duplicatedLines);
      processClassMeasure(duplicationsData, file1, line1, file2, line2, duplicatedLines);

    }

    for (ClassDuplicationData data : duplicationsData.values()) {
      data.saveUsing(context);
    }

    saveMeasures();
  }


  private void processClassMeasure(Map<Resource, ClassDuplicationData> fileContainer, Resource file, int duplicationStartLine,
                                   Resource targetFile, int targetDuplicationStartLine, int duplicatedLines) throws ParseException {
    if (file != null) {
      ClassDuplicationData data = fileContainer.get(file);
      if (data == null) {
        data = new ClassDuplicationData(file, context);
        fileContainer.put(file, data);
      }
      data.cumulate(targetFile, (double) targetDuplicationStartLine, (double) duplicationStartLine, (double) duplicatedLines);
    }
  }

  private void addFileMeasure(Double value, Metric metric, Resource resource) {
    resourcesBag.add(value, metric, resource);
    Resource parent = new Php().getParent(resource);
    if (parent != null) {
      resourcesBag.add(value, metric, parent);
    }
  }

  private void addProjectMeasure(Double value, Metric metric) {
    resourcesBag.add(value, metric, null);
  }


  private void saveMeasures() throws ParseException {
    for (Resource resource : resourcesBag.getResources()) {
      for (Metric metric : resourcesBag.getMetrics(resource)) {
        Double measure = resourcesBag.getMeasure(metric, resource);
        saveMeasure(resource, metric, measure);
      }
    }
  }

  private void saveMeasure(Resource resource, Metric metric, Double measure) {
    if (resource != null) {
      context.addMeasure(resource, metric, measure);
    } else {
      context.addMeasure(metric, measure);
    }
  }


  private class ClassDuplicationData {
    protected double duplicatedLines;
    protected double duplicatedBlocks;
    protected Resource resource;
    private ProjectContext context;
    private List<StringBuilder> duplicationXMLEntries = new ArrayList<StringBuilder>();

    private ClassDuplicationData(Resource resource, ProjectContext context) {
      this.context = context;
      this.resource = resource;
    }

    protected void cumulate(Resource targetResource, Double targetDuplicationStartLine, Double duplicationStartLine, Double duplicatedLines) {
      StringBuilder xml = new StringBuilder();
      xml.append("<duplication lines=\"").append(duplicatedLines.intValue())
        .append("\" start=\"").append(duplicationStartLine.intValue())
        .append("\" target-start=\"").append(targetDuplicationStartLine.intValue())
        .append("\" target-resource=\"").append(context.getResourceKey(targetResource)).append("\"/>");

      duplicationXMLEntries.add(xml);

      this.duplicatedLines += duplicatedLines;
      this.duplicatedBlocks++;
    }

    protected void saveUsing(ProjectContext context) {
      context.addMeasure(resource, CoreMetrics.DUPLICATED_FILES, 1d);
      context.addMeasure(resource, CoreMetrics.DUPLICATED_LINES, duplicatedLines);
      context.addMeasure(resource, CoreMetrics.DUPLICATED_BLOCKS, duplicatedBlocks);
      context.addMeasure(resource, CoreMetrics.DUPLICATIONS_DATA, getDuplicationXMLData());

      Resource parent = new Php().getParent(resource);
      if (parent != null) {
        resourcesBag.add(1d, CoreMetrics.DUPLICATED_FILES, parent);
        resourcesBag.add(duplicatedLines, CoreMetrics.DUPLICATED_LINES, parent);
        resourcesBag.add(duplicatedBlocks, CoreMetrics.DUPLICATED_BLOCKS, parent);
      }
      resourcesBag.add(1d, CoreMetrics.DUPLICATED_FILES, null);
      resourcesBag.add(duplicatedLines, CoreMetrics.DUPLICATED_LINES, null);
      resourcesBag.add(duplicatedBlocks, CoreMetrics.DUPLICATED_BLOCKS, null);
    }

    private String getDuplicationXMLData() {
      StringBuilder duplicationXML = new StringBuilder("<duplications>");
      for (StringBuilder xmlEntry : duplicationXMLEntries) {
        duplicationXML.append(xmlEntry);
      }
      duplicationXML.append("</duplications>");
      return duplicationXML.toString();
    }
  }
}
