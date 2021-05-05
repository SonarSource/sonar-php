/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2021 SonarSource SA
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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Test;
import org.sonar.plugins.php.api.symbols.QualifiedName;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.plugins.php.api.symbols.QualifiedName.qualifiedName;

public class ClassSymbolIndexTest {

  @Test
  public void class_without_superclass() {
    ClassSymbolData a = data("ns1\\a");
    ClassSymbolData b = data("ns1\\b");
    ClassSymbolIndex symbols = createSymbols(a, b);
    assertThat(symbols.get(a).superClass()).isEmpty();
    assertThat(symbols.get(b).superClass()).isEmpty();
  }

  @Test
  public void superclass_in_current_file() {
    ClassSymbolData a = data("ns1\\a");
    ClassSymbolData b = data("ns1\\b", "ns1\\a");
    ClassSymbolIndex symbols = createSymbols(a, b);
    assertThat(symbols.get(a).superClass()).isEmpty();
    assertThat(symbols.get(b).superClass()).containsSame(symbols.get(a));
  }

  @Test
  public void superclass_outside_current_file() {
    ClassSymbolData a = data("ns1\\a");
    ClassSymbolData b = data("ns1\\b", "ns1\\a");
    ClassSymbolIndex symbols = createSymbols(projectData(a), b);
    assertThat(symbols.get(b).superClass().get().qualifiedName()).isEqualTo(a.qualifiedName());
  }

  @Test
  public void two_classes_with_same_superclass_outside_current_file() {
    ClassSymbolData a = data("ns1\\a");
    ClassSymbolData b = data("ns1\\b", "ns1\\a");
    ClassSymbolData c = data("ns1\\c", "ns1\\a");
    ClassSymbolIndex symbols = createSymbols(projectData(a), b, c);
    assertThat(symbols.get(b).superClass()).containsSame(symbols.get(c).superClass().get());
  }

  @Test
  public void superclass_of_superclass_outside_current_file() {
    ClassSymbolData a = data("ns1\\a");
    ClassSymbolData b = data("ns1\\b", "ns1\\a");
    ClassSymbolData c = data("ns1\\c", "ns1\\b");
    ClassSymbolIndex symbols = createSymbols(projectData(a, b), c);
    ClassSymbol cSuperClass = symbols.get(c).superClass().get();
    assertThat(cSuperClass.qualifiedName()).isEqualTo(b.qualifiedName());
    assertThat(cSuperClass.superClass().get().qualifiedName()).isEqualTo(a.qualifiedName());
  }

  @Test
  public void cycle_between_classes_in_current_file() {
    ClassSymbolData a = data("ns1\\a", "ns1\\c");
    ClassSymbolData b = data("ns1\\b", "ns1\\a");
    ClassSymbolData c = data("ns1\\c", "ns1\\b");
    ClassSymbolIndex symbols = createSymbols(a, b, c);
    assertThat(symbols.get(a).superClass()).containsSame(symbols.get(c));
    assertThat(symbols.get(b).superClass()).containsSame(symbols.get(a));
    assertThat(symbols.get(c).superClass()).containsSame(symbols.get(b));
  }

  @Test
  public void cycle_between_classes_outside_current_file() {
    ClassSymbolData a = data("ns1\\a", "ns1\\b");
    ClassSymbolData b = data("ns1\\b", "ns1\\a");
    ClassSymbolData c = data("ns1\\c", "ns1\\a");
    ClassSymbolIndex symbols = createSymbols(projectData(a, b), c);
    ClassSymbol cSuperClass = symbols.get(c).superClass().get();
    assertThat(cSuperClass.superClass().get().superClass()).containsSame(cSuperClass);
  }

  @Test
  public void unknown_super_class() {
    ClassSymbolData a = data("ns1\\a", "ns1\\c");
    ClassSymbolData b = data("ns1\\b", "ns1\\c");
    ClassSymbolIndex symbols = createSymbols(a, b);
    Optional<ClassSymbol> superClass = symbols.get(a).superClass();
    assertThat(superClass).isNotEmpty();
    assertThat(superClass.get().isUnknownSymbol()).isTrue();
    assertThat(superClass.get().qualifiedName()).isEqualTo(qualifiedName("ns1\\c"));
    assertThat(superClass.get().superClass()).isEmpty();
    assertThat(superClass.get().location()).isEqualTo(UnknownLocationInFile.UNKNOWN_LOCATION);
    assertThat(symbols.get(b).superClass()).containsSame(superClass.get());
  }

