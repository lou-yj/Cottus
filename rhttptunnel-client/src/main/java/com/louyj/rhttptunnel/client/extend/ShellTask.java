package com.louyj.rhttptunnel.client.extend;

import java.util.concurrent.Callable;

import org.springframework.shell.Input;

/**
 *
 * Create at 2020年6月29日
 *
 * @author Louyj
 *
 */
public class ShellTask implements Callable<Object> {

	private Input input;
	private CustomShell shell;

	public ShellTask(Input input, CustomShell shell) {
		super();
		this.input = input;
		this.shell = shell;
	}

	@Override
	public Object call() throws Exception {
		try {
			return shell.evaluate(input);
		} catch (Exception e) {
			return e;
		}
	}

}
