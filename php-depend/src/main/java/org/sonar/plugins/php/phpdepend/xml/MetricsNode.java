package org.sonar.plugins.php.phpdepend.xml;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * The MetricsNode class represent a phpdepend metrics node. It's used by XStream to marschall or unmarshall xml files.
 */
@XStreamAlias("metrics")
public final class MetricsNode {

	/** The packages. */
	@XStreamImplicit
	private List<FileNode> files;

	/**
	 * Instantiates a new metrics.
	 * 
	 * @param files the files
	 */
	public MetricsNode(final List<FileNode> files) {
		super();
		this.files = files;
	}

	/**
	 * Adds the file.
	 * 
	 * @param file the file
	 */
	public void addFile(final FileNode packageNode) {
		if (files == null) {
			files = new ArrayList<FileNode>();
		}
		files.add(packageNode);
	}

	/**
	 * Gets the files.
	 * 
	 * @return the files
	 */
	public List<FileNode> getFiles() {
		return files;
	}

	/**
	 * Sets the packages.
	 * 
	 * @param packages the new packages
	 */
	public void setFiles(final List<FileNode> files) {
		this.files = files;
	}

}
