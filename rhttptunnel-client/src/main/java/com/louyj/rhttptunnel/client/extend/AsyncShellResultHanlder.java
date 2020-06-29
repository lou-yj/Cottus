package com.louyj.rhttptunnel.client.extend;

import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.PostConstruct;

import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.shell.ExitRequest;
import org.springframework.shell.Input;
import org.springframework.shell.ResultHandler;
import org.springframework.shell.Shell;
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

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		while (true) {
			try {
				Object exchange = exchanger.exchange(null);
				Future<Object> future = executorService.submit(new ShellTask((Input) exchange, shell));
				result = future.get();
				if (result != Shell.NO_INPUT && !(result instanceof ExitRequest)) {
					terminal.writer().print(new AttributedString("\r"));
					handler.handleResult(result);
					terminal.writer().print(customPromptProvider.getPrompt().toAnsi(terminal));
					terminal.writer().flush();
				}
				waiting = false;
			} catch (InterruptedException e) {
				break;
			} catch (Exception e) {
				System.err.println(e.getClass() + ":" + e.getMessage());
			}
		}
	}

	public void evaluate(Input input) {
		try {
			exchanger.exchange(input, 1, TimeUnit.MILLISECONDS);
			waiting = true;
		} catch (InterruptedException e) {
		} catch (TimeoutException e) {
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
