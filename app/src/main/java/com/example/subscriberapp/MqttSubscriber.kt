package com.example.subscriberapp

import android.util.Log
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.MqttGlobalPublishFilter
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe
import java.nio.charset.StandardCharsets
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect
import java.time.Duration
import java.util.UUID

interface MessageListener{
    fun onMessageReceived(message: String)
}

class MqttSubscriber(private val activity: MainActivity, private val listener: MessageListener) {
    private var mqttClient: Mqtt5BlockingClient? = null
    private lateinit var databaseHelper: DatabaseHelper
    private var isReceivingMessages = true

    companion object{
        private val TOPIC = "assignment/location"
        private val HOST = "broker-816027622.sundaebytestt.com"
    }

    init {
        databaseHelper = DatabaseHelper(activity)
        connectAndSubscribe()
    }

    private fun connectAndSubscribe() {
        mqttClient = MqttClient.builder()
            .identifier(UUID.randomUUID().toString())
            .useMqttVersion5()
            .serverHost(HOST)
            .serverPort(1883)
            .buildBlocking()

        try {
            // Connect to the broker
            mqttClient!!.connectWith()
                .cleanStart(true) // Start a clean session
                .keepAlive(30) // Set keep-alive interval to 30 seconds
                .sessionExpiryInterval(60) // Set session expiry interval to 60 seconds
                .send() // Execute the connection
            Log.d("MQTT","Connected")
        } catch (e: Exception) {
            Log.e("MQTT", "Error connecting to broker")
        }

        // Build and set up the subscription
        val subscribeMessage = Mqtt5Subscribe.builder()
            .topicFilter(TOPIC)
            .qos(MqttQos.AT_LEAST_ONCE) // QoS level 1 for reliable delivery
            .build()

        // Subscribe to the topic with a message handler
        mqttClient?.subscribe(subscribeMessage)
        Log.d("MQTT", "Subscribed to topic: $TOPIC")

        startReceivingMessages()
    }

    fun startReceivingMessages() {
        Thread {
            try {
                while (isReceivingMessages && mqttClient?.state?.isConnected == true) {
                    Log.d("MQTT", "Waiting for Messages...")
                    // Receive the message (blocks until a message arrives)
                    val message = mqttClient!!.publishes(MqttGlobalPublishFilter.ALL).receive()
                    val payload = message.payload.orElse(null)
                    payload?.let {
                        val messageContent = StandardCharsets.UTF_8.decode(it).toString()
                        Log.d("MQTT", "Message arrived: $messageContent")

                        //notify listener about new message
                        listener.onMessageReceived(messageContent)

                    } ?: Log.e("MQTT", "Received a message with no payload")
                }
            } catch (e: Exception) {
                Log.e("MQTT", "Error receiving messages: ${e.message}")
                reconnect()
            }
        }.start()
    }

    private fun reconnect() {
        try {
            mqttClient?.connect() // Attempt to reconnect
            Log.d("MQTT", "Reconnected to MQTT broker")
        } catch (e: Exception) {
            Log.e("MQTT", "Reconnection failed: ${e.message}")
        }
    }

    fun disconnect() {
        mqttClient?.disconnect()
        Log.d("MQTT", "Disconnected from MQTT broker")
    }
}