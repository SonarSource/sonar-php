/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2010 MyCompany
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

package org.sonar.plugins.php.phpunit.sensor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.PropertiesBuilder;
import org.sonar.api.resources.Project;
import org.sonar.api.utils.ParsingUtils;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.php.core.resources.PhpFile;
import org.sonar.plugins.php.core.resources.PhpPackage;
import org.sonar.plugins.php.phpunit.xml.CoverageNode;
import org.sonar.plugins.php.phpunit.xml.FileNode;
import org.sonar.plugins.php.phpunit.xml.LineNode;
import org.sonar.plugins.php.phpunit.xml.MetricsNode;
import org.sonar.plugins.php.phpunit.xml.ProjectNode;

import com.thoughtworks.xstream.XStream;

/**
 * The Class PhpUnitCoverageResultParser.
 */
public class PhpUnitCoverageResultParser {

	/** The logger. */
	private static Logger logger = LoggerFactory.getLogger(PhpUnitCoverageResultParser.class);
	
	private static final String MSG_SAVE_MEASURES = "Saving {0} for {1} with value {2}";
	
	/** The project. */
	private Project project;

	/** The context. */
	private SensorContext context;

	/** The class by package. */
	private Map<String, PackageNode> classByPackage;

	/**
	 * Instantiates a new php unit coverage result parser.
	 * 
	 * @param project the project
	 * @param context the context
	 */
	public PhpUnitCoverageResultParser(Project project, SensorContext context) {
		super();
		this.project = project;
		this.context = context;
		classByPackage = new HashMap<String, PackageNode>();
	}

	/**
	 * Parses the.
	 * 
	 * @param coverageReportFile the coverage report file
	 */
	public void parse(File coverageReportFile) {
		if (coverageReportFile == null) {
			insertZeroWhenNoReports();
		} else {
			logger.info("Parsing file : {0}", coverageReportFile.getName());
			parseFile(context, coverageReportFile, project);
		}
	}

	/**
	 * Insert zero when no reports.
	 */
	private void insertZeroWhenNoReports() {
		context.saveMeasure(CoreMetrics.COVERAGE, 0d);
	}

	/**
	 * Parses the file.
	 * 
	 * @param context the context
	 * @param coverageReportFile the coverage report file
	 * @param project the project
	 */
	private void parseFile(SensorContext context, File coverageReportFile, Project project) {
		CoverageNode coverage = getCoverage(coverageReportFile);
		double percentage = 0d;
		if (coverage.getProjects() != null && coverage.getProjects().size() > 0) {
			ProjectNode projectNode = coverage.getProjects().get(0);
			for (FileNode file : projectNode.getFiles()) {
				cumulateResults(file);
			}
			percentage = Double.valueOf(projectNode.getMetrics().getCoveredElements())
					/ Double.valueOf(projectNode.getMetrics().getTotalElementsCount());
			saveMeasures(classByPackage);
		}
		if (logger.isDebugEnabled()) {
			logger.debug(MessageFormat.format("Saving {0} for project {1} with value {2}", CoreMetrics.COVERAGE.getName(),
					project.getName(), Double.valueOf((convertPercentage(percentage)))));
		}
		context.saveMeasure(CoreMetrics.COVERAGE, convertPercentage(percentage));
	}

	/**
	 * Cumulate results.
	 * 
	 * @param file the file
	 */
	private void cumulateResults(FileNode file) {
		PhpFile phpFile = PhpFile.fromAbsolutePath(file.getName(), project);
		if (phpFile == null) {
			logger.info(file.getName() + " can't be found amoung the project source directories");
			return;
		}
		PhpPackage phpPackage = phpFile.getParent();
		PackageNode packageNode = classByPackage.get(phpPackage.getName());
		if (packageNode == null) {
			packageNode = new PackageNode(phpPackage);
			classByPackage.put(phpPackage.getName(), packageNode);
		}
		packageNode.addClassByFileNode(file, phpFile);
	}

	/**
	 * Save measures for all classes.
	 * 
	 * @param classByPackage the class by package
	 * @return the double
	 */
	private void saveMeasures(Map<String, PackageNode> classByPackage) {
		// For each package
		for (String packageName : classByPackage.keySet()) {
			PackageNode node = classByPackage.get(packageName);
			PhpPackage phpPackage = node.getPackage();
			double coveragePercent = 0d;
			// Saves the class measures and adds its coverage percent
			for (FileNode fileNode : node.getClassByFileNode().keySet()) {
				coveragePercent += saveClassMeasure(fileNode, node.getClassByFileNode().get(fileNode));
			}
			double percentageByPackage = coveragePercent / node.getClassByFileNode().size();
			if (logger.isDebugEnabled()) {
				logger.debug(MessageFormat.format(MSG_SAVE_MEASURES, CoreMetrics.COVERAGE.getName(),
						packageName, Double.valueOf((convertPercentage(percentageByPackage)))));
			}
			// Saves the measure for the package
			context.saveMeasure(phpPackage, CoreMetrics.COVERAGE, convertPercentage(percentageByPackage));
		}
	}

