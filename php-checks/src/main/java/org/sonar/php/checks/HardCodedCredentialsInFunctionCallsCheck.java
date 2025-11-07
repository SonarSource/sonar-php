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
package org.sonar.php.checks;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.checks.utils.argumentmatching.ArgumentMatcher;
import org.sonar.php.checks.utils.argumentmatching.ArgumentVerifierUnaryFunction;
import org.sonar.php.checks.utils.argumentmatching.FunctionArgumentCheck;
import org.sonar.php.tree.impl.declaration.ClassNamespaceNameTreeImpl;
import org.sonar.php.tree.impl.declaration.NamespaceNameTreeImpl;
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.expression.MemberAccessTree;
import org.sonar.plugins.php.api.tree.expression.NewExpressionTree;
import org.sonarsource.analyzer.commons.internal.json.simple.JSONArray;
import org.sonarsource.analyzer.commons.internal.json.simple.JSONObject;
import org.sonarsource.analyzer.commons.internal.json.simple.parser.JSONParser;
import org.sonarsource.analyzer.commons.internal.json.simple.parser.ParseException;

import static java.nio.charset.StandardCharsets.UTF_8;

@Rule(key = "S6437")
public class HardCodedCredentialsInFunctionCallsCheck extends FunctionArgumentCheck {

  private static final String MESSAGE = "Revoke and change this password, as it is compromised.";

  private static final String LOCATION_OF_FUNCTIONS_JSON = "/org/sonar/php/checks/hardCodedCredentialsInFunctionCallsCheck/";
  private static final Set<String> SENSITIVE_FUNCTIONS_JSON = Set.of(
    "generatedSensitiveFunctions.json",
    "manuallyCreatedSensitiveFunctions.json");
  private static final Map<String, SensitiveMethod> SENSITIVE_FUNCTIONS = JsonSensitiveFunctionsReader.parseSensitiveFunctions(LOCATION_OF_FUNCTIONS_JSON,
    SENSITIVE_FUNCTIONS_JSON);

  private static final Map<String, ArgumentMatcher> matcherMap = new HashMap<>();

  private boolean isPhpUnitTestCase = false;

  @Override
  public void visitClassDeclaration(ClassDeclarationTree tree) {
    isPhpUnitTestCase = CheckUtils.isSubClassOfTestCase(tree);

    super.visitClassDeclaration(tree);

    isPhpUnitTestCase = false;
  }

  @Override
  public void visitNewExpression(NewExpressionTree tree) {
    if (!isPhpUnitTestCase && tree.expression().is(Kind.FUNCTION_CALL)) {
      FunctionCallTree functionCallTree = (FunctionCallTree) tree.expression();
      if (functionCallTree.callee().is(Kind.NAMESPACE_NAME)) {
        ClassNamespaceNameTreeImpl callee = (ClassNamespaceNameTreeImpl) functionCallTree.callee();
        QualifiedName fqn = callee.symbol().qualifiedName();
        checkForSensitiveMethod(functionCallTree, fqn + "::" + "__construct");
      }
    }

    super.visitNewExpression(tree);
  }

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    ExpressionTree callee = tree.callee();

