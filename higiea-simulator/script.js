import config from './config.js';

let selectedZone = 'all'; // Default to all
const zoneSelect = document.getElementById('zoneSelect');

zoneSelect.addEventListener('change', () => {
    selectedZone = zoneSelect.value;
    const zoneBaseUrl = config.ZONES[selectedZone];

    // Disable "Add New Sensor" button if "All" is selected
    addSensorBtn.disabled = (selectedZone === 'all');

    // Clear existing markers
    clearExistingRoute();
    clearTruckMarker();
    Object.values(sensorMarkers).forEach(marker => map.removeLayer(marker));
    sensorMarkers = {};

    // Reload data based on selected zone
    if (selectedZone !== 'all') {
        loadSensors(zoneBaseUrl);
        loadTrucks(zoneBaseUrl);
    }
});

// Initialize the map
const barcelonaBounds = L.latLngBounds(
    [41.2611, 2.0528],
    [41.4670, 2.2285]
);

const map = L.map('map', {
    center: [41.3851, 2.1734],
    zoom: 14,
    minZoom: 14,
    maxBounds: barcelonaBounds // Restrict map to Barcelona's bounds
});

// Add tile layer (map background)
L.tileLayer('https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png').addTo(map);

// Variables
let isAddingSensor = false;
const addSensorBtn = document.getElementById('addSensorBtn');
const truckSelect = document.getElementById('truckSelect');
let sensorMarkers = {}; // Keyed by sensor ID

// MQTT Client
let mqttClient;
// MQTT Connection Options
const mqttOptions = {
    connectTimeout: 4000,
    clientId: 'higiea_simulator' + Math.random().toString(16).substring(2, 8),
    username: config.MQTT_USERNAME, // Avoid hardcoding credentials
    password: config.MQTT_PASSWORD,
    keepalive: 60,
    clean: true,
    reconnectPeriod: 4000,
};

// Connect to the MQTT broker over WebSocket
function connectMqtt() {
    const brokerUrl = config.MQTT_BROKER_URL
    mqttClient = mqtt.connect(brokerUrl, mqttOptions);

    mqttClient.on('connect', () => {
        console.log('MQTT connected');
    });

    mqttClient.on('error', (err) => {
        console.error('MQTT connection error:', err);
        mqttClient.end();
    });
}

connectMqtt();

// Load sensors and trucks
if (selectedZone !== 'all') {
    const baseUrl = config.ZONES[selectedZone];
    loadSensors(baseUrl);
    loadTrucks(baseUrl);
}

// Event listeners
addSensorBtn.addEventListener('click', () => {
    isAddingSensor = !isAddingSensor;
    addSensorBtn.style.backgroundColor = isAddingSensor ? 'blue' : '';
    if (isAddingSensor) {
        map.on('click', onMapClick);
    } else {
        map.off('click', onMapClick);
    }
});

truckSelect.addEventListener('change', () => {
    const selectedTruckId = truckSelect.value;
    const selectedOption = truckSelect.options[truckSelect.selectedIndex];
    const routeId = selectedOption.dataset.routeId;

    // Clear existing route and truck markers
    clearExistingRoute();
    clearTruckMarker();

    if (routeId && routeId !== 'null') {
        fetchAndDisplayRoute(routeId);
    } else {
        alert('Selected truck does not have an assigned route.');
        loadSensors();
    }
});

// Functions

// Load sensors from backend and add to map
function loadSensors(baseUrl) {
    axios.get(`${baseUrl}/sensors`)
        .then(response => {
            const sensors = response.data;
            sensors.forEach(sensor => {
                addSensorMarker(sensor);
            });
        })
        .catch(error => {
            console.error('Error fetching sensors:', error);
        });
}

function loadTrucks(baseUrl) {
    axios.get(`${baseUrl}/trucks`)
        .then(response => {
            const trucks = response.data;
            populateTruckDropdown(trucks);
        })
        .catch(error => {
            console.error('Error fetching trucks:', error);
        });
}

