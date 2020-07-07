package com.louyj.rhttptunnel.worker.shell;

import static com.google.common.base.Charsets.UTF_8;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
		Pair<SubmitStatus, String> submit = submit(
				String.format("export _WORKER_WORK_DIRECTORY=\"%s\"", workDirectory));
		fetchAllSlient(submit);
		submit = submit("function ___echo_with_exit_code___(){ local code=$?; echo \"$*\"; return ${code}; }");
		fetchAllSlient(submit);
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
		if (StringUtils.equals(StringUtils.trim(cmd), "exit")) {
			this.close();
			return Pair.of(SubmitStatus.NOTALIVE, EMPTY);
		}
		currentCommandId = UUID.randomUUID().toString();
		currentStartTime = System.currentTimeMillis();
		shellIn.write(encodeWithNewLine(cmd));
		shellIn.write(encodeWithNewLine(String.format("___echo_with_exit_code___ '%s'", currentCommandId)));
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
		if (outBuffer.size() > 0) {
			String lastLine = encode(outBuffer.toByteArray());
			if (StringUtils.equals(lastLine, cmdId)) {
				currentCommandId = null;
				shellOutput.finished = true;
				outBuffer.reset();
				cleanBuffer(shellOutput);
			} else if (StringUtils.endsWith(lastLine, cmdId)) {
				shellOutput.out.add(lastLine.substring(0, lastLine.length() - cmdId.length()));
				currentCommandId = null;
				shellOutput.finished = true;
				outBuffer.reset();
				cleanBuffer(shellOutput);
			}
		} else if (CollectionUtils.isNotEmpty(shellOutput.out)) {
			String lastLine = shellOutput.out.get(shellOutput.out.size() - 1);
			if (StringUtils.equals(lastLine, cmdId)) {
				shellOutput.out.remove(shellOutput.out.size() - 1);
				currentCommandId = null;
				shellOutput.finished = true;
				cleanBuffer(shellOutput);
			} else if (StringUtils.endsWith(lastLine, cmdId)) {
				shellOutput.out.remove(shellOutput.out.size() - 1);
				shellOutput.out.add(lastLine.substring(0, lastLine.length() - cmdId.length()));
				currentCommandId = null;
				shellOutput.finished = true;
				cleanBuffer(shellOutput);
			}
		}
		return shellOutput;
	}

	private void cleanBuffer(ShellOutput shellOutput) {
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

	private byte[] encodeWithNewLine(String cnt) {
		return (cnt + "\n").getBytes(UTF_8);
	}

	private String encode(byte[] cnt) {
		return new String(cnt, UTF_8);
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
