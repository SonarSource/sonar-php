/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * sonarqube@googlegroups.com
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
package org.sonar.php.toolkit;

import com.google.common.base.Charsets;
import com.sonar.sslr.impl.Parser;
import org.sonar.colorizer.Tokenizer;
import org.sonar.php.PHPConfiguration;
import org.sonar.php.parser.PHPParser;
import org.sonar.sslr.toolkit.AbstractConfigurationModel;
import org.sonar.sslr.toolkit.ConfigurationProperty;

import java.util.Collections;
import java.util.List;

public class PhpConfigurationModel extends AbstractConfigurationModel {

  @Override
  public List<ConfigurationProperty> getProperties() {
    return Collections.emptyList();
  }

  @Override
  public Parser doGetParser() {
    return PHPParser.create(new PHPConfiguration(Charsets.UTF_8));
  }

  @Override
  public List<Tokenizer> doGetTokenizers() {
    return Collections.emptyList();
  }

}
