/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
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
package org.sonar.php.checks.utils;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.sonar.php.symbols.ClassSymbol;
import org.sonar.php.symbols.MethodSymbol;
import org.sonar.php.tree.impl.declaration.MethodDeclarationTreeImpl;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

public abstract class PhpUnitCheck extends PHPVisitorCheck {

  private static final Map<String, Assertion> ASSERTION = assertions();

  private boolean isPhpUnitTestCase = false;
  private boolean isPhpUnitTestMethod = false;

  private static Map<String, Assertion> assertions() {
    return Stream.of(
      new Assertion("assertArrayHasKey"),
      new Assertion("assertArraySubset"),
      new Assertion("assertArrayNotHasKey"),
      new Assertion("assertContains"),
      new Assertion("assertContainsEquals"),
      new Assertion("assertAttributeContains"),
      new Assertion("assertNotContains"),
      new Assertion("assertNotContainsEquals"),
      new Assertion("assertAttributeNotContains"),
      new Assertion("assertContainsOnly"),
      new Assertion("assertContainsOnlyInstancesOf"),
      new Assertion("assertAttributeContainsOnly"),
      new Assertion("assertNotContainsOnly"),
      new Assertion("assertAttributeNotContainsOnly"),
      new Assertion("assertCount", true),
      new Assertion("assertAttributeCount"),
      new Assertion("assertNotCount", true),
      new Assertion("assertAttributeNotCount"),
      new Assertion("assertEquals", true),
      new Assertion("assertEqualsCanonicalizing", true),
      new Assertion("assertEqualsIgnoringCase", true),
      new Assertion("assertEqualsWithDelta", true),
      new Assertion("assertAttributeEquals"),
      new Assertion("assertNotEquals", true),
      new Assertion("assertNotEqualsCanonicalizing", true),
      new Assertion("assertNotEqualsIgnoringCase", true),
      new Assertion("assertNotEqualsWithDelta", true),
      new Assertion("assertAttributeNotEquals"),
      new Assertion("assertEmpty"),
      new Assertion("assertAttributeEmpty"),
      new Assertion("assertNotEmpty"),
      new Assertion("assertAttributeNotEmpty"),
      new Assertion("assertGreaterThan", true),
      new Assertion("assertAttributeGreaterThan"),
      new Assertion("assertGreaterThanOrEqual", true),
      new Assertion("assertAttributeGreaterThanOrEqual"),
      new Assertion("assertLessThan", true),
      new Assertion("assertAttributeLessThan", true),
      new Assertion("assertLessThanOrEqual", true),
      new Assertion("assertAttributeLessThanOrEqual"),
      new Assertion("assertFileEquals", true),
      new Assertion("assertFileEqualsCanonicalizing", true),
      new Assertion("assertFileEqualsIgnoringCase", true),
      new Assertion("assertFileNotEquals", true),
      new Assertion("assertFileNotEqualsCanonicalizing", true),
      new Assertion("assertFileNotEqualsIgnoringCase", true),
      new Assertion("assertStringEqualsFile", true),
      new Assertion("assertStringEqualsFileCanonicalizing", true),
      new Assertion("assertStringEqualsFileIgnoringCase", true),
      new Assertion("assertStringNotEqualsFile", true),
      new Assertion("assertStringNotEqualsFileCanonicalizing", true),
      new Assertion("assertStringNotEqualsFileIgnoringCase", true),
      new Assertion("assertIsReadable"),
      new Assertion("assertNotIsReadable"),
      new Assertion("assertIsWritable"),
      new Assertion("assertNotIsWritable"),
      new Assertion("assertDirectoryExists"),
      new Assertion("assertDirectoryNotExists"),
      new Assertion("assertDirectoryIsReadable"),
      new Assertion("assertDirectoryNotIsReadable"),
      new Assertion("assertDirectoryIsWritable"),
      new Assertion("assertDirectoryNotIsWritable"),
      new Assertion("assertFileExists"),
      new Assertion("assertFileNotExists"),
      new Assertion("assertFileIsReadable"),
      new Assertion("assertFileNotIsReadable"),
      new Assertion("assertFileIsWritable"),
      new Assertion("assertFileNotIsWritable"),
      new Assertion("assertTrue"),
      new Assertion("assertNotTrue"),
      new Assertion("assertFalse"),
      new Assertion("assertNotFalse"),
      new Assertion("assertNull"),
      new Assertion("assertNotNull"),
      new Assertion("assertFinite"),
      new Assertion("assertInfinite"),
      new Assertion("assertNan"),
      new Assertion("assertClassHasAttribute"),
      new Assertion("assertClassNotHasAttribute"),
      new Assertion("assertClassHasStaticAttribute"),
      new Assertion("assertClassNotHasStaticAttribute"),
      new Assertion("assertObjectHasAttribute"),
      new Assertion("assertObjectNotHasAttribute"),
      new Assertion("assertSame", true),
      new Assertion("assertAttributeSame"),
      new Assertion("assertNotSame", true),
      new Assertion("assertAttributeNotSame"),
      new Assertion("assertInstanceOf", true),
      new Assertion("assertAttributeInstanceOf"),
      new Assertion("assertNotInstanceOf", true),
      new Assertion("assertAttributeNotInstanceOf"),
      new Assertion("assertInternalType", true),
      new Assertion("assertAttributeInternalType"),
      new Assertion("assertIsArray"),
      new Assertion("assertIsBool"),
      new Assertion("assertIsFloat"),
      new Assertion("assertIsInt"),
      new Assertion("assertIsNumeric"),
      new Assertion("assertIsObject"),
      new Assertion("assertIsResource"),
      new Assertion("assertIsString"),
      new Assertion("assertIsScalar"),
      new Assertion("assertIsCallable"),
      new Assertion("assertIsIterable"),
      new Assertion("assertNotInternalType", true),
      new Assertion("assertIsNotArray"),
      new Assertion("assertIsNotBool"),
      new Assertion("assertIsNotFloat"),
      new Assertion("assertIsNotInt"),
      new Assertion("assertIsNotNumeric"),
      new Assertion("assertIsNotObject"),
      new Assertion("assertIsNotResource"),
      new Assertion("assertIsNotString"),
      new Assertion("assertIsNotScalar"),
      new Assertion("assertIsNotCallable"),
      new Assertion("assertIsNotIterable"),
      new Assertion("assertAttributeNotInternalType"),
      new Assertion("assertRegExp"),
      new Assertion("assertNotRegExp"),
      new Assertion("assertSameSize", true),
      new Assertion("assertNotSameSize", true),
      new Assertion("assertStringMatchesFormat"),
      new Assertion("assertStringNotMatchesFormat"),
      new Assertion("assertStringMatchesFormatFile"),
      new Assertion("assertStringNotMatchesFormatFile"),
      new Assertion("assertStringStartsWith"),
      new Assertion("assertStringStartsNotWith"),
      new Assertion("assertStringContainsString"),
      new Assertion("assertStringContainsStringIgnoringCase"),
      new Assertion("assertStringNotContainsString"),
      new Assertion("assertStringNotContainsStringIgnoringCase"),
      new Assertion("assertStringEndsWith"),
      new Assertion("assertStringEndsNotWith"),
      new Assertion("assertXmlFileEqualsXmlFile", true),
      new Assertion("assertXmlFileNotEqualsXmlFile", true),
      new Assertion("assertXmlStringEqualsXmlFile", true),
      new Assertion("assertXmlStringNotEqualsXmlFile", true),
      new Assertion("assertXmlStringEqualsXmlString", true),
      new Assertion("assertXmlStringNotEqualsXmlString", true),
      new Assertion("assertEqualXMLStructure", true),
      new Assertion("assertThat"),
      new Assertion("assertJson"),
      new Assertion("assertJsonStringEqualsJsonString", true),
      new Assertion("assertJsonStringNotEqualsJsonString", true),
      new Assertion("assertJsonStringEqualsJsonFile", true),
      new Assertion("assertJsonStringNotEqualsJsonFile", true),
      new Assertion("assertJsonFileEqualsJsonFile", true),
      new Assertion("assertJsonFileNotEqualsJsonFile", true),
      new Assertion("logicalAnd"),
      new Assertion("logicalOr"),
      new Assertion("logicalNot"),
      new Assertion("logicalXor"),
      new Assertion("anything"),
      new Assertion("isTrue"),
      new Assertion("callback"),
      new Assertion("isFalse"),
      new Assertion("isJson"),
      new Assertion("isNull"),
      new Assertion("isFinite"),
      new Assertion("isInfinite"),
      new Assertion("isNan"),
      new Assertion("attribute"),
      new Assertion("contains"),
      new Assertion("containsEqual"),
      new Assertion("containsIdentical"),
      new Assertion("containsOnly"),
      new Assertion("containsOnlyInstancesOf"),
      new Assertion("arrayHasKey"),
      new Assertion("equalTo"),
      new Assertion("attributeEqualTo"),
      new Assertion("isEmpty"),
      new Assertion("isWritable"),
      new Assertion("isReadable"),
      new Assertion("directoryExists"),
      new Assertion("fileExists"),
      new Assertion("greaterThan"),
      new Assertion("greaterThanOrEqual"),
      new Assertion("classHasAttribute"),
      new Assertion("classHasStaticAttribute"),
      new Assertion("objectHasAttribute"),
      new Assertion("identicalTo"),
      new Assertion("isInstanceOf"),
      new Assertion("isType"),
      new Assertion("lessThan"),
      new Assertion("lessThanOrEqual"),
      new Assertion("matchesRegularExpression"),
      new Assertion("matches"),
      new Assertion("stringStartsWith"),
      new Assertion("stringContains"),
      new Assertion("stringEndsWith")).collect(Collectors.toMap(e -> e.name().toLowerCase(Locale.ENGLISH), e -> e));
  }

