package org.sonar.plugins.php.phpdepend.sensor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.Project;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.php.core.resources.PhpFile;
import org.sonar.plugins.php.phpdepend.ResourcesBag;
import org.sonar.plugins.php.phpdepend.xml.ClassNode;
import org.sonar.plugins.php.phpdepend.xml.FileNode;
import org.sonar.plugins.php.phpdepend.xml.FunctionNode;
import org.sonar.plugins.php.phpdepend.xml.MethodNode;
import org.sonar.plugins.php.phpdepend.xml.MetricsNode;
import org.sonar.plugins.php.phpdepend.xml.PackageNode;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;

/**
 * The PhpDependResultsParser par pdepend reports files and associate measures
 * with metrics and resources.
 */
public class PhpDependResultsParser {

	private static final Logger LOG = LoggerFactory.getLogger(PhpDependResultsParser.class);

	private SensorContext context;

	private Set<Metric> metrics;

	private Project project;

	private ResourcesBag resourcesBag;

	/**
	 * Instantiates a new php depend results parser.
	 * 
	 * @param config the config
	 * @param context the context
	 */
	public PhpDependResultsParser(Project project, SensorContext context) {
		this.project = project;
		this.context = context;
		resourcesBag = new ResourcesBag();
		metrics = getMetrics();
	}

	/**
	 * Instantiates a new php depend results parser.
	 * 
	 * @param config the config
	 * @param context the context
	 * @param metrics the metrics
	 */
	protected PhpDependResultsParser(Project project, SensorContext context, Set<Metric> metrics) {
		this.project = project;
		this.context = context;
		this.metrics = metrics;
		resourcesBag = new ResourcesBag();
	}

	/**
	 * If the given value is not null, the metric, resource and value will be
	 * associated
	 * 
	 * @param file
	 *            the file
	 * @param metric
	 *            the metric
	 * @param value
	 *            the value
	 */
	private void addMeasure(PhpFile file, Metric metric, Double value) {
		if (value != null) {
			resourcesBag.add(value, metric, file);
		}
	}

	/**
	 * Adds the measure if the given metrics isn't already present on this
	 * resource.
	 * 
	 * @param file
	 * @param metric
	 * @param value
	 */
	private void addMeasureIfNecessary(PhpFile file, Metric metric, double value) {
		if (resourcesBag.getMeasure(metric, file) == null || resourcesBag.getMeasure(metric, file) == 0) {
			resourcesBag.add(value, metric, file);
		}
	}

	/**
	 * Collects the given class measures and launches {@see
	 * #collectFunctionMeasures(MethodNode, PhpFile)} for all its descendant.
	 * 
	 * @param file
	 *            the php related file
	 * @param classNode
	 *            representing the class in the report file
	 */
	private void collectClassMeasures(ClassNode classNode, PhpFile file) {
		addMeasureIfNecessary(file, CoreMetrics.LINES, classNode.getLinesNumber());
		addMeasureIfNecessary(file, CoreMetrics.COMMENT_LINES, classNode.getCommentLineNumber());
		addMeasureIfNecessary(file, CoreMetrics.NCLOC, classNode.getCodeLinesNumber());
		// Adds one class to this file
		addMeasure(file, CoreMetrics.CLASSES, 1.0);
		// for all methods in this class.
		if (classNode.getMethodes() != null && classNode.getMethodes().size() != 0) {
			for (MethodNode methodNode : classNode.getMethodes()) {
				collectMethodMeasures(methodNode, file);
			}
		}
	}

	/**
	 * Collects the given function measures.
	 * 
	 * @param file the php related file
	 * @param functionNode representing the class in the report file
	 */
	private void collectFunctionsMeasures(FunctionNode functionNode, PhpFile file) {
		addMeasureIfNecessary(file, CoreMetrics.LINES, functionNode.getLinesNumber());
		addMeasureIfNecessary(file, CoreMetrics.COMMENT_LINES, functionNode.getCommentLineNumber());
		addMeasureIfNecessary(file, CoreMetrics.NCLOC, functionNode.getCodeLinesNumber());
		// Adds one class to this file
		addMeasure(file, CoreMetrics.FUNCTIONS, 1.0);
	}

	/**
	 * Collect the fiven php file measures and launches {@see
	 * #collectClassMeasures(ClassNode, PhpFile)} for all its descendant. Indeed
	 * even if it's not a good practice it isn't illegal to have more than one
	 * public class in one php file.
	 * 
	 * @param file the php file
	 * @param fileNode the node representing the file in the report file.
	 */
	private void collectFileMeasures(FileNode fileNode, PhpFile file) {
		addMeasure(file, CoreMetrics.LINES, fileNode.getLinesNumber());
		addMeasure(file, CoreMetrics.NCLOC, fileNode.getCodeLinesNumber());
		addMeasure(file, CoreMetrics.COMMENT_LINES, fileNode.getCommentLineNumber());
		// Adds one file to this php file
		addMeasure(file, CoreMetrics.FILES, 1.0);
		// for all class in this file
		if (fileNode.getClasses() != null) {
			for (ClassNode classNode : fileNode.getClasses()) {
				collectClassMeasures(classNode, file);
			}// for all class in this file
		}
		if (fileNode.getFunctions() != null) {
			for (FunctionNode funcNode : fileNode.getFunctions()) {
				collectFunctionsMeasures(funcNode, file);
			}
		}
	}

