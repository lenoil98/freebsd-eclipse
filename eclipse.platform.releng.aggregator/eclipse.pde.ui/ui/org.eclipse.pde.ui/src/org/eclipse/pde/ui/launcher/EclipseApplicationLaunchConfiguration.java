/*******************************************************************************
 * Copyright (c) 2005, 2013 IBM Corporation and others.
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
 *     EclipseSource Corporation - ongoing enhancements
 *******************************************************************************/
package org.eclipse.pde.ui.launcher;

import java.io.File;
import java.util.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.TargetPlatform;
import org.eclipse.pde.internal.core.*;
import org.eclipse.pde.internal.core.util.CoreUtility;
import org.eclipse.pde.internal.core.util.VersionUtil;
import org.eclipse.pde.internal.launching.PDEMessages;
import org.eclipse.pde.internal.launching.launcher.*;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.osgi.framework.Version;

/**
 * A launch delegate for launching Eclipse applications
 * <p>
 * Clients may subclass and instantiate this class.
 * </p>
 * @since 3.2
 * @deprecated use {@link org.eclipse.pde.launching.EclipseApplicationLaunchConfiguration}
 * @see org.eclipse.pde.launching.AbstractPDELaunchConfiguration
 */
@Deprecated
public class EclipseApplicationLaunchConfiguration extends AbstractPDELaunchConfiguration {

	// used to generate the dev classpath entries
	// key is bundle ID, value is a model
	private Map<String, IPluginModelBase> fAllBundles;

	// key is a model, value is startLevel:autoStart
	private Map<IPluginModelBase, String> fModels;

	/**
	 * To avoid duplicating variable substitution (and duplicate prompts)
	 * this variable will store the substituted workspace location.
	 */
	private String fWorkspaceLocation;

	@Override
	public String[] getProgramArguments(ILaunchConfiguration configuration) throws CoreException {
		ArrayList<String> programArgs = new ArrayList<>();

		// If a product is specified, then add it to the program args
		if (configuration.getAttribute(org.eclipse.pde.launching.IPDELauncherConstants.USE_PRODUCT, false)) {
			String product = configuration.getAttribute(org.eclipse.pde.launching.IPDELauncherConstants.PRODUCT, ""); //$NON-NLS-1$
			if (product.length() > 0) {
				programArgs.add("-product"); //$NON-NLS-1$
				programArgs.add(product);
			} else { // TODO product w/o an application and product... how to handle gracefully?
				programArgs.add("-application"); //$NON-NLS-1$
				programArgs.add(configuration.getAttribute(org.eclipse.pde.launching.IPDELauncherConstants.APPLICATION, "")); //$NON-NLS-1$
			}
		} else {
			// specify the application to launch
			programArgs.add("-application"); //$NON-NLS-1$
			programArgs.add(configuration.getAttribute(org.eclipse.pde.launching.IPDELauncherConstants.APPLICATION, TargetPlatform.getDefaultApplication()));
		}

		// specify the workspace location for the runtime workbench
		if (fWorkspaceLocation == null) {
			fWorkspaceLocation = LaunchArgumentsHelper.getWorkspaceLocation(configuration);
		}
		if (fWorkspaceLocation.length() > 0) {
			programArgs.add("-data"); //$NON-NLS-1$
			programArgs.add(fWorkspaceLocation);
		}

		boolean showSplash = true;
		if (configuration.getAttribute(org.eclipse.pde.launching.IPDELauncherConstants.USEFEATURES, false)) {
			validateFeatures();
			IPath installPath = PDEPlugin.getWorkspace().getRoot().getLocation();
			programArgs.add("-install"); //$NON-NLS-1$
			programArgs.add("file:" + installPath.removeLastSegments(1).addTrailingSeparator().toString()); //$NON-NLS-1$
			if (!configuration.getAttribute(org.eclipse.pde.launching.IPDELauncherConstants.CONFIG_USE_DEFAULT_AREA, true)) {
				programArgs.add("-configuration"); //$NON-NLS-1$
				programArgs.add("file:" + new Path(getConfigDir(configuration).getPath()).addTrailingSeparator().toString()); //$NON-NLS-1$
			}
			programArgs.add("-update"); //$NON-NLS-1$
			// add the output folder names
			programArgs.add("-dev"); //$NON-NLS-1$
			programArgs.add(ClasspathHelper.getDevEntriesProperties(getConfigDir(configuration).toString() + "/dev.properties", true)); //$NON-NLS-1$
		} else {
			String productID = LaunchConfigurationHelper.getProductID(configuration);
			Properties prop = LaunchConfigurationHelper.createConfigIniFile(configuration, productID, fAllBundles, fModels, getConfigDir(configuration));
			showSplash = prop.containsKey("osgi.splashPath") || prop.containsKey("splashLocation"); //$NON-NLS-1$ //$NON-NLS-2$
			String brandingId = LaunchConfigurationHelper.getContributingPlugin(productID);
			TargetPlatform.createPlatformConfiguration(getConfigDir(configuration), fAllBundles.values().toArray(new IPluginModelBase[fAllBundles.size()]), brandingId != null ? (IPluginModelBase) fAllBundles.get(brandingId) : null);
			TargetPlatformHelper.checkPluginPropertiesConsistency(fAllBundles, getConfigDir(configuration));
			programArgs.add("-configuration"); //$NON-NLS-1$
			programArgs.add("file:" + new Path(getConfigDir(configuration).getPath()).addTrailingSeparator().toString()); //$NON-NLS-1$

			// add the output folder names
			programArgs.add("-dev"); //$NON-NLS-1$
			programArgs.add(ClasspathHelper.getDevEntriesProperties(getConfigDir(configuration).toString() + "/dev.properties", fAllBundles)); //$NON-NLS-1$
		}
		// necessary for PDE to know how to load plugins when target platform = host platform
		// see PluginPathFinder.getPluginPaths() and PluginPathFinder.isDevLaunchMode()
		IPluginModelBase base = fAllBundles.get(PDECore.PLUGIN_ID);
		if (base != null && VersionUtil.compareMacroMinorMicro(base.getBundleDescription().getVersion(), new Version("3.3.1")) < 0) //$NON-NLS-1$
			programArgs.add("-pdelaunch"); //$NON-NLS-1$

		String[] args = super.getProgramArguments(configuration);
		Collections.addAll(programArgs, args);

		if (!programArgs.contains("-nosplash") && showSplash) { //$NON-NLS-1$
			if (TargetPlatformHelper.getTargetVersion() >= 3.1) {
				programArgs.add(0, "-launcher"); //$NON-NLS-1$

				IPath path = null;
				if (TargetPlatform.getOS().equals("macosx")) { //$NON-NLS-1$
					path = new Path(TargetPlatform.getLocation()).append("Eclipse.app/Contents/MacOS/eclipse"); //$NON-NLS-1$
				} else {
					path = new Path(TargetPlatform.getLocation()).append("eclipse"); //$NON-NLS-1$
					if (TargetPlatform.getOS().equals("win32")) { //$NON-NLS-1$
						path = path.addFileExtension("exe"); //$NON-NLS-1$
					}
				}

				programArgs.add(1, path.toOSString()); //This could be the branded launcher if we want (also this does not bring much)
				programArgs.add(2, "-name"); //$NON-NLS-1$
				programArgs.add(3, "Eclipse"); //This should be the name of the product //$NON-NLS-1$
				programArgs.add(4, "-showsplash"); //$NON-NLS-1$
				programArgs.add(5, "600"); //$NON-NLS-1$
			} else {
				programArgs.add(0, "-showsplash"); //$NON-NLS-1$
				programArgs.add(1, computeShowsplashArgument());
			}
		}
		return programArgs.toArray(new String[programArgs.size()]);
	}

