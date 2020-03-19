package com.louyj.rhttptunnel.worker;

import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.AsyncAuthException;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.password.PasswordChangeRequiredException;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.shell.ProcessShellFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * Created on 2020年3月19日
 *
 * @author Louyj
 *
 */
@Component
public class SshdServer implements InitializingBean {

	private SshServer sshd;

	@Value("23456")
	private int port;

	@Override
	public void afterPropertiesSet() throws Exception {
		sshd = SshServer.setUpDefaultServer();
		sshd.setPort(14567);
		sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
		sshd.setShellFactory(new ProcessShellFactory(new String[] { "/bin/sh", "-i", "-l" }));
		sshd.setPasswordAuthenticator(new PasswordAuthenticator() {

			@Override
			public boolean authenticate(String username, String password, ServerSession session)
					throws PasswordChangeRequiredException, AsyncAuthException {
				return true;
			}
		});
		sshd.start();
	}

}
