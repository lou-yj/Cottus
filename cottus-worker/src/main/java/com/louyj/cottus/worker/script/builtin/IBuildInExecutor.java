package com.louyj.cottus.worker.script.builtin;

import javax.script.Bindings;

/**
 *
 * Create at 2020年7月21日
 *
 * @author Louyj
 *
 */
public interface IBuildInExecutor {

	String name();

	Object execute(Bindings bindings);

}
