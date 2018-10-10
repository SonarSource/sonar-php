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
package org.sonar.php.cfg;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.plugins.php.api.tree.Tree;

public class PhpCfgEndBlockTest extends PHPTreeModelTest {

  @Test(expected = UnsupportedOperationException.class)
  public void cannot_add_element() {
    PhpCfgEndBlock endBlock = new PhpCfgEndBlock();
    Tree tree = parse("array()", PHPLexicalGrammar.ARRAY_INIALIZER);
    endBlock.addElement(tree);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void cannot_replace_successors() {
    PhpCfgEndBlock endBlock = new PhpCfgEndBlock();
    Map<PhpCfgBlock, PhpCfgBlock> map = new HashMap<>();
    map.put(endBlock, endBlock);
    endBlock.replaceSuccessors(map);
  }
}
