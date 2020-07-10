package com.louyj.rhttptunnel.server.data;

import java.util.List;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteCluster;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;

import com.louyj.rhttptunnel.server.automation.AlarmHandlerInfo;
import com.louyj.rhttptunnel.server.automation.event.AlarmEvent;

/**
 *
 * Create at 2020年7月1日
 *
 * @author Louyj
 *
 */
public class IgniteTest {

	public static void main(String[] args) {
		IgniteConfiguration cfg = new IgniteConfiguration();
		cfg.setWorkDirectory("d://ignite");
		cfg.setClientMode(false);
		cfg.setPeerClassLoadingEnabled(true);
		cfg.setLocalHost("127.0.0.1");

		DataStorageConfiguration dscfg = new DataStorageConfiguration();
		dscfg.getDefaultDataRegionConfiguration().setPersistenceEnabled(true);
		cfg.setDataStorageConfiguration(dscfg);
		Ignite ignite = Ignition.start(cfg);
		IgniteCluster cluster = ignite.cluster();
		cluster.active(true);

		IgniteCache<Object, Object> cache = ignite.getOrCreateCache(new CacheConfiguration<>().setName("workerlabels")
				.setIndexedTypes(String.class, AlarmHandlerInfo.class, String.class, AlarmEvent.class));

		SqlFieldsQuery sql = new SqlFieldsQuery(
				"SELECT handlerId, alarmGroup, max(alarmTime) filter(where handled=true) FROM AlarmHandlerInfo info,AlarmEvent alarm where alarm.uuid=info.alarmId  and handled=false group by handlerId,alarmGroup having count(handled=false) > 0 and count(handled=true)<=0");

		try (QueryCursor<List<?>> cursor = cache.query(sql)) {
			for (List<?> row : cursor)
				System.out.println("host=" + row.get(0));
		}

		ignite.close();
	}

}
