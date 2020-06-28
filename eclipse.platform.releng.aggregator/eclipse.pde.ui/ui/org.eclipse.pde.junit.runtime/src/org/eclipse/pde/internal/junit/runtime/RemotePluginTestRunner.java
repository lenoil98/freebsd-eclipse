/*******************************************************************************
 *  Copyright (c) 2003, 2018 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Ralf Ebert - Bug 307076 : JUnit Plug-in test runner exception "No Classloader found for plug-in ..." is confusing
 *******************************************************************************/
package org.eclipse.pde.internal.junit.runtime;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.internal.junit.runner.RemoteTestRunner;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.wiring.BundleWiring;

/**
 * Runs JUnit tests contained inside a plugin.
 */
public class RemotePluginTestRunner extends RemoteTestRunner {

	private String fTestPluginName;
	private ClassLoader fLoaderClassLoader;

	class BundleClassLoader extends ClassLoader {
		private Bundle bundle;

		public BundleClassLoader(Bundle target) {
			this.bundle = target;
		}

		@Override
		protected Class<?> findClass(String name) throws ClassNotFoundException {
			return bundle.loadClass(name);
		}

		@Override
		protected URL findResource(String name) {
			return bundle.getResource(name);
		}

		@Override
		protected Enumeration<URL> findResources(String name) throws IOException {
			return bundle.getResources(name);
		}
	}

	static class TestBundleClassLoader extends ClassLoader {
		protected Bundle bundle;

		public TestBundleClassLoader(Bundle target) {
			this.bundle = target;
		}

		@Override
		protected Class<?> findClass(String name) throws ClassNotFoundException {
			return bundle.loadClass(name);
		}

		@Override
		protected URL findResource(String name) {
			return bundle.getResource(name);
		}

		@Override
		protected Enumeration<URL> findResources(String name) throws IOException {
			return bundle.getResources(name);
		}

		@Override
		public Enumeration<URL> getResources(String res) throws IOException {
			Enumeration<URL> bundleResources = bundle.getResources(res);
			if (bundleResources == null)
				return Collections.emptyEnumeration();

			List<URL> compatibleResources = new ArrayList<>();
			while (bundleResources.hasMoreElements()) {
				URL nativeUrl = FileLocator.resolve(bundleResources.nextElement());
				compatibleResources.add(nativeUrl);
			}

			return Collections.enumeration(compatibleResources);
		}
	}

	/**
	 * The main entry point. Supported arguments in addition
	 * to the ones supported by RemoteTestRunner:
	 * <pre>
	 * -testpluginname: the name of the plugin containing the tests.
	  * </pre>
	 * @see RemoteTestRunner
	 */

	public static void main(String[] args) {
		RemotePluginTestRunner testRunner = new RemotePluginTestRunner();
		testRunner.init(args);
		ClassLoader currentTCCL = Thread.currentThread().getContextClassLoader();
		if (isJUnit5(args)) {
			//change the classloader so that the test classes in testplugin are discoverable
			//by junit5 framework  see bug 520811
			if (runAsJUnit5(args))
				Thread.currentThread().setContextClassLoader(getPluginClassLoader2(testRunner.getfTestPluginName()));
			else
				Thread.currentThread().setContextClassLoader(getPluginClassLoader(testRunner.getfTestPluginName()));
		}
		testRunner.run();
		if (isJUnit5(args)) {
			Thread.currentThread().setContextClassLoader(currentTCCL);
		}
	}

	private static ClassLoader getPluginClassLoader2(String getfTestPluginName) {
		Bundle bundle = Platform.getBundle(getfTestPluginName);
		if (bundle == null) {
			throw new IllegalArgumentException("Bundle \"" + getfTestPluginName + "\" not found. Possible causes include missing dependencies, too restrictive version ranges, or a non-matching required execution environment."); //$NON-NLS-1$ //$NON-NLS-2$
		}
		final String pluginId = getfTestPluginName;
		List<String> engines = new ArrayList<>();
		Bundle bund = FrameworkUtil.getBundle(RemotePluginTestRunner.class);
		Bundle[] bundles = bund.getBundleContext().getBundles();
		for (Bundle iBundle : bundles) {
			try {
				BundleWiring bundleWiring = Platform.getBundle(iBundle.getSymbolicName()).adapt(BundleWiring.class);
				Collection<String> listResources = bundleWiring.listResources("META-INF/services", "org.junit.platform.engine.TestEngine", BundleWiring.LISTRESOURCES_LOCAL); //$NON-NLS-1$//$NON-NLS-2$
				if (!listResources.isEmpty())
					engines.add(iBundle.getSymbolicName());
			} catch (Exception e) {
				// check the next bundle
			}
		}
		engines.add(pluginId);
		List<Bundle> platformEngineBundles = new ArrayList<>();
		for (Iterator<String> iterator = engines.iterator(); iterator.hasNext();) {
			String string = iterator.next();
			Bundle bundle2 = Platform.getBundle(string);
			platformEngineBundles.add(bundle2);
		}

		return new MultiBundleClassLoader(platformEngineBundles);
	}

