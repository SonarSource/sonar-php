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
package org.sonar.php.highlighter;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.sonar.sslr.api.typed.ActionParser;
import org.junit.Test;
import org.sonar.php.parser.PHPParserBuilder;
import org.sonar.php.tree.symbols.SymbolTableImpl;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;

import java.util.Collections;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

public class SymbolHighlighterTest {

  private static final ActionParser<Tree> PARSER = PHPParserBuilder.createParser(Charsets.UTF_8);

  @Test
  public void test_empty_input() throws Exception {
    List<SymbolHighlightingData> data = getData("<?php ");

    assertThat(data).hasSize(0);
  }

  @Test
  public void test_no_usages() throws Exception {
    List<SymbolHighlightingData> data = getData("<?php   $a = 1; ");

    assertThat(data).hasSize(1);
    assertData(data.get(0), 8, 10, Collections.<Integer>emptyList());
  }

  @Test
  public void test_usages() throws Exception {
    List<SymbolHighlightingData> data = getData("<?php   $a = 1; echo $a; $a = 4; ");

    assertThat(data).hasSize(1);
    assertData(data.get(0), 8, 10, ImmutableList.of(21, 25));
  }

  @Test
  public void test_compound_variable() throws Exception {
    List<SymbolHighlightingData> data = getData("<?php   $a = 1; echo \"${a}\"; echo \"$a\";");

    assertThat(data).hasSize(1);
    assertData(data.get(0), 8, 10, ImmutableList.of(35));
  }

  @Test
  public void test_use_clause() throws Exception {
    List<SymbolHighlightingData> data = getData("<?php $b = 42; $f = function() use($b) { echo $b; };");

    assertThat(data).hasSize(3);   // global $b, local $b, $f
    assertData(data.get(0), 6, 8, ImmutableList.of(35));     // global $b
    assertData(data.get(2), 35, 37, ImmutableList.of(46));   // local $b
  }

  private List<SymbolHighlightingData> getData(String s) {
    return SymbolHighlighter.getHighlightData(SymbolTableImpl.create((CompilationUnitTree) PARSER.parse(s)), new SourceFileOffsets(s));

  }

  private static void assertData(SymbolHighlightingData data, Integer startOffset, Integer endOffset, List<Integer> references) {
    assertThat(data.startOffset()).isEqualTo(startOffset);
    assertThat(data.endOffset()).isEqualTo(endOffset);
    assertThat(data.referencesStartOffset()).isEqualTo(references);
  }

}
