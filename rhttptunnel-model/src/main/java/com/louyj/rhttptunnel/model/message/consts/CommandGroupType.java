package com.louyj.rhttptunnel.model.message.consts;

import java.util.Set;

import com.google.common.collect.Sets;

/**
 *
 * Create at 2020年7月22日
 *
 * @author Louyj
 *
 */
public enum CommandGroupType {

	CORE_UNDEFINED,

	CORE_SUPER_ADMIN,

	CORE_ALLOW_ALL,

	CORE_ADMIN(CORE_SUPER_ADMIN), CORE_NORMAL(CORE_ADMIN, CORE_SUPER_ADMIN),

	CORE_PERM(CORE_NORMAL), CORE_PERM_MGR(CORE_ADMIN),

	CORE_ALARMER(CORE_NORMAL),

	CORE_EXECUTOR(CORE_NORMAL),

	CORE_HANDLER(CORE_NORMAL),

	CORE_REPO(CORE_NORMAL), CORE_REPO_MGR(CORE_ADMIN),

	CORE_SYSTEM(CORE_NORMAL), CORE_SYSTEM_MGR(CORE_ADMIN),

	CORE_CLIENT(CORE_NORMAL), CORE_CLIENT_MGR(CORE_ADMIN),

	CORE_CONFIG(CORE_NORMAL), CORE_CONFIG_MGR(CORE_ADMIN),

	CORE_WORKER(CORE_NORMAL), CORE_WORKER_MGR(CORE_ADMIN),

	CORE_WORKERFS(CORE_NORMAL),

	CORE_USER_MGR(CORE_ADMIN);

	private Set<CommandGroupType> parents;

	private CommandGroupType() {

	}

	private CommandGroupType(CommandGroupType... parents) {
		this.parents = Sets.newHashSet(parents);
	}

	public Set<CommandGroupType> getParents() {
		return parents;
	}

}
