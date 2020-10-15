package com.louyj.cottus.worker.script.metrics;

import static com.louyj.cottus.worker.ClientDetector.CLIENT;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.python.apache.commons.compress.utils.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.louyj.rhttptunnel.model.message.server.TaskMetricsMessage;
import com.louyj.rhttptunnel.model.message.server.TaskScheduleMessage;
import com.louyj.rhttptunnel.model.message.server.TaskScheduleMessage.MetricsType;
import com.louyj.cottus.worker.ClientDetector;

/**
 *
 * Create at 2020年7月8日
 *
 * @author Louyj
 *
 */
public interface IMetricsParser {

	Logger logger = LoggerFactory.getLogger(IMetricsParser.class);

	String EXEC_HOST = "HOST";
	String EXEC_IP = "IP";
	String PARSE_ERROR = "PARSE_ERROR";
	String TASK_START_TIME = "TASK_START_TIME";
	String TASK_END_TIME = "TASK_END_TIME";
	String TASK_DURATION = "TASK_DURATION";

	MetricsType type();

	default List<TaskMetricsMessage> parse(TaskScheduleMessage taskMessage, String metrics, long startTime,
			long endTime) {
		Map<String, Object> sreTags = Maps.newHashMap();
		sreTags.put(EXEC_HOST, CLIENT.getHost());
		sreTags.put(EXEC_IP, CLIENT.getIp());
		sreTags.put(TASK_START_TIME, startTime);
		sreTags.put(TASK_END_TIME, endTime);
		sreTags.put(TASK_DURATION, endTime - startTime);
		sreTags.putAll(taskMessage.getLabels());

		List<TaskMetricsMessage> result = Lists.newArrayList();
		for (String line : metrics.split("\n")) {
			TaskMetricsMessage metricsMessage = new TaskMetricsMessage(ClientDetector.CLIENT,
					taskMessage.getExchangeId(), taskMessage.getServerMsgId());
			metricsMessage.setSre(sreTags);
			try {
				doParse(metricsMessage, line);
			} catch (Exception e) {
				logger.error("", e);
				String format = String.format("Bad metrics format, type %s content %s", type().name(), line);
				throw new RuntimeException(format);
			}
			if (metricsMessage.getTimestamp() == null)
				metricsMessage.setTimestamp(System.currentTimeMillis());
			result.add(metricsMessage);
		}
		return result;
	}

	void doParse(TaskMetricsMessage metricsMessage, String metrics);

	default Map<String, Object> parsePropertiesLineToMap(String line) {
		Map<String, Object> result = Maps.newHashMap();
		if (StringUtils.isBlank(line)) {
			return result;
		}
		String[] split = line.split(",");
		for (String token : split) {
			String[] kv = token.split("=");
			String k = kv[0];
			String v = kv[1];
			result.put(k, v);
		}
		return result;
	}

	default String tryGet(String[] arr, int index) {
		if (arr.length > index) {
			return arr[index];
		}
		return null;
	}

}
