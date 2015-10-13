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
package org.sonar.php;

import com.google.common.collect.ImmutableList;
import com.sonar.sslr.api.typed.ActionParser;
import org.sonar.php.api.CharsetAwareVisitor;
import org.sonar.php.parser.PHPParserBuilder;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.visitors.Issue;
import org.sonar.plugins.php.api.visitors.PHPCheck;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;

public class PHPAnalyzer {

  private final ActionParser<Tree> parser;
  private final ImmutableList<PHPCheck> checks;

  public PHPAnalyzer(Charset charset, ImmutableList<PHPCheck> checks) {
    this.parser = PHPParserBuilder.createParser(charset);
    this.checks = checks;

    for (PHPCheck check : checks) {
      if (check instanceof CharsetAwareVisitor) {
        ((CharsetAwareVisitor) check).setCharset(charset);
      }
      check.init();
    }
  }

  public List<Issue> analyze(File file) {
    // fixme : handle parsing exceptions
    CompilationUnitTree tree = (CompilationUnitTree) parser.parse(file);

    ImmutableList.Builder<Issue> issuesBuilder = ImmutableList.builder();
    for (PHPCheck check : checks) {
      issuesBuilder.addAll(check.analyze(file, tree));
    }

    return issuesBuilder.build();
  }

}
