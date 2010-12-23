/*
 * Sonar PHP Plugin
 * Copyright (C) 2010 Akram Ben Aissi
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

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

import java.io.StringReader;

import org.junit.Test;
import org.sonar.colorizer.CodeColorizer;

public class PhpSourceCodeColorizerTest {

  PhpSourceCodeColorizer phpColorizer = new PhpSourceCodeColorizer();
  CodeColorizer codeColorizer = new CodeColorizer(phpColorizer.getTokenizers());

  @Test
  public void testHighlightPhpKeywords() {
    assertThat(highlight("interface"), containsString("<span class=\"k\">interface</span>"));
    assertThat(highlight("isset"), containsString("<span class=\"k\">isset</span>"));
  }

  private String highlight(String phpSourceCode) {
    return codeColorizer.toHtml(new StringReader(phpSourceCode));
  }

}