    if (!isPhpUnitTestCase) {
      if (callee.is(Kind.CLASS_MEMBER_ACCESS) && ((MemberAccessTree) callee).object().is(Kind.NAMESPACE_NAME)) {
        MemberAccessTree memberAccessTreeCallee = (MemberAccessTree) callee;
        QualifiedName fqn = ((ClassNamespaceNameTreeImpl) memberAccessTreeCallee.object()).symbol().qualifiedName();

        Tree method = memberAccessTreeCallee.member();
        checkForSensitiveMethod(tree, fqn + "::" + method);
      } else if (callee.is(Kind.NAMESPACE_NAME)) {
        NamespaceNameTreeImpl namespaceNameTree = (NamespaceNameTreeImpl) callee;
        checkForSensitiveMethod(tree, namespaceNameTree.qualifiedName());
      }
    }
    super.visitFunctionCall(tree);
  }

  void checkForSensitiveMethod(FunctionCallTree tree, String fqnMethodName) {
    if (SENSITIVE_FUNCTIONS.containsKey(fqnMethodName)) {
      SensitiveMethod sensitiveMethod = SENSITIVE_FUNCTIONS.get(fqnMethodName);
      Set<ArgumentMatcher> methodMatchers = sensitiveMethod.getCorrespondingMatchers();

      String lowerCaseFunctionName = CheckUtils.getLowerCaseFunctionName(tree);
      if (lowerCaseFunctionName != null) {
        for (ArgumentMatcher methodMatcher : methodMatchers) {
          checkArgument(tree, lowerCaseFunctionName, methodMatcher);
        }
      }
    }
  }

  @Override
  protected void createIssue(ExpressionTree argument) {
    context().newIssue(this, argument, MESSAGE);
  }

  private static class SensitiveMethod {
    private final String name;
    private final String cls;
    private final Set<Integer> sensitiveIndices;
    private final List<String> orderedArguments;

    public SensitiveMethod(String name, String cls, Set<Integer> sensitiveIndices, List<String> orderedArguments) {
      this.name = name;
      this.cls = cls;
      this.sensitiveIndices = sensitiveIndices;
      this.orderedArguments = orderedArguments;
    }

    public String uniqueName() {
      return cls.isEmpty() ? name : (cls + "::" + name);
    }

    public Set<ArgumentMatcher> getCorrespondingMatchers() {
      Function<ExpressionTree, Boolean> isRegularStringLiteral = tree -> tree.is(Kind.REGULAR_STRING_LITERAL) && !isEmptyStringLiteral((LiteralTree) tree);

      return sensitiveIndices.stream().map(index -> matcherMap.computeIfAbsent(index + ";" + orderedArguments.get(index),
        key -> ArgumentVerifierUnaryFunction.builder()
          .position(index)
          .name(orderedArguments.get(index))
          .matchingFunction(isRegularStringLiteral)
          .build()))
        .collect(Collectors.toSet());
    }

    private static boolean isEmptyStringLiteral(LiteralTree literal) {
      return literal.value().substring(1, literal.value().length() - 1).isEmpty();
    }
  }

  static class JsonSensitiveFunctionsReader {
    private static final JSONParser jsonParser = new JSONParser();

    private static final Logger LOG = LoggerFactory.getLogger(JsonSensitiveFunctionsReader.class);

    private JsonSensitiveFunctionsReader() {
    }

    static Map<String, SensitiveMethod> parseSensitiveFunctions(String location, Set<String> fileNames) {
      Map<String, SensitiveMethod> sensitiveFunctions = new HashMap<>();

      for (String fileName : fileNames) {
        try {
          JSONArray readArray = parseResource(location + fileName);
          for (Object element : readArray) {
            JSONObject castElement = (JSONObject) element;
            String cls = (String) castElement.get("cls");
            String name = (String) castElement.get("name");

            JSONArray args = (JSONArray) castElement.get("args");
            JSONArray indices = (JSONArray) castElement.get("indices");

            SensitiveMethod sensitiveMethod = new SensitiveMethod(name, cls, retrieveSensitiveIndices(indices),
              retrieveActualArguments(args));
            sensitiveFunctions.put(sensitiveMethod.uniqueName(), sensitiveMethod);
          }
        } catch (IOException | ParseException e) {
          LOG.warn("JSON containing the sensitive functions for hard coded credentials couldn't be read correctly from " +
            "resources at {}.", fileName);
        }
      }
      return sensitiveFunctions;
    }

    static JSONArray parseResource(String location) throws IOException, ParseException {
      InputStream in = JsonSensitiveFunctionsReader.class.getResourceAsStream(location);

      if (in == null) {
        throw new FileNotFoundException(String.format("Json file with name %s not found.", location));
      }

      return (JSONArray) jsonParser.parse(new InputStreamReader(in, UTF_8));
    }

    private static List<String> retrieveActualArguments(JSONArray array) {
      List<String> arguments = new ArrayList<>();
      for (Object argObj : array) {
        // can be either '$variableName' or '?ObjectName $variableName' and we are looking for 'variableName'
        String[] splitString = ((String) argObj).split(" ");
        int indexOfActualArgument = splitString.length == 1 ? 0 : 1;
        String argument = splitString[indexOfActualArgument];
        // removes the '$' from the argument
        argument = argument.substring(1);
        arguments.add(argument);
      }
      return arguments;
    }

    private static Set<Integer> retrieveSensitiveIndices(JSONArray array) {
      Set<Integer> indices = new HashSet<>();
      for (Object index : array) {
        indices.add(toInteger(index));
      }
      return indices;
    }

    static Integer toInteger(Object value) {
      if (value instanceof Number number) {
        return number.intValue();
      }
      return null;
    }
  }
}
