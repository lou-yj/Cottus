package com.louyj.rhttptunnel.model.message.consts;

import org.apache.commons.lang3.StringUtils;

/**
 *
 * Create at 2020年7月23日
 *
 * @author Louyj
 *
 */
public enum EncryptType {

	RSA, AES, NONE;

	public static EncryptType of(String name) {
		for (EncryptType type : values()) {
			if (StringUtils.equalsIgnoreCase(type.name(), name)) {
				return type;
			}
		}
		return NONE;
	}

}
