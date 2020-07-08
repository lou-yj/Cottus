package com.louyj.rhttptunnel.worker.script.metrics;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.message.server.TaskMetricsMessage;
import com.louyj.rhttptunnel.model.message.server.TaskScheduleMessage.MetricsType;

/**
 * 
 * metrics_name,tag1=xxx,tag2=xxx field1=xxx,field2=xxx timestamp
 *
 * Create at 2020年7月8日
 *
 * @author Louyj
 *
 */
@Component
public class StandardMetricsParser implements IMetricsParser {

	@Override
	public MetricsType type() {
		return MetricsType.STANDARD;
	}

	@Override
	public void doParse(TaskMetricsMessage metricsMessage, String metrics) {
		String[] tokens = metrics.split("\\s+");
		int index = 0;
		String nameAndTags = tryGet(tokens, index++);
		int indexOf = nameAndTags.indexOf(",");
		String name = nameAndTags.substring(0, indexOf);
		String tags = nameAndTags.substring(indexOf + 1);
		String fields = tryGet(tokens, index++);
		String timeStr = tryGet(tokens, index++);
		long timestamp = System.currentTimeMillis();
		if (StringUtils.isNotBlank(timeStr)) {
			timestamp = NumberUtils.toLong(tokens[3]);
		}
		metricsMessage.setName(name);
		metricsMessage.setTags(parsePropertiesLineToMap(tags));
		metricsMessage.setFields(parsePropertiesLineToMap(fields));
		metricsMessage.setTimestamp(timestamp);
	}

}
