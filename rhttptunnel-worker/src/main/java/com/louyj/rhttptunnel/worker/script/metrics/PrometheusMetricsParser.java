package com.louyj.rhttptunnel.worker.script.metrics;

import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.message.server.TaskMetricsMessage;
import com.louyj.rhttptunnel.model.message.server.TaskScheduleMessage.MetricsType;

/**
 * 
 * metrics_name tag1=xxx,tag2=xxx field1=xxx,field2=xxx timestamp
 *
 * Create at 2020年7月8日
 *
 * @author Louyj
 *
 */
@Component
public class PrometheusMetricsParser implements IMetricsParser {

	private Pattern pattern;

	@PostConstruct
	public void init() {
		pattern = Pattern.compile("^(?<name>\\S+?)\\{(?<tags>.*?)\\}\\s+(?<value>\\S+)(\\s+(?<time>\\S+)){0,1}$");
	}

	@Override
	public MetricsType type() {
		return MetricsType.PROMETHEUS;
	}

	@Override
	public void doParse(TaskMetricsMessage metricsMessage, String metrics) {
		Matcher matcher = pattern.matcher(metrics);
		if (matcher.matches() == false) {
			throw new RuntimeException("not prometheus metrics");
		}
		String name = matcher.group("name");
		String tags = matcher.group("tags");
		String value = matcher.group("value");
		String timeStr = matcher.group("time");
		long timestamp = System.currentTimeMillis();
		if (StringUtils.isNotBlank(timeStr)) {
			timestamp = NumberUtils.toLong(timeStr);
		}
		metricsMessage.setName(name);
		metricsMessage.setTags(parsePropertiesLineToMap(tags));
		metricsMessage.setFields(Collections.singletonMap("value", value));
		metricsMessage.setTimestamp(timestamp);
	}

}