  @Test
  public void get_by_qualified_name() {
    ClassSymbolData a = data("ns1\\a", "ns1\\c");
    ClassSymbolData b = data("ns1\\b", "ns1\\c");
    ClassSymbolData c = data("ns1\\c");
    ClassSymbolIndex symbols = createSymbols(projectData(a), b, c);
    assertThat(symbols.get(a)).isNull();
    assertThat(symbols.get(b.qualifiedName())).isSameAs(symbols.get(b));
    assertThat(symbols.get(a.qualifiedName()).superClass()).containsSame(symbols.get(c));

    assertThat(symbols.get(qualifiedName("unknown")).isUnknownSymbol()).isTrue();
    assertThat(symbols.get(qualifiedName("unknown"))).isSameAs(symbols.get(qualifiedName("unknown")));
    assertThat(symbols.get(qualifiedName("unknown"))).isNotEqualTo(symbols.get(qualifiedName("otherunknown")));
  }

  @Test
  public void isOrSubclassOf() {
    ClassSymbolIndex symbols = createSymbols(
      data("a", "b"),
      data("b"),
      data("c", "d"));
    ClassSymbol a = symbols.get(fqn("a"));
    ClassSymbol b = symbols.get(fqn("b"));
    ClassSymbol c = symbols.get(fqn("c"));
    assertThat(a.isOrSubClassOf(fqn("a"))).isEqualTo(Trilean.TRUE);
    assertThat(a.isOrSubClassOf(fqn("b"))).isEqualTo(Trilean.TRUE);
    assertThat(a.isOrSubClassOf(fqn("c"))).isEqualTo(Trilean.FALSE);
    assertThat(b.isOrSubClassOf(fqn("a"))).isEqualTo(Trilean.FALSE);
    assertThat(c.isOrSubClassOf(fqn("d"))).isEqualTo(Trilean.TRUE);
    assertThat(c.isOrSubClassOf(fqn("e"))).isEqualTo(Trilean.UNKNOWN);
    ClassSymbol unknown = symbols.get(fqn("unknown"));
    assertThat(unknown.isOrSubClassOf(fqn("x"))).isEqualTo(Trilean.UNKNOWN);
    assertThat(unknown.isOrSubClassOf(fqn("unknown"))).isEqualTo(Trilean.TRUE);
  }

  @Test
  public void isOrSubclassOf_with_cycle() {
    ClassSymbolIndex symbols = createSymbols(
      data("a", "b"),
      data("b", "a"));
    ClassSymbol a = symbols.get(fqn("a"));
    assertThat(a.isOrSubClassOf(fqn("a"))).isEqualTo(Trilean.TRUE);
    assertThat(a.isOrSubClassOf(fqn("b"))).isEqualTo(Trilean.TRUE);
    assertThat(a.isOrSubClassOf(fqn("c"))).isEqualTo(Trilean.FALSE);
  }

  @Test
  public void isSubTypeOf() {
    ClassSymbolIndex symbols = createSymbols(
      data("a", "b"),
      data("b"),
      data("c", "d", singletonList("e")),
      data("e", "f"));
    ClassSymbol a = symbols.get(fqn("a"));
    ClassSymbol b = symbols.get(fqn("b"));
    ClassSymbol c = symbols.get(fqn("c"));
    assertThat(a.isSubTypeOf(fqn("a"))).isEqualTo(Trilean.TRUE);
    assertThat(a.isSubTypeOf(fqn("b"))).isEqualTo(Trilean.TRUE);
    assertThat(a.isSubTypeOf(fqn("c"))).isEqualTo(Trilean.FALSE);
    assertThat(b.isSubTypeOf(fqn("a"))).isEqualTo(Trilean.FALSE);
    assertThat(c.isSubTypeOf(fqn("d"))).isEqualTo(Trilean.TRUE);
    assertThat(c.isSubTypeOf(fqn("e"))).isEqualTo(Trilean.TRUE);
    assertThat(c.isSubTypeOf(fqn("f"))).isEqualTo(Trilean.TRUE);
    assertThat(c.isSubTypeOf(fqn("x"))).isEqualTo(Trilean.UNKNOWN);
  }

  @Test
  public void isSubTypeOf_with_cycle() {
    ClassSymbolIndex symbols = createSymbols(
      data("a", "b"),
      data("b", "a"));
    ClassSymbol a = symbols.get(fqn("a"));
    assertThat(a.isSubTypeOf(fqn("a"))).isEqualTo(Trilean.TRUE);
    assertThat(a.isSubTypeOf(fqn("b"))).isEqualTo(Trilean.TRUE);
    assertThat(a.isSubTypeOf(fqn("c"))).isEqualTo(Trilean.FALSE);
  }

