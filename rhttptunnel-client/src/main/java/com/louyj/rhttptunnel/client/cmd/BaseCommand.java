package com.louyj.rhttptunnel.client.cmd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.Availability;

import com.louyj.rhttptunnel.client.ClientSession;
import com.louyj.rhttptunnel.client.MessagePoller;
import com.louyj.rhttptunnel.model.bean.RoleType;
import com.louyj.rhttptunnel.model.http.MessageExchanger;

/**
 *
 * Create at 2020年6月30日
 *
 * @author Louyj
 *
 */
public abstract class BaseCommand {

	@Autowired
	protected ClientSession session;

	@Autowired
	protected MessagePoller messagePoller;

	@Autowired
	protected MessageExchanger messageExchanger;

	public Availability workerAdminContext() {
		if (checkPermission(RoleType.ADMIN) && session.inWorkerMode()) {
			return Availability.available();
		}
		return Availability.unavailable("of bad context");
	}

	public Availability workerContext() {
		if (checkPermission(RoleType.NORMAL) && session.inWorkerMode()) {
			return Availability.available();
		}
		return Availability.unavailable("of bad context");
	}

	public Availability serverAdminContext() {
		if (checkPermission(RoleType.ADMIN) && session.inServerMode()) {
			return Availability.available();
		}
		return Availability.unavailable("of bad context");
	}

	public Availability serverContext() {
		if (checkPermission(RoleType.NORMAL) && session.inServerMode()) {
			return Availability.available();
		}
		return Availability.unavailable("of bad context");
	}

	public Availability clientAdminContext() {
		if (checkPermission(RoleType.ADMIN) && session.inUnconnectedMode()) {
			return Availability.available();
		}
		return Availability.unavailable("of bad context");
	}

	public Availability clientContext() {
		if (checkPermission(RoleType.NORMAL) && session.inUnconnectedMode()) {
			return Availability.available();
		}
		return Availability.unavailable("of bad context");
	}

	public Availability notWorkerAdminContext() {
		if (checkPermission(RoleType.ADMIN) && session.inWorkerMode() == false) {
			return Availability.available();
		}
		return Availability.unavailable("of bad context");
	}

	public Availability notWorkerContext() {
		if (checkPermission(RoleType.NORMAL) && session.inWorkerMode() == false) {
			return Availability.available();
		}
		return Availability.unavailable("of bad context");
	}

	public Availability connectAdminContext() {
		if (checkPermission(RoleType.ADMIN) && session.inUnconnectedMode() == false) {
			return Availability.available();
		}
		return Availability.unavailable("of bad context");
	}

	public Availability connectContext() {
		if (checkPermission(RoleType.NORMAL) && session.inUnconnectedMode() == false) {
			return Availability.available();
		}
		return Availability.unavailable("of bad context");
	}

	protected boolean checkPermission(RoleType roleType) {
		if (session.getRole().getLevel() < roleType.getLevel()) {
			return false;
		}
		return true;
	}

}
