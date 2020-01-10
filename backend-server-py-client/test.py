import requests
import pprint

BASEURL = "http://RaspberryPi.isse.tu-clausthal.de:5000"

def get_parking_spaces():
    return requests\
        .get(BASEURL + "/parking-spaces")\
        .json()

def put_parking_space(id, data):
    requests\
        .put(BASEURL + "/parking-space/" + str(id), json=data)

# examples

put_parking_space(0, { "occupied": True })
put_parking_space(0, { "bounds": { "x": 1, "y": 2, "w": 3, "h": 4 } })

parking_spaces = get_parking_spaces()

pp = pprint.PrettyPrinter(indent=4)
pp.pprint(parking_spaces)
