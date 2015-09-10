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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.sonar.api.web.CodeColorizerFormat;
import org.sonar.colorizer.CDocTokenizer;
import org.sonar.colorizer.CppDocTokenizer;
import org.sonar.colorizer.InlineDocTokenizer;
import org.sonar.colorizer.KeywordsTokenizer;
import org.sonar.colorizer.StringTokenizer;
import org.sonar.colorizer.Tokenizer;
import org.sonar.php.api.PHPKeyword;
import org.sonar.plugins.php.api.Php;

import java.util.List;
import java.util.Set;

/**
 * Class used to colorize source code in HTML.
 */
public class PhpSourceCodeColorizer extends CodeColorizerFormat {

  /**
   * An array containing reserved variables.
   */
  private static final String[] PHP_RESERVED_VARIABLES_ARRAY = new String[]{"__FUNCTION__", "__CLASS__", "__METHOD__", "__NAMESPACE__",
    "__DIR__", "__FILE__", "__LINE__", "$this"};

  /**
   * Simple constructor
   */
  public PhpSourceCodeColorizer() {
    super(Php.KEY);
  }

  /**
   * We use here the C/C++ tokenizers, the custom PHP Tokenizer and the standard String tokenir (handles simple quotes and double quotes
   * delimited strings).
   *
   * @see org.sonar.api.web.CodeColorizerFormat#getTokenizers()
   */
  @Override
  public List<Tokenizer> getTokenizers() {
    Set<String> keywords = Sets.newHashSet(PHP_RESERVED_VARIABLES_ARRAY);
    keywords.addAll(Sets.newHashSet(PHPKeyword.getKeywordValues()));

    String tagAfter = "</span>";
    List<Tokenizer> tokenizers = Lists.newArrayList();
    tokenizers.add(new CDocTokenizer("<span class=\"cd\">", tagAfter));
    tokenizers.add(new ShellCommentTokenizer("<span class=\"cd\">", tagAfter));
    tokenizers.add(new CppDocTokenizer("<span class=\"cppd\">", tagAfter));
    tokenizers.add(new KeywordsTokenizer("<span class=\"k\">", tagAfter, keywords));
    tokenizers.add(new StringTokenizer("<span class=\"s\">", tagAfter));
    return tokenizers;
  }

  private static class ShellCommentTokenizer extends InlineDocTokenizer {

    public ShellCommentTokenizer(String tagBefore, String tagAfter) {
      super("#", tagBefore, tagAfter);
    }

  }
}
