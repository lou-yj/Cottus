package com.louyj.rhttptunnel.worker.shell;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteResultHandler;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Charsets;

/**
 *
 * Created on 2020年3月25日
 *
 * @author Louyj
 *
 */
public class ShellHolder {

	private String clientId;
	private File infile;
	private File outfile;
	private File resfile;
	private File scriptfile;
	private File errFile;

	public ShellHolder(String workDirectory, String clientId) {
		this.clientId = clientId;
		infile = new File(workDirectory, ".temp/" + clientId + ".in");
		outfile = new File(workDirectory, ".temp/" + clientId + ".out");
		resfile = new File(workDirectory, ".temp/" + clientId + ".res");
		scriptfile = new File(workDirectory, ".temp/" + clientId + ".sc");
		errFile = new File(workDirectory, ".temp/" + clientId + ".err");
		infile.getParentFile().mkdirs();
	}

	public void start() throws IOException {
		InputStream openStream = this.getClass().getClassLoader().getResource("shell.txt").openStream();
		String shell = IOUtils.toString(openStream, Charsets.UTF_8);
		String inpath = infile.getAbsolutePath();
		String outpath = outfile.getAbsolutePath();
		String respath = resfile.getAbsolutePath();
		String scpath = scriptfile.getAbsolutePath();
		String errpath = errFile.getAbsolutePath();
		String script = shell.replace("\r\n", "\n").replace("{{in}}", inpath).replace("{{out}}", outpath)
				.replace("{{res}}", respath).replace("{{err}}", errpath).replace("{{uuid}}", clientId);
		IOUtils.write(script, new FileOutputStream(scriptfile));
		String cmdLine = "/bin/bash " + scpath;

		ExecuteWatchdog watchdog = new ExecuteWatchdog(TimeUnit.HOURS.toMillis(1));
		ExecuteResultHandler handler = new DefaultExecuteResultHandler();
		CommandLine commandline = CommandLine.parse(cmdLine);
		DefaultExecutor executor = new DefaultExecutor();
		executor.setWatchdog(watchdog);
		executor.execute(commandline, handler);
	}

	public void close() throws IOException, InterruptedException {
		if (infile.exists()) {
			exec("exit", -1);
			TimeUnit.SECONDS.sleep(1);
		}
		FileUtils.deleteQuietly(infile);
		FileUtils.deleteQuietly(outfile);
		FileUtils.deleteQuietly(errFile);
		FileUtils.deleteQuietly(resfile);
		FileUtils.deleteQuietly(scriptfile);
	}

	public boolean isAlive() {
		if (infile.exists() == false) {
			return false;
		}
		try {
			String result = exec(clientId, 5);
			return StringUtils.equals(result, clientId);
		} catch (IOException | InterruptedException e) {
			return false;
		}
	}

	public void ensureRunning() throws IOException, InterruptedException {
		if (isAlive()) {
			return;
		}
		close();
		start();
	}

	public String exec(String cmd, int timeout) throws IOException, InterruptedException {
		long reslast = resfile.lastModified();
		long errlast = errFile.lastModified();
		long outlast = outfile.lastModified();
		FileUtils.writeStringToFile(infile, cmd + "\n", true);
		if (StringUtils.equals(cmd, "exit")) {
			return "";
		}
		if (timeout == -1) {
			timeout = Integer.MAX_VALUE;
		}
		long startms = System.currentTimeMillis();
		while (true) {
			if (System.currentTimeMillis() - startms > timeout * 1000) {
				return null;
			}
			long reslast2 = resfile.lastModified();
			String content = null;
			if (reslast2 > reslast) {
				if (outfile.lastModified() > outlast) {
					content = FileUtils.readFileToString(outfile, "utf8");
				}
				if (errFile.lastModified() > errlast) {
					String content1 = FileUtils.readFileToString(errFile, "utf8");
					if (StringUtils.isNotBlank(content1)) {
						content1 = content1.replace(scriptfile.getAbsolutePath() + ":", "");
						content1 = content1.replaceFirst("\\s*\\d:\\s*eval:\\s*", "");
						if (StringUtils.isBlank(content)) {
							content = "[ERROR] " + content1;
						} else {
							content += "\n[ERROR] " + content1;
						}
					}
				}
				return content;
			} else {
				TimeUnit.MILLISECONDS.sleep(100);
			}
		}
	}

}
