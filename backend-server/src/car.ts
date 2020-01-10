import mqtt from "mqtt";

import debug from "debug";
const log = debug("car");

import { ICarPosition } from "./models/car-position";

interface ICarOptions {
    host: string;
    positionTopic?: string;
}

const DEFAULT_POSITION_TOPIC = "aadc2019/sensors/pozyx";

export class Car {

    private host: string;
    private positionTopic: string;

    private client?: mqtt.MqttClient;

    private position: ICarPosition = { x: 0, y: 0 };

    public getPosition() { return this.position; }

    constructor(options: ICarOptions) {
        this.host = options.host;
        this.positionTopic = options.positionTopic || DEFAULT_POSITION_TOPIC;
    }

    public connect = () => new Promise<void>((resolve) => {

        this.client = mqtt.connect(this.host);

        this.client.once("connect", () => {
            log("connect");
            this.client!.subscribe(this.positionTopic);
            resolve();
        });

        this.client.on("message", (topic, _payload) => {
            if (topic === this.positionTopic) {
                const payload = JSON.parse(_payload.toString());

                this.position = {
                    x: payload.position[0],
                    y: payload.position[1],
                };

                log("position", this.position);
            }
        });

    })

}
