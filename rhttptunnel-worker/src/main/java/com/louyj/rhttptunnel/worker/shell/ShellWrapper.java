package com.louyj.rhttptunnel.worker.shell;

import static com.google.common.base.Charsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 *
 * Create at 2020年6月30日
 *
 * @author Louyj
 *
 */
public class ShellWrapper {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private long pumpSleepTime = 1l;
	private int bufferSize = 8192;

	private Process shell;
	private InputStream shellOut;
	private InputStream shellErr;
	private OutputStream shellIn;
	private String currentCommandId;
	private ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
	private ByteArrayOutputStream errBuffer = new ByteArrayOutputStream();

	static enum SubmitStatus {
		SUCCESS, BUSY, NOTALIVE
	}

	static class ShellOutput {
		boolean finished = false;
		List<String> out = Collections.emptyList();
		List<String> err = Collections.emptyList();
	}

	public void setup() throws IOException, InterruptedException {
		String[] command = new String[] { "/bin/bash" };
		Map<String, String> varsMap = Maps.newHashMap();
		ProcessBuilder builder = new ProcessBuilder(command);
		if (MapUtils.isNotEmpty(varsMap)) {
			Map<String, String> procEnv = builder.environment();
			procEnv.putAll(varsMap);
		}
		shell = builder.start();
		shellOut = shell.getInputStream();
		shellErr = shell.getErrorStream();
		shellIn = shell.getOutputStream();
	}

	public boolean isAlive() {
		return shell.isAlive();
	}

	public synchronized Pair<SubmitStatus, String> submit(String cmd) throws IOException {
		if (currentCommandId != null) {
			return Pair.of(SubmitStatus.BUSY, EMPTY);
		}
		if (shell.isAlive() == false) {
			return Pair.of(SubmitStatus.NOTALIVE, EMPTY);
		}
		currentCommandId = UUID.randomUUID().toString();
		shellIn.write(encodeWithNewLine(cmd));
		shellIn.write(encodeWithNewLine(String.format("echo '%s'", currentCommandId)));
		return Pair.of(SubmitStatus.SUCCESS, currentCommandId);
	}

	public ShellOutput fetchResult(String cmdId) throws IOException {
		if (cmdId != currentCommandId) {
			throw new RuntimeException("Command id not matched");
		}
		ShellOutput shellOutput = new ShellOutput();
		shellOutput.err = readlines(shellErr);
		shellOutput.out = readlines(shellOut);
		if (CollectionUtils.isNotEmpty(shellOutput.out)) {
			String lastLine = shellOutput.out.get(shellOutput.out.size() - 1);
			if (StringUtils.equals(lastLine, cmdId)) {
				currentCommandId = null;
				shellOutput.finished = true;
				shellOutput.out.remove(shellOutput.out.size() - 1);
			}
		}
		return shellOutput;
	}

	public void close() {
		try {
			shell.destroy();
			shell.waitFor();
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	private byte[] encodeWithNewLine(String cnt) {
		return (cnt + "\n").getBytes(UTF_8);
	}

	private String encode(byte[] cnt) {
		return new String(cnt, UTF_8);
	}

	private List<String> readlines(InputStream in) throws IOException {
		List<String> result = Lists.newArrayList();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		while (in.available() > 0) {
			int read = in.read();
			baos.write(read);
			if (read == '\n') {
				result.add(encode(baos.toByteArray()));
				baos = new ByteArrayOutputStream();
			}
		}
		return result;
	}

	protected boolean pumpStream(InputStream in, OutputStream out, byte[] buffer) throws IOException {
		int available = in.available();
		if (available > 0) {
			int len = in.read(buffer);
			if (len > 0) {
				out.write(buffer, 0, len);
				out.flush();
				return true;
			}
		} else if (available == -1) {
			out.close();
		}
		return false;
	}

	protected void pumpStreams() {
		try {
			for (byte[] buffer = new byte[bufferSize];;) {
				if (pumpStream(in, shellIn, buffer)) {
					continue;
				}
				if (pumpStream(shellOut, out, buffer)) {
					continue;
				}
				if (pumpStream(shellErr, err, buffer)) {
					continue;
				}
				if ((!shell.isAlive()) && (in.available() <= 0) && (shellOut.available() <= 0)
						&& (shellErr.available() <= 0)) {
					return;
				}
				Thread.sleep(pumpSleepTime);
			}
		} catch (Exception e) {
			try {
				shell.destroy();
				shell.waitFor();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
//			int exitValue = shell.exitValue();
			e.printStackTrace();
		}

	}

}
