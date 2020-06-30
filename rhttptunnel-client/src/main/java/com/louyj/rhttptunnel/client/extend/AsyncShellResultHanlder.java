package com.louyj.rhttptunnel.client.extend;

import static org.springframework.shell.Shell.NO_INPUT;

import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.jline.terminal.Terminal;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.shell.ExitRequest;
import org.springframework.shell.Input;
import org.springframework.shell.ResultHandler;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.client.CustomPromptProvider;

/**
 *
 * Create at 2020年6月29日
 *
 * @author Louyj
 *
 */
@SuppressWarnings("rawtypes")
@Component
public class AsyncShellResultHanlder extends Thread implements ApplicationContextAware {

	private ApplicationContext applicationContext;

	@Qualifier("main")
	@Autowired
	private ResultHandler handler;
	@Autowired
	private Terminal terminal;
	@Autowired
	private CustomPromptProvider customPromptProvider;

	private CustomShell shell;
	private boolean waiting = false;

	private ExecutorService executorService = Executors.newSingleThreadExecutor();
	private Exchanger<Object> exchanger = new Exchanger<>();
	private Object result;

	public Object getResult() {
		return result;
	}

	public boolean isWaiting() {
		return waiting;
	}

	@PostConstruct
	public void init() {
		this.shell = applicationContext.getBean(CustomShell.class);
		this.start();
	}

	@Override
	public void run() {
		while (true) {
			try {
				Object exchange = exchanger.exchange(null);
				Future<Object> future = executorService.submit(new ShellTask((Input) exchange, shell));
				result = future.get();
				if (result != NO_INPUT && !(result instanceof ExitRequest)) {
					handleResult(result);
				}
				waiting = false;
			} catch (InterruptedException e) {
				break;
			} catch (Exception e) {
				handleResult(e);
			}
		}
	}

	public void evaluate(Input input) {
		try {
			exchanger.exchange(input);
			waiting = true;
		} catch (InterruptedException e) {
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@SuppressWarnings("unchecked")
	public void handleResult(Object obj) {
		terminal.writer().print("\r" + StringUtils.repeat(' ', 50) + "\r");
		handler.handleResult(obj);
		terminal.writer().print("\r" + StringUtils.repeat(' ', 50) + "\r");
		terminal.writer().print(customPromptProvider.getPrompt().toAnsi(terminal));
		terminal.writer().flush();
	}

}
