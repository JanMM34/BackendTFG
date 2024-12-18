package com.ub.higiea.infrastructure.ports.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ub.higiea.application.services.MessageService;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MqttMessageListener implements MqttCallback {

    private final MessageService messageService;
    private final ObjectMapper mapper = new ObjectMapper();

    public MqttMessageListener(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public void connectionLost(Throwable throwable) {

    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        readMessage(topic, mqttMessage);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }

    public void readMessage(String topic, MqttMessage mqttMessage) {
        try {
            String payload = new String(mqttMessage.getPayload());

            MqttSensorMessage sensorMessage = mapper.readValue(payload, MqttSensorMessage.class);

            UUID sensorId = sensorMessage.getSensorId();
            int state = sensorMessage.getState();

            messageService.handleMessage(sensorId, state).subscribe();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