// Populate the truck dropdown menu
function populateTruckDropdown(trucks) {
    // Clear existing options
    truckSelect.innerHTML = '<option value="" disabled selected>Select a truck</option>';

    trucks.forEach(truck => {
        const option = document.createElement('option');
        option.value = truck.plate;
        option.textContent = `Truck ${truck.plate} (Capacity: ${truck.maxLoadCapacity})`;

        // Store routeId in data attribute
        option.dataset.routeId = truck.routeId;

        if (!truck.routeId) {
            // Disable option if routeId is null
            option.disabled = true;
            option.textContent += ' - No Route';
        }

        truckSelect.appendChild(option);
    });
}

// Add a sensor marker to the map
function addSensorMarker(sensor) {
    const { id, location, containerState } = sensor;
    const latitude = location.x; // Latitude
    const longitude = location.y; // Longitude

    // Determine marker color based on container state
    let markerColor;
    switch (containerState) {
        case 'FULL':
            markerColor = 'red';
            break;
        case 'HALF':
            markerColor = 'orange';
            break;
        case 'EMPTY':
            markerColor = 'green';
            break;
        default:
            markerColor = 'gray'; // Unknown state
    }

    // Create a custom icon
    const sensorIcon = L.divIcon({
        className: 'sensor-marker',
        html: `<div style="background-color: ${markerColor}; width: 12px; height: 12px; border-radius: 50%;"></div>`,
        iconSize: [12, 12],
        popupAnchor: [0, -6],
    });



    // Add marker to the map
    const marker = L.marker([latitude, longitude], {icon: sensorIcon})
        .addTo(map)
        .bindPopup(createSensorPopupContent(id, containerState));

    sensorMarkers[id] = marker;

    return marker
}

function updateSensorMarker(sensorId, newState) {
    const marker = sensorMarkers[sensorId];
    if (marker) {
        // Determine the new marker color based on state
        let markerColor;
        switch (parseInt(newState)) {
            case 0:
                markerColor = 'green';
                break;
            case 50:
                markerColor = 'orange';
                break;
            case 80:
                markerColor = 'red';
                break;
            default:
                markerColor = 'gray'; // Unknown state
        }

        // Update marker's icon dynamically
        const updatedIcon = L.divIcon({
            className: 'sensor-marker',
            html: `<div style="background-color: ${markerColor}; width: 14px; height: 14px; border-radius: 50%;"></div>`,
            iconSize: [14, 14],
            popupAnchor: [0, -7],
        });

        marker.setIcon(updatedIcon);

    } else {
        console.warn(`Marker for sensor ${sensorId} not found.`);
    }
}

// Update sensor state via MQTT message
function updateSensorState(sensorId, newState) {
    const topic = `sensors/${selectedZone}`;
    const message = {
        sensorId: sensorId,
        state: parseInt(newState),
    };

    mqttClient.publish(topic, JSON.stringify(message), err => {
        if (err) {
            console.error('MQTT publish error:', err);
            alert('Failed to update sensor state.');
        } else {
            updateSensorMarker(sensorId, newState);
        }
    });
}

// Create popup content with update button
function createSensorPopupContent(sensorId, containerState) {
    return `
        <div class="sensor-popup">
            <strong>Sensor ID:</strong> ${sensorId}<br>
            <strong>Current State:</strong> ${containerState}<br>
            <div class="state-buttons">
                <button onclick="updateSensorState('${sensorId}', 0)">Set to EMPTY</button>
                <button onclick="updateSensorState('${sensorId}', 50)">Set to HALF</button>
                <button onclick="updateSensorState('${sensorId}', 80)">Set to FULL</button>
            </div>
        </div>
    `;
}

