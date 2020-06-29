package com.louyj.rhttptunnel.worker;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * Created on 2020年3月26日
 *
 * @author Louyj
 *
 */
@Component
public class ConfigManager {

	@Autowired
	private List<IConfigListener> listeners;

	public String get(String clientId, String key) {
		if (StringUtils.isBlank(key)) {
			StringBuilder buffer = new StringBuilder();
			for (IConfigListener listener : listeners) {
				List<String> keys = listener.keys();
				for (String k : keys) {
					String value = listener.value(clientId, k);
					buffer.append(k + "=" + value + "\n");
				}
			}
			return buffer.toString();
		} else {
			for (IConfigListener listener : listeners) {
				if (listener.keys().contains(key)) {
					return listener.value(clientId, key);
				}
			}
			return "";
		}
	}

	public void set(String clientId, String key, String value) {
		for (IConfigListener listener : listeners) {
			if (listener.keys().contains(key)) {
				listener.onChanged(clientId, key, value);
			}
		}
	}

	public static interface IConfigListener {

		List<String> keys();

		String value(String clientId, String key);

		void onChanged(String clientId, String key, String value);

	}

}