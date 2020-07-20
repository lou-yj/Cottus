package com.louyj.rhttptunnel.server;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteAtomicLong;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteCluster;
import org.apache.ignite.IgniteQueue;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.CollectionConfiguration;
import org.apache.ignite.events.Event;
import org.apache.ignite.lang.IgnitePredicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class IgniteRegistry {

	@Value("${cache.backups:1}")
	private int backups;

	@Autowired
	private Ignite ignite;

	private CollectionConfiguration colCfg;

	@PostConstruct
	public void init() {
		colCfg = new CollectionConfiguration();
		colCfg.setCollocated(true);
		colCfg.setBackups(1);
	}

	public <K, V> IgniteCache<K, V> getOrCreateCache(String name, Class<?>... indexedTypes) {
		return ignite.getOrCreateCache(cacheConfig(name, indexedTypes));
	}

	public <K, V> IgniteCache<K, V> getOrCreateCache(String name, int durationAmount, TimeUnit unit,
			Class<?>... indexedTypes) {
		return ignite.getOrCreateCache(cacheConfig(name, durationAmount, unit, indexedTypes));
	}

	public IgniteAtomicLong atomicLong(String name, long initVal, boolean create) {
		return ignite.atomicLong(name, initVal, create);
	}

	public boolean isMaster() {
		IgniteCluster cluster = ignite.cluster();
		return cluster.forOldest().node().id().equals(cluster.localNode().id());
	}

	public <T> IgniteQueue<T> queue(String name, int cap) {
		return ignite.<T>queue(name, cap, colCfg);
	}

	public void localListen(IgnitePredicate<? extends Event> lsnr, int... types) {
		ignite.events().localListen(lsnr, types);
	}

	public Object localId() {
		return ignite.cluster().localNode().consistentId();
	}

	public Collection<ClusterNode> nodes() {
		return ignite.cluster().nodes();
	}

	public Object oldestId() {
		return ignite.cluster().forOldest().node().consistentId();
	}

	<K, V> CacheConfiguration<K, V> cacheConfig(String name, Class<?>... indexedTypes) {
		CacheConfiguration<K, V> configuration = new CacheConfiguration<K, V>().setName(name).setBackups(backups);
		if (indexedTypes.length > 0) {
			configuration.setIndexedTypes(indexedTypes);
		}
		return configuration;
	}

	@SuppressWarnings("unchecked")
	<K, V> CacheConfiguration<K, V> cacheConfig(String name, int durationAmount, TimeUnit unit,
			Class<?>... indexedTypes) {
		return (CacheConfiguration<K, V>) cacheConfig(name, indexedTypes)
				.setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(new Duration(TimeUnit.DAYS, 10)));
	}

}
