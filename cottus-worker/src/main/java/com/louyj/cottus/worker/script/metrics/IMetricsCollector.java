package com.louyj.cottus.worker.script.metrics;

import java.util.Map;

import com.louyj.cottus.worker.script.EvalResult;
import com.louyj.rhttptunnel.model.message.server.TaskScheduleMessage;
import com.louyj.rhttptunnel.model.message.server.TaskScheduleMessage.MetricsCollectType;

/**
 *
 * Create at 2020年7月8日
 *
 * @author Louyj
 *
 */
public interface IMetricsCollector {

	MetricsCollectType type();

	String collect(TaskScheduleMessage taskScheduleMessage, EvalResult evalResult, Map<String, Object> env)
			throws Exception;

}
