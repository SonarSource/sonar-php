package org.sonar.plugins.php.phpunit.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;


/**
 * The Class ClassNode.
 */
@XStreamAlias("class")
public class ClassNode {

	/** The ignored node. */
	@XStreamOmitField
	@XStreamAlias("metrics")
	private Object ignoredNode;
}
