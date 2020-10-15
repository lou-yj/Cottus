package com.louyj.cottus.server;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCluster;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * Create at 2020年7月20日
 *
 * @author Louyj
 *
 */
@Configuration
public class BeansFactory {

	@Value("${data.dir:/data}")
	private String dataDir;

	@Value("${ignite.localhost.bind:127.0.0.1}")
	private String localhostBind;

	@Bean
	public Ignite ignite() {
		System.setProperty("IGNITE_NO_ASCII", "true");
		System.setProperty("IGNITE_QUIET", "true");
		IgniteConfiguration cfg = new IgniteConfiguration();
		cfg.setWorkDirectory(dataDir + "/ignite");
		cfg.setClientMode(false);
		cfg.setPeerClassLoadingEnabled(true);
		cfg.setLocalHost(localhostBind);
		cfg.setMetricsLogFrequency(0);

		DataStorageConfiguration dscfg = new DataStorageConfiguration();
		dscfg.getDefaultDataRegionConfiguration().setPersistenceEnabled(true);
		cfg.setDataStorageConfiguration(dscfg);
		Ignite ignite = Ignition.start(cfg);
		IgniteCluster cluster = ignite.cluster();
		cluster.active(true);
		cluster.baselineAutoAdjustEnabled(true);
		cluster.baselineAutoAdjustTimeout(10_000);
		return ignite;
	}

}
