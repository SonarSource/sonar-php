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
package org.sonar.plugins.php.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sonar.api.web.CodeColorizerFormat;
import org.sonar.colorizer.CDocTokenizer;
import org.sonar.colorizer.CppDocTokenizer;
import org.sonar.colorizer.KeywordsTokenizer;
import org.sonar.colorizer.Tokenizer;

public class PhpSourceCodeColorizer extends CodeColorizerFormat {

  private static final Set<String> PHP_KEYWORDS = new HashSet<String>();

  private static final String[] PHP_KEYWORDS_ARRAY = new String[] {
      "and", "or", "xor", "__FILE__", "exception", "__LINE__", "array", "as", "break", "case",
      "class", "const", "continue", "declare", "default",
      "die", "do", "echo", "else", "elseif",
      "empty", "enddeclare", "endfor", "endforeach", "endif",
      "endswitch", "endwhile", "eval", "exit", "extends",
      "for", "foreach", "function", "global", "if",
      "include", "include_once", "isset", "list", "new",
      "print", "require", "require_once", "return", "static",
      "switch", "unset", "use", "var", "while",
      "__FUNCTION__", "__CLASS__", "__METHOD__", "final", "php_user_filter",
      "interface", "implements", "instanceof", "public", "private",
      "protected", "abstract", "clone", "try", "catch",
      "throw", "cfunction", "old_function", "this", "final",
      "__NAMESPACE__", "namespace", "goto", "__DIR__"
  };

  static {
    Collections.addAll(PHP_KEYWORDS, PHP_KEYWORDS_ARRAY);
  }

  public PhpSourceCodeColorizer() {
    super(Php.KEY);
  }

  @Override
  public List<Tokenizer> getTokenizers() {
    return Collections.unmodifiableList(Arrays.asList(new CDocTokenizer("<span class=\"cd\">", "</span>"), new CppDocTokenizer(
        "<span class=\"cppd\">",
        "</span>"),
        new KeywordsTokenizer("<span class=\"k\">", "</span>", PHP_KEYWORDS)));
  }
}
