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
package org.sonar.php.checks;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.ArgumentMatcher;
import org.sonar.php.checks.utils.ArgumentVerifierUnaryFunction;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.checks.utils.FunctionArgumentCheck;
import org.sonar.php.tree.impl.declaration.ClassNamespaceNameTreeImpl;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
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

  private static final String JSON_LOCATION = "HardCodedCredentialsSensitiveFunctions.json";

  private static final Map<String, SensitiveMethod> SENSITIVE_FUNCTIONS = JsonSensitiveFunctionsReader.parseSensitiveFunctions();

  private static final Logger LOG = Loggers.get(HardCodedCredentialsInFunctionCallsCheck.class);

  @Override
  public void visitNewExpression(NewExpressionTree tree) {
    FunctionCallTree functionCallTree = (FunctionCallTree) tree.expression();
    ClassNamespaceNameTreeImpl callee = (ClassNamespaceNameTreeImpl) functionCallTree.callee();
    String fqn = callee.symbol().qualifiedName().toString();
    String methodName = "__construct";
    checkForSensitiveMethod(functionCallTree, fqn + "::" + methodName);

    super.visitNewExpression(tree);
  }

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    ExpressionTree callee = tree.callee();

    if (callee.is(Kind.OBJECT_MEMBER_ACCESS)) {
      // Is it even possible?
      // TODO: Remove after more testing
      throw new RuntimeException("Delete me after testing!");
    }

    if (callee.is(Kind.CLASS_MEMBER_ACCESS)) {
      MemberAccessTree memberAccessTreeCallee = (MemberAccessTree) callee;
      // TODO: Use Nils method
      // TODO: How will this work, if method is not static? -> Test me!
      String fqn = ((ClassNamespaceNameTreeImpl) memberAccessTreeCallee.object()).symbol().qualifiedName().toString();

      String methodName = memberAccessTreeCallee.member().toString();
      checkForSensitiveMethod(tree, fqn + "::" + methodName);
    }
    super.visitFunctionCall(tree);
  }

  private void checkForSensitiveMethod(FunctionCallTree tree, String fqnMethodName) {
    if (SENSITIVE_FUNCTIONS.containsKey(fqnMethodName)) {
      SensitiveMethod sensitiveMethod = SENSITIVE_FUNCTIONS.get(fqnMethodName);
      ArgumentMatcher[] methodMatchers = sensitiveMethod.getCorrespondingMatchers();

      String lowerCaseFunctionName = CheckUtils.getLowerCaseFunctionName(tree);
      if (lowerCaseFunctionName == null) {
        return;
      }
      checkArgument(tree, lowerCaseFunctionName, methodMatchers);
    }
  }

  @Override
  protected void createIssue(ExpressionTree argument) {
    context().newIssue(this, argument, MESSAGE);
  }

  private static class SensitiveMethod {
    private final String name;
    private final String cls;

    //TODO: Check if this is used somehow?
    private final String type;
    private final Set<Integer> sensitiveIndices;

    private final List<String> orderedArguments;

    public SensitiveMethod(String name, String cls, String type, Set<Integer> sensitiveIndices, List<String> orderedArguments) {
      this.name = name;
      this.cls = cls;
      this.type = type;
      this.sensitiveIndices = sensitiveIndices;
      this.orderedArguments = orderedArguments;
    }

    public String uniqueName() {
      return cls + "::" + name;
    }

    public ArgumentMatcher[] getCorrespondingMatchers() {
      Function<ExpressionTree, Boolean> isRegularStringLiteral = tree -> tree.is(Kind.REGULAR_STRING_LITERAL);

      ArgumentMatcher[] matchers = new ArgumentMatcher[sensitiveIndices.size()];

      Iterator<Integer> iterator = sensitiveIndices.iterator();
      int matcherIndex = 0;
      while (iterator.hasNext()) {
        Integer index = iterator.next();
        matchers[matcherIndex] = new ArgumentVerifierUnaryFunction(index, orderedArguments.get(index), isRegularStringLiteral);
        matcherIndex++;
      }

      return matchers;
    }
  }

  private static class JsonSensitiveFunctionsReader {
    private static final JSONParser jsonParser = new JSONParser();

    private static Map<String, SensitiveMethod> parseSensitiveFunctions() {
      Map<String, SensitiveMethod> sensitiveFunctions = new HashMap<>();

      try {
        JSONArray readArray = parseResource();
        for (Object element : readArray) {
          JSONObject castElement = (JSONObject) element;
          String cls = (String) castElement.get("cls");
          String name = (String) castElement.get("name");
          String type = (String) castElement.get("type");

          JSONArray args = (JSONArray) castElement.get("args");
          JSONArray indices = (JSONArray) castElement.get("indices");


          SensitiveMethod sensitiveMethod = new SensitiveMethod(name, cls, type, retrieveSensitiveIndices(indices),
            retrieveActualArguments(args));
          sensitiveFunctions.put(sensitiveMethod.uniqueName(), sensitiveMethod);
        }
      } catch (IOException | ParseException e) {
        LOG.warn("Json containing the sensitive functions for hard coded credentials couldn't be read correctly.");
      }
      return sensitiveFunctions;
    }

    private static JSONArray parseResource() throws IOException, ParseException {
      ClassLoader classloader = Thread.currentThread().getContextClassLoader();
      InputStream in = classloader.getResourceAsStream(JSON_LOCATION);

      if (in == null) {
        throw new FileNotFoundException(String.format("Json file with name %s not found.", JSON_LOCATION));
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

    private static Integer toInteger(Object value) {
      if (value instanceof Number) {
        return ((Number) value).intValue();
      }
      return null;
    }
  }
}
