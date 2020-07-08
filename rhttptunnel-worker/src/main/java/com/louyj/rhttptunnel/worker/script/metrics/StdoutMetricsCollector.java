package com.louyj.rhttptunnel.worker.script.metrics;

import java.util.Map;

import com.louyj.rhttptunnel.model.message.TaskScheduleMessage;
import com.louyj.rhttptunnel.model.message.TaskScheduleMessage.MetricsCollectType;
import com.louyj.rhttptunnel.worker.script.EvalResult;

/**
 *
 * Create at 2020年7月8日
 *
 * @author Louyj
 *
 */
public class StdoutMetricsCollector implements IMetricsCollector {

	@Override
	public MetricsCollectType type() {
		return MetricsCollectType.STDOUT_METRICS;
	}

	@Override
	public String collect(TaskScheduleMessage taskScheduleMessage, EvalResult evalResult, Map<String, Object> env) {
		return evalResult.getStdout().toString();
	}

}
