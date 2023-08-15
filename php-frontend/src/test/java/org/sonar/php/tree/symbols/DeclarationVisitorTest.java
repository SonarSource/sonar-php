/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2023 SonarSource SA
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
package org.sonar.php.tree.symbols;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import org.junit.Test;
import org.mockito.Mockito;
import org.sonar.plugins.php.api.visitors.PhpFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.php.tree.symbols.DeclarationVisitor.pathOf;

public class DeclarationVisitorTest {

  @Test
  public void pathOfShouldNotRelayOnPhysicalFile() throws IOException, URISyntaxException {
    PhpFile phpFile = Mockito.mock(PhpFile.class);
    URI uri = Files.createTempFile("foo", ".php").toUri();
    Mockito.when(phpFile.uri()).thenReturn(uri);
    assertThat(pathOf(phpFile)).isEqualTo(Paths.get(uri).toString());

    uri = new URI("myscheme", null, "/file1.php", null);

    Mockito.when(phpFile.uri()).thenReturn(uri);
    assertThat(pathOf(phpFile)).isNull();

    Mockito.when(phpFile.uri()).thenThrow(InvalidPathException.class);
    assertThat(pathOf(phpFile)).isNull();
  }
}
