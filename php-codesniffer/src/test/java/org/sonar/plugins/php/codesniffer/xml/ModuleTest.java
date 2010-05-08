/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2010 SQLi
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

package org.sonar.plugins.php.codesniffer.xml;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.sonar.plugins.checkstyle.xml.Module;

/**
 * The Class ModuleTest.
 */
public class ModuleTest {

  /**
   * Should build x stream from xml.
   * 
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  @Test
  public void shouldBuildXStreamFromXml() throws IOException {
    InputStream input = getClass().getResourceAsStream("/org/sonar/plugins/php/codesniffer/xml/ModuleTest/shouldBuildXStreamFromXml.xml");
    Module module = Module.fromXml(IOUtils.toString(input));

    assertThat(module.getName(), is("Checker"));
    assertThat(module.getChildren().size(), is(2));

    Module child1 = module.getChildren().get(0);
    assertThat(child1.getName(), is("Translation"));
    assertThat(child1.getProperties().size(), is(2));
    assertThat(child1.getProperties().get(0).getName(), is("severity"));
    assertThat(child1.getProperties().get(0).getValue(), is("error"));
    assertThat(child1.getProperties().get(1).getName(), is("fileExtensions"));
    assertThat(child1.getProperties().get(1).getValue(), is("properties"));

    Module child2 = module.getChildren().get(1);
    assertThat(child2.getName(), is("TreeWalker"));
    assertThat(child2.getChildren().size(), is(2));

    Module grandSon1 = child2.getChildren().get(0);
    assertThat(grandSon1.getName(), is("AnonInnerLength"));
    assertThat(grandSon1.getProperties().size(), is(1));
    assertThat(grandSon1.getProperties().get(0).getName(), is("max"));
    assertThat(grandSon1.getProperties().get(0).getValue(), is("15"));

    Module grandSon2 = child2.getChildren().get(1);
    assertThat(grandSon2.getName(), is("TypeName"));
    assertThat(grandSon2.getProperties().size(), is(1));
    assertThat(grandSon2.getProperties().get(0).getName(), is("severity"));
    assertThat(grandSon2.getProperties().get(0).getValue(), is("warning"));
  }
}
