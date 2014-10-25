/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
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
package org.sonar.php.metrics;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.sonar.graph.DirectedGraph;
import org.sonar.php.PHPAstScanner;
import org.sonar.php.PHPConfiguration;
import org.sonar.squidbridge.AstScanner;
import org.sonar.squidbridge.api.*;
import org.sonar.squidbridge.indexer.QueryByType;
import org.sonar.sslr.parser.LexerlessGrammar;

import java.io.File;
import java.util.Collection;

import static org.fest.assertions.Assertions.assertThat;

public class DependenciesVisitorTest {

  private DirectedGraph<SourceCode, SourceCodeEdge> graph;
  private SourceCodeSearchEngine index;

  @Test
  public void testDepends() {
    scanFile("RuntimeError.php");
    assertDepends("RuntimeError",
      "RuntimeException");

    scanFile("Vendor/Common/BaseModel.php");
    assertDepends("Vendor\\Common\\BaseModel");

    scanFile("Vendor/Common/UnitTest.php");
    assertDepends("Vendor\\Common\\UnitTest",
      "PHPUnit_Framework_TestCase");

    scanFile("Vendor/Errors/IAlias.php");
    assertDepends("Vendor\\Errors\\IAlias",
      "IRoot");

    scanFile("Vendor/Errors/RuntimeError.php");
    assertDepends("Vendor\\Errors\\RuntimeError",
      "RuntimeError");

    scanFile("Vendor/Package/PackageInterface.php");
    assertDepends("Vendor\\Package\\PackageInterface",
      "Vendor\\Errors\\IAlias");

    scanFile("Vendor/Package/UnitTest.php");
    assertDepends("Vendor\\Package\\UnitTest",
      "Vendor\\Common\\UnitTest",
      "Vendor\\Package\\PackageInterface",
      "Vendor\\Errors\\RuntimeError",
      "RuntimeError");

    scanFile("Vendor/Package/UserModel.php");
    assertDepends("Vendor\\Package\\UserModel",
      "IRoot",
      "Vendor\\Common\\BaseModel",
      "Vendor\\Errors\\RuntimeError");

    scanFile("complex.php");
    assertDepends("A\\One");
    assertDepends("A\\Two");
    assertDepends("B\\Three");
    assertDepends("B\\Four");
    assertHasEdge(new SourcePackage("B"), new SourcePackage("A"));
    assertHasEdge(new SourcePackage("C"), new SourcePackage("A"));
  }

  @Test
  public void testGraph() {
    ImmutableList<File> files = ImmutableList.of(
      new File("src/test/resources/dependencies/Vendor/Package/UserModel.php"),
      new File("src/test/resources/dependencies/Vendor/Common/BaseModel.php"),
      new File("src/test/resources/dependencies/IRoot.php"),
      new File("src/test/resources/dependencies/Vendor/Errors/IAlias.php"),
      new File("src/test/resources/dependencies/Vendor/Package/PackageInterface.php"),
      new File("src/test/resources/dependencies/Vendor/Package/UnitTest.php"),
      new File("src/test/resources/dependencies/Vendor/Errors/RuntimeError.php"),
      new File("src/test/resources/dependencies/Vendor/Common/UnitTest.php"),
      new File("src/test/resources/dependencies/RuntimeError.php")
    );
    scanFiles(files);

    assertClassWithParents("IRoot");
    assertClassWithParents("RuntimeError");
    assertClassWithParents("Vendor\\Common\\UnitTest");
    assertClassWithParents("Vendor\\Errors\\IAlias");
    assertClassWithParents("Vendor\\Errors\\RuntimeError");

    String from = "Vendor\\Package\\UnitTest";
    assertDependenciesSize(from, 4);
    assertHasEdge(from, "RuntimeError");
    assertHasEdge(from, "Vendor\\Common\\UnitTest");
    assertHasEdge(from, "Vendor\\Package\\PackageInterface");
    assertHasEdge(from, "Vendor\\Errors\\RuntimeError");

    Collection<SourceCode> packages = index.search(new QueryByType(SourcePackage.class));
    assertThat(packages).hasSize(4);

    SourcePackage sp = new SourcePackage("Vendor\\Package");
    assertHasEdge(sp, new SourcePackage("Vendor\\Common"));
    assertHasEdge(sp, new SourcePackage("Vendor\\Errors"));
    assertDependenciesSize(sp, 2);
  }

  @Test
  public void constant() {
    scanFile("constant.php");
    assertDepends("A");
    assertDepends("B", "A");
  }

  @Test
  public void tryCatch() {
    scanFile("try-catch.php");
    assertDepends("A", "RuntimeError");
    assertDependenciesSize("RuntimeError", 0);
  }

  @Test
  public void instanceOf() {
    scanFile("instanceof.php");
    assertDepends("Foo");
    assertDepends("Bar", "Foo");
  }

  private void assertDepends(String from, String... to) {
    assertClassWithParents(from);
    for (String t : to) {
      assertClass(t);
      assertHasEdge(from, t);
    }
    assertDependenciesSize(from, to.length);
  }

  private void scanFile(String s) {
    ImmutableList<File> files = ImmutableList.of(new File("src/test/resources/dependencies/" + s));
    scanFiles(files);
  }

  private void scanFiles(ImmutableList<File> files) {
    graph = new DirectedGraph<SourceCode, SourceCodeEdge>();
    DependenciesVisitor visitor = new DependenciesVisitor(graph);
    AstScanner<LexerlessGrammar> scanner = PHPAstScanner.create(new PHPConfiguration(Charsets.UTF_8), visitor);
    scanner.scanFiles(files);
    index = scanner.getIndex();
  }

  private SourceCode assertClass(String from) {
    SourceCode sourceClass = index.search(from);
    assertThat(sourceClass).isInstanceOf(SourceClass.class);
    assertHasParent(sourceClass);
    return sourceClass;
  }

  private void assertClassWithParents(String from) {
    SourceCode sourceClass = assertClass(from);

    SourceCode sourceFile = sourceClass.getParent();
    assertThat(sourceFile).as(sourceClass.getKey() + " parent").isInstanceOf(SourceFile.class);
    assertHasParent(sourceFile);

    SourceCode sourcePackage = sourceFile.getParent();
    assertThat(sourcePackage).as(sourceFile.getKey() + " parent").isInstanceOf(SourcePackage.class);
    assertHasParent(sourcePackage);

    SourceCode sourceProject = sourcePackage.getParent();
    assertThat(sourceProject).as(sourcePackage.getKey() + " parent").isInstanceOf(SourceProject.class);
  }

  private void assertDependenciesSize(String from, int size) {
    SourceClass sourceCode = new SourceClass(from);
    assertDependenciesSize(sourceCode, size);
  }

  private void assertDependenciesSize(SourceCode from, int size) {
    assertThat(graph.getOutgoingEdges(from)).hasSize(size);
  }

  private void assertHasEdge(String from, String to) {
    SourceClass fromClass = new SourceClass(from);
    SourceClass toClass = new SourceClass(to);
    assertHasEdge(fromClass, toClass);
  }

  private void assertHasEdge(SourceCode from, SourceCode to) {
    if (!graph.hasEdge(from, to)) {
      throw new AssertionError(from.getKey() + " dose not have edge to " + to.getKey());
    }
  }

  private void assertHasParent(SourceCode sourceCode) {
    if (sourceCode.getParent() == null) {
      throw new AssertionError(sourceCode.getKey() + " dose not have parent");
    }

  }
}