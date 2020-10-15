package com.louyj.cottus.worker.script.metrics;

import java.util.Map;

import com.louyj.cottus.worker.script.EvalResult;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.message.server.TaskScheduleMessage;
import com.louyj.rhttptunnel.model.message.server.TaskScheduleMessage.MetricsCollectType;

/**
 *
 * Create at 2020年7月8日
 *
 * @author Louyj
 *
 */
@Component
public class ExitValueMetricsCollector implements IMetricsCollector {

	@Override
	public MetricsCollectType type() {
		return MetricsCollectType.EXITVALUE_WRAPPER;
	}

	@Override
	public String collect(TaskScheduleMessage taskScheduleMessage, EvalResult evalResult, Map<String, Object> env) {
		return String.format("%s exitValue=%s", taskScheduleMessage.getName(), evalResult.getEval());
	}

}
