package com.project.contactsdemo.core.config;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientConnectionStrategyConfig.ReconnectMode;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.core.HazelcastInstance;
import com.project.contactsdemo.core.properties.HazelcastProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The application is a Hazelcast <em>client</em>: it connects to a separately running Hazelcast cluster
 * (a container locally and in Docker Compose). All application instances share that cluster's data, which
 * is what makes the cache consistent behind a load balancer. An embedded member would give each instance its
 * own separate cache.
 */
@Configuration
public class HazelcastClientConfig {

    @Bean
    public ClientConfig clientConfig(HazelcastProperties hazelcastProperties){
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setInstanceName("training-instance");
        clientConfig.setClusterName(hazelcastProperties.clusterName());
        ClientNetworkConfig networkConfig = clientConfig.getNetworkConfig();
        networkConfig.addAddress(hazelcastProperties.address());
        networkConfig
                .setRedoOperation(true)
                .setConnectionTimeout(2000);
        // Start and keep running even when the cluster is unreachable: the client connects (and reconnects)
        // in the background, and cache operations fail fast meanwhile, so CacheService falls back to the database.
        // Without this, startup waits until the cluster is reachable.
        clientConfig.getConnectionStrategyConfig()
                .setAsyncStart(true)
                .setReconnectMode(ReconnectMode.ASYNC);
        return clientConfig;
    }

    @Bean
    public HazelcastInstance trainingInstance(ClientConfig clientConfig) {
        return HazelcastClient.newHazelcastClient(clientConfig);
    }
}