	private static ClassLoader getPluginClassLoader(String getfTestPluginName) {
		Bundle bundle = Platform.getBundle(getfTestPluginName);
		if (bundle == null) {
			throw new IllegalArgumentException("Bundle \"" + getfTestPluginName + "\" not found. Possible causes include missing dependencies, too restrictive version ranges, or a non-matching required execution environment."); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return new TestBundleClassLoader(bundle);
	}

	/**
	 * Returns the Plugin class loader of the plugin containing the test.
	 * @see RemoteTestRunner#getTestClassLoader()
	 */
	@Override
	protected ClassLoader getTestClassLoader() {
		final String pluginId = getfTestPluginName();
		return getClassLoader(pluginId);
	}

	public ClassLoader getClassLoader(final String bundleId) {
		Bundle bundle = Platform.getBundle(bundleId);
		if (bundle == null) {
			throw new IllegalArgumentException("Bundle \"" + bundleId + "\" not found. Possible causes include missing dependencies, too restrictive version ranges, or a non-matching required execution environment."); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return new BundleClassLoader(bundle);
	}

	@Override
	public void init(String[] args) {
		readPluginArgs(args);
		boolean isJUnit5 = isJUnit5(args);
		if (isJUnit5) {
			// changing the classloader to get the testengines for junit5
			// during initialization - see bug 520811
			ClassLoader currentTCCL = Thread.currentThread().getContextClassLoader();
			try {
				// Get all bundles with junit5 test engine
				List<String> platformEngines = new ArrayList<>();
				Bundle bundle = FrameworkUtil.getBundle(getClass());
				Bundle[] bundles = bundle.getBundleContext().getBundles();
				for (Bundle iBundle : bundles) {
					try {
						BundleWiring bundleWiring = Platform.getBundle(iBundle.getSymbolicName()).adapt(BundleWiring.class);
						Collection<String> listResources = bundleWiring.listResources("META-INF/services", "org.junit.platform.engine.TestEngine", BundleWiring.LISTRESOURCES_LOCAL); //$NON-NLS-1$//$NON-NLS-2$
						if (!listResources.isEmpty())
							platformEngines.add(iBundle.getSymbolicName());
					} catch (Exception e) {
						// check the next bundle
					}
				}

				Thread.currentThread().setContextClassLoader(getJUnit5Classloader(platformEngines));
				defaultInit(args);
			} finally {
				Thread.currentThread().setContextClassLoader(currentTCCL);
			}
			return;
		}
		defaultInit(args);
	}

	private ClassLoader getJUnit5Classloader(List<String> platformEngine) {
		List<Bundle> platformEngineBundles = new ArrayList<>();
		for (Iterator<String> iterator = platformEngine.iterator(); iterator.hasNext();) {
			String string = iterator.next();
			Bundle bundle = Platform.getBundle(string);
			platformEngineBundles.add(bundle);
		}
		return new MultiBundleClassLoader(platformEngineBundles);
	}

	private static boolean runAsJUnit5(String[] args) {
		for (String string : args) {
			if (string.equalsIgnoreCase("-runasjunit5")) { //$NON-NLS-1$
				return true;
			}
		}
		return false;
	}

	private static boolean isJUnit5(String[] args) {
		if (runAsJUnit5(args) == true)
			return true;

		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("org.eclipse.jdt.internal.junit5.runner.JUnit5TestLoader")) //$NON-NLS-1$
				return true;
		}
		return false;
	}

	public void readPluginArgs(String[] args) {
		for (int i = 0; i < args.length; i++) {
			if (isFlag(args, i, "-testpluginname")) //$NON-NLS-1$
				setfTestPluginName(args[i + 1]);

			if (isFlag(args, i, "-loaderpluginname")) //$NON-NLS-1$
				fLoaderClassLoader = getClassLoader(args[i + 1]);
		}

		if (getfTestPluginName() == null)
			throw new IllegalArgumentException("Parameter -testpluginnname not specified."); //$NON-NLS-1$

		if (fLoaderClassLoader == null)
			fLoaderClassLoader = getClass().getClassLoader();
	}

	@Override
	protected Class<?> loadTestLoaderClass(String className) throws ClassNotFoundException {
		return fLoaderClassLoader.loadClass(className);
	}

	private boolean isFlag(String[] args, int i, final String wantedFlag) {
		String lowerCase = args[i].toLowerCase(Locale.ENGLISH);
		return lowerCase.equals(wantedFlag) && i < args.length - 1;
	}

	public String getfTestPluginName() {
		return fTestPluginName;
	}

	public void setfTestPluginName(String fTestPluginName) {
		this.fTestPluginName = fTestPluginName;
	}
}
