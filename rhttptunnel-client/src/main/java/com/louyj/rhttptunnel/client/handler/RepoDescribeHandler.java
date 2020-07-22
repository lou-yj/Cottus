package com.louyj.rhttptunnel.client.handler;

import java.io.PrintStream;

import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.client.exception.EndOfMessageException;
import com.louyj.rhttptunnel.client.util.LogUtils;
import com.louyj.rhttptunnel.model.bean.automate.RepoConfig;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.repo.RepoDescribeMessage;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
@Component
public class RepoDescribeHandler implements IMessageHandler {

	@Override
	public Class<? extends BaseMessage> supportType() {
		return RepoDescribeMessage.class;
	}

	@Override
	public void handle(BaseMessage message, PrintStream writer) throws Exception {
		RepoDescribeMessage repoSetMessage = (RepoDescribeMessage) message;
		RepoConfig repoConfig = repoSetMessage.getRepoConfig();
		if (repoConfig == null) {
			LogUtils.printMessage("Repository Not Set", writer);
		} else {
			LogUtils.printMessage(String.format("Repository URL: %s", repoConfig.getUrl()), writer);
			LogUtils.printMessage(String.format("Repository Branch: %s", repoConfig.getBranch()), writer);
			LogUtils.printMessage(String.format("Repository User: %s", repoConfig.getUsername()), writer);
			LogUtils.printMessage(String.format("Repository Rule Directory: %s", repoConfig.getRuleDirectory()),
					writer);
		}
		throw new EndOfMessageException();
	}

}
