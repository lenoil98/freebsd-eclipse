/*******************************************************************************
 * Copyright (c) 2018 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lucas Bullen (Red Hat Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.test;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.tools.ant.taskdefs.optional.junitlauncher.TestResultFormatter;
import org.apache.tools.ant.util.DOMElementWriter;
import org.apache.tools.ant.util.DateUtils;
import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

/**
 * A {@link TestResultFormatter} which generates an XML report of the tests. The generated XML reports
 * conforms to the schema of the XML that was generated by the {@code junit} task's XML
 * report formatter and can be used by the {@code junitreport} task
 */
public class LegacyXmlResultFormatter extends AbstractJUnitResultFormatter {

	private static final double ONE_SECOND = 1000.0;

	OutputStream outputStream;
	final Map<TestIdentifier, Stats> testIds = new ConcurrentHashMap<>();
	final Map<TestIdentifier, Optional<String>> skipped = new ConcurrentHashMap<>();
	final Map<TestIdentifier, Optional<Throwable>> failed = new ConcurrentHashMap<>();
	final Map<TestIdentifier, Optional<Throwable>> errored = new ConcurrentHashMap<>();
	final Map<TestIdentifier, Optional<Throwable>> aborted = new ConcurrentHashMap<>();

	TestPlan testPlan;
	long testPlanStartedAt = -1;
	long testPlanEndedAt = -1;
	final AtomicLong numTestsRun = new AtomicLong(0);
	final AtomicLong numTestsFailed = new AtomicLong(0);
	final AtomicLong numTestsErrored = new AtomicLong(0);
	final AtomicLong numTestsSkipped = new AtomicLong(0);
	final AtomicLong numTestsAborted = new AtomicLong(0);


	@Override
	public void testPlanExecutionStarted(final TestPlan plan) {
		this.testPlan = plan;
		this.testPlanStartedAt = System.currentTimeMillis();
	}

	@Override
	public void testPlanExecutionFinished(final TestPlan plan) {
		this.testPlanEndedAt = System.currentTimeMillis();
		// format and print out the result
		try {
			new XMLReportWriter().write();
		} catch (IOException | XMLStreamException e) {
			handleException(e);
			return;
		}
	}

	@Override
	public void dynamicTestRegistered(final TestIdentifier testIdentifier) {
		// nothing to do
	}

	@Override
	public void executionSkipped(final TestIdentifier testIdentifier, final String reason) {
		final long currentTime = System.currentTimeMillis();
		this.numTestsSkipped.incrementAndGet();
		this.skipped.put(testIdentifier, Optional.ofNullable(reason));
		// a skipped test is considered started and ended now
		final Stats stats = new Stats(testIdentifier, currentTime);
		stats.endedAt = currentTime;
		this.testIds.put(testIdentifier, stats);
	}

	@Override
	public void executionStarted(final TestIdentifier testIdentifier) {
		final long currentTime = System.currentTimeMillis();
		this.testIds.putIfAbsent(testIdentifier, new Stats(testIdentifier, currentTime));
		if (testIdentifier.isTest()) {
			this.numTestsRun.incrementAndGet();
		}
	}

	@Override
	public void executionFinished(final TestIdentifier testIdentifier, final TestExecutionResult testExecutionResult) {
		final long currentTime = System.currentTimeMillis();
		final Stats stats = this.testIds.get(testIdentifier);
		if (stats != null) {
			stats.endedAt = currentTime;
		}
		switch (testExecutionResult.getStatus()) {
		case SUCCESSFUL: {
			break;
		}
		case ABORTED: {
			this.numTestsAborted.incrementAndGet();
			this.aborted.put(testIdentifier, testExecutionResult.getThrowable());
			break;
		}
		case FAILED: {
			Optional<Throwable> throwableOptional = testExecutionResult.getThrowable();
			if(throwableOptional.isPresent() && throwableOptional.get() instanceof AssertionError) {
				this.numTestsFailed.incrementAndGet();
				this.failed.put(testIdentifier, testExecutionResult.getThrowable());
			}else {
				this.numTestsErrored.incrementAndGet();
				this.errored.put(testIdentifier, testExecutionResult.getThrowable());
			}
			break;
		}
		}
	}

	@Override
	public void reportingEntryPublished(final TestIdentifier testIdentifier, final ReportEntry entry) {
		// nothing to do
	}

	@Override
	public void setDestination(final OutputStream os) {
		this.outputStream = os;
	}

	private final class Stats {
		final long startedAt;
		long endedAt;

		Stats(final TestIdentifier testIdentifier, final long startedAt) {
			this.startedAt = startedAt;
		}
	}

	private final class XMLReportWriter {

		private static final String ELEM_TESTSUITE = "testsuite";
		private static final String ELEM_PROPERTIES = "properties";
		private static final String ELEM_PROPERTY = "property";
		private static final String ELEM_TESTCASE = "testcase";
		private static final String ELEM_SKIPPED = "skipped";
		private static final String ELEM_FAILURE = "failure";
		private static final String ELEM_ERROR = "error";
		private static final String ELEM_ABORTED = "aborted";
		private static final String ELEM_SYSTEM_OUT = "system-out";
		private static final String ELEM_SYSTEM_ERR = "system-err";


