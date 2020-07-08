package com.louyj.rhttptunnel.server.automation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.louyj.rhttptunnel.model.bean.automate.Executor;

/**
 *
 * Create at 2020年7月3日
 * 
 * @author Louyj
 *
 */
public class SamplerScheduleTask implements Runnable {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private Executor executor;

	private AutomateManager automateManager;

	public SamplerScheduleTask(Executor sampler, AutomateManager automateManager) {
		super();
		this.executor = sampler;
		this.automateManager = automateManager;
	}

	@Override
	public void run() {
		try {
			automateManager.scheduleExecutorTask(executor);
		} catch (Exception e) {
			logger.error("", e);
		}
	}

}
