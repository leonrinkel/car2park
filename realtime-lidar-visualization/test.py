import json
import math

import paho.mqtt.client as mqtt

from multiprocessing import Process, Manager, Value

import matplotlib.pyplot as plt

car_host = "139.174.25.3"
lidar_topic = "aadc2019/sensors/lidar"

def on_log(string):
    print(string)

def on_message(lidar_data, msg):
    payload = json.loads(msg.payload)
    lidar_data[:] = payload["values"]

def process_1_fn(lidar_data):
    mqttc = mqtt.Client()
    mqttc.on_log = lambda mqttc, obj, level, string: on_log(string)
    mqttc.on_message = lambda mqttc, obj, msg: on_message(lidar_data, msg)
    mqttc.connect(car_host)
    mqttc.subscribe(lidar_topic)
    mqttc.loop_forever()

def process_2_fn(lidar_data):
    fig = plt.figure()

    ax = fig.add_subplot(111)
    ax.set_xlim((-3000, 3000))
    ax.set_ylim((-3000, 3000))

    ln, = ax.plot([], [], "o")

    fig.canvas.draw()
    plt.show(block=False)

    while True:
        points_x, points_y = [], []

        for point in lidar_data:
            angle = point[1]
            radius = point[2]
            x = radius * math.cos(math.radians(angle))
            y = radius * math.sin(math.radians(angle))
            points_x.append(x)
            points_y.append(y)

        #zoom = 1500
        #fig.axis((-zoom, zoom, -zoom, zoom))

        #fig.plot(points_x, points_y, "o")

        ln.set_xdata(points_x)
        ln.set_ydata(points_y)

        fig.canvas.draw()
        plt.pause(0.01)

if __name__ == "__main__":
    manager = Manager()
    lidar_data = manager.list()

    process_1 = Process(target=process_1_fn, args=(lidar_data,))
    process_2 = Process(target=process_2_fn, args=(lidar_data,))

    process_1.start()
    process_2.start()

    process_1.join()
    process_2.join()
