package com.louyj.rhttptunnel.worker.script.shell;

/**
 *
 * Create at 2020年7月7日
 *
 * @author Louyj
 *
 */
public class ShellScriptResult {

	private int exitValue;

	private String stdout;

	private String stderr;

	public int getExitValue() {
		return exitValue;
	}

	public void setExitValue(int exitValue) {
		this.exitValue = exitValue;
	}

	public String getStdout() {
		return stdout;
	}

	public void setStdout(String stdout) {
		this.stdout = stdout;
	}

	public String getStderr() {
		return stderr;
	}

	public void setStderr(String stderr) {
		this.stderr = stderr;
	}

}
