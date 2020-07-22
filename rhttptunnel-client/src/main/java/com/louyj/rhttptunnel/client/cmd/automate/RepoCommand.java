package com.louyj.rhttptunnel.client.cmd.automate;

import static com.louyj.rhttptunnel.client.ClientDetector.CLIENT;
import static com.louyj.rhttptunnel.model.http.Endpoints.CLIENT_EXCHANGE;
import static com.louyj.rhttptunnel.model.message.consts.CommandGroupType.CORE_ADMIN;
import static com.louyj.rhttptunnel.model.message.consts.CommandGroupType.CORE_REPO_MGR;
import static com.louyj.rhttptunnel.model.message.consts.CommandGroupType.CORE_REPO;

import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;

import com.louyj.rhttptunnel.client.annotation.CommandGroups;
import com.louyj.rhttptunnel.client.cmd.BaseCommand;
import com.louyj.rhttptunnel.model.bean.automate.RepoConfig;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.repo.RepoSetMessage;
import com.louyj.rhttptunnel.model.message.repo.RepoShowMessage;
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

	@CommandGroups({ CORE_REPO, CORE_ADMIN })
	@ShellMethod(value = "show repository infomation")
	@ShellMethodAvailability("serverContext")
	public String repoInfo() {
		RepoShowMessage message = new RepoShowMessage(CLIENT);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

	@CommandGroups({ CORE_REPO_MGR, CORE_ADMIN })
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
		RepoSetMessage message = new RepoSetMessage(CLIENT);
		message.setRepoConfig(repoConfig);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

	@CommandGroups({ CORE_REPO_MGR, CORE_ADMIN })
	@ShellMethod(value = "Update repository files at server side")
	@ShellMethodAvailability("serverContext")
	public String repoUpdate() {
		RepoUpdateMessage message = new RepoUpdateMessage(CLIENT);
		BaseMessage response = messageExchanger.jsonPost(CLIENT_EXCHANGE, message);
		return messagePoller.pollExchangeMessage(response);
	}

}
