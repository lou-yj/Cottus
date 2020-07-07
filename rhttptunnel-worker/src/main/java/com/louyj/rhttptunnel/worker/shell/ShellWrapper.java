package com.louyj.rhttptunnel.worker.shell;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.sshd.common.util.OsUtils;
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
public class ShellWrapper implements Closeable {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private Process shell;
	private InputStream shellOut;
	private InputStream shellErr;
	private OutputStream shellIn;
	private String currentCommandId;
	private long currentStartTime;
	private ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
	private ByteArrayOutputStream errBuffer = new ByteArrayOutputStream();

	private int timeout = 60;
	private String workDirectory;
	private boolean isWin32 = false;
	private String encoding;

	public static enum SubmitStatus {
		SUCCESS, BUSY, NOTALIVE
	}

	public ShellWrapper(String workDirectory) {
		this.workDirectory = workDirectory;
	}

	public static class ShellOutput {
		public boolean finished = false;
		public List<String> out = Lists.newArrayList();
		public List<String> err = Lists.newArrayList();
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public void setup() throws IOException, InterruptedException {
		isWin32 = OsUtils.isWin32();
		encoding = StringUtils.defaultIfBlank(System.getProperty("sun.jnu.encoding"), "UTF8");
		String[] command = null;
		if (isWin32) {
			command = new String[] { "cmd.exe" };
		} else {
			command = new String[] { "/bin/bash" };
		}
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
		if (isWin32) {

		} else {
			Pair<SubmitStatus, String> submit = submit(
					String.format("export _WORKER_WORK_DIRECTORY=\"%s\"", workDirectory));
			fetchAllSlient(submit);
			submit = submit("function ___echo_with_exit_code___(){ local code=$?; echo \"$*\"; return ${code}; }");
			fetchAllSlient(submit);
		}
	}

	public boolean isAlive() {
		try {
			return shell.isAlive();
		} catch (Exception e) {
			logger.warn("Check alive failed", e);
			return false;
		}
	}

	public synchronized Pair<SubmitStatus, String> submit(String cmd) throws IOException {
		if (currentCommandId != null) {
			return Pair.of(SubmitStatus.BUSY, EMPTY);
		}
		if (shell.isAlive() == false) {
			return Pair.of(SubmitStatus.NOTALIVE, EMPTY);
		}
		if (StringUtils.equals(StringUtils.trim(cmd), "exit")) {
			this.close();
			return Pair.of(SubmitStatus.NOTALIVE, EMPTY);
		}
		currentCommandId = UUID.randomUUID().toString();
		currentStartTime = System.currentTimeMillis();
		shellIn.write(encodeWithNewLine(cmd));
		if (isWin32) {
			shellIn.write(encodeWithNewLine(String.format("echo '%s'", currentCommandId)));
		} else {
			shellIn.write(encodeWithNewLine(String.format("___echo_with_exit_code___ '%s'", currentCommandId)));
		}
		shellIn.flush();
		return Pair.of(SubmitStatus.SUCCESS, currentCommandId);
	}

	public ShellOutput fetchAllResult(String cmdId) throws InterruptedException, IOException {
		ShellOutput result = new ShellOutput();
		while (true) {
			ShellOutput fetchResult = fetchResult(cmdId);
			if (fetchResult.finished == false && isEmpty(fetchResult.out) && isEmpty(fetchResult.err)) {
				TimeUnit.MILLISECONDS.sleep(10);
				continue;
			}
			result.finished = fetchResult.finished;
			result.out.addAll(fetchResult.out);
			result.err.addAll(fetchResult.err);
			if (fetchResult.finished) {
				break;
			}
		}
		return result;
	}

	public ShellOutput fetchResult(String cmdId) throws IOException {
		return fetchResult(cmdId, timeout);
	}

