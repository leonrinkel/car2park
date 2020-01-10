import express from "express";
import bodyParser from "body-parser";

import debug from "debug";
const log = debug("server");

import { Car } from "./car";
import { Database } from "./database";

interface IServerOptions {
    host?: string;
    port?: number;
    car: Car;
    database: Database;
}

const DEFAULT_HOST = "0.0.0.0";
const DEFAULT_PORT = 5000;

export class Server {

    private host: string;
    private port: number;

    private car: Car;
    private database: Database;

    private app = express();

    constructor(options: IServerOptions) {
        this.host = options.host || DEFAULT_HOST;
        this.port = options.port || DEFAULT_PORT;

        this.car = options.car;
        this.database = options.database;

        this.mountMiddleware();
        this.mountRoutes();
    }

    private mountMiddleware() {
        this.app.use(bodyParser.json());
    }

    private mountRoutes() {

        this.app.get("/car-position", (req, res) => {
            res
                .status(200)
                .json(this.car.getPosition());
        });

        this.app.get("/parking-spaces", (req, res) => {
            res
                .status(200)
                .json(this.database.getParkingSpaces());
        });

        this.app.put("/parking-space/:id", (req, res) => {
            const id = Number(req.params.id);
            log("PUT /parking-space/:id body", req.body);
            this.database.updateParkingSpace(id, req.body);
            res.status(200).end();
        });

    }

    public listen = () => new Promise<void>((resolve) =>
        this.app.listen(this.port, this.host, () => resolve()));

}
