package com.louyj.cottus.worker.shell;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;

/**
 *
 * Created on 2020年3月25日
 *
 * @author Louyj
 *
 */
public class ShellHolder1 {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private String clientId;
	private File infile;
	private File outfile;
	private File resfile;
	private File scriptfile;
	private File errFile;
	private File killFile;

	private int timeout = 60;

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public ShellHolder1(String workDirectory, String clientId) {
		this.clientId = clientId;
		infile = new File(workDirectory, ".temp/." + clientId + ".in");
		outfile = new File(workDirectory, ".temp/." + clientId + ".out");
		resfile = new File(workDirectory, ".temp/." + clientId + ".res");
		scriptfile = new File(workDirectory, ".temp/." + clientId + ".sc");
		errFile = new File(workDirectory, ".temp/." + clientId + ".err");
		killFile = new File(workDirectory, ".temp/.stop.sc");
	}

	public void start() throws IOException {
		infile.getParentFile().mkdirs();
		try {
			infile.createNewFile();
			outfile.createNewFile();
			resfile.createNewFile();
			scriptfile.createNewFile();
			errFile.createNewFile();
		} catch (Exception e) {
			logger.error("", e);
		}
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

	public void close() {
		try {
			if (infile.exists()) {
				exec("exit");
				TimeUnit.SECONDS.sleep(1);
			}
			InputStream openStream = this.getClass().getClassLoader().getResource("kill.txt").openStream();
			String shell = IOUtils.toString(openStream, Charsets.UTF_8);
			shell = shell.replace("\r\n", "\n");
			if (killFile.exists() == false) {
				IOUtils.write(shell, new FileOutputStream(killFile));
			} else {
				String killShell = FileUtils.readFileToString(killFile, Charsets.UTF_8);
				if (StringUtils.equals(shell, killShell) == false) {
					IOUtils.write(shell, new FileOutputStream(killFile));
				}
			}
			String cmdLine = "sh " + killFile.getAbsolutePath() + " " + clientId;
			ExecuteWatchdog watchdog = new ExecuteWatchdog(TimeUnit.SECONDS.toMillis(10));
			CommandLine commandline = CommandLine.parse(cmdLine);
			DefaultExecutor executor = new DefaultExecutor();
			executor.setWatchdog(watchdog);
			executor.execute(commandline);
		} catch (Exception e) {
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
			String result = exec(clientId);
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

	public String exec(String cmd) throws IOException, InterruptedException {
		FileUtils.writeStringToFile(resfile, "");
		FileUtils.writeStringToFile(outfile, "");
		FileUtils.writeStringToFile(errFile, "");
		long reslen = resfile.length();
		FileUtils.writeStringToFile(infile, cmd + "\n", true);
		if (StringUtils.equals(cmd, "exit")) {
			return "";
		}
		long start = System.currentTimeMillis();
		while (true) {
			long reslen2 = resfile.length();
			String content = null;
			if (reslen2 != reslen) {
				content = FileUtils.readFileToString(outfile, "utf8");
				{
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
			if (System.currentTimeMillis() - start > timeout * 1000) {
				return "Timeout after " + timeout + " seconds\n";
			}
		}
	}

}