  @Test
  public void get_method() {
    List<MethodSymbolData> methods = Arrays.asList(
      method("methodA"),
      method("methodB")
    );

    ClassSymbolIndex symbols = createSymbols(
      data("a", methods),
      data("b")
    );

    ClassSymbol a = symbols.get(fqn("a"));
    assertThat(a.declaredMethods()).hasSize(2);
    assertThat(a.getDeclaredMethod("methodA").isUnknownSymbol()).isFalse();
    assertThat(a.getDeclaredMethod("random").isUnknownSymbol()).isTrue();
    assertThat(a.is(ClassSymbol.Kind.NORMAL)).isTrue();

    ClassSymbol b = symbols.get(fqn("b"));
    assertThat(b.declaredMethods()).isEmpty();
    assertThat(b.getDeclaredMethod("methodA")).isInstanceOf(UnknownMethodSymbol.class);
    assertThat(a.is(ClassSymbol.Kind.INTERFACE)).isFalse();

    ClassSymbol unknown = symbols.get(fqn("unknown"));
    assertThat(unknown.declaredMethods()).isEmpty();
    assertThat(unknown.getDeclaredMethod("foo")).isInstanceOf(UnknownMethodSymbol.class);
  }

  @Test
  public void unknown_class_and_method() {
    ClassSymbolIndex symbols = createSymbols(data("a"));

    ClassSymbol classSymbol = symbols.get(fqn("x"));
    assertThat(classSymbol).isInstanceOf(UnknownClassSymbol.class);
    assertThat(classSymbol.is(ClassSymbol.Kind.NORMAL)).isFalse();

    MethodSymbol methodSymbol = classSymbol.getDeclaredMethod("y");
    assertThat(methodSymbol).isInstanceOf(UnknownMethodSymbol.class);
    assertThat(methodSymbol.visibility()).isEqualTo(Visibility.PUBLIC);
    assertThat(methodSymbol.owner()).isInstanceOf(UnknownClassSymbol.class);
  }


  private ClassSymbolIndex createSymbols(ClassSymbolData... data) {
    return createSymbols(new ProjectSymbolData(), data);
  }

  private ClassSymbolIndex createSymbols(ProjectSymbolData projectData, ClassSymbolData... data) {
    ClassSymbolIndex result = ClassSymbolIndex.create(new HashSet<>(Arrays.asList(data)), projectData);
    for (ClassSymbolData d : data) {
      assertThat(result.get(d).qualifiedName()).isEqualTo(d.qualifiedName());
      assertThat(result.get(d).isUnknownSymbol()).isFalse();
    }
    return result;
  }

  private ProjectSymbolData projectData(ClassSymbolData... data) {
    ProjectSymbolData projectSymbolData = new ProjectSymbolData();
    for (ClassSymbolData d : data) {
      projectSymbolData.add(d);
    }
    return projectSymbolData;
  }

  private QualifiedName fqn(String name) {
    return qualifiedName(name);
  }

  private ClassSymbolData data(String fqn) {
    return new ClassSymbolData(someLocation(), qualifiedName(fqn), null, emptyList(), emptyList());
  }

  private ClassSymbolData data(String fqn, List<MethodSymbolData> methods) {
    return new ClassSymbolData(someLocation(), qualifiedName(fqn), null, emptyList(), methods);
  }

  private ClassSymbolData data(String fqn, String superClassFqn) {
    return new ClassSymbolData(someLocation(), qualifiedName(fqn), qualifiedName(superClassFqn), emptyList(), emptyList());
  }

  private ClassSymbolData data(String fqn, String superClassFqn, List<String> interfaceFqns) {
    List<QualifiedName> interfaces = interfaceFqns.stream().map(this::fqn).collect(Collectors.toList());
    return new ClassSymbolData(someLocation(), qualifiedName(fqn), qualifiedName(superClassFqn), interfaces, emptyList());
  }

  private MethodSymbolData method(String name) {
    return new MethodSymbolData(someLocation(), name, emptyList(), new FunctionSymbolData.FunctionSymbolProperties(), Visibility.PUBLIC);
  }

  private LocationInFileImpl someLocation() {
    return new LocationInFileImpl("path", 1, 0, 1, 3);
  }
}
