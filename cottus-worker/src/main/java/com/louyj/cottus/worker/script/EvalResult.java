package com.louyj.cottus.worker.script;

import java.io.StringWriter;

public class EvalResult {

	private Object eval;

	private StringWriter stdout;

	private StringWriter stderr;

	public Object getEval() {
		return eval;
	}

	public void setEval(Object eval) {
		this.eval = eval;
	}

	public StringWriter getStdout() {
		return stdout;
	}

	public void setStdout(StringWriter stdout) {
		this.stdout = stdout;
	}

	public StringWriter getStderr() {
		return stderr;
	}

	public void setStderr(StringWriter stderr) {
		this.stderr = stderr;
	}

}
