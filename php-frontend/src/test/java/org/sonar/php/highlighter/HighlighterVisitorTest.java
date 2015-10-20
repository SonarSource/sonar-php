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
package org.sonar.php.highlighter;

import com.google.common.base.Charsets;
import com.sonar.sslr.api.typed.ActionParser;
import org.junit.Test;
import org.sonar.php.parser.PHPParserBuilder;
import org.sonar.plugins.php.api.tree.Tree;

import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

public class HighlighterVisitorTest {

  private static final ActionParser<Tree> PARSER = PHPParserBuilder.createParser(Charsets.UTF_8);

  @Test
  public void empty_input() throws Exception {
    List<HighlightingData> data = getData("<?php ");

    assertThat(data).hasSize(0);
  }

  @Test
  public void multiline_comment() throws Exception {
    List<HighlightingData> data = getData("<?php   /*Comment*/ ");

    assertThat(data).hasSize(1);
    assertData(data.get(0), 8, 19, "cd");
  }

  @Test
  public void single_line_comment() throws Exception {
    List<HighlightingData> data = getData("<?php   //Comment ");

    assertThat(data).hasSize(1);
    assertData(data.get(0), 8, 18, "cd");
  }

  @Test
  public void shell_style_comment() throws Exception {
    List<HighlightingData> data = getData("<?php   #Comment ");

    assertThat(data).hasSize(1);
    assertData(data.get(0), 8, 17, "cd");
  }

  @Test
  public void phpdoc_comment() throws Exception {
    List<HighlightingData> data = getData("<?php   /**Comment*/ ");

    assertThat(data).hasSize(1);
    assertData(data.get(0), 8, 20, "j");
  }

  @Test
  public void keyword() throws Exception {
    List<HighlightingData> data = getData("<?php eval(\"1\");");

    assertThat(data).hasSize(2);
    assertData(data.get(0), 6, 10, "k");
    assertData(data.get(1), 11, 14, "s");
  }

  @Test
  public void php_reserved_variables() throws Exception {
    List<HighlightingData> data = getData("<?php $a = $this; $b = __LINE__;");

    assertThat(data).hasSize(2);
    assertData(data.get(0), 11, 16, "k");
    assertData(data.get(1), 23, 31, "k");
  }

  @Test
  public void string() throws Exception {
    List<HighlightingData> data = getData("<?php $x = \"a\";");

    assertThat(data).hasSize(1);
    assertData(data.get(0), 11, 14, "s");
  }

  @Test
  public void expandable_string() throws Exception {
    List<HighlightingData> data = getData("<?php \"Hello $name!\";");

    assertThat(data).hasSize(4);
    assertData(data.get(0), 6, 7, "s");    // open quote
    assertData(data.get(1), 19, 20, "s");  // close quote
    assertData(data.get(2), 7, 13, "s");
    assertData(data.get(3), 18, 19, "s");
  }

  @Test
  public void numbers() throws Exception {
    List<HighlightingData> data = getData("<?php $x = 1; $y = 1.0;");

    assertThat(data).hasSize(2);
    assertData(data.get(0), 11, 12, "c");
    assertData(data.get(1), 19, 22, "c");
  }

  private List<HighlightingData> getData(String s) {
    return HighlighterVisitor.getHighlightData(PARSER.parse(s), new SourceFileOffsets(s));

  }

  private static void assertData(HighlightingData data, Integer startOffset, Integer endOffset, String code) {
    assertThat(data.startOffset()).isEqualTo(startOffset);
    assertThat(data.endOffset()).isEqualTo(endOffset);
    assertThat(data.highlightCode()).isEqualTo(code);
  }

}
