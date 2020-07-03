package com.louyj.rhttptunnel.server.automation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.louyj.rhttptunnel.model.bean.Sampler;

/**
 *
 * Create at 2020年7月3日
 * 
 * @author Louyj
 *
 */
public class SamplerScheduleTask implements Runnable {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private Sampler sampler;

	private AutomateManager automateManager;

	public SamplerScheduleTask(Sampler sampler, AutomateManager automateManager) {
		super();
		this.sampler = sampler;
		this.automateManager = automateManager;
	}

	@Override
	public void run() {
		try {
			automateManager.scheduleSample(sampler);
		} catch (Exception e) {
			logger.error("", e);
		}
	}

}
