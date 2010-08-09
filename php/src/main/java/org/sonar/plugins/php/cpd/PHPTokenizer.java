/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2010 SQLi
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */

package org.sonar.plugins.php.cpd;

import java.io.IOException;
import java.util.List;

import net.sourceforge.pmd.cpd.SourceCode;
import net.sourceforge.pmd.cpd.TokenEntry;
import net.sourceforge.pmd.cpd.Tokenizer;
import net.sourceforge.pmd.cpd.Tokens;

public class PHPTokenizer implements Tokenizer {

	public void tokenize(SourceCode tokens, Tokens tokenEntries)
			throws IOException {
		List code = tokens.getCode();
		boolean commentLine = false;
		for (int i = 0; i < code.size(); i++) {
			String currentLine = (String) code.get(i);
			for (int j = 0; j < currentLine.length(); j++) {
				char tok = currentLine.charAt(j);
				if (j == currentLine.indexOf("//", j)) {
					break;
				} else if (j == currentLine.indexOf("/*", j)) {
					j++;
					commentLine = true;
				} else if (commentLine && j == currentLine.indexOf("*/", j)) {
					j++;
					commentLine = false;
				} else if (!commentLine && !Character.isWhitespace(tok)
						&& tok != '{' && tok != '}' && tok != ';') {
					tokenEntries.add(new TokenEntry(String.valueOf(tok), tokens
							.getFileName(), i + 1));
				}
			}
		}
		tokenEntries.add(TokenEntry.getEOF());
	}

}
