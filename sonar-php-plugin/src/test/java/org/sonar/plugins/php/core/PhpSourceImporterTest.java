/*
 * Sonar PHP Plugin
 * Copyright (C) 2010 Codehaus Sonar Plugins
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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.sonar.api.resources.Language;
import org.sonar.plugins.php.api.Php;

/**
 * Tests the basic functionality of the PhpSourceImporter.
 * 
 * @author juergen_kellerer, 2010-10-21
 * @version 1.0
 */
public class PhpSourceImporterTest {

  @Test
  public void testCreateImporter() throws Exception {
    Php php = new Php();
    PhpSourceImporter importer = new PhpSourceImporter(php);
    assertThat(importer.getLanguage(), is((Language) php));
  }

}
