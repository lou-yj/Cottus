package com.louyj.rhttptunnel.client.extend;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.shell.ExitRequest;
import org.springframework.shell.Input;
import org.springframework.shell.InputProvider;
import org.springframework.shell.ResultHandler;
import org.springframework.shell.Shell;

/**
 *
 * Create at 2020年6月29日
 *
 * @author Louyj
 *
 */
@SuppressWarnings("rawtypes")
public class CustomShell extends Shell {

	@Qualifier("main")
	@Autowired
	private final ResultHandler syncHandler;
	@Autowired
	private AsyncShellResultHanlder asyncShellResultHanlder;

	public CustomShell(ResultHandler resultHandler, AsyncShellResultHanlder asyncShellResultHanlder) {
		super(resultHandler);
		this.syncHandler = resultHandler;
		this.asyncShellResultHanlder = asyncShellResultHanlder;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run(InputProvider inputProvider) throws IOException {
		while (!(asyncShellResultHanlder.getResult() instanceof ExitRequest)) {
			Input input;
			try {
				input = inputProvider.readInput();
			} catch (Exception e) {
				if (e instanceof ExitRequest) {
				} else {
					syncHandler.handleResult(e);
				}
				continue;
			}
			if (input == null) {
				break;
			}
			asyncShellResultHanlder.evaluate(input);
		}
	}

}
