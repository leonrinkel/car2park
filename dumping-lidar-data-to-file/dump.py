import paho.mqtt.client as mqtt

car_host = "139.174.25.3"
lidar_topic = "aadc2019/sensors/lidar"

file = open("dump.txt", "wb")

def on_message(mqttc, obj, msg):
    file.write(msg.payload)
    file.flush()

mqttc = mqtt.Client()
mqttc.on_message = on_message
mqttc.connect(car_host)
mqttc.subscribe(lidar_topic)
mqttc.loop_forever()
