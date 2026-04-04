package com.github.pop4959.srbot.trail;

public class Color {
    public float r,g,b,a;

    public Color(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public static Color blend(Color src, Color dst) {
        float outA = src.a + dst.a * (1 - src.a);
        if (outA == 0) return new Color(0, 0, 0, 0);
        float outR = (src.r * src.a + dst.r * dst.a * (1 - src.a)) / outA;
        float outG = (src.g * src.a + dst.g * dst.a * (1 - src.a)) / outA;
        float outB = (src.b * src.a + dst.b * dst.a * (1 - src.a)) / outA;
        return new Color(outR, outG, outB, outA);
    }

    public static Color multiply(Color a, Color b) {
        return new Color(a.r * b.r, a.g * b.g, a.b * b.b, a.a * b.a);
    }

    public int toRGB() {
        int ia = (int)(a * 255.0);
        int ir = (int)(r * 255.0);
        int ig = (int)(g * 255.0);
        int ib = (int)(b * 255.0);
        return (ia << 24) | (ir << 16) | (ig << 8) | ib;
    }

    static public Color parse(String s) {
        String[] parts = s.split(",");
        if (parts.length == 3) {
            return new Color(Float.parseFloat(parts[0]),
                    Float.parseFloat(parts[1]),
                    Float.parseFloat(parts[2]), 1.0f);
        } else if (parts.length == 4) {
            return new Color(Float.parseFloat(parts[0]),
                    Float.parseFloat(parts[1]),
                    Float.parseFloat(parts[2]),
                    Float.parseFloat(parts[3]));
        } else {
            throw new IllegalArgumentException("Invalid color format: " + s);
        }
    }
}