let config;

try {
    config = {
        API_BASE_URL: process.env.API_BASE_URL,
        MQTT_BROKER_URL: process.env.MQTT_BROKER_URL,
        MQTT_USERNAME: process.env.MQTT_USERNAME,
        MQTT_PASSWORD: process.env.MQTT_PASSWORD,
    };
} catch (error) {
    config = {
        ZONES: {
            all: null,
            zone_a: 'http://localhost:8081',
            zone_b: 'http://localhost:8082',
        },
        MQTT_BROKER_URL: 'ws://localhost:9001',
        MQTT_USERNAME: '',
        MQTT_PASSWORD: '',
    };
}

export default config;
