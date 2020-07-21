package com.louyj.rhttptunnel.model.config;

import java.util.List;

/**
 *
 * Create at 2020年7月21日
 *
 * @author Louyj
 *
 */
public interface IConfigListener {

	List<String> keys();

	String value(String clientId, String key);

	void onChanged(String clientId, String key, String value);

}
