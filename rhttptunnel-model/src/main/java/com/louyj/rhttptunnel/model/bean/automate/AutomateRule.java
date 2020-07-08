package com.louyj.rhttptunnel.model.bean.automate;

/**
 *
 * Create at 2020年7月3日
 *
 * @author Louyj
 *
 */
public class AutomateRule {

	private Executor executor;

	private Alarmer alarmer;

	private Handler handler;

	public Executor getExecutor() {
		return executor;
	}

	public void setExecutor(Executor executor) {
		this.executor = executor;
	}

	public Alarmer getAlarmer() {
		return alarmer;
	}

	public void setAlarmer(Alarmer alarmer) {
		this.alarmer = alarmer;
	}

	public Handler getHandler() {
		return handler;
	}

	public void setHandler(Handler handler) {
		this.handler = handler;
	}

}
