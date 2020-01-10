import re
import json
import math

import matplotlib.pyplot as plt
import matplotlib.animation as animation

lidar_data = []

with open("dump.txt", "r") as file:
  content = file.read()
  
  pattern = re.compile("\{[^\{\}]+\}")
  for match in pattern.finditer(content):
    lidar_data.append(json.loads(match.group(0)))

print(len(lidar_data))

def lidar_values_to_points(values):
  points_x, points_y = [], []
  
  for value in values:
    angle = value[1] + 90
    radius = value[2]

    x = radius * math.cos(math.radians(angle))
    y = radius * math.sin(math.radians(angle))
    
    points_x.append(x)
    points_y.append(y)
  
  return points_x, points_y

fig = plt.figure()
size = 3000
ax = plt.axes(xlim=(-size/2, size/2), ylim=(0, size))
plot, = ax.plot([], [], "o")

def init():
  plot.set_data([], [])
  return plot,

def animate(i):
  values = lidar_data[i]["values"]
  plot.set_data(lidar_values_to_points(values))
  return plot,

anim = animation.FuncAnimation(fig, animate, init_func=init,
  frames=len(lidar_data), interval=20, blit=True)

anim.save("lidar.gif", writer="pillow")
