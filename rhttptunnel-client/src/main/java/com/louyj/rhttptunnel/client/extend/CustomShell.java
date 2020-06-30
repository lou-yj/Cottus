package com.louyj.rhttptunnel.client.extend;

import java.io.IOException;

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

	private final ResultHandler syncHandler;

	public CustomShell(ResultHandler resultHandler) {
		super(resultHandler);
		this.syncHandler = resultHandler;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run(InputProvider inputProvider) throws IOException {
		Object result = null;
		while (!(result instanceof ExitRequest)) {
			Input input;
			try {
				input = inputProvider.readInput();
			} catch (ExitRequest e) {
				continue;
			} catch (Exception e) {
				syncHandler.handleResult(e);
				continue;
			}
			if (input == null) {
				break;
			}
			checkPermission(input);
			result = evaluate(input);
			if (result != NO_INPUT && !(result instanceof ExitRequest)) {
				syncHandler.handleResult(result);
			}
		}
	}

	public void checkPermission(Input input) {
		// todo
	}

}
