/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2022 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.plugins.php.reports.phpunit;

import com.ctc.wstx.exc.WstxIOException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import javax.xml.stream.XMLStreamException;
import org.codehaus.staxmate.SMInputFactory;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.sonar.plugins.php.reports.phpunit.xml.CoverageNode;
import org.sonar.plugins.php.reports.phpunit.xml.FileNode;
import org.sonar.plugins.php.reports.phpunit.xml.LineNode;
import org.sonar.plugins.php.reports.phpunit.xml.PackageNode;
import org.sonar.plugins.php.reports.phpunit.xml.ProjectNode;
import org.sonarsource.analyzer.commons.xml.ParseException;

class CoverageFileParserForPhpUnit {

  /**
   * Read the coverage report file and call the consumer for each FileNode
   */
  protected void parse(File report, Consumer<FileNode> consumer) throws IOException {
    CoverageNode coverage = getCoverage(report);
    List<ProjectNode> projects = coverage.getProjects();
    if (!projects.isEmpty()) {
      ProjectNode projectNode = projects.get(0);
      parse(projectNode, consumer);
    }
  }

  private static void parse(ProjectNode projectNode, Consumer<FileNode> consumer) {
    consumeFileNodes(projectNode.getFiles(), consumer);
    parse(projectNode.getPackages(), consumer);
  }

  private static void consumeFileNodes(List<FileNode> fileNodes, Consumer<FileNode> consumer) {
    fileNodes.forEach(consumer);
  }

  private static void parse(List<PackageNode> packages, Consumer<FileNode> consumer) {
    for (PackageNode packageNode : packages) {
      consumeFileNodes(packageNode.getFiles(),consumer);
    }
  }

  private static CoverageNode getCoverage(File report) throws ParseException, IOException {
    SMInputFactory inputFactory = JUnitLogParserForPhpUnit.inputFactory();
    try {
      SMHierarchicCursor rootCursor = inputFactory.rootElementCursor(report);
      rootCursor.advance();
      if (!"coverage".equals(rootCursor.getLocalName())) {
        throw new XMLStreamException("Report should start with <coverage>");
      }
      return parseCoverageNode(rootCursor);
    } catch (WstxIOException e) {
      throw new IOException(e.getMessage(), e.getCause());
    } catch (XMLStreamException e) {
      throw new ParseException(e);
    }
  }

  private static CoverageNode parseCoverageNode(SMHierarchicCursor cursor) throws XMLStreamException {
    CoverageNode result = new CoverageNode();
    SMInputCursor childCursor = cursor.childElementCursor("project");
    while (childCursor.getNext() != null) {
      result.getProjects().add(parseProjectNode(childCursor));
    }
    return result;
  }

  private static ProjectNode parseProjectNode(SMInputCursor cursor) throws XMLStreamException {
    ProjectNode result = new ProjectNode();
    result.setName(cursor.getAttrValue("name"));
    SMInputCursor childCursor = cursor.childElementCursor();
    while (childCursor.getNext() != null) {
      if ("package".equals(childCursor.getLocalName())) {
        result.getPackages().add(parsePackageNode(childCursor));
      } else if ("file".equals(childCursor.getLocalName())) {
        result.getFiles().add(parseFileNode(childCursor));
      }
    }
    return result;
  }

  private static PackageNode parsePackageNode(SMInputCursor cursor) throws XMLStreamException {
    PackageNode result = new PackageNode();
    result.setName(cursor.getAttrValue("name"));
    SMInputCursor childCursor = cursor.childElementCursor("file");
    while (childCursor.getNext() != null) {
      result.getFiles().add(parseFileNode(childCursor));
    }
    return result;
  }

  private static FileNode parseFileNode(SMInputCursor cursor) throws XMLStreamException {
    FileNode result = new FileNode();
    result.setName(cursor.getAttrValue("name"));
    SMInputCursor childCursor = cursor.childElementCursor("line");
    while (childCursor.getNext() != null) {
      result.getLines().add(parseLineNode(childCursor));
    }
    return result;
  }

  private static LineNode parseLineNode(SMInputCursor cursor) throws XMLStreamException {
    int count = attributeIntValue(cursor, "count");
    int num = attributeIntValue(cursor, "num");
    String type = cursor.getAttrValue("type");
    return new LineNode(count, num, type);
  }

  private static int attributeIntValue(SMInputCursor cursor, String name) throws XMLStreamException {
    return Integer.parseInt(cursor.getAttrValue(name));
  }

}
