package com.louyj.cottus.worker.handler;

import static com.louyj.cottus.worker.ClientDetector.CLIENT;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.List;

import com.louyj.cottus.worker.message.ClientWorkerManager;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.louyj.rhttptunnel.model.bean.worker.MemoryInfo;
import com.louyj.rhttptunnel.model.bean.worker.SystemLoadInfo;
import com.louyj.rhttptunnel.model.bean.worker.VmLoadInfo;
import com.louyj.rhttptunnel.model.bean.worker.Workerload;
import com.louyj.rhttptunnel.model.config.IConfigListener;
import com.louyj.rhttptunnel.model.message.BaseMessage;
import com.louyj.rhttptunnel.model.message.ShowWorkerInfoMessage;
import com.louyj.rhttptunnel.model.message.WorkerInfoMessage;

/**
 *
 * Created on 2020年3月15日
 *
 * @author Louyj
 *
 */
@Component
public class WorkerInfoHandler implements IMessageHandler, IConfigListener {

	private static final String REMOTE_SHUTDOWN_ENABLE = "workerinfo.system.properties";

	@Autowired
	private ClientWorkerManager clientWorkerManager;

	private boolean systemProperties = false;

	@Override
	public Class<? extends BaseMessage> supportType() {
		return ShowWorkerInfoMessage.class;
	}

	@Override
	public List<BaseMessage> handle(BaseMessage message) throws Exception {
		OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
		MemoryMXBean memBean = ManagementFactory.getPlatformMXBean(MemoryMXBean.class);
		RuntimeMXBean rtBean = ManagementFactory.getPlatformMXBean(RuntimeMXBean.class);

		SystemLoadInfo systemLoadInfo = new SystemLoadInfo();
		systemLoadInfo.setName(osBean.getName());
		systemLoadInfo.setVersion(osBean.getVersion());
		systemLoadInfo.setArch(osBean.getArch());
		systemLoadInfo.setAvailableProcessors(osBean.getAvailableProcessors());
		systemLoadInfo.setLoadAverage(osBean.getSystemLoadAverage());

		VmLoadInfo vmLoadInfo = new VmLoadInfo();
		vmLoadInfo.setName(rtBean.getVmName());
		vmLoadInfo.setVendor(rtBean.getVmVendor());
		vmLoadInfo.setUptime(rtBean.getUptime());
		vmLoadInfo.setStartTime(rtBean.getStartTime());
		if (systemProperties) {
			vmLoadInfo.setSystemProperties(rtBean.getSystemProperties());
		}
		vmLoadInfo.setHeapMemoryUsage(MemoryInfo.of(memBean.getHeapMemoryUsage()));
		vmLoadInfo.setNonHeapMemoryUsage(MemoryInfo.of(memBean.getNonHeapMemoryUsage()));

		Workerload workerWorkload = new Workerload();
		workerWorkload.setClientIds(clientWorkerManager.clientIds());
		workerWorkload.setSystemLoadInfo(systemLoadInfo);
		workerWorkload.setVmLoadInfo(vmLoadInfo);

		WorkerInfoMessage workerLoadMessage = new WorkerInfoMessage(CLIENT, message.getExchangeId());
		workerLoadMessage.setWorkload(workerWorkload);
		return Lists.newArrayList(workerLoadMessage);
	}

	@Override
	public List<String> keys() {
		return Lists.newArrayList(REMOTE_SHUTDOWN_ENABLE);
	}

	@Override
	public String value(String clientId, String key) {
		return String.valueOf(systemProperties);
	}

	@Override
	public void onChanged(String clientId, String key, String value) {
		systemProperties = BooleanUtils.toBoolean(value);
	}

}
