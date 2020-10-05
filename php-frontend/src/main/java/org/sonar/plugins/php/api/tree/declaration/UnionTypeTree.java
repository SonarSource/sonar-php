package org.sonar.plugins.php.api.tree.declaration;

import com.google.common.annotations.Beta;
import org.sonar.plugins.php.api.tree.SeparatedList;
import org.sonar.plugins.php.api.tree.Tree;

@Beta
public interface UnionTypeTree extends Tree {
  SeparatedList<TypeTree> types();
}