	/**
	 * Convert percentage.
	 * 
	 * @param percentage the percentage
	 * @return the double
	 */
	private double convertPercentage(Number percentage) {
		return ParsingUtils.scaleValue(percentage.doubleValue() * 100.0);
	}

	/**
	 * Saves one class measure and calls {@link #saveLineMeasure(LineNode, PropertiesBuilder, String))} for each class lines.
	 * 
	 * @param fileNode the file node
	 * @param phpFile the php file
	 * @return the double
	 */
	private double saveClassMeasure(FileNode fileNode, PhpFile phpFile) {
		// Properties builder will generate the data associate with COVERAGE_LINE_HITS_DATA metrics.
		// This should look like (lineNumner=Count) : 1=0;2=1;3=1....
		PropertiesBuilder<Integer, Integer> lineHits = new PropertiesBuilder<Integer, Integer>(
				CoreMetrics.COVERAGE_LINE_HITS_DATA);
		for (LineNode line : fileNode.getLines()) {
			saveLineMeasure(line, lineHits);
		}
		double methodCoveragePercent = new Double(fileNode.getMetrics().getCoveredElements())
				/ new Double(fileNode.getMetrics().getTotalElementsCount());
		double lineCoveragePercent = new Double(fileNode.getMetrics().getCoveredStatements())
				/ new Double(fileNode.getMetrics().getTotalStatementsCount());
		if (logger.isDebugEnabled()) {
			logger.debug(MessageFormat.format(MSG_SAVE_MEASURES, CoreMetrics.COVERAGE.getName(), phpFile
					.getName(), convertPercentage(methodCoveragePercent)));
			logger.debug(MessageFormat.format(MSG_SAVE_MEASURES, CoreMetrics.LINE_COVERAGE.getName(), phpFile
					.getName(), convertPercentage(lineCoveragePercent)));
			logger.debug(MessageFormat.format(MSG_SAVE_MEASURES, CoreMetrics.COVERAGE_LINE_HITS_DATA.getName(),
					phpFile.getName(), lineHits.buildData()));
		}
		context.saveMeasure(phpFile, CoreMetrics.COVERAGE, convertPercentage(methodCoveragePercent));
		context.saveMeasure(phpFile, CoreMetrics.LINE_COVERAGE, convertPercentage(lineCoveragePercent));
		context.saveMeasure(phpFile, lineHits.build());
		return methodCoveragePercent;
	}

	/**
	 * Save line measure.
	 * 
	 * @param line the line
	 * @param lineHits the line hits
	 * @param className the class name
	 */
	private void saveLineMeasure(LineNode line, PropertiesBuilder<Integer, Integer> lineHits) {
		lineHits.add(line.getNum(), line.getCount());
	}

	/**
	 * Gets the coverage.
	 * 
	 * @param coverageReportFile the coverage report file
	 * @return the coverage
	 */
	private CoverageNode getCoverage(File coverageReportFile) {
		InputStream inputStream = null;
		try {
			XStream xstream = new XStream();
			xstream.aliasSystemAttribute("classType", "class");
			xstream.processAnnotations(CoverageNode.class);
			xstream.processAnnotations(ProjectNode.class);
			xstream.processAnnotations(FileNode.class);
			xstream.processAnnotations(MetricsNode.class);
			xstream.processAnnotations(LineNode.class);
			inputStream = new FileInputStream(coverageReportFile);
			return (CoverageNode) xstream.fromXML(inputStream);
		} catch (IOException e) {
			throw new SonarException("Can't read pUnit report : " + coverageReportFile.getName(), e);
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
	}

	/**
	 * The Class PackageNode.
	 */
	private class PackageNode {

		/** The php package. */
		private PhpPackage phpPackage;

		/** The class by file node. */
		private Map<FileNode, PhpFile> classByFileNode;

		/**
		 * Gets the class by file node.
		 * 
		 * @return the class by file node
		 */
		public Map<FileNode, PhpFile> getClassByFileNode() {
			return classByFileNode;
		}

		/**
		 * Instantiates a new package node.
		 * 
		 * @param phpPackage the php package
		 */
		public PackageNode(PhpPackage phpPackage) {
			super();
			this.phpPackage = phpPackage;
			classByFileNode = new HashMap<FileNode, PhpFile>();
		}

		/**
		 * Adds the class by file node.
		 * 
		 * @param fileNode the file node
		 * @param phpFile the php file
		 */
		public void addClassByFileNode(FileNode fileNode, PhpFile phpFile) {
			classByFileNode.put(fileNode, phpFile);
		}

		/**
		 * Gets the package.
		 * 
		 * @return the package
		 */
		public PhpPackage getPackage() {
			return phpPackage;
		}

	}

}
