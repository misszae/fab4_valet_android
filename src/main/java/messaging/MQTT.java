package messaging;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import android.content.Context;
import android.os.AsyncTask;

public class MQTT {
    MqttAndroidClient mqttAndroidClient;

    final String serverUri = "mqtt://0sze9v.messaging.internetofthings.ibmcloud.com";

    String clientId = "test_client02";
    final String subscriptionTopic = "iot-2/evt/status/fmt/json";

    final String publishTopic = "iot-2/evt/status/phone/json";
    final String publishMessage = "Hello World!";

    Context ctx;
    public MQTT(Context ctx) {

        mqttAndroidClient = new MqttAndroidClient(ctx, serverUri, clientId);
        this.ctx = ctx;
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
/*
                if (reconnect) {
                    addToHistory("Reconnected to : " + serverURI);
                    // Because Clean Session is true, we need to re-subscribe
                    subscribeToTopic();
                } else {
                    addToHistory("Connected to: " + serverURI);
                }*/
            }

            @Override
            public void connectionLost(Throwable cause) {
                //addToHistory("The Connection was lost.");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                //addToHistory("Incoming message: " + new String(message.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }
    private class Connect extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setAutomaticReconnect(true);
            mqttConnectOptions.setCleanSession(false);

            try {
                //addToHistory("Connecting to " + serverUri);
                mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                        disconnectedBufferOptions.setBufferEnabled(true);
                        disconnectedBufferOptions.setBufferSize(100);
                        disconnectedBufferOptions.setPersistBuffer(false);
                        disconnectedBufferOptions.setDeleteOldestMessages(false);
                        mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                        subscribeToTopic();
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        //addToHistory("Failed to connect to: " + serverUri);
                    }
                });


            } catch (MqttException ex) {
                ex.printStackTrace();
            }

            return null;
        }
    }
    public void Connect() {
        Connect c = new Connect();
        c.execute();
    }

    public void subscribeToTopic(){
        try {
            mqttAndroidClient.subscribe(subscriptionTopic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    //addToHistory("Subscribed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    //addToHistory("Failed to subscribe");
                }
            });

            // THIS DOES NOT WORK!
            mqttAndroidClient.subscribe(subscriptionTopic, 0, new IMqttMessageListener() {
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    // message Arrived!
                    System.out.println("Message: " + topic + " : " + new String(message.getPayload()));
                }
            });

        } catch (MqttException ex){
            System.err.println("Exception whilst subscribing");
            ex.printStackTrace();
        }
    }


    private class Publish extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            try {
                MqttMessage message = new MqttMessage();
                message.setPayload(publishMessage.getBytes());
                mqttAndroidClient.publish(publishTopic, message);
                //addToHistory("Message Published");
                if(!mqttAndroidClient.isConnected()){
                    //addToHistory(mqttAndroidClient.getBufferedMessageCount() + " messages in buffer.");
                }
            } catch (MqttException e) {
                System.err.println("Error Publishing: " + e.getMessage());
                e.printStackTrace();
            }
            return null;
        }
    }
    public void publishMessage(){
        Publish p = new Publish();
        p.execute();
    }

}
