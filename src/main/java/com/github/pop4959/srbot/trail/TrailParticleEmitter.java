package com.github.pop4959.srbot.trail;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class TrailParticleEmitter extends TrailLayer {
    private List<TrailParticle> particles;
    
    //trail settings
    private byte enabled; //0 = never, 1 = always, 2 = only at superspeed, 3 = not at superspeed
    // private String layer; //unused
    private String image;
    private boolean visible;
    // private boolean isAnimated;
    private byte spriteMode;
    private Vector2 spriteCount;
    private int spriteFPS;
    private float spawnRate; //seconds between spawns
    private int amount;
    private float lifetime;
    private boolean fade;
    private float scale;
    private float scaleSpeed;
    private float scaleVariance;
    private float rotation;
    private float rotatationVariance;
    private float rotationSpeed;
    private float rotationSpeedVariance;
    private boolean rotateWithPlayer;
    private Color color;
    private float alpha;
    private Vector2 offset;
    private Vector2 offsetVariance;
    private float force;
    private float forceVariance;
    private Vector2 direction;
    private Vector2 directionVariance;
    private boolean useWorldAxis;
    private boolean sameSideUp;
    private boolean hasGravity;
    private Vector2 gravityStrength;
    // boolean isBetaTrail;

    private float timeSinceLastSpawn;
    private Random random;
    private int imageWidth;
    private int imageHeight;

    private int sequentialFrameIndex; //for sequential sprite mode

    public TrailParticleEmitter() {
        super(0,(byte)1);

        particles = new java.util.ArrayList<>();
        random = new Random();
        image = "";
        timeSinceLastSpawn = 0;
        sequentialFrameIndex = 0;

        enabled = 0;
        // layer = "ObjectLayer";
        imageWidth = 0;
        imageHeight = 0;
        visible = true;
        // isAnimated = false;
        spriteMode = 0;
        // spriteSize = new Vector2(100, 100);
        spriteCount = new Vector2(1, 1);
        spriteFPS = 30;
        spawnRate = 0.25f;
        amount = 1;
        lifetime = 1.0f;
        fade = true;
        scale = 1.0f;
        scaleSpeed = -1.0f;
        scaleVariance = 0.5f;
        rotation = 0.0f;
        rotatationVariance = 0.0f;
        rotationSpeed = 0.0f;
        rotationSpeedVariance = 0.0f;
        rotateWithPlayer = false;
        color = new Color(1.0f, 1.0f, 1.0f, 1.0f);
        alpha = 1.0f;
        offset = new Vector2(0, 0);
        offsetVariance = new Vector2(0, 0);
        force = 0.0f;
        forceVariance = 0.0f;
        direction = new Vector2(0, 0);
        directionVariance = new Vector2(0, 0);
        useWorldAxis = false;
        sameSideUp = false;
        hasGravity = false;
        gravityStrength = new Vector2(0, 0);
    }

    public TrailParticleEmitter(HashMap<String, String> properties, int imageWidth, int imageHeight) {
        this();
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        loadFromHashMap(properties);
    }

    public String getImage() {
        return image;
    }

    public byte getEnabled() {
        return enabled;
    }

     public boolean getIsVisible() {
         return visible;
     }

    private float randomFloat() {
        return (random.nextFloat() - 0.5f) * 2;
    }

    public void setImageDimensions(int width, int height) {
        this.imageWidth = width;
        this.imageHeight = height;
    }

    public boolean isVisible() {
        return visible;
    }

    public void emit(Vector2 position, Vector2 velocity) {
        float scaleA = scale + randomFloat() * scaleVariance;
        float rotationA = rotation + randomFloat() * rotatationVariance;
        float rotationSpeedA = rotationSpeed + randomFloat() * rotationSpeedVariance;
        float playerRotation = (float)Math.atan2(velocity.y, velocity.x) + (velocity.x < 0 ? (sameSideUp ? (float)Math.PI : -(float)Math.PI/2.0f) : 0);
        float offsetXA = offset.x + randomFloat() * offsetVariance.x;
        float offsetYA = offset.y + randomFloat() * offsetVariance.y;
        float forceA = force + randomFloat() * forceVariance;
        float directionXA = direction.x + randomFloat() * directionVariance.x;
        float directionYA = direction.y + randomFloat() * directionVariance.y;
        Vector2 worldDirection = Vector2.normalize(new Vector2(directionXA, directionYA));
        Vector2 playerDirection = Vector2.normalize(velocity);

        rotationA = (float)Math.toRadians(rotationA);
        rotationSpeedA = (float)Math.toRadians(rotationSpeedA);

        float initialRotation = rotateWithPlayer ? rotationA + playerRotation : rotationA;

        Vector2 initialPosition = new Vector2(position.x + offsetXA, position.y + offsetYA);
        Vector2 initialVelocity = useWorldAxis ? Vector2.scale(worldDirection, forceA) : Vector2.scale(playerDirection, forceA);
        Vector2 initialAcceleration = hasGravity ? gravityStrength : new Vector2(0, 0);

        int frameIndex = 0;
        if (spriteMode == 3) { //sequential, so determine frame index at spawn time
            frameIndex = sequentialFrameIndex;
            sequentialFrameIndex = (sequentialFrameIndex + 1) % (int)(spriteCount.x * spriteCount.y);
        }

        TrailParticle newParticle = new TrailParticle(
            spriteMode,
            (int)spriteCount.x,
            (int)spriteCount.y,
            spriteFPS,
            lifetime,
            fade,
            new Vector2(scaleA, scaleA),
            scaleSpeed,
            initialRotation,
            rotationSpeedA,
            color,
            alpha,
            initialPosition,
            initialVelocity,
            initialAcceleration,
            frameIndex,
            imageWidth,
            imageHeight
        );

        particles.add(newParticle);
    }

    private void update(float deltaTime) {
        for (int i = particles.size() - 1; i >= 0; i--) {
            TrailParticle particle = particles.get(i);
            particle.update(deltaTime);
            if (particle.getRemainingLifetime() <= 0) {
                particles.remove(i);
            } else if (particle.getScale().x <= 0 || particle.getScale().y <= 0) {
                particles.remove(i);
            }
        }

        vertexArray.setSize(particles.size() * 4);
        for (TrailParticle particle : particles) {
            particle.updateVertexBuffer(deltaTime);
            vertexArray.addVertices(particle.getVertices());
        }
    }

    @Override
    public void update(float deltaTime, Vector2 position, Vector2 velocity) {
        timeSinceLastSpawn += deltaTime;
        update(deltaTime);

        if (enabled == Trail.ENABLED_NEVER) return;
        if (enabled == Trail.ENABLED_ONLY_SUPERSPEED && Trail.calculateSpeed(velocity) < Trail.SUPERSPEED_THRESHOLD) {
            return;
        }
        if (enabled == Trail.ENABLED_NOT_SUPERSPEED && Trail.calculateSpeed(velocity) >= Trail.SUPERSPEED_THRESHOLD) {
             return;
        }
         if (enabled == Trail.ENABLED_ALWAYS && Vector2.length(velocity) < Trail.AFTERIMAGE_THRESHOLD) {
             return;
         }

        if (timeSinceLastSpawn >= spawnRate) {
            timeSinceLastSpawn = timeSinceLastSpawn % spawnRate;
            for (int i = 0; i < amount; i++) {
                emit(position, velocity);
            }
        }
    }

    @Override
    public void reset() {
        particles.clear();
        timeSinceLastSpawn = 0;
        sequentialFrameIndex = 0;
    }

    @Override
    public void loadFromHashMap(HashMap<String, String> properties) {
        String enabledString = properties.getOrDefault("Enabled", "NEVER");
        String spriteModeString = properties.getOrDefault("spriteMode", "DEFAULT");

        switch (enabledString.toUpperCase()) {
            case "NEVER" -> enabled = 0;
            case "ALWAYS" -> enabled = 1;
            case "ONLY AT SUPERSPEED" -> enabled = 2;
            case "NOT AT SUPERSPEED" -> enabled = 3;
            default -> throw new IllegalArgumentException("Invalid Enabled value: " + enabledString);
        }

        switch (spriteModeString.toUpperCase()) {
            case "DEFAULT" -> spriteMode = 0;
            case "ANIMATED" -> spriteMode = 1;
            case "RANDOM" -> spriteMode = 2;
            case "SEQUENTIAL" -> spriteMode = 3;
            default -> throw new IllegalArgumentException("Invalid sprite mode value: " + spriteModeString);
        }

        //enabled
        order = Integer.parseInt(properties.getOrDefault("Order", "0"));
        // layer = properties.getOrDefault("Layer", "TrailBehindLocalPlayersLayer");
        image = properties.getOrDefault("Image", "");
        // visible = Boolean.parseBoolean(properties.getOrDefault("Visible", "TRUE"));
        // isAnimated = Boolean.parseBoolean(properties.getOrDefault("isAnimated", "FALSE"));
        //sprite mode
        // spriteSize = Vector2.Parse(properties.getOrDefault("SpriteSize", "100,100"));
        spriteCount = Vector2.parse(properties.getOrDefault("SpriteCount", "1,1"));
        spriteFPS = Integer.parseInt(properties.getOrDefault("FPS", "30"));
        spawnRate = Float.parseFloat(properties.getOrDefault("Spawn Rate", "0.25"));
        amount = Integer.parseInt(properties.getOrDefault("Amount", "1"));
        lifetime = Float.parseFloat(properties.getOrDefault("LifeTime", "1"));
        fade = Boolean.parseBoolean(properties.getOrDefault("FadeOut", "TRUE"));
        scale = Float.parseFloat(properties.getOrDefault("Scale", "1"));
        scaleSpeed = Float.parseFloat(properties.getOrDefault("ScaleSpeed", "-1"));
        scaleVariance = Float.parseFloat(properties.getOrDefault("Scale Variance", "0.5"));
        rotation = Float.parseFloat(properties.getOrDefault("Rotation", "0"));
        rotatationVariance = Float.parseFloat(properties.getOrDefault("Rotation Variance", "0"));
        rotationSpeed = Float.parseFloat(properties.getOrDefault("Rotation Speed", "0"));
        rotationSpeedVariance =
            Float.parseFloat(properties.getOrDefault("Rotation Speed Variance", "0"));
        rotateWithPlayer =
            Boolean.parseBoolean(properties.getOrDefault("Rotate with Player", "FALSE"));
        color = Color.parse(properties.getOrDefault("Color", "1,1,1"));
        alpha = Float.parseFloat(properties.getOrDefault("Opacity", "1"));
        offset = Vector2.parse(properties.getOrDefault("Offset", "0,0"));
        offsetVariance =
            Vector2.parse(properties.getOrDefault("OffsetVariance", "0,0"));
        force = Float.parseFloat(properties.getOrDefault("Force", "0"));
        forceVariance =
            Float.parseFloat(properties.getOrDefault("Force Variance", "0"));
        direction = Vector2.parse(properties.getOrDefault("Direction", "0,0"));
        directionVariance =
            Vector2.parse(properties.getOrDefault("Direction Variance", "0,0"));
        useWorldAxis = Boolean.parseBoolean(properties.getOrDefault("Use World Axis", "false"));
        sameSideUp = Boolean.parseBoolean(properties.getOrDefault("Same Side Up", "false"));
        hasGravity = Boolean.parseBoolean(properties.getOrDefault("hasGravity", "false"));
        gravityStrength = Vector2.parse(properties.getOrDefault("gravity", "0,0"));
        // isBetaTrail = Boolean.parseBoolean(properties.getOrDefault("Is Beta Trail", "false"));
    }
}