  @Override
  public void visitClassDeclaration(ClassDeclarationTree tree) {
    isPhpUnitTestCase = CheckUtils.isSubClassOfTestCase(tree);
    if (isPhpUnitTestCase) {
      visitPhpUnitTestCase(tree);
    }

    super.visitClassDeclaration(tree);

    isPhpUnitTestCase = false;
  }

  @Override
  public void visitMethodDeclaration(MethodDeclarationTree tree) {
    isPhpUnitTestMethod = isTestCaseMethod(tree);
    if (isPhpUnitTestMethod) {
      visitPhpUnitTestMethod(tree);
    }

    super.visitMethodDeclaration(tree);

    isPhpUnitTestMethod = false;
  }

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    if (isPhpUnitTestMethod) {
      getAssertion(tree).ifPresent(assertion -> visitPhpUnitAssertion(tree, assertion));
    }
    super.visitFunctionCall(tree);
  }

  protected boolean isTestCaseMethod(MethodDeclarationTree tree) {
    return isPhpUnitTestCase && isTestMethod(((MethodDeclarationTreeImpl) tree).symbol());
  }

  protected boolean hasTestMethod(ClassSymbol symbol) {
    return symbol.declaredMethods().stream().anyMatch(this::isTestMethod);
  }

  protected boolean isTestMethod(MethodSymbol symbol) {
    return symbol.isTestMethod().isTrue();
  }

  protected void visitPhpUnitTestCase(ClassDeclarationTree tree) {
    // can be specified in child check
  }

  protected void visitPhpUnitTestMethod(MethodDeclarationTree tree) {
    // can be specified in child check
  }

  protected void visitPhpUnitAssertion(FunctionCallTree tree, Assertion assertion) {
    // can be specified in child check
  }

  public boolean isPhpUnitTestCase() {
    return isPhpUnitTestCase;
  }

  public boolean isPhpUnitTestMethod() {
    return isPhpUnitTestMethod;
  }

  public static boolean isAssertion(FunctionCallTree tree) {
    return getAssertion(tree).isPresent();
  }

  public static boolean isFail(FunctionCallTree tree) {
    String name = CheckUtils.getLowerCaseFunctionName(tree);
    return name != null && name.endsWith("fail");
  }

  public static Optional<Assertion> getAssertion(FunctionCallTree tree) {
    String name = CheckUtils.lowerCaseFunctionName(tree);
    if (name != null && ASSERTION.containsKey(name)) {
      return Optional.of(ASSERTION.get(name));
    }
    return Optional.empty();
  }

  public static class Assertion {

    private final String name;
    private final boolean hasExpectedValue;

    public Assertion(String name) {
      this(name, false);
    }

    public Assertion(String name, boolean hasExpectedValue) {
      this.name = name;
      this.hasExpectedValue = hasExpectedValue;
    }

    public String name() {
      return name;
    }

    public boolean hasExpectedValue() {
      return hasExpectedValue;
    }
  }
}
