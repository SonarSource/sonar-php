/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * dev@sonar.codehaus.org
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.php.checks;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Grammar;
import org.sonar.check.BelongsToProfile;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.checks.SquidCheck;

/**
 * @author David van Laatum
 */
@Rule (
         key = "S667",
         priority = Priority.MAJOR )
@BelongsToProfile ( title = CheckList.SONAR_WAY_PROFILE, priority
                    = Priority.MAJOR )
public class EmptyOnFunctionCheck extends SquidCheck<Grammar> {

  @Override
  public void init () {
    subscribeTo ( PHPGrammar.INTERNAL_FUNCTION );
  }

  @Override
  public void visitNode ( AstNode astNode ) {
    if ( "empty".equalsIgnoreCase ( astNode.getTokenValue () ) ) {
      for ( AstNode c : astNode.getChildren () ) {
        if ( containsFunction ( c ) ) {
          getContext ().createLineViolation ( this,
                                              "Called empty on function return",
                                              astNode );
          break;
        }
      }
    }
  }

  protected boolean containsFunction ( AstNode astNode ) {
    boolean rt = false;
    if ( astNode.getType () == PHPGrammar.DIMENSIONAL_OFFSET ) {
      // functions are allowed in array indexes
    } else if ( astNode.getType () == PHPGrammar.FUNCTION_CALL_PARAMETER_LIST
                        && ( astNode.getNextSibling () == null || astNode
                            .getNextSibling ().getType ()
                                                                  != PHPGrammar.OBJECT_MEMBER_ACCESS ) ) {
      rt = true;
    } else {
      for ( AstNode c : astNode.getChildren () ) {
        if ( containsFunction ( c ) ) {
          rt = true;
          break;
        }
      }
    }
    return rt;
  }
}