	public ShellOutput fetchResult(String cmdId, int timeout) throws IOException {
		while (true) {
			ShellOutput shellOutput = fetchResultOnce(cmdId);
			if (shellOutput.finished) {
				return shellOutput;
			}
			if (isNotEmpty(shellOutput.out) || isNotEmpty(shellOutput.err)) {
				return shellOutput;
			}
			if (System.currentTimeMillis() - currentStartTime > timeout * 1000) {
				shellOutput = new ShellOutput();
				shellOutput.finished = true;
				shellOutput.err.add("Timeout after " + timeout + " seconds");
				this.close();
				return shellOutput;
			}
		}
	}

	private ShellOutput fetchResultOnce(String cmdId) throws IOException {
		if (currentCommandId == null) {
			throw new RuntimeException("Current no command submited.");
		}
		if (cmdId != currentCommandId) {
			throw new RuntimeException("Command id not matched");
		}
		ShellOutput shellOutput = new ShellOutput();
		shellOutput.err = readlines(shellErr, errBuffer);
		shellOutput.out = readlines(shellOut, outBuffer);
		if (isFinished(shellOutput, cmdId)) {
			cleanBuffer(shellOutput);
			if (!isWin32) {
				String lastLine = removeLastLine(shellOutput.out);
				if (lastLine == null) {
					throw new RuntimeException("Unexpect null lastline when finished");
				}
				lastLine = lastLine.trim();
				if (lastLine.equals(cmdId)) {

				} else if (lastLine.endsWith(cmdId)) {
					shellOutput.out.add(lastLine.substring(0, lastLine.length() - cmdId.length()));
				} else {
					throw new RuntimeException("Unexpect lastline when finished");
				}
			}
			currentCommandId = null;
			shellOutput.finished = true;
		}
		return shellOutput;
	}

	private boolean isFinished(ShellOutput shellOutput, String cmdId) throws UnsupportedEncodingException {
		if (isWin32) {
			for (int i = 0; i < shellOutput.out.size(); i++) {
				String line = shellOutput.out.get(i);
				if (line.contains(cmdId)) {
					shellOutput.out = shellOutput.out.subList(0, i);
					return true;
				}
			}
		}
		String lastLine = null;
		if (outBuffer.size() > 0) {
			lastLine = encode(outBuffer.toByteArray());
		} else {
			lastLine = lastLine(shellOutput.out);
		}
		if (lastLine == null) {
			return false;
		}
		lastLine = lastLine.trim();
		if (lastLine.equals(cmdId) || lastLine.endsWith(cmdId)) {
			return true;
		}
		return false;
	}

	private String lastLine(List<String> list) {
		if (CollectionUtils.isNotEmpty(list)) {
			return list.get(list.size() - 1);
		}
		return null;
	}

	private String removeLastLine(List<String> list) {
		if (CollectionUtils.isNotEmpty(list)) {
			return list.remove(list.size() - 1);
		}
		return null;
	}

	private void cleanBuffer(ShellOutput shellOutput) throws UnsupportedEncodingException {
		if (outBuffer.size() > 0) {
			shellOutput.out.add(encode(outBuffer.toByteArray()));
			outBuffer.reset();
		}
		if (errBuffer.size() > 0) {
			shellOutput.err.add(encode(errBuffer.toByteArray()));
			errBuffer.reset();
		}
	}

	public void close() {
		try {
			shell.destroy();
			shell.waitFor();
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	private byte[] encodeWithNewLine(String cnt) throws UnsupportedEncodingException {
		return (cnt + "\n").getBytes(encoding);
	}

	private String encode(byte[] cnt) throws UnsupportedEncodingException {
		return new String(cnt, encoding);
	}

	private List<String> readlines(InputStream in, ByteArrayOutputStream baos) throws IOException {
		List<String> result = Lists.newArrayList();
		while (in.available() > 0) {
			int read = in.read();
			if (read == '\n') {
				result.add(encode(baos.toByteArray()));
				baos.reset();
			} else {
				baos.write(read);
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

	private void fetchAllSlient(Pair<SubmitStatus, String> submit) throws InterruptedException, IOException {
		if (submit.getLeft() != SubmitStatus.SUCCESS) {
			throw new RuntimeException("Shell submit failed " + submit.getLeft());
		}
		fetchAllResult(submit.getRight());
	}

}
