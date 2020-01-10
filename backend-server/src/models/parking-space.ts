export interface IParkingSpaceBounds {
    x: number;
    y: number;
    w: number;
    h: number;
}

export interface IParkingSpace {
    id: number;
    bounds: IParkingSpaceBounds;
    occupied: boolean;
}
