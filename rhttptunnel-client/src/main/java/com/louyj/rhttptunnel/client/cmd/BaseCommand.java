package com.louyj.rhttptunnel.client.cmd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.Availability;

import com.louyj.rhttptunnel.client.ClientSession;
import com.louyj.rhttptunnel.client.MessagePoller;
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

	public Availability workerContext() {
		if (session.inWorkerMode()) {
			return Availability.available();
		}
		return Availability.unavailable("of bad context");
	}

	public Availability serverContext() {
		if (session.inServerMode()) {
			return Availability.available();
		}
		return Availability.unavailable("of bad context");
	}

	public Availability clientContext() {
		if (session.inUnconnectedMode()) {
			return Availability.available();
		}
		return Availability.unavailable("of bad context");
	}

	public Availability notWorkerContext() {
		if (session.inWorkerMode() == false) {
			return Availability.available();
		}
		return Availability.unavailable("of bad context");
	}

	public Availability connectContext() {
		if (session.inUnconnectedMode() == false) {
			return Availability.available();
		}
		return Availability.unavailable("of bad context");
	}

}
