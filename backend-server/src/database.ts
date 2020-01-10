import { IParkingSpace, IParkingSpaceBounds } from "./models/parking-space";

interface IParkingSpaceUpdate {
    bounds?: IParkingSpaceBounds;
    occupied?: boolean;
}

export class Database {

    private parkingSpaces: IParkingSpace[] = [
        { id: 0, bounds: { x: 0, y: 0, w: 0, h: 0 }, occupied: false },
        { id: 1, bounds: { x: 0, y: 0, w: 0, h: 0 }, occupied: false },
        { id: 2, bounds: { x: 0, y: 0, w: 0, h: 0 }, occupied: false },
        { id: 3, bounds: { x: 0, y: 0, w: 0, h: 0 }, occupied: false },
    ];

    constructor() {}

    public getParkingSpaces = () => this.parkingSpaces;

    public updateParkingSpace(id: number, update: IParkingSpaceUpdate) {
        this.parkingSpaces = this.parkingSpaces.map((p) => {
            if (p.id === id) return { ...p, ...update };
            else return p;
        });
    }

}
