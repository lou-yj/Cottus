package com.louyj.rhttptunnel.client.annotation;

import com.louyj.rhttptunnel.model.message.consts.CommandGroupType;

/**
 *
 * Create at 2020年7月22日
 *
 * @author Louyj
 *
 */
public @interface CommandGroups {

	CommandGroupType[] value();

}
