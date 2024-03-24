export default interface Shape {
    x0: number;
    x1: number;
    y0: number;
    y1: number;
    xref?: string;
    yref?: string;
    type?: string;
    fillcolor?: string;
    opacity?: number;
    line: {
        dash?: string;
        width?: string;
        color?: string;
    };
    label?: {
        text?: string;
        font?: {
            size?: number;
            color?: string;
        };
        xanchor?: string;
    };
};
