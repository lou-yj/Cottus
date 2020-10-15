package com.louyj.cottus.worker.sshd;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;

import com.google.common.collect.Maps;

/**
 *
 * Create at 2020年6月30日
 *
 * @author Louyj
 *
 */
public class JHttpTunnelTest {

	private long pumpSleepTime = 1l;
	private int bufferSize = 8192;

	private Process shell;
	private InputStream shellOut;
	private InputStream shellErr;
	private OutputStream shellIn;
	private InputStream in = System.in;
	private OutputStream out = System.out;
	private OutputStream err = System.err;

	public static void main(String[] args) throws IOException, InterruptedException {
		new JHttpTunnelTest().setup();
	}

	public void setup() throws IOException, InterruptedException {
//		ExecutorService executorService = Executors.newSingleThreadExecutor();
		String[] command = new String[] { "/bin/bash" };
//		String[] command = new String[] { "cmd.exe" };
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
//		executorService.execute(this::pumpStreams);
		pumpStreams();
		System.out.println("3333");

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
					System.out.println("111");
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
			System.out.println("2222");
		}

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

}
