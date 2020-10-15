package com.louyj.cottus.server.handler.client;

import static com.louyj.rhttptunnel.model.message.ClientInfo.SERVER;

import java.util.List;

import com.louyj.cottus.server.ServerRegistry;
import com.louyj.cottus.server.auth.UserPermissionManager;
import com.louyj.cottus.server.handler.IClientMessageHandler;
import com.louyj.cottus.server.session.WorkerSession;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.Charsets;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.louyj.rhttptunnel.model.bean.Permission;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ConnectMessage;
import com.louyj.rhttptunnel.model.message.RejectMessage;
import com.louyj.rhttptunnel.model.message.auth.RoleMessage;
import com.louyj.rhttptunnel.model.util.RsaUtils;
import com.louyj.cottus.server.session.ClientSession;

/**
 *
 * Created on 2020年3月16日
 *
 * @author Louyj
 *
 */
@Component
public class ConnectHandler implements IClientMessageHandler {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private UserPermissionManager userManager;
	@Autowired
	private ServerRegistry serverRegistry;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return ConnectMessage.class;
	}

	@Override
	public boolean asyncMode() {
		return false;
	}

	@Override
	public BaseMessage handle(List<WorkerSession> workerSessions, ClientSession clientSession, BaseMessage message)
			throws Exception {
		try {
			ConnectMessage connectMessage = (ConnectMessage) message;
			if (connectMessage.isSuperAdmin()) {
				byte[] bs = RsaUtils.decrypt(Base64.decodeBase64(connectMessage.getPassword()),
						serverRegistry.getSuperPublicKey());
				String cid = new String(bs, Charsets.UTF_8);
				if (StringUtils.equals(cid, message.getClientId())) {
					RoleMessage roleMessage = new RoleMessage(SERVER, message.getExchangeId());
					roleMessage.setPermission(new Permission());
					roleMessage.setSuperAdmin(true);
					clientSession.setSuperAdmin(true);
					clientSession.setUserName(connectMessage.getUser());
					return roleMessage;
				} else {
					logger.warn("Decrypt cid not matched, excpet {} actual {}", message.getClientId(), cid);
				}
			} else {
				Permission permission = userManager.verify(connectMessage.getUser(), connectMessage.getPassword());
				if (permission != null) {
					RoleMessage roleMessage = new RoleMessage(SERVER, message.getExchangeId());
					roleMessage.setPermission(permission);
					clientSession.setSuperAdmin(false);
					clientSession.setUserName(connectMessage.getUser());
					clientSession.setPermission(permission);
					return roleMessage;
				}
			}
		} catch (Exception e) {
			logger.warn("Auth failed", e);
		}
		clientSession.setAesKey(null);
		clientSession.setPublicKey(null);
		return RejectMessage.sreason(message.getExchangeId(), "Auth failed");
	}

}