		private static final String ATTR_CLASSNAME = "classname";
		private static final String ATTR_NAME = "name";
		private static final String ATTR_VALUE = "value";
		private static final String ATTR_TIME = "time";
		private static final String ATTR_TIMESTAMP = "timestamp";
		private static final String ATTR_NUM_ABORTED = "aborted";
		private static final String ATTR_NUM_FAILURES = "failures";
		private static final String ATTR_NUM_ERRORS = "errors";
		private static final String ATTR_NUM_TESTS = "tests";
		private static final String ATTR_NUM_SKIPPED = "skipped";
		private static final String ATTR_MESSAGE = "message";
		private static final String ATTR_TYPE = "type";

		public XMLReportWriter() {
			// TODO Auto-generated constructor stub
		}

		void write() throws XMLStreamException, IOException {
			final XMLStreamWriter writer = XMLOutputFactory.newFactory().createXMLStreamWriter(outputStream, "UTF-8");
			try {
				writer.writeStartDocument();
				writeTestSuite(writer);
				writer.writeEndDocument();
			} finally {
				writer.close();
			}
		}

		void writeTestSuite(final XMLStreamWriter writer) throws XMLStreamException, IOException {
			// write the testsuite element
			writer.writeStartElement(ELEM_TESTSUITE);
			final String testsuiteName = determineTestSuiteName();
			writer.writeAttribute(ATTR_NAME, testsuiteName);
			// time taken for the tests execution
			writer.writeAttribute(ATTR_TIME, String.valueOf((testPlanEndedAt - testPlanStartedAt) / ONE_SECOND));
			// add the timestamp of report generation
			final String timestamp = DateUtils.format(new Date(), DateUtils.ISO8601_DATETIME_PATTERN);
			writer.writeAttribute(ATTR_TIMESTAMP, timestamp);
			writer.writeAttribute(ATTR_NUM_TESTS, String.valueOf(numTestsRun.longValue()));
			writer.writeAttribute(ATTR_NUM_FAILURES, String.valueOf(numTestsFailed.longValue()));
			writer.writeAttribute(ATTR_NUM_ERRORS, String.valueOf(numTestsErrored.longValue()));
			writer.writeAttribute(ATTR_NUM_SKIPPED, String.valueOf(numTestsSkipped.longValue()));
			writer.writeAttribute(ATTR_NUM_ABORTED, String.valueOf(numTestsAborted.longValue()));

			// write the properties
			writeProperties(writer);
			// write the tests
			writeTestCase(writer);
			writeSysOut(writer);
			writeSysErr(writer);
			// end the testsuite
			writer.writeEndElement();
		}

		void writeProperties(final XMLStreamWriter writer) throws XMLStreamException {
			final Properties properties = LegacyXmlResultFormatter.this.context.getProperties();
			if (properties == null || properties.isEmpty()) {
				return;
			}
			writer.writeStartElement(ELEM_PROPERTIES);
			for (final String prop : properties.stringPropertyNames()) {
				writer.writeStartElement(ELEM_PROPERTY);
				writer.writeAttribute(ATTR_NAME, prop);
				writer.writeAttribute(ATTR_VALUE, properties.getProperty(prop));
				writer.writeEndElement();
			}
			writer.writeEndElement();
		}

		void writeTestCase(final XMLStreamWriter writer) throws XMLStreamException {
			for (final Map.Entry<TestIdentifier, Stats> entry : testIds.entrySet()) {
				final TestIdentifier testId = entry.getKey();
				if (!testId.isTest()) {
					// only interested in test methods
					continue;
				}
				// find the parent class of this test method
				final Optional<TestIdentifier> parent = testPlan.getParent(testId);
				if (!parent.isPresent()) {
					continue;
				}
				final String classname = parent.get().getLegacyReportingName();
				writer.writeStartElement(ELEM_TESTCASE);
				writer.writeAttribute(ATTR_CLASSNAME, classname);
				writer.writeAttribute(ATTR_NAME, testId.getDisplayName());
				final Stats stats = entry.getValue();
				writer.writeAttribute(ATTR_TIME, String.valueOf((stats.endedAt - stats.startedAt) / ONE_SECOND));
				// skipped element if the test was skipped
				writeSkipped(writer, testId);
				// failed element if the test failed
				writeFailed(writer, testId);
				// errored element if the test failed with an error
				writeErrored(writer, testId);
				// aborted element if the test was aborted
				writeAborted(writer, testId);

				writer.writeEndElement();
			}
		}

		private void writeSkipped(final XMLStreamWriter writer, final TestIdentifier testIdentifier) throws XMLStreamException {
			if (!skipped.containsKey(testIdentifier)) {
				return;
			}
			writer.writeStartElement(ELEM_SKIPPED);
			final Optional<String> reason = skipped.get(testIdentifier);
			if (reason.isPresent()) {
				writer.writeAttribute(ATTR_MESSAGE, reason.get());
			}
			writer.writeEndElement();
		}