	private void validateFeatures() throws CoreException {
		IPath installPath = PDEPlugin.getWorkspace().getRoot().getLocation();
		String lastSegment = installPath.lastSegment();
		boolean badStructure = lastSegment == null;
		if (!badStructure) {
			IPath featuresPath = installPath.removeLastSegments(1).append("features"); //$NON-NLS-1$
			badStructure = !lastSegment.equalsIgnoreCase("plugins") //$NON-NLS-1$
					|| !featuresPath.toFile().exists();
		}
		if (badStructure) {
			throw new CoreException(LauncherUtils.createErrorStatus(PDEMessages.WorkbenchLauncherConfigurationDelegate_badFeatureSetup));
		}
		// Ensure important files are present
		ensureProductFilesExist(getProductPath());
	}

	private IPath getProductPath() {
		return PDEPlugin.getWorkspace().getRoot().getLocation().removeLastSegments(1);
	}

	private String computeShowsplashArgument() {
		IPath eclipseHome = new Path(TargetPlatform.getLocation());
		IPath fullPath = eclipseHome.append("eclipse"); //$NON-NLS-1$
		return fullPath.toOSString() + " -showsplash 600"; //$NON-NLS-1$
	}

	private void ensureProductFilesExist(IPath productArea) {
		File productDir = productArea.toFile();
		File marker = new File(productDir, ".eclipseproduct"); //$NON-NLS-1$
		IPath eclipsePath = new Path(TargetPlatform.getLocation());
		if (!marker.exists())
			CoreUtility.copyFile(eclipsePath, ".eclipseproduct", marker); //$NON-NLS-1$

		File configDir = new File(productDir, "configuration"); //$NON-NLS-1$
		if (!configDir.exists())
			configDir.mkdirs();
		File ini = new File(configDir, "config.ini"); //$NON-NLS-1$
		if (!ini.exists())
			CoreUtility.copyFile(eclipsePath.append("configuration"), "config.ini", ini); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	protected File getConfigDir(ILaunchConfiguration config) {
		if (fConfigDir == null) {
			try {
				if (config.getAttribute(org.eclipse.pde.launching.IPDELauncherConstants.USEFEATURES, false) && config.getAttribute(org.eclipse.pde.launching.IPDELauncherConstants.CONFIG_USE_DEFAULT_AREA, true)) {
					String root = getProductPath().toString();
					root += "/configuration"; //$NON-NLS-1$
					fConfigDir = new File(root);
					if (!fConfigDir.exists())
						fConfigDir.mkdirs();
				} else {
					fConfigDir = LaunchConfigurationHelper.getConfigurationArea(config);
				}
			} catch (CoreException e) {
				fConfigDir = LaunchConfigurationHelper.getConfigurationArea(config);
			}
		}
		return fConfigDir;
	}

	/**
	 * Clears the workspace prior to launching if the workspace exists and the option to
	 * clear it is turned on.  Also clears the configuration area if that option is chosen.
	 *
	 * @param configuration
	 * 			the launch configuration
	 * @param monitor
	 * 			the progress monitor
	 * @throws CoreException
	 * 			if unable to retrieve launch attribute values or the clear operation was cancelled
	 * @since 3.3
	 */
	@Override
	protected void clear(ILaunchConfiguration configuration, IProgressMonitor monitor) throws CoreException {
		if (fWorkspaceLocation == null) {
			fWorkspaceLocation = LaunchArgumentsHelper.getWorkspaceLocation(configuration);
		}

		SubMonitor subMon = SubMonitor.convert(monitor, 50);

		// Clear workspace and prompt, if necessary
		LauncherUtils.clearWorkspace(configuration, fWorkspaceLocation, subMon.split(25));

		subMon.setWorkRemaining(25);
		if (subMon.isCanceled()) {
			throw new CoreException(Status.CANCEL_STATUS);
		}

		// clear config area, if necessary
		if (configuration.getAttribute(org.eclipse.pde.launching.IPDELauncherConstants.CONFIG_CLEAR_AREA, false))
			CoreUtility.deleteContent(getConfigDir(configuration), subMon.split(25));

		subMon.done();
	}

	@Override
	protected void preLaunchCheck(ILaunchConfiguration configuration, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		fWorkspaceLocation = null;

		fModels = BundleLauncherHelper.getMergedBundleMap(configuration, false);
		fAllBundles = new HashMap<>(fModels.size());
		Iterator<?> iter = fModels.keySet().iterator();
		while (iter.hasNext()) {
			IPluginModelBase model = (IPluginModelBase) iter.next();
			fAllBundles.put(model.getPluginBase().getId(), model);
		}
		validateConfigIni(configuration);
		super.preLaunchCheck(configuration, launch, monitor);
	}

	private void validateConfigIni(ILaunchConfiguration configuration) throws CoreException {
		if (!configuration.getAttribute(org.eclipse.pde.launching.IPDELauncherConstants.CONFIG_GENERATE_DEFAULT, true)) {
			String templateLoc = configuration.getAttribute(org.eclipse.pde.launching.IPDELauncherConstants.CONFIG_TEMPLATE_LOCATION, ""); //$NON-NLS-1$
			IStringVariableManager mgr = VariablesPlugin.getDefault().getStringVariableManager();
			templateLoc = mgr.performStringSubstitution(templateLoc);

			File templateFile = new File(templateLoc);
			if (!templateFile.exists()) {
				if (!LauncherUtils.generateConfigIni())
					throw new CoreException(Status.CANCEL_STATUS);
				// with the way the launcher works, if a config.ini file is not found one will be generated automatically.
				// This check was to warn the user a config.ini needs to be generated. - bug 161265, comment #7
			}
		}
	}

	@Override
	public String[] getVMArguments(ILaunchConfiguration configuration) throws CoreException {
		String[] vmArgs = super.getVMArguments(configuration);
		IPluginModelBase base = fAllBundles.get(PDECore.PLUGIN_ID);
		if (base != null && VersionUtil.compareMacroMinorMicro(base.getBundleDescription().getVersion(), new Version("3.3.1")) >= 0) { //$NON-NLS-1$
			// necessary for PDE to know how to load plugins when target platform = host platform
			// see PluginPathFinder.getPluginPaths() and PluginPathFinder.isDevLaunchMode()
			String[] result = new String[vmArgs.length + 1];
			System.arraycopy(vmArgs, 0, result, 0, vmArgs.length);
			result[vmArgs.length] = "-Declipse.pde.launch=true"; //$NON-NLS-1$
			return result;
		}
		return vmArgs;
	}

}
