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

import org.junit.Test;
import org.sonar.colorizer.CodeColorizer;

import java.io.StringReader;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

public class PhpSourceCodeColorizerTest {

  PhpSourceCodeColorizer phpColorizer = new PhpSourceCodeColorizer();
  CodeColorizer codeColorizer = new CodeColorizer(phpColorizer.getTokenizers());

  @Test
  public void testHighlightCDoc() {
    assertThat(highlight("// Hello class"), containsString("<span class=\"cd\">// Hello class</span>"));
  }

  @Test
  public void testHighlightShell() {
    assertThat(highlight("# Hello class"), containsString("<span class=\"cd\"># Hello class</span>"));
  }

  @Test
  public void testHighlightCppDoc() {
    assertThat(highlight("/* Hello class */"), containsString("<span class=\"cppd\">/* Hello class */</span>"));
  }

  @Test
  public void testHighlightPhpKeywords() {
    assertThat(highlight("interface"), containsString("<span class=\"k\">interface</span>"));
    assertThat(highlight("isset"), containsString("<span class=\"k\">isset</span>"));
  }

  @Test
  public void testHighlightTraitsKeywords() {
    assertThat(highlight("trait"), containsString("<span class=\"k\">trait</span>"));
    assertThat(highlight("use"), containsString("<span class=\"k\">use</span>"));
  }

  @Test
  public void testHighlightReservedVariables() {
    assertThat(highlight("__FUNCTION__"), containsString("<span class=\"k\">__FUNCTION__</span>"));
  }

  @Test
  public void testHighlightString() {
    assertThat(highlight("\"Hello class 100\""), containsString("<span class=\"s\">\"Hello class 100\"</span>"));
  }

  private String highlight(String phpSourceCode) {
    return codeColorizer.toHtml(new StringReader(phpSourceCode));
  }

}
