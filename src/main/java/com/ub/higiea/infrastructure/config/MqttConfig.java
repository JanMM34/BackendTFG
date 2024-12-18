package com.ub.higiea.infrastructure.config;

import com.ub.higiea.infrastructure.ports.mqtt.MqttMessageListener;
import jakarta.annotation.PostConstruct;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MqttConfig {

    private final MqttMessageListener mqttMessageListener;

    private final String clientId;

    @Value("${mqtt.broker.url}")
    private String brokerUrl;

    @Value("${mqtt.broker.topic}")
    private String topic;

    @Value("${mqtt.broker.username}")
    private String username;

    @Value("${mqtt.broker.password}")
    private String password;

    MemoryPersistence persistence;

    public MqttConfig(MqttMessageListener mqttMessageListener) {
        this.mqttMessageListener = mqttMessageListener;
        this.clientId = MqttClient.generateClientId();
        persistence = new MemoryPersistence();
    }

    @PostConstruct
    public void connect() {
        try {

            MqttClient client = new MqttClient(brokerUrl, clientId, persistence);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setUserName(username);
            options.setPassword(password.toCharArray());

            client.setCallback(mqttMessageListener);
            client.connect(options);
            client.subscribe(topic);

        } catch (MqttException e) {
            e.printStackTrace();
            System.err.println("Failed to connect to the broker: " + e.getMessage());
        }
    }

}
