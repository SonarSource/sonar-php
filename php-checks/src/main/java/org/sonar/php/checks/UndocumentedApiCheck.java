/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
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

import com.google.common.io.Files;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.php.api.CharsetAwareVisitor;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Checks every method and class declaration for presence of a comment in the line before
 * <p>
 * Every declaration should have a short comment explaining what the {class|method} is doing
 *
 * @author Nils-Janis Mahlstädt <nils-janis.mahlstaedt@hmmh.de>
 */
@Rule(
        key = UndocumentedApiCheck.KEY,
        name = UndocumentedApiCheck.MESSAGE,
        priority = Priority.MINOR,
        tags = {Tags.DESIGN, Tags.CONVENTION})
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.UNDERSTANDABILITY)
@SqaleConstantRemediation("10min")
public class UndocumentedApiCheck extends PHPVisitorCheck implements CharsetAwareVisitor {

    public static final String KEY = "S1176";
    public static final String MESSAGE = "method an class declarations should always be documented";

    private static final String MESSAGE_PARTIAL = " declaration should always be documented";
    private static final boolean defaultTrue = true;
    private static final boolean defaultFalse = false;

    private Charset charset;
    private String currentFilename = "";
    //lines in currentFile
    private List<String> internal_lines;

    // check if class has comment
    @RuleProperty(
            key = "class",
            defaultValue = "true")
    boolean checkClassComment = defaultTrue;

    // check if function has comment
    @RuleProperty(
            key = "method",
            defaultValue = "true")
    boolean checkFunctionDeclComment = defaultTrue;

    // check if method has comment
    @RuleProperty(
            key = "method",
            defaultValue = "true")
    boolean checkMethodDeclComment = defaultTrue;

    @RuleProperty(
            key = "enforceBlockComment",
            defaultValue = "false")
    boolean enforceBlockComment = defaultFalse;

    @Override
    public void setCharset(Charset charset) {
        this.charset = charset;
    }


    @Override
    public void visitClassDeclaration(ClassDeclarationTree tree) {
        if (checkClassComment) {
            //get current line in file
            int position = tree.classEntryTypeToken().line();
            if (!isComment(previousLine(position))) {
                context().newIssue(this, "A class" + MESSAGE_PARTIAL).tree(tree);
            }
        }

        //super.scan(tree);
        super.visitClassDeclaration(tree);
    }

    @Override
    public void visitFunctionDeclaration(FunctionDeclarationTree tree) {
        if (checkFunctionDeclComment) {
            //get current line in file
            int position = tree.name().token().line();
            if (!isComment(previousLine(position))) {
                context().newIssue(this, "A function" + MESSAGE_PARTIAL).tree(tree);
            }
        }

        //super.scan(tree);
        super.visitFunctionDeclaration(tree);
    }

    @Override
    public void visitMethodDeclaration(MethodDeclarationTree tree) {
        if (checkMethodDeclComment) {
            //get current line in file
            int position = tree.name().token().line();
            if (!isComment(previousLine(position))) {
                context().newIssue(this, "A method" + MESSAGE_PARTIAL).tree(tree);
            }
        }

        //super.scan(tree);
        super.visitMethodDeclaration(tree);
    }

    /**
     * Get the raw lines from the document currently linked in context()
     * <p>
     * Will only read lines
     *
     * @return
     */
    private List<String> lines() {
        try {
            String filename = context().file().getCanonicalPath();

            if (internal_lines == null || !(currentFilename.equals(filename))) {
                //Lines were never read or the file changed
                currentFilename = context().file().getCanonicalPath();
                internal_lines = Files.readLines(context().file(), charset);
            }

        } catch (IOException e) {
            throw new IllegalStateException("Check S1451: Can't read the file", e);
        }

        return internal_lines;
    }

    /**
     * checks if String c is a comment
     * <p>
     * only accepts single line comments ("//" and "#") if enforceBlockComment is true!
     *
     * @param c string to check
     * @return true if c is comment, false otherwise
     */
    private boolean isComment(String c) {
        String line = c.trim();
        boolean isSingleLineCommentBegin = line.startsWith("//") || line.startsWith("#");
        boolean isBlockCommentBegin = line.startsWith("/**") || line.startsWith("/*");
        boolean isBlockCommentEnd = line.endsWith("*/");

        if (enforceBlockComment) {
            //only accept block comments
            return isBlockCommentBegin || isBlockCommentEnd;
        } else {
            //accept all comments
            return isBlockCommentBegin || isSingleLineCommentBegin || isBlockCommentEnd;
        }

    }

    private String previousLine(int line) {
        return getLine(line - 1);
    }

    /**
     * get line from lines with try catch
     *
     * @param line line number to get
     * @return line if found or an empty string on error
     */
    private String getLine(int line) {
        try {
            return lines().get(line - 1);
        } catch (Exception ex) {
            System.out.println("error! could not get line " + line);
            return "";
        }
    }
}
