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
package org.sonar.plugins.php;

import org.sonar.graph.DirectedGraph;
import org.sonar.graph.DirectedGraphAccessor;
import org.sonar.squidbridge.api.SourceCode;
import org.sonar.squidbridge.api.SourceCodeEdge;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class PHPSquid implements DirectedGraphAccessor<SourceCode, SourceCodeEdge> {

  private final DirectedGraph<SourceCode, SourceCodeEdge> graph = new DirectedGraph<SourceCode, SourceCodeEdge>();

  public DirectedGraph<SourceCode, SourceCodeEdge> getGraph() {
    return graph;
  }

  @Override
  public SourceCodeEdge getEdge(SourceCode from, SourceCode to) {
    return graph.getEdge(from, to);
  }

  @Override
  public boolean hasEdge(SourceCode from, SourceCode to) {
    return graph.hasEdge(from, to);
  }

  @Override
  public Set<SourceCode> getVertices() {
    return graph.getVertices();
  }

  @Override
  public Collection<SourceCodeEdge> getOutgoingEdges(SourceCode from) {
    return graph.getOutgoingEdges(from);
  }

  @Override
  public Collection<SourceCodeEdge> getIncomingEdges(SourceCode to) {
    return graph.getIncomingEdges(to);
  }

  public List<SourceCodeEdge> getEdges(Collection<SourceCode> sourceCodes) {
    return graph.getEdges(sourceCodes);
  }

}
