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
package org.sonar.plugins.php.api.symbols;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import java.util.LinkedList;
import java.util.List;
import org.sonar.php.tree.symbols.Scope;
import org.sonar.plugins.php.api.tree.expression.IdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

@Beta
public class Symbol {

  public enum Kind {
    VARIABLE("variable"),
    FUNCTION("function"),
    PARAMETER("parameter"),
    CLASS("class"),
    FIELD("field");

    private final String value;

    Kind(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }

  private final String name;
  private final IdentifierTree declaration;
  private Kind kind;
  private Scope scope;
  private List<SyntaxToken> usages = new LinkedList<>();
  private List<SyntaxToken> modifiers = new LinkedList<>();

  public Symbol(IdentifierTree declaration, Kind kind, Scope scope) {
    this.declaration = declaration;
    this.name = declaration.text();
    this.kind = kind;
    this.scope = scope;
  }

  public ImmutableList<SyntaxToken> modifiers() {
    return ImmutableList.copyOf(modifiers);
  }

  public boolean hasModifier(String modifier) {
    for (SyntaxToken syntaxToken : modifiers) {
      if (syntaxToken.text().equalsIgnoreCase(modifier)) {
        return true;
      }
    }
    return false;
  }

  public void addModifiers(List<SyntaxToken> modifiers) {
    this.modifiers.addAll(modifiers);
  }

  public void addUsage(SyntaxToken usage){
    usages.add(usage);
  }

  public void addUsage(IdentifierTree usage){
    usages.add(usage.token());
  }

  public List<SyntaxToken> usages(){
    return usages;
  }

  public Scope scope() {
    return scope;
  }

  public String name() {
    return name;
  }

  public IdentifierTree declaration() {
    return declaration;
  }

  public boolean is(Symbol.Kind kind) {
    return kind.equals(this.kind);
  }

  public boolean called(String name) {
    if (kind == Kind.VARIABLE || kind == Kind.PARAMETER) {
      return name.equals(this.name);
    } else {
      return name.equalsIgnoreCase(this.name);
    }
  }

  public Kind kind() {
    return kind;
  }

  @Override
  public String toString() {
    return "Symbol{" +
      "name='" + name + '\'' +
      ", kind=" + kind +
      ", scope=" + scope +
      '}';
  }
}
