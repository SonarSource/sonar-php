/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2018 SonarSource SA
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
package org.sonar.php.parser;

import com.sonar.sslr.api.typed.ActionParser;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import javax.annotation.Nullable;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.sslr.grammar.GrammarRuleKey;

public class PHPParser extends ActionParser<Tree> {

  public PHPParser(GrammarRuleKey rootRule, int lineOffset) {
    // we can pass any charset, it's not used. To parse file, we use sting content of it.
    super(
      StandardCharsets.UTF_8,
      PHPLexicalGrammar.createGrammarBuilder(),
      PHPGrammar.class,
      new TreeFactory(),
      new PHPNodeBuilder(lineOffset),
      rootRule);
  }

  @Override
  public Tree parse(File file) {
    return setParents(super.parse(file));
  }

  @Override
  public Tree parse(String source) {
    return setParents(super.parse(source));
  }

  private static Tree setParents(Tree tree) {
    PHPTree current = (PHPTree) tree;
    Queue<PHPTree> setParentQueue = new LinkedList<>();
    while (current != null) {
      Iterator<Tree> childrenIterator = current.childrenIterator();
      while (childrenIterator.hasNext()) {
        setParent(current, (PHPTree) childrenIterator.next(), setParentQueue);
      }
      current = setParentQueue.poll();
    }
    return tree;
  }

  private static void setParent(PHPTree parent, @Nullable PHPTree child, Queue<PHPTree> setParentQueue) {
    if (child != null) {
      child.setParent(parent);
      if (!child.isLeaf()) {
        setParentQueue.add(child);
      }
    }
  }

}
