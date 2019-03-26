/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.debug.tests.viewer.model.JFaceViewerPerformanceTests;
import org.eclipse.debug.tests.viewer.model.VirtualViewerPerformanceTests;

/**
 * Tests for release builds.
 *
 * @since 3.6
 */
public class PerformanceSuite extends TestSuite {

    /**
     * Returns the suite.  This is required to use the JUnit Launcher.
     *
     * @return the test suite
     */
    public static Test suite() {
        return new PerformanceSuite();
    }

    /**
     * Constructs the automated test suite. Adds all tests.
     */
    public PerformanceSuite() {
        // JFace viewer tests
        addTest(new TestSuite(JFaceViewerPerformanceTests.class));

        // Virtual viewer tests
        addTest(new TestSuite(VirtualViewerPerformanceTests.class));
    }

}
