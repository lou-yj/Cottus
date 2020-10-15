package com.louyj.cottus.client.handler;

import java.io.PrintStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import com.google.common.collect.Lists;
import com.louyj.rhttptunnel.model.message.BaseMessage;

/**
 *
 * Created on 2020年3月14日
 *
 * @author Louyj
 *
 */
public interface IMessageHandler {

	Class<? extends BaseMessage> supportType();

	void handle(BaseMessage message, PrintStream writer) throws Exception;

	default String formatMap(Map<?, ?> map) {
		List<String> labelsList = Lists.newArrayList();
		for (Entry<?, ?> entry : map.entrySet()) {
			labelsList.add(entry.getKey() + "=" + entry.getValue());
		}
		return StringUtils.join(labelsList, "\n");
	}

	default String formatTime(Long time) {
		return new DateTime(time).toString("yyyy-MM-dd HH:mm:ss");
	}

	default String formatCollection(Collection<String> items) {
		return StringUtils.join(items, "\n");
	}

}
