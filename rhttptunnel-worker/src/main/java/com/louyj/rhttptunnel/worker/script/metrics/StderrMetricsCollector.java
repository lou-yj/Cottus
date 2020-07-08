package com.louyj.rhttptunnel.worker.script.metrics;

import java.util.Map;

import com.louyj.rhttptunnel.model.message.server.TaskScheduleMessage;
import com.louyj.rhttptunnel.model.message.server.TaskScheduleMessage.MetricsCollectType;
import com.louyj.rhttptunnel.worker.script.EvalResult;

/**
 *
 * Create at 2020年7月8日
 *
 * @author Louyj
 *
 */
public class StderrMetricsCollector implements IMetricsCollector {

	@Override
	public MetricsCollectType type() {
		return MetricsCollectType.STDERR_METRICS;
	}

	@Override
	public String collect(TaskScheduleMessage taskScheduleMessage, EvalResult evalResult, Map<String, Object> env) {
		return evalResult.getStderr().toString();
	}

}
