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
package org.sonar.php.highlighter;

import java.util.List;
import org.sonar.api.batch.sensor.highlighting.TypeOfText;
import org.sonar.api.batch.sensor.internal.SensorContextTester;

import static org.assertj.core.api.Assertions.assertThat;

public class HighlightChecker {
  
  private String componentKey;
  
  public HighlightChecker(String componentKey) {
    this.componentKey = componentKey;
  }

  /**
   * Checks the highlighting of a range of columns. The first column of a line has index 0.
   * The range is the columns of the token.
   */
  public void checkOnRange(SensorContextTester context, int line, int firstColumn, int length, TypeOfText expectedTypeOfText) {
    // check that every column of the token is highlighted (and with the expected type)
    for (int column = firstColumn; column < firstColumn + length; column++) {
      checkInternal(context, line, column, "", expectedTypeOfText);
    }

    // check that the column before the token is not highlighted
    if (firstColumn != 0) {
      checkInternal(context, line, firstColumn - 1, " (= before the token)", null);
    }

    // check that the column after the token is not highlighted
    checkInternal(context, line, firstColumn + length, " (= after the token)", null);
  }

  /**
   * Checks the highlighting of one column. The first column of a line has index 0.
   */
  public void check(SensorContextTester context, int line, int column, TypeOfText expectedTypeOfText) {
    checkInternal(context, line, column, "", expectedTypeOfText);
  }

  private void checkInternal(SensorContextTester context, int line, int column, String messageComplement, TypeOfText expectedTypeOfText) {
    List<TypeOfText> foundTypeOfTexts = context.highlightingTypeAt(componentKey, line, column);

    int expectedNumberOfTypeOfText = expectedTypeOfText == null ? 0 : 1;
    String message = "number of TypeOfTexts at line " + line + " and column " + column + messageComplement;
    assertThat(foundTypeOfTexts).as(message).hasSize(expectedNumberOfTypeOfText);
    if (expectedNumberOfTypeOfText > 0) {
      message = "found TypeOfTexts at line " + line + " and column " + column + messageComplement;
      assertThat(foundTypeOfTexts.get(0)).as(message).isEqualTo(expectedTypeOfText);
    }
  }

}
