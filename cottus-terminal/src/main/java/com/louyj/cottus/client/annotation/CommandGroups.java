package com.louyj.cottus.client.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.louyj.rhttptunnel.model.message.consts.CommandGroupType;

/**
 *
 * Create at 2020年7月22日
 *
 * @author Louyj
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface CommandGroups {

	CommandGroupType[] value();

}
