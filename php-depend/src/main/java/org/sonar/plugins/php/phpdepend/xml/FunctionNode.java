package org.sonar.plugins.php.phpdepend.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * The Function representing a phpdepend report file function node.
 */
@XStreamAlias("function")
public final class FunctionNode {

	/** The code lines number. */
	@XStreamAsAttribute
	@XStreamAlias("ncloc")
	private double codeLinesNumber;

	/** The lines number. */
	@XStreamAsAttribute
	@XStreamAlias("loc")
	private double linesNumber;

	/** The comment line number. */
	@XStreamAsAttribute
	@XStreamAlias("cloc")
	private double commentLineNumber;

	/**
	 * Instantiates a new class node.
	 * 
	 * @param codeLinesNumber the code lines number
	 * @param linesNumber the lines number
	 * @param complexity the complexity
	 * @param methodNumber the method number
	 * @param methodes the methodes
	 */
	public FunctionNode(final double codeLinesNumber, final double commentLineNumber, final double linesNumber) {
		super();
		this.codeLinesNumber = codeLinesNumber;
		this.commentLineNumber = commentLineNumber;
		this.linesNumber = linesNumber;
	}

	/**
	 * Gets the code lines number.
	 * 
	 * @return the code lines number
	 */
	public double getCodeLinesNumber() {
		return codeLinesNumber;
	}

	/**
	 * Gets the comment line number.
	 * 
	 * @return the comment line number
	 */
	public double getCommentLineNumber() {
		return commentLineNumber;
	}

	/**
	 * Gets the lines number.
	 * 
	 * @return the lines number
	 */
	public double getLinesNumber() {
		return linesNumber;
	}

	/**
	 * Sets the code lines number.
	 * 
	 * @param codeLinesNumber the new code lines number
	 */
	public void setCodeLinesNumber(final double codeLinesNumber) {
		this.codeLinesNumber = codeLinesNumber;
	}

	/**
	 * Sets the comment line number.
	 * 
	 * @param commentLineNumber the new comment line number
	 */
	public void setCommentLineNumber(final double commentLineNumber) {
		this.commentLineNumber = commentLineNumber;
	}

	/**
	 * Sets the lines number.
	 * 
	 * @param linesNumber the new lines number
	 */
	public void setLinesNumber(final double linesNumber) {
		this.linesNumber = linesNumber;
	}
}
