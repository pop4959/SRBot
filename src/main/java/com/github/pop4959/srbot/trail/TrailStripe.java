package com.github.pop4959.srbot.trail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TrailStripe extends TrailLayer {

    class TrailStripeVertex {
        Vector2 position;
        float lifetime;
        boolean isNewStart;
    }

    public class Segment {
        public int startIndex;
        public int endIndex;
    }

    //settings
    private byte enabled; //0 = never, 1 = always, 2 = only at superspeed, 3 = not at superspeed
    // private String layer; //unused
    private boolean visible;
    private float lifetime;
    private Color color;
    private float opacity;
    private boolean taper;
    private boolean fade;
    // private float fadeOutSpeed; //unused-- likely won't implement since it's honestly uglier than doing nothing
    private float width;
    private Vector2 offset;
    private boolean invertOffset;
    private boolean flipHorizontal;
    private boolean flipVertical;
    private boolean sameSideUp;
    private float noiseAmplitude;
    private float waveAmplitude;
    private float waveFrequency;
    private float wavePhaseOffset;

    private List<TrailStripeVertex> trailVertices;
    private List<Integer> newStartIndices;

    private boolean lastPointNotAdded = true;

    TrailStripe() {
        super(0, (byte)0);
        this.trailVertices = new ArrayList<>();
        this.newStartIndices = new ArrayList<>();

        enabled = 0;
        // layer = "TrailBehindLocalPlayersLayer";
        image = "";
        visible = true;
        lifetime = 2.0f;
        color = new Color(1.0f, 1.0f, 1.0f, 1.0f);
        taper = false;
        fade = false;
        // fadeOutSpeed = 1.0f;
        width = 40.0f;
        offset = new Vector2(0, 0);
        invertOffset = true;
        flipHorizontal = false;
        flipVertical = false;
        sameSideUp = false;
        noiseAmplitude = 0;
        waveAmplitude = 0;
        waveFrequency = 0;
        wavePhaseOffset = 0;
    }

    public String getImage() {
        return image;
    }

    public boolean getIsVisible() {
        return visible;
    }

    public byte getEnabled() {
        return enabled;
    }

    public float noise(float x) {
        return (float)Math.sin(x);
    }

    public void addPoint(Vector2 position, Vector2 velocity, float gameTime) {
        if (enabled == Trail.ENABLED_NEVER) {
            lastPointNotAdded = true;
            return;
        } else if (enabled == Trail.ENABLED_ONLY_SUPERSPEED && Trail.calculateSpeed(velocity) < Trail.SUPERSPEED_THRESHOLD) {
            lastPointNotAdded = true;
            return;
        } else if (enabled == Trail.ENABLED_NOT_SUPERSPEED && Trail.calculateSpeed(velocity) >= Trail.SUPERSPEED_THRESHOLD) {
            lastPointNotAdded = true;
            return;
        } else if (enabled == Trail.ENABLED_ALWAYS && Vector2.length(velocity) < Trail.AFTERIMAGE_THRESHOLD) {
            lastPointNotAdded = true;
            return;
        }

        TrailStripeVertex newVertex = new TrailStripeVertex();
        newVertex.lifetime = this.lifetime;
        newVertex.isNewStart = false;

        if (lastPointNotAdded) {
            newVertex.isNewStart = true;
        }

        lastPointNotAdded = false;

        float sin = this.waveAmplitude
                * (float)Math.sin((this.waveFrequency * gameTime + this.wavePhaseOffset));
        Vector2 normal = Vector2.normalize(velocity);
        Vector2 adjustedOffset = new Vector2(offset.x, -offset.y);
        adjustedOffset.y *= !invertOffset && velocity.x > 0 ? -1 : 1;
        adjustedOffset = Vector2.transform(adjustedOffset, normal);
        normal = new Vector2(normal.y, -normal.x);

        Vector2 adjusted = Vector2.sum(
                position,
                adjustedOffset,
                Vector2.scale(normal, sin));
        newVertex.position = adjusted;

        trailVertices.add(newVertex);
    }

    private Vector2 calculateNormal(int index) {
        Vector2 previous;
        Vector2 next;
        previous = trailVertices.get(Math.max(0, index - 1)).position;
        next = trailVertices.get(Math.min(trailVertices.size() - 1, index + 1)).position;
        if (trailVertices.get(index).isNewStart) {
            previous = trailVertices.get(index).position;
        }
        if (index < trailVertices.size() - 1 && trailVertices.get(index + 1).isNewStart) {
            next = trailVertices.get(index).position;
        }
        Vector2 velocity = Vector2.normalize(Vector2.subtract(next, previous));
        return new Vector2(velocity.y, -velocity.x);
    }

    public void updateVertexBuffer() {
        vertexArray.setSize(trailVertices.size() * 2);
        newStartIndices.clear();

        float length = 0;
        for (int i = 0; i < trailVertices.size() - 1; i++) {
            length += Vector2.distance(trailVertices.get(i).position, trailVertices.get(i + 1).position);
        }

        if (length == 0) {
            return;
        }

        float accumulatedLength = 0;
        for (int i = 0; i < trailVertices.size(); i++) {
            if (trailVertices.get(i).isNewStart) {
                newStartIndices.add(i);
            }
            Vector2 normal = calculateNormal(i);

            float t = accumulatedLength / length;
            accumulatedLength += (i < trailVertices.size() - 1)
                    ? Vector2.distance(trailVertices.get(i).position, trailVertices.get(i + 1).position)
                    : 0;
            float effectiveWidth = this.width;
            if (this.taper) {
                effectiveWidth *= (t);
            }
            float effectiveAlpha = this.color.a * this.opacity;
            if (this.fade) {
                effectiveAlpha *= (t);
            }
            float u = (float)i / (trailVertices.size() - 1);
            if (flipHorizontal) {
                u = 1 - u;
            }
            float v = flipVertical ? 1 : 0;

            if (sameSideUp && normal.y > 0) {
                v = 1 - v;
            }

            TrailStripeVertex vertex = trailVertices.get(i);

            float noise = noise(vertex.position.x);
            Vector2 offset = Vector2.scale(normal, noise * this.noiseAmplitude);

            Vector2 v1 = Vector2.sum(
                    vertex.position,
                    Vector2.scale(normal, effectiveWidth / 2f),
                    offset);

            Vector2 v2 = Vector2.sum(
                    vertex.position,
                    Vector2.scale(normal, -effectiveWidth / 2f),
                    offset);

            Color color = new Color(this.color.r, this.color.g, this.color.b,
                    effectiveAlpha);

            Vertex v1Vertex = new Vertex(v1, color, new Vector2(u, v));
            Vertex v2Vertex = new Vertex(v2, color, new Vector2(u, 1 - v));

            vertexArray.addVertex(v1Vertex);
            vertexArray.addVertex(v2Vertex);
        }
    }

    public int getSegmentCount() {
        return newStartIndices.size();
    }

    public Segment getSegment(int index) {
        if (index >= newStartIndices.size()) {
            throw new IndexOutOfBoundsException();
        }
        Segment segment = new Segment();
        segment.startIndex = newStartIndices.get(index) * 2; //each vertex has 2 vertices in the vertex array
        if (index == newStartIndices.size() - 1) {
            segment.endIndex = vertexArray.getVertexCount();
        } else {
            segment.endIndex = newStartIndices.get(index + 1) * 2;
        }
        return segment;
    }

    @Override
    public void update(float deltaTime, Vector2 position, Vector2 velocity) {
        for (int i = trailVertices.size() - 1; i >= 0; i--) {
            TrailStripeVertex vertex = trailVertices.get(i);
            vertex.lifetime -= deltaTime;
            if (vertex.lifetime <= 0) {
                if (vertex.isNewStart) {
                    //transfer to next vertex
                    if (i + 1 < trailVertices.size()) {
                        trailVertices.get(i + 1).isNewStart = true;
                    }
                }
                trailVertices.remove(i);
            }
        }
        updateVertexBuffer();
    }

    @Override
    public void reset() {
        trailVertices.clear();
        newStartIndices.clear();
        lastPointNotAdded = true;
    }

    @Override
    public void loadFromHashMap(HashMap<String, String> properties) {
        // layer = properties.getOrDefault("Layer", "TrailBehindLocalPlayersLayer");

        switch (properties.getOrDefault("Enabled", "NEVER").toUpperCase()) {
            case "ALWAYS":
                enabled = 1;
                break;
            case "ONLY AT SUPERSPEED":
                enabled = 2;
                break;
            case "NOT AT SUPERSPEED":
                enabled = 3;
                break;
            default:
                enabled = 0;
        }

        order = Byte.parseByte(properties.getOrDefault("Order", "0"));
        image = properties.getOrDefault("Image", "");
        visible = properties.getOrDefault("Visible", "TRUE").equalsIgnoreCase("TRUE");
        lifetime = Float.parseFloat(properties.getOrDefault("LifeTime", "2"));
        color = Color.parse(properties.getOrDefault("Color", "1,1,1"));
        opacity = Float.parseFloat(properties.getOrDefault("Opacity", "1"));
        taper = properties.getOrDefault("Taper", "FALSE").equalsIgnoreCase("TRUE");
        fade = properties.getOrDefault("FadeOut", "FALSE").equalsIgnoreCase("TRUE");
        // fadeOutSpeed = Float.parseFloat(properties.getOrDefault("FadeOut Speed", "1"));
        width = Float.parseFloat(properties.getOrDefault("Size", "40"));
        offset = Vector2.parse(properties.getOrDefault("OffsetVector", "0,0"));
        invertOffset = properties.getOrDefault("Invert Offset", "TRUE").equalsIgnoreCase("TRUE");
        flipHorizontal = properties.getOrDefault("Flip Horizontally", "FALSE").equalsIgnoreCase("TRUE");
        flipVertical = properties.getOrDefault("Flip Vertically", "FALSE").equalsIgnoreCase("TRUE");
        sameSideUp = properties.getOrDefault("Force right side Up", "FALSE").equalsIgnoreCase("TRUE");
        noiseAmplitude = Float.parseFloat(properties.getOrDefault("Noise", "0"));
        waveAmplitude = Float.parseFloat(properties.getOrDefault("Sinewave Amplitude", "0"));
        waveFrequency = Float.parseFloat(properties.getOrDefault("Sinewave Frequency", "0"));
        wavePhaseOffset = Float.parseFloat(properties.getOrDefault("Sine Phase Offset", "0"));
    }
}