		private void writeFailed(final XMLStreamWriter writer, final TestIdentifier testIdentifier) throws XMLStreamException {
			if (!failed.containsKey(testIdentifier)) {
				return;
			}
			writer.writeStartElement(ELEM_FAILURE);
			final Optional<Throwable> cause = failed.get(testIdentifier);
			if (cause.isPresent()) {
				final Throwable t = cause.get();
				final String message = t.getMessage();
				if (message != null && !message.trim().isEmpty()) {
					writer.writeAttribute(ATTR_MESSAGE, message);
				}
				writer.writeAttribute(ATTR_TYPE, t.getClass().getName());
				writer.writeCharacters(ExceptionUtils.readStackTrace(t));
			}
			writer.writeEndElement();
		}

		private void writeErrored(final XMLStreamWriter writer, final TestIdentifier testIdentifier) throws XMLStreamException {
			if (!errored.containsKey(testIdentifier)) {
				return;
			}
			writer.writeStartElement(ELEM_ERROR);
			final Optional<Throwable> cause = errored.get(testIdentifier);
			if (cause.isPresent()) {
				final Throwable t = cause.get();
				final String message = t.getMessage();
				if (message != null && !message.trim().isEmpty()) {
					writer.writeAttribute(ATTR_MESSAGE, message);
				}
				writer.writeAttribute(ATTR_TYPE, t.getClass().getName());
				writer.writeCharacters(ExceptionUtils.readStackTrace(t));
			}
			writer.writeEndElement();
		}

		private void writeAborted(final XMLStreamWriter writer, final TestIdentifier testIdentifier) throws XMLStreamException {
			if (!aborted.containsKey(testIdentifier)) {
				return;
			}
			writer.writeStartElement(ELEM_ABORTED);
			final Optional<Throwable> cause = aborted.get(testIdentifier);
			if (cause.isPresent()) {
				final Throwable t = cause.get();
				final String message = t.getMessage();
				if (message != null && !message.trim().isEmpty()) {
					writer.writeAttribute(ATTR_MESSAGE, message);
				}
				writer.writeAttribute(ATTR_TYPE, t.getClass().getName());
			}
			writer.writeEndElement();
		}

		private void writeSysOut(final XMLStreamWriter writer) throws XMLStreamException, IOException {
			if (!LegacyXmlResultFormatter.this.hasSysOut()) {
				return;
			}
			writer.writeStartElement(ELEM_SYSTEM_OUT);
			try (final Reader reader = LegacyXmlResultFormatter.this.getSysOutReader()) {
				writeCharactersFrom(reader, writer);
			}
			writer.writeEndElement();
		}

		private void writeSysErr(final XMLStreamWriter writer) throws XMLStreamException, IOException {
			if (!LegacyXmlResultFormatter.this.hasSysErr()) {
				return;
			}
			writer.writeStartElement(ELEM_SYSTEM_ERR);
			try (final Reader reader = LegacyXmlResultFormatter.this.getSysErrReader()) {
				writeCharactersFrom(reader, writer);
			}
			writer.writeEndElement();
		}

		private void writeCharactersFrom(final Reader reader, final XMLStreamWriter writer) throws IOException, XMLStreamException {
			final char[] chars = new char[1024];
			int numRead = -1;
			while ((numRead = reader.read(chars)) != -1) {
				// although it's called a DOMElementWriter, the encode method is just a
				// straight forward XML util method which doesn't concern about whether
				// DOM, SAX, StAX semantics.
				// TODO: Perhaps make it a static method
				final String encoded = new DOMElementWriter().encode(new String(chars, 0, numRead));
				writer.writeCharacters(encoded);
			}
		}

		private String determineTestSuiteName() {
			// this is really a hack to try and match the expectations of the XML report in JUnit4.x
			// world. In JUnit5, the TestPlan doesn't have a name and a TestPlan (for which this is a
			// listener) can have numerous tests within it
			final Set<TestIdentifier> roots = testPlan.getRoots();
			if (roots.isEmpty()) {
				return "UNKNOWN";
			}
			for (final TestIdentifier root : roots) {
				final Optional<ClassSource> classSource = findFirstClassSource(root);
				if (classSource.isPresent()) {
					return classSource.get().getClassName();
				}
			}
			return "UNKNOWN";
		}

		private Optional<ClassSource> findFirstClassSource(final TestIdentifier root) {
			if (root.getSource().isPresent()) {
				final TestSource source = root.getSource().get();
				if (source instanceof ClassSource) {
					return Optional.of((ClassSource) source);
				}
			}
			for (final TestIdentifier child : testPlan.getChildren(root)) {
				final Optional<ClassSource> classSource = findFirstClassSource(child);
				if (classSource.isPresent()) {
					return classSource;
				}
			}
			return Optional.empty();
		}
	}

}
