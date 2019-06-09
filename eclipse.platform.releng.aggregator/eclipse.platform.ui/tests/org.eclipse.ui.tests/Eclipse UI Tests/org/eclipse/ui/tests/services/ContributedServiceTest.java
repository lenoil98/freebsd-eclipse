/*******************************************************************************
 * Copyright (c) 2007, 2019 IBM Corporation and others.
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
 *     Paul Pazderski - Bug 546537: migrate class to JUnit4 so that the containing suite can be converted to JUnit4
 *******************************************************************************/

package org.eclipse.ui.tests.services;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.services.IServiceLocatorCreator;
import org.eclipse.ui.internal.services.IWorkbenchLocationService;
import org.eclipse.ui.internal.services.WorkbenchLocationService;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;
import org.eclipse.ui.services.AbstractServiceFactory;
import org.eclipse.ui.services.IDisposable;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.ui.services.IServiceScopes;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

/**
 * @since 3.4
 *
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class ContributedServiceTest extends UITestCase {

	public ContributedServiceTest() {
		super(ContributedServiceTest.class.getName());
	}

	@Test
	public void testGlobalService() throws Exception {
		IWorkbenchLocationService wls = getWorkbench()
				.getService(IWorkbenchLocationService.class);
		assertNotNull(wls.getWorkbench());
		assertNull(wls.getWorkbenchWindow());

		ILevelService l = getWorkbench().getService(
				ILevelService.class);
		assertNotNull(l);
		assertEquals(1, l.getLevel());

		l = getWorkbench().getService(ILevelService.class);
		assertNotNull(l);
		assertEquals(1, l.getLevel());

		assertEquals(1, LevelServiceFactory.instancesCreated);
	}

	@Test
	@Ignore // TODO
	public void testWindowService() throws Exception {
		IServiceLocator locator = getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchLocationService wls = locator
				.getService(IWorkbenchLocationService.class);
		assertNotNull(wls.getWorkbenchWindow());

		ILevelService l = locator
				.getService(ILevelService.class);
		assertNotNull(l);
		assertEquals(2, l.getLevel());

		assertEquals(2, LevelServiceFactory.instancesCreated);

		l = locator.getService(ILevelService.class);
		assertNotNull(l);
		assertEquals(2, l.getLevel());

		l = getWorkbench().getService(ILevelService.class);
		assertNotNull(l);
		assertEquals(1, l.getLevel());

		assertEquals(2, LevelServiceFactory.instancesCreated);
	}

	private static class TempLevelFactory extends AbstractServiceFactory {
		private int level;

		public TempLevelFactory(int l) {
			level = l;
		}

		@Override
		public Object create(Class serviceInterface,
				IServiceLocator parentLocator, IServiceLocator locator) {
			return (ILevelService) () -> level;
		}
	}

	@Test
	@Ignore // TODO
	public void testLocalServiceCreated() throws Exception {
		IServiceLocator parent = getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchLocationService wls = parent
				.getService(IWorkbenchLocationService.class);
		assertNotNull(wls.getWorkbenchWindow());

		IServiceLocatorCreator lc = parent
				.getService(IServiceLocatorCreator.class);
		IServiceLocator locator = lc.createServiceLocator(parent, null,
				new IDisposable() {
					@Override
					public void dispose() {
					}
				});

		ILevelService l = locator
				.getService(ILevelService.class);
		assertNotNull(l);
		assertEquals(3, l.getLevel());

		assertEquals(3, LevelServiceFactory.instancesCreated);

		if (locator instanceof IDisposable) {
			((IDisposable) locator).dispose();
		}

		locator = lc.createServiceLocator(parent, null, new IDisposable() {
			@Override
			public void dispose() {
			}
		});
		l = locator.getService(ILevelService.class);
		assertNotNull(l);
		assertEquals(3, l.getLevel());

		assertEquals(4, LevelServiceFactory.instancesCreated);
		if (locator instanceof IDisposable) {
			((IDisposable) locator).dispose();
		}

		locator = lc.createServiceLocator(parent, new TempLevelFactory(8),
				new IDisposable() {
					@Override
					public void dispose() {
					}
				});
		l = locator.getService(ILevelService.class);
		assertNotNull(l);
		assertEquals(8, l.getLevel());

		assertEquals(4, LevelServiceFactory.instancesCreated);
		if (locator instanceof IDisposable) {
			((IDisposable) locator).dispose();
		}
	}

	@Test
	@Ignore // TODO
	public void testLocalDialogService() throws Exception {
		IServiceLocator parent = getWorkbench();
		IServiceLocatorCreator lc = parent
				.getService(IServiceLocatorCreator.class);
		IServiceLocator locator = lc.createServiceLocator(parent,
				new AbstractServiceFactory() {
					@Override
					public Object create(Class serviceInterface,
							IServiceLocator parentLocator,
							IServiceLocator locator) {
						if (IWorkbenchLocationService.class
								.equals(serviceInterface)) {
							IWorkbenchLocationService wls = parentLocator
									.getService(IWorkbenchLocationService.class);
							return new WorkbenchLocationService(
									IServiceScopes.DIALOG_SCOPE, wls
											.getWorkbench(), null, null, null,
									null, wls.getServiceLevel()+1);
						}
						return null;
					}
				}, new IDisposable() {
					@Override
					public void dispose() {
					}
				});
		IWorkbenchLocationService wls = locator
				.getService(IWorkbenchLocationService.class);
		assertNotNull(wls.getWorkbench());
		assertNull(wls.getWorkbenchWindow());
		assertEquals(1, wls.getServiceLevel());
		assertEquals(IServiceScopes.DIALOG_SCOPE, wls.getServiceScope());
	}

	@Test
	public void testWorkbenchServiceFactory() throws Exception {
		IWorkbenchWindow window = getWorkbench().getActiveWorkbenchWindow();
		IProgressService progress = window
				.getService(IProgressService.class);
		assertNotNull(progress);

		assertEquals(getWorkbench().getProgressService(), progress);
		IViewPart part = null;
		IViewReference[] refs = window.getActivePage().getViewReferences();
		for (IViewReference ref : refs) {
			if ((part = ref.getView(false)) != null) {
				break;
			}
		}

		assertNotNull(part);
		progress = part.getSite().getService(
				IProgressService.class);
		assertFalse(progress == getWorkbench().getProgressService());
		assertEquals(part.getSite().getService(
				IWorkbenchSiteProgressService.class), progress);
		assertEquals(part.getSite().getAdapter(
				IWorkbenchSiteProgressService.class), progress);
	}
}
