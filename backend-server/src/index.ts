import { Car } from "./car";
import { Database } from "./database";
import { Server } from "./server";

(async () => {

    const car = new Car({ host: "mqtt://139.174.25.3" });
    const database = new Database();
    const server = new Server({ car, database });

    await car.connect();
    await server.listen();

})();
