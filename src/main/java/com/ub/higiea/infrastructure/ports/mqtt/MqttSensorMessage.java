package com.ub.higiea.infrastructure.ports.mqtt;

import java.util.UUID;

public class MqttSensorMessage {

    private UUID sensorId;
    private int state;

    public MqttSensorMessage(){

    }

    public MqttSensorMessage(UUID sensorId, int state) {
        this.sensorId = sensorId;
        this.state = state;
    }

    public UUID getSensorId() {
        return sensorId;
    }

    public int getState() {
        return state;
    }

}
