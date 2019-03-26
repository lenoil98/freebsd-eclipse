/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.tests.harness.util.ActionUtil;

/**
 * Tests the lifecycle for a view action delegate.
 */
public class IViewActionDelegateTest extends IActionDelegateTest {

    public static String TEST_VIEW_ID = "org.eclipse.ui.tests.api.IViewActionDelegateTest";

    /**
     * Constructor for IWorkbenchWindowActionDelegateTest
     */
    public IViewActionDelegateTest(String testName) {
        super(testName);
    }

    public void testInit() throws Throwable {
        // When an action delegate is run the
        // init, selectionChanged, and run methods should
        // be called, in that order.

        // Run the action.
        testRun();

        // Verify lifecycle.
        MockActionDelegate delegate = getDelegate();
        assertNotNull(delegate);
        assertTrue(delegate.callHistory.verifyOrder(new String[] { "init",
                "selectionChanged", "run" }));
    }

    /**
     * @see IActionDelegateTest#createActionWidget()
     */
    @Override
	protected Object createActionWidget() throws Throwable {
        MockViewPart view = (MockViewPart) fPage.showView(TEST_VIEW_ID);
        return view;
    }

    /**
     * @see IActionDelegateTest#runAction()
     */
    @Override
	protected void runAction(Object widget) throws Throwable {
        MockViewPart view = (MockViewPart) widget;
        IMenuManager mgr = view.getViewSite().getActionBars().getMenuManager();
        ActionUtil.runActionWithLabel(this, mgr, "Mock Action");
    }

    /**
     * @see IActionDelegateTest#fireSelection()
     */
    @Override
	protected void fireSelection(Object widget) throws Throwable {
        MockViewPart view = (MockViewPart) widget;
        view.fireSelection();
    }
}

