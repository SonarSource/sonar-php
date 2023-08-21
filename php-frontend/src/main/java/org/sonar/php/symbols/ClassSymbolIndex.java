/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2023 SonarSource SA
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
package org.sonar.php.symbols;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.visitors.LocationInFile;

public class ClassSymbolIndex {

  private final Map<QualifiedName, ClassSymbol> symbolsByQualifiedName = new HashMap<>();
  private final Map<ClassSymbolData, ClassSymbolImpl> symbolsByData = new HashMap<>();
  private final ProjectSymbolData projectSymbolData;

  private ClassSymbolIndex(ProjectSymbolData projectSymbolData) {
    this.projectSymbolData = projectSymbolData;
  }

  public static ClassSymbolIndex create(List<ClassSymbolData> fileDeclarations, ProjectSymbolData projectSymbolData) {
    ClassSymbolIndex index = new ClassSymbolIndex(projectSymbolData);
    index.init(fileDeclarations);
    return index;
  }

  public ClassSymbol get(QualifiedName qualifiedName) {
    return symbolsByQualifiedName.computeIfAbsent(qualifiedName, qn -> projectSymbolData.classSymbolData(qn)
      .<ClassSymbol>map(ClassSymbolImpl::new)
      .orElse(new UnknownClassSymbol(qualifiedName)));
  }

  public ClassSymbol get(ClassSymbolData classSymbolData) {
    return symbolsByData.get(classSymbolData);
  }

  private void init(List<ClassSymbolData> fileDeclarations) {
    for (ClassSymbolData data : fileDeclarations) {
      ClassSymbolImpl symbol = new ClassSymbolImpl(data);
      symbolsByQualifiedName.put(symbol.qualifiedName(), symbol);
      symbolsByData.put(data, symbol);
    }
  }

  private class ClassSymbolImpl extends AbstractClassSymbol {

    private final ClassSymbolData data;
    private final Map<String, MethodSymbol> methods;
    private ClassSymbol superClass;

    private ClassSymbolImpl(ClassSymbolData data) {
      this.data = data;
      this.methods = data.methods().stream()
        .map(d -> new MethodSymbolImpl(d, this))
        .collect(Collectors.toMap(m -> m.name().toLowerCase(Locale.ROOT), Function.identity(), (a, b) -> a));
    }

    @Override
    public LocationInFile location() {
      return data.location();
    }

    @Override
    public QualifiedName qualifiedName() {
      return data.qualifiedName();
    }

    @Override
    public Optional<ClassSymbol> superClass() {
      if (superClass == null) {
        data.superClass().ifPresent(name -> superClass = get(name));
      }
      return Optional.ofNullable(superClass);
    }

    @Override
    public List<ClassSymbol> implementedInterfaces() {
      return data.implementedInterfaces().stream().map(i -> get(i)).collect(Collectors.toList());
    }

    @Override
    public boolean is(Kind kind) {
      return kind == data.kind();
    }

    @Override
    public List<MethodSymbol> declaredMethods() {
      return new ArrayList<>(methods.values());
    }

    @Override
    public MethodSymbol getDeclaredMethod(String name) {
      return methods.getOrDefault(name.toLowerCase(Locale.ROOT), new UnknownMethodSymbol(qualifiedName() + "::" + name));
    }

    @Override
    public boolean isUnknownSymbol() {
      return false;
    }
  }

}
