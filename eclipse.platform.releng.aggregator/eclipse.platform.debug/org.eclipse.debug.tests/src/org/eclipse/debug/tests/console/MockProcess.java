/*******************************************************************************
 * Copyright (c) 2019, 2020 Paul Pazderski and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Paul Pazderski - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.tests.console;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.RuntimeProcess;
import org.eclipse.debug.tests.launching.LaunchConfigurationTests;

/**
 * A mockup process which can either simulate generation of output or wait for
 * input to read.
 */
public class MockProcess extends Process {
	/**
	 * Use as run time parameter if mockup process should not terminate until
	 * {@link #destroy()} is used.
	 */
	public static final int RUN_FOREVER = -1;

	/** Mockup processe's standard streams. */
	private final ByteArrayOutputStream stdin = new ByteArrayOutputStream();
	private final InputStream stdout;
	private final InputStream stderr;

	/** Lock used in {@link #waitFor()}. */
	private final Object waitForTerminationLock = new Object();
	/**
	 * Store number of bytes received which are not buffered anymore (i.e. those
	 * input was already passed through {@link #getReceivedInput()}).
	 */
	private AtomicInteger receivedInput = new AtomicInteger(0);
	/**
	 * The time (in epoch milliseconds) when the mockup process terminates.
	 * <p>
	 * If this value is in the future it is the processes timeout. If it is in
	 * the past it is the processes termination time. If it is <code>-1</code>
	 * the process does not terminate and must be stopped using
	 * {@link #destroy()}.
	 * </p>
	 */
	private long endTime;
	/** The simulated exit code. */
	private int exitCode = 0;

	/**
	 * Create new silent mockup process which runs for a given amount of time.
	 * Does not read input or produce any output.
	 *
	 * @param runTimeMs runtime of the mockup process in milliseconds. If
	 *            <code>0</code> the process terminates immediately. A
	 *            <i>negative</i> value means the mockup process never
	 *            terminates and must stopped with {@link #destroy()}.
	 */
	public MockProcess(long runTimeMs) {
		this(null, null, runTimeMs);
	}

	/**
	 * Create new mockup process and feed standard output streams with given
	 * content.
	 *
	 * @param stdout mockup process standard output stream. May be
	 *            <code>null</code>.
	 * @param stderr mockup process standard error stream. May be
	 *            <code>null</code>.
	 * @param runTimeMs runtime of the mockup process in milliseconds. If
	 *            <code>0</code> the process terminates immediately. A
	 *            <i>negative</i> value means the mockup process never
	 *            terminates and must stopped with {@link #destroy()}.
	 */
	public MockProcess(InputStream stdout, InputStream stderr, long runTimeMs) {
		super();
		this.stdout = (stdout != null ? stdout : new ByteArrayInputStream(new byte[0]));
		this.stderr = (stderr != null ? stderr : new ByteArrayInputStream(new byte[0]));
		this.endTime = runTimeMs < 0 ? RUN_FOREVER : System.currentTimeMillis() + runTimeMs;
	}

