package com.louyj.rhttptunnel.worker.handler;

import static com.louyj.rhttptunnel.worker.ClientDetector.CLIENT;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.louyj.rhttptunnel.model.message.AckMessage;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ShellMessage;

/**
 *
 * Created on 2020年3月15日
 *
 * @author Louyj
 *
 */
@Component
public class ShellHandler implements IMessageHandler {

	@Value("${work.directory}")
	private String workDirectory;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return ShellMessage.class;
	}

	@Override
	public List<BaseMessage> handle(BaseMessage message) throws Exception {
		ShellMessage shellMessage = (ShellMessage) message;
		File infile = new File(workDirectory, "temp/" + message.getExchangeId() + ".in");
		File outfile = new File(workDirectory, "temp/" + message.getExchangeId() + ".out");
		File resfile = new File(workDirectory, "temp/" + message.getExchangeId() + ".res");
		File errFile = new File(workDirectory, "temp/" + message.getExchangeId() + ".err");
		File scriptfile = new File(workDirectory, "temp/" + message.getExchangeId() + ".sc");

		long reslast = resfile.lastModified();
		long errlast = errFile.lastModified();
		long outlast = outfile.lastModified();
		FileUtils.writeStringToFile(infile, shellMessage.getMessage() + "\n", true);
		if (StringUtils.equals(shellMessage.getMessage(), "exit")) {
			return Lists.newArrayList(AckMessage.cack(CLIENT, message.getExchangeId()));
		}
		while (true) {
			long reslast2 = resfile.lastModified();
			String content = null;
			if (reslast2 > reslast) {
				if (outfile.lastModified() > outlast) {
					content = FileUtils.readFileToString(outfile, "utf8");
				}
				if (errFile.lastModified() > errlast) {
					String content1 = FileUtils.readFileToString(errFile, "utf8");
					if (StringUtils.isNotBlank(content1)) {
						content1 = content1.replace(scriptfile.getAbsolutePath() + ":", "");
						content1 = content1.replaceFirst("\\s*\\d:\\s*eval:\\s*", "");
						if (StringUtils.isBlank(content)) {
							content = "[ERROR] " + content1;
						} else {
							content += "\n[ERROR] " + content1;
						}
					}
				}
				ShellMessage response = new ShellMessage(CLIENT, message.getExchangeId());
				response.setMessage(content);
				return Lists.newArrayList(response);
			} else {
				TimeUnit.MILLISECONDS.sleep(100);
			}
		}

	}

}
