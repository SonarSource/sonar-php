/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
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
package org.sonar.plugins.php.duplications;

import com.google.common.base.Charsets;
import java.io.File;
import java.nio.charset.StandardCharsets;
import net.sourceforge.pmd.cpd.SourceCode;
import net.sourceforge.pmd.cpd.Tokenizer;
import net.sourceforge.pmd.cpd.Tokens;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.resources.Project;
import org.sonar.test.TestUtils;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PHPCPDMappingTest {

  private static final String UTF_8 = StandardCharsets.UTF_8.displayName();

  private Tokenizer tokenizer;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Before
  public void test() throws Exception {
    FileSystem fs = mock(FileSystem.class);
    when(fs.encoding()).thenReturn(Charsets.UTF_8);
    Project project = mock(Project.class);

    PhpCPDMapping phpcpdMapping = new PhpCPDMapping(null, project, fs);
    tokenizer = phpcpdMapping.getTokenizer();
  }

  @Test
  public void testTokenize() throws Exception {
    File file = TestUtils.getResource("org/sonar/plugins/php/duplications/SmallFile.php");
    SourceCode source = new SourceCode(new SourceCode.FileCodeLoader(file, UTF_8));
    Tokens tokens = new Tokens();
    tokenizer.tokenize(source, tokens);

    assertThat(tokens.size(), is(33));
  }

  @Test
  public void unknown_file() throws Exception {
    SourceCode source = new SourceCode(new SourceCode.FileCodeLoader(new File("xxx"), UTF_8));
    thrown.expect(IllegalStateException.class);
    tokenizer.tokenize(source, new Tokens());
  }

}
