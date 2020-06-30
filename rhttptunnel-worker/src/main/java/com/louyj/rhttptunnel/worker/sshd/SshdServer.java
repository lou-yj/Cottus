package com.louyj.rhttptunnel.worker.sshd;

import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.AsyncAuthException;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.password.PasswordChangeRequiredException;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.shell.InteractiveProcessShellFactory;
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

	@Value("${sshd.server.port:14567}")
	private int port = 14567;

	@Override
	public void afterPropertiesSet() throws Exception {
		sshd = SshServer.setUpDefaultServer();
		sshd.setPort(port);
		sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
//		sshd.setShellFactory(new ProcessShellFactory("/bin/bash -i", new String[] { "/bin/bash", "-i" }));
		sshd.setShellFactory(new InteractiveProcessShellFactory());
//		sshd.setShellFactory(new ProcessShellFactory("/bin/bash -i -l", new String[] { "/bin/bash", "-i", "-l" }));
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
