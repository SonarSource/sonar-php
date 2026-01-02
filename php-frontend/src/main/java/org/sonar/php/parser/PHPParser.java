/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.php.parser;

import com.sonar.sslr.api.RecognitionException;
import com.sonar.sslr.api.typed.ActionParser;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import javax.annotation.Nullable;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.php.utils.Throwables;
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
    try {
      return setParents(super.parse(source));
    } catch (RuntimeException e) {
      Throwable rootCause = Throwables.getRootCause(e);
      throw (rootCause instanceof RecognitionException recognitionException) ? recognitionException : e;
    }
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
