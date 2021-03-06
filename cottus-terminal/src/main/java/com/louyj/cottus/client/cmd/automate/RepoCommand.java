package com.louyj.cottus.client.cmd.automate;

import static com.louyj.rhttptunnel.model.http.Endpoints.CLIENT_EXCHANGE;
import static com.louyj.rhttptunnel.model.message.consts.CommandGroupType.CORE_REPO_MGR;

import java.util.Scanner;

import com.louyj.cottus.client.ClientDetector;
import com.louyj.cottus.client.annotation.CommandGroups;
import com.louyj.cottus.client.cmd.BaseCommand;
import org.apache.commons.lang3.StringUtils;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;

import com.louyj.rhttptunnel.model.bean.automate.RepoConfig;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.repo.RepoDescribeMessage;
import com.louyj.rhttptunnel.model.message.repo.RepoSetMessage;
import com.louyj.rhttptunnel.model.message.repo.RepoUpdateMessage;

/**
 *
 * Created on 2020年3月15日
 *
 * @author Louyj
 *
 */
@ShellComponent
public class RepoCommand extends BaseCommand {

	@CommandGroups({ CORE_REPO_MGR })
	@ShellMethod(value = "show repository infomation")
	@ShellMethodAvailability("serverContext")
	public String repoInfo() {
		RepoDescribeMessage message = new RepoDescribeMessage(ClientDetector.CLIENT);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

	@CommandGroups({ CORE_REPO_MGR })
	@SuppressWarnings("resource")
	@ShellMethod(value = "set repository infomation")
	@ShellMethodAvailability("serverContext")
	public String repoSet() {
		Scanner scanner = new Scanner(System.in);
		String url = null;
		String branch = null;
		String user = null;
		String password = null;
		String ruleDirectory = null;
		while (true) {
			System.out.print("Repository URL: > ");
			url = scanner.nextLine();
			if (StringUtils.isBlank(url) || url.matches("^(http|https)://.*?$") == false) {
				System.out.println("Bad format");
			} else {
				break;
			}
		}
		while (true) {
			System.out.print("Repository Branch: > ");
			branch = scanner.nextLine();
			if (StringUtils.isBlank(branch)) {
				System.out.println("Empty Input");
			} else {
				break;
			}
		}
		while (true) {
			System.out.print("Repository User: > ");
			user = scanner.nextLine();
			if (StringUtils.isBlank(user)) {
				System.out.println("Empty Input");
			} else {
				break;
			}
		}
		while (true) {
			System.out.print("Repository Password: > ");
			password = scanner.nextLine();
			if (StringUtils.isBlank(password)) {
				System.out.println("Empty Input");
			} else {
				break;
			}
		}
		while (true) {
			System.out.print("Repository Rule Directory: > ");
			ruleDirectory = scanner.nextLine();
			if (StringUtils.isBlank(ruleDirectory)) {
				System.out.println("Empty Input");
			} else {
				break;
			}
		}
		RepoConfig repoConfig = new RepoConfig();
		repoConfig.setUrl(url);
		repoConfig.setBranch(branch);
		repoConfig.setUsername(user);
		repoConfig.setPassword(password);
		repoConfig.setRuleDirectory(ruleDirectory);
		RepoSetMessage message = new RepoSetMessage(ClientDetector.CLIENT);
		message.setRepoConfig(repoConfig);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

	@CommandGroups({ CORE_REPO_MGR })
	@ShellMethod(value = "Update repository files at server side")
	@ShellMethodAvailability("serverContext")
	public String repoUpdate() {
		RepoUpdateMessage message = new RepoUpdateMessage(ClientDetector.CLIENT);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

}
