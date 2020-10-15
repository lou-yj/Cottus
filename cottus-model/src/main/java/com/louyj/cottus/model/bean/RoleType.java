package com.louyj.cottus.model.bean;

/**
 *
 * Created on 2020年3月20日
 *
 * @author Louyj
 *
 */
public enum RoleType {

	ADMIN(100), NORMAL(50);

	private int level;

	private RoleType(int level) {
		this.level = level;
	}

	public int getLevel() {
		return level;
	}

}
