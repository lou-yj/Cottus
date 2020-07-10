package com.louyj.rhttptunnel.worker.script.metrics;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import com.google.common.base.Charsets;
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
@Component
public class FileMetricsCollector implements IMetricsCollector {

	public static final String METRICS_FILE_LOCATION_FORMAT1 = "metrics_file_location";
	public static final String METRICS_FILE_LOCATION_FORMAT2 = "metricsFileLocation";

	@Override
	public MetricsCollectType type() {
		return MetricsCollectType.FILE_METRICS;
	}

	@Override
	public String collect(TaskScheduleMessage taskScheduleMessage, EvalResult evalResult, Map<String, Object> env)
			throws IOException {
		String filePath = MapUtils.getString(env, METRICS_FILE_LOCATION_FORMAT1);
		File file = new File(filePath);
		if (file.exists() == false) {
			throw new RuntimeException("metrics file not found");
		}
		return FileUtils.readFileToString(file, Charsets.UTF_8);
	}

}