	/**
	 * Create new mockup process and wait for input on standard input stream.
	 * The mockup process terminates after receiving the given amount of data or
	 * after it's timeout.
	 *
	 * @param expectedInputSize number of bytes to receive before termination
	 * @param timeoutMs mockup process will be stopped after given amount of
	 *            milliseconds. If <i>negative</i> timeout is disabled.
	 */
	public MockProcess(final int expectedInputSize, long timeoutMs) {
		super();
		this.stdout = new ByteArrayInputStream(new byte[0]);
		this.stderr = new ByteArrayInputStream(new byte[0]);
		this.endTime = (timeoutMs > 0 ? System.currentTimeMillis() + timeoutMs : RUN_FOREVER);

		final Thread inputMonitor = new Thread(() -> {
			while (!MockProcess.this.isTerminated()) {
				synchronized (waitForTerminationLock) {
					if (receivedInput.get() + stdin.size() >= expectedInputSize) {
						endTime = System.currentTimeMillis();
						waitForTerminationLock.notifyAll();
						break;
					}
				}
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					break;
				}
			}
		}, "Mockup Process Input Monitor");
		inputMonitor.setDaemon(true);
		inputMonitor.start();
	}

	/**
	 * Get bytes received through stdin since last invocation of this method.
	 * <p>
	 * Not thread safe. It may miss some input if new content is written while
	 * this method is executed.
	 * </p>
	 *
	 * @return standard input since last invocation
	 */
	public synchronized byte[] getReceivedInput() {
		final byte[] content = stdin.toByteArray();
		stdin.reset();
		receivedInput.addAndGet(content.length);
		return content;
	}

	@Override
	public OutputStream getOutputStream() {
		return stdin;
	}

	@Override
	public InputStream getInputStream() {
		return stdout;
	}

	@Override
	public InputStream getErrorStream() {
		return stderr;
	}

	@Override
	public int waitFor() throws InterruptedException {
		synchronized (waitForTerminationLock) {
			while (!isTerminated()) {
				if (endTime == RUN_FOREVER) {
					waitForTerminationLock.wait();
				} else {
					final long waitTime = endTime - System.currentTimeMillis();
					if (waitTime > 0) {
						waitForTerminationLock.wait(waitTime);
					}
				}
			}
		}
		return exitCode;
	}

	@Override
	public boolean waitFor(long timeout, TimeUnit unit) throws InterruptedException {
		long remainingMs = unit.toMillis(timeout);
		final long timeoutMs = System.currentTimeMillis() + remainingMs;
		synchronized (waitForTerminationLock) {
			while (!isTerminated() && remainingMs > 0) {
				long waitTime = endTime == RUN_FOREVER ? Long.MAX_VALUE : endTime - System.currentTimeMillis();
				waitTime = Math.min(waitTime, remainingMs);
				if (waitTime > 0) {
					waitForTerminationLock.wait(waitTime);
				}
				remainingMs = timeoutMs - System.currentTimeMillis();
			}
		}
		return isTerminated();
	}

	@Override
	public int exitValue() {
		if (!isTerminated()) {
			final String end = (endTime == RUN_FOREVER ? "never." : "in " + (endTime - System.currentTimeMillis()) + " ms.");
			throw new IllegalThreadStateException("Mockup process terminates " + end);
		}
		return exitCode;
	}

	@Override
	public void destroy() {
		destroy(0);
	}

	/**
	 * Simulate a delay for the mockup process shutdown.
	 *
	 * @param delay amount of milliseconds must pass after destroy was called
	 *            and before the mockup process goes in terminated state
	 */
	public void destroy(int delay) {
		synchronized (waitForTerminationLock) {
			endTime = System.currentTimeMillis() + delay;
			waitForTerminationLock.notifyAll();
		}
	}

	/**
	 * Check if this process is already terminated.
	 *
	 * @return <code>true</code> if process is terminated
	 */
	private boolean isTerminated() {
		return endTime != RUN_FOREVER && System.currentTimeMillis() > endTime;
	}

	/**
	 * Set the exit code returned once the process is finished.
	 *
	 * @param exitCode new exit code
	 */
	public void setExitValue(int exitCode) {
		this.exitCode = exitCode;
	}

	/**
	 * Create a {@link RuntimeProcess} which wraps this {@link MockProcess}.
	 * <p>
	 * Note: the process will only be connected to a minimal dummy launch
	 * object.
	 * </p>
	 *
	 * @return the created {@link RuntimeProcess}
	 */
	public RuntimeProcess toRuntimeProcess() {
		return toRuntimeProcess("MockProcess");
	}

	/**
	 * Create a {@link RuntimeProcess} which wraps this {@link MockProcess}.
	 * <p>
	 * Note: the process will only be connected to a minimal dummy launch
	 * object.
	 * </p>
	 *
	 * @param name a custom name for the process
	 * @return the created {@link RuntimeProcess}
	 */
	public RuntimeProcess toRuntimeProcess(String name) {
		return (RuntimeProcess) DebugPlugin.newProcess(new Launch(null, ILaunchManager.RUN_MODE, null), this, name);
	}

	/**
	 * Create a {@link RuntimeProcess} which wraps this {@link MockProcess}.
	 * <p>
	 * This method also attaches a
	 * {@link LaunchConfigurationTests#ID_TEST_LAUNCH_TYPE} launch configuration
	 * to the {@link RuntimeProcess}.
	 * </p>
	 *
	 * @param name name for the process and launch configuration
	 * @return the created {@link RuntimeProcess}
	 */
	public RuntimeProcess toRuntimeProcess(String name, Map<String, Object> launchConfigAttributes) throws CoreException {
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType launchType = launchManager.getLaunchConfigurationType(LaunchConfigurationTests.ID_TEST_LAUNCH_TYPE);
		ILaunchConfigurationWorkingCopy launchConfiguration = launchType.newInstance(null, name);
		if (launchConfigAttributes != null) {
			launchConfiguration.setAttributes(launchConfigAttributes);
		}
		return (RuntimeProcess) DebugPlugin.newProcess(new Launch(launchConfiguration, ILaunchManager.RUN_MODE, null), this, name);
	}
}