// Handle map click event for adding new sensors
function onMapClick(e) {
    if (!isAddingSensor) return;

    const { lat, lng } = e.latlng;

    const zoneBaseUrl = config.ZONES[selectedZone];

    // Create a sensor create request
    const newSensor = {
        latitude: lat,
        longitude: lng,
        containerState: 'EMPTY' // Default state
    };

    // Send POST request to create a new sensor
    axios.post(`${zoneBaseUrl}/sensors`, newSensor)
        .then(response => {
            const createdSensor = response.data;
            addSensorMarker(createdSensor);
            isAddingSensor = false;
            addSensorBtn.style.backgroundColor = '';
            map.off('click', onMapClick);
        })
        .catch(error => {
            console.error('Error adding sensor:', error);
        });
}

// Fetch and display route by ID
function fetchAndDisplayRoute(routeId) {
    const zoneBaseUrl = config.ZONES[selectedZone];
    axios.get(`${zoneBaseUrl}/routes/${routeId}`)
        .then(response => {
            const geoJson = response.data;
            displayRouteOnMap(geoJson);
        })
        .catch(error => {
            console.error('Error fetching route:', error);
        });
}

// Display route on the map
function displayRouteOnMap(routeData) {
    // Remove existing route layer if any
    clearExistingRoute();
    // Create a layer group to hold all route-related layers
    map.routeLayer = L.layerGroup().addTo(map);

    // Iterate over each feature in the GeoJSON
    routeData.features.forEach(feature => {
        const { type, properties, geometry } = feature;

        if (geometry.type === 'Point') {
            const coordinates = geometry.coordinates;
            const latitude = coordinates[1]; // GeoJSON uses [lon, lat]
            const longitude = coordinates[0];

            if (properties.type === 'depot') {
                // Add depot marker
                addDepotMarker(latitude, longitude);
            } else if (properties.sensorId) {
                // Add sensor marker
                addSensorMarkerOnRoute(latitude, longitude, properties);
            }
        } else if (geometry.type === 'LineString') {
            // Add route line
            const routeLine = L.geoJSON(feature, {
                style: {
                    color: 'blue',
                    weight: 4,
                    opacity: 0.7
                }
            }).addTo(map.routeLayer);
        }
    });
}

function addDepotMarker(latitude, longitude) {
    // Create a custom icon for the depot
    const depotIcon = L.divIcon({
        className: 'depot-marker',
        html: `<div style="background-color: purple; width: 16px; height: 16px; border-radius: 50%;"></div>`,
        iconSize: [16, 16],
        popupAnchor: [0, -8],
    });

    // Add marker to the route layer
    const depotMarker = L.marker([latitude, longitude], { icon: depotIcon })
        .addTo(map.routeLayer)
        .bindPopup(`<div class="depot-popup">
                      <strong>Depot</strong>
                    </div>`);
}

function addSensorMarkerOnRoute(latitude, longitude, properties) {
    const { sensorId, containerState } = properties;

    // Determine marker color based on container state
    let markerColor;
    switch (containerState) {
        case 'FULL':
            markerColor = 'red';
            break;
        case 'HALF':
            markerColor = 'orange';
            break;
        case 'EMPTY':
            markerColor = 'green';
            break;
        default:
            markerColor = 'gray'; // Unknown state
    }

    // Create a custom icon
    const sensorIcon = L.divIcon({
        className: 'sensor-marker',
        html: `<div style="background-color: ${markerColor}; width: 14px; height: 14px; border-radius: 50%;"></div>`,
        iconSize: [14, 14],
        popupAnchor: [0, -7],
    });

    const popupContent = createSensorPopupContent(sensorId, containerState);

    L.marker([latitude, longitude], { icon: sensorIcon })
        .addTo(map.routeLayer)
        .bindPopup(popupContent);
}

// Clear existing route from the map
function clearExistingRoute() {
    if (map.routeLayer) {
        map.removeLayer(map.routeLayer);
        map.routeLayer = null;
    }
}

// Clear truck marker from the map
function clearTruckMarker() {
    if (map.truckMarker) {
        map.removeLayer(map.truckMarker);
        map.truckMarker = null;
    }
}

window.updateSensorState = updateSensorState;
