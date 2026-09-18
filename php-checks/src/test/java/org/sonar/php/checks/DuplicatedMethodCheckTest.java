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
package org.sonar.php.checks;

import com.sonar.sslr.api.typed.ActionParser;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.php.checks.DuplicatedMethodCheck.AccessorUtils;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.php.parser.PHPParserBuilder;
import org.sonar.plugins.php.CheckVerifier;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;

import static org.assertj.core.api.Assertions.assertThat;

class DuplicatedMethodCheckTest {

  private final ActionParser<Tree> parser = PHPParserBuilder.createParser(PHPLexicalGrammar.TOP_STATEMENT);

  @Test
  void test() throws Exception {
    CheckVerifier.verify(new DuplicatedMethodCheck(), "DuplicatedMethodCheck.php");
  }

  @Test
  void shouldRecognizeGetters() {
    assertAccessors(
      "private string $value;" +
        "public string $publicValue;" +
        "public function getValue(): string { return $this->value; }" +
        "public function getWithParameter($parameter): string { return $this->value; }" +
        "protected function getProtected(): string { return $this->value; }" +
        "public static function getStatic(): string { return $this->value; }" +
        "public function getPublicValue(): string { return $this->publicValue; }" +
        "public function getDynamicValue(): string { return $this->dynamicValue; }" +
        "public function getFromCall(): string { return loadValue(); }",
      "getValue");
  }

  @Test
  void shouldRecognizeBooleanGetters() {
    assertAccessors(
      "private bool $value;" +
        "public function isValue(): bool { return $this->value; }" +
        "public function isValueWithoutType() { return $this->value; }" +
        "public function isNullableValue(): ?bool { return $this->value; }" +
        "public function isStringValue(): string { return $this->value; }" +
        "public function isValueFromCall(): bool { return loadValue(); }",
      "isValue", "isValueWithoutType", "isNullableValue");
  }

  @Test
  void shouldRecognizeSetters() {
    assertAccessors(
      "private string $value;" +
        "public string $publicValue;" +
        "public function setValue(string $value): void { $this->value = $value; }" +
        "public function setValueWithoutType(string $value) { $this->value = $value; }" +
        "public function setValueWithStringType(string $value): string { $this->value = $value; }" +
        "public function setValueFromCall(string $value): void { store($value); }" +
        "public function setValueFromOtherParameter(string $value): void { $this->value = $other; }" +
        "public function setPublicValue(string $value): void { $this->publicValue = $value; }",
      "setValue", "setValueWithoutType");
  }

  @Test
  void shouldRecognizeAccessorsForPromotedProperties() {
    assertAccessors(
      "public function __construct(private string $privateValue, protected string $protectedValue, public string $publicValue) {}" +
        "public function getPrivateValue(): string { return $this->privateValue; }" +
        "public function getProtectedValue(): string { return $this->protectedValue; }" +
        "public function getPublicValue(): string { return $this->publicValue; }",
      "getPrivateValue", "getProtectedValue");
  }

  @Test
  void shouldRejectOrdinaryOneStatementMethods() {
    assertAccessors(
      "public function getReturnedCall($id) { return $this->load($id); }" +
        "public function setCallStatement($value): void { $this->store($value); }" +
        "public function isLiteral(): bool { return false; }" +
        "public function getFactoryCall() { return Result::create(); }" +
        "public function setFluently() { return $this; }" +
        "public function getNull() { return null; }" +
        "public function getArray() { return []; }" +
        "public function getObject() { return new Result(); }" +
        "public function getUnsupported() { throw new LogicException(); }");
  }

  private void assertAccessors(String classMembers, String... expectedAccessorNames) {
    ClassDeclarationTree classTree = (ClassDeclarationTree) parser.parse("class TestClass {" + classMembers + "}");
    AccessorUtils accessorUtils = new AccessorUtils(classTree);
    assertThat(methods(classTree))
      .filteredOn(accessorUtils::isAccessor)
      .extracting(method -> method.name().text())
      .containsExactly(expectedAccessorNames);
  }

  private static List<MethodDeclarationTree> methods(ClassDeclarationTree classTree) {
    return classTree.members().stream()
      .filter(member -> member.is(Tree.Kind.METHOD_DECLARATION))
      .map(MethodDeclarationTree.class::cast)
      .toList();
  }

}
