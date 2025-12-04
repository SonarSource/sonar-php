/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.plugins.php.api.tree;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

public interface SeparatedList<T extends Tree> extends List<T> {

  SyntaxToken getSeparator(int i);

  List<SyntaxToken> getSeparators();

  Iterator<Tree> elementsAndSeparators(final Function<T, ? extends Tree> elementTransformer);

  Iterator<Tree> elementsAndSeparators();

}
