package com.louyj.rhttptunnel.server;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCluster;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IgniteRegistry {

	@Value("${data.dir:/data}")
	private String dataDir;

	@Value("${ignite.localhost.bind:127.0.0.1}")
	private String localhostBind;

	@Bean
	public Ignite ignite() {
		IgniteConfiguration cfg = new IgniteConfiguration();
		cfg.setWorkDirectory(dataDir + "/ignite");
		cfg.setClientMode(false);
		cfg.setPeerClassLoadingEnabled(true);
		cfg.setLocalHost(localhostBind);

		DataStorageConfiguration dscfg = new DataStorageConfiguration();
		dscfg.getDefaultDataRegionConfiguration().setPersistenceEnabled(true);
		cfg.setDataStorageConfiguration(dscfg);
		Ignite ignite = Ignition.start(cfg);
		IgniteCluster cluster = ignite.cluster();
		cluster.active(true);
		return ignite;
	}

}
