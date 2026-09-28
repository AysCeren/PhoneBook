package com.project.contactsdemo.core.config;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.core.HazelcastInstance;
import com.project.contactsdemo.core.properties.HazelcastProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
                .setConnectionTimeout(200);
        return clientConfig;
    }

    @Bean
    public HazelcastInstance trainingInstance(ClientConfig clientConfig) {
        return HazelcastClient.newHazelcastClient(clientConfig);
    }
}