	/**
	 * Collect function measures.
	 * 
	 * @param file the file
	 * @param methodNode the method node
	 */
	private void collectMethodMeasures(MethodNode methodNode, PhpFile file) {
		// Adds one method to this file
		addMeasure(file, CoreMetrics.FUNCTIONS, 1.0);
		addMeasure(file, CoreMetrics.COMPLEXITY, methodNode.getComplexity());
	}

	/**
	 * Collect measures.
	 * 
	 * @param reportXml the report xml
	 * @throws FileNotFoundException the file not found exception
	 * @throws ParseException the parse exception
	 */
	protected void collectMeasures(File reportXml) throws FileNotFoundException, ParseException {
		MetricsNode metricsNode = getMetrics(reportXml);
		PhpFile currentResourceFile = null;
		for (FileNode fileNode : metricsNode.getFiles()) {
			String fileName = fileNode.getFileName();
			currentResourceFile = PhpFile.fromAbsolutePath(fileName, project);
			if (currentResourceFile == null) {
				LOG.warn("The following file doesn't belong to current project sources or tests : " + fileName);
			} else {
				collectFileMeasures(fileNode, currentResourceFile);
			}
		}
		saveMeasures();
	}

	/**
	 * Gets the metrics.
	 * 
	 * @return the metrics
	 */
	private Set<Metric> getMetrics() {
		Set<Metric> metrics = new HashSet<Metric>();
		metrics.add(CoreMetrics.LINES);
		metrics.add(CoreMetrics.NCLOC);
		metrics.add(CoreMetrics.FUNCTIONS);
		metrics.add(CoreMetrics.COMMENT_LINES);
		metrics.add(CoreMetrics.FILES);
		metrics.add(CoreMetrics.COMPLEXITY);
		metrics.add(CoreMetrics.CLASSES);
		return metrics;
	}

	/**
	 * Gets the metrics.
	 * 
	 * @param report the report
	 * @return the metrics
	 */
	private MetricsNode getMetrics(File report) {
		InputStream inputStream = null;
		try {
			XStream xstream = new XStream();
			xstream.processAnnotations(MetricsNode.class);
			xstream.processAnnotations(PackageNode.class);
			xstream.processAnnotations(FileNode.class);
			xstream.processAnnotations(ClassNode.class);
			xstream.processAnnotations(FunctionNode.class);
			xstream.processAnnotations(MethodNode.class);
			inputStream = new FileInputStream(report);
			return (MetricsNode) xstream.fromXML(inputStream);
		} catch (XStreamException e) {
			throw new SonarException("PDepend report isn't valid: " + report.getName(), e);
		} catch (IOException e) {
			throw new SonarException("Can't read pUnit report : " + report.getName(), e);
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
	}

	/**
	 * Parses the pdepend report file.
	 */
	public void parse(File reportXml) {
		// If no files can be found, plugin will stop normally only logging the
		// error
		if (!reportXml.exists()) {
			LOG.error("Result file not found : " + reportXml.getAbsolutePath() + ". Plugin will stop");
			return;
		}
		try {
			LOG.info("Collecting measures...");
			collectMeasures(reportXml);
		} catch (Exception e) {
			LOG.error("Report file is invalid or can't be found, plugin will stop.", e);
			throw new SonarException(e);
		}
	}

	/**
	 * Saves on measure in the context. One value is associated with a metric
	 * and a resource.
	 * 
	 * @param resource Can be a PhpFile or a PhpPackage
	 * @param metric the metric evaluated
	 * @param measure the corresponding value
	 */
	private void saveMeasure(PhpFile resource, Metric metric, Double measure) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Saving " + metric.getName() + " for resource " + resource.getKey() + " with value " + measure);
		}
		context.saveMeasure(resource, metric, measure);
	}

	/**
	 * Saves all the measure contained in the resourceBag used for this
	 * analysis.
	 * 
	 * @throws ParseException
	 */
	private void saveMeasures() {
		LOG.info("Saving measures...");
		for (PhpFile resource : resourcesBag.getResources()) {
			for (Metric metric : resourcesBag.getMetrics(resource)) {
				if (metrics.contains(metric)) {
					Double measure = resourcesBag.getMeasure(metric, resource);
					saveMeasure(resource, metric, measure);
				}
			}
		}
	}

}