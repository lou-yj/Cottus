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
public interface IMetricsCollector {

	MetricsCollectType type();

	String collect(TaskScheduleMessage taskScheduleMessage, EvalResult evalResult, Map<String, Object> env)
			throws Exception;

}
