package com.louyj.rhttptunnel.server.data;

import java.io.Serializable;
import java.util.List;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteCluster;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.cache.query.annotations.QuerySqlField;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;

import com.louyj.rhttptunnel.server.workerlabel.LabelRule;

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

		IgniteCache<Object, Object> cache = ignite.getOrCreateCache(
				new CacheConfiguration<>().setName("workerlabels").setIndexedTypes(String.class, LabelRule.class));

//		LabelRule rule = new LabelRule();
//		rule.setHost("localhost");
//		rule.setIp("127.0.0.1");
//		rule.getLabels().put("xxx", "xxx");
//		cache.put(rule.identify(), rule);

		System.out.println(">> Created the cache and add the values.");

		SqlFieldsQuery sql = new SqlFieldsQuery("SELECT labels FROM LabelRule");

		// Iterate over the result set.
		try (QueryCursor<List<?>> cursor = cache.query(sql)) {
			for (List<?> row : cursor)
				System.out.println("host=" + row.get(0));
		}

		ignite.close();
	}

	public static class Person implements Serializable {
		/** Indexed field. Will be visible for SQL engine. */
		@QuerySqlField(index = true)
		private long id;

		/** Queryable field. Will be visible for SQL engine. */
		@QuerySqlField
		private String name;

		/** Will NOT be visible for SQL engine. */
		private int age;

		/**
		 * Indexed field sorted in descending order. Will be visible for SQL engine.
		 */
		@QuerySqlField(index = true, descending = true)
		private float salary;
	}

}
