/*******************************************************************************
 *  Copyright (c) 2000, 2017 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 * 
 *  Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.build;

import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import org.eclipse.equinox.p2.publisher.eclipse.Feature;
import org.eclipse.equinox.p2.publisher.eclipse.FeatureEntry;
import org.eclipse.pde.internal.build.site.BuildTimeSite;

public class SourceFeatureWriter extends FeatureWriter {

	public SourceFeatureWriter(OutputStream out, Feature feature, BuildTimeSite site) {
		super(out, feature, site);
	}

	@Override
	public void printIncludes() {
		Map<String, String> parameters = new LinkedHashMap<>();
		// TO CHECK Here we should have the raw list...
		FeatureEntry[] features = feature.getEntries();
		for (int i = 0; i < features.length; i++) {
			if (features[i].isRequires() || features[i].isPlugin())
				continue;
			parameters.clear();
			parameters.put(ID, features[i].getId());
			parameters.put(VERSION, features[i].getVersion());
			if (features[i].isOptional())
				parameters.put("optional", "true"); //$NON-NLS-1$ //$NON-NLS-2$
			if (features[i].getArch() != null)
				parameters.put("arch", features[i].getArch()); //$NON-NLS-1$
			if (features[i].getWS() != null)
				parameters.put("ws", features[i].getWS()); //$NON-NLS-1$
			if (features[i].getOS() != null)
				parameters.put("os", features[i].getOS()); //$NON-NLS-1$
			printTag("includes", parameters, true, true, true); //$NON-NLS-1$
		}
	}
}
