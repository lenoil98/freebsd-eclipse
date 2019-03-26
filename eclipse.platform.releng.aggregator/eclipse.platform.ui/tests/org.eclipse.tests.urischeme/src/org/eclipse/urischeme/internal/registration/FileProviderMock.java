/*******************************************************************************
 * Copyright (c) 2018 SAP SE and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP SE - initial version
 *******************************************************************************/
package org.eclipse.urischeme.internal.registration;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileProviderMock implements IFileProvider {

	Map<String, Object> readAnswers = new HashMap<>();
	Map<String, Boolean> fileExistsAnswers = new HashMap<>();
	Map<String, Boolean> isDirectoryAnswers = new HashMap<>();
	List<String> recordedReadPaths = new ArrayList<>();
	String writePath = "not/written";

	byte[] writtenBytes;
	Writer writer;

	@SuppressWarnings("unchecked")
	@Override
	public List<String> readAllLines(String path) throws IOException {
		recordedReadPaths.add(path);

		Object answer = readAnswers.get(path);
		if (answer instanceof Exception) {
			readAnswers.remove(path); // throw only once
			throw (IOException) answer;
		}
		return (List<String>) answer;
	}

	@Override
	public void write(String path, byte[] bytes) throws IOException {
		this.writePath = path;
		this.writtenBytes = bytes;
	}

	@Override
	public Reader newReader(String path) throws IOException {
		this.recordedReadPaths.add(path);

		Object answer = readAnswers.get(path);
		if (answer instanceof Exception) {
			readAnswers.remove(path); // throw only once
			throw (IOException) answer;
		}
		return (Reader) answer;
	}

	@Override
	public Writer newWriter(String path) throws IOException {
		this.writePath = path;
		return this.writer;
	}

	@Override
	public boolean fileExists(String path) {
		return fileExistsAnswers.get(path);
	}

	@Override
	public boolean isDirectory(String path) {
		return isDirectoryAnswers.get(path);
	}

}
