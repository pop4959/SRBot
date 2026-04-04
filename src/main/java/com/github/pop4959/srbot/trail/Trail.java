package com.github.pop4959.srbot.trail;

import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Trail {
    //a lot of this is only used as a target to read to
    private int version;
    private String name;
    private String author;
    private String description;
    private long lastUpdated;
    private String icon;
    private HashMap<String, String> images; //key = image name, value = path in zip file
    private HashMap<String, BufferedImage> loadedImages; //key = image name, value = loaded image
    private Boolean keepDefaultTrail;
    private long workshopId;

    public static final float SUPERSPEED_THRESHOLD = 800.0f;
    public static final float AFTERIMAGE_THRESHOLD = 400.0f;

    public static final int ENABLED_NEVER = 0;
    public static final int ENABLED_ALWAYS = 1;
    public static final int ENABLED_ONLY_SUPERSPEED = 2;
    public static final int ENABLED_NOT_SUPERSPEED = 3;

    public List<TrailLayer> layers;
    private float totalTime;

    public Trail() {
        version = 0;
        name = "";
        author = "";
        description = "";
        lastUpdated = 0;
        icon = "icon";
        images = new HashMap<>();
        loadedImages = new HashMap<>();
        keepDefaultTrail = false;
        workshopId = 0;
        totalTime = 0.0f;

        layers = new ArrayList<TrailLayer>();
    }

    public Trail(String filename) throws IOException {
        images = new HashMap<>();
        loadedImages = new HashMap<>();

        layers = new ArrayList<TrailLayer>();

        readFromFile(filename);
    }

    public HashMap<String, BufferedImage> getLoadedImages() {
        return loadedImages;
    }

    public static float calculateSpeed(Vector2 velocity) {
        return Vector2.length(velocity);
        // return Math.abs(velocity.x);
    }

    public void readFromFile(String filename) throws IOException {
        try (ZipFile zipFile = new ZipFile(filename)) {
            ZipEntry settingsEntry = zipFile.getEntry("settings.trail");
            if (settingsEntry == null) {
                throw new IOException("settings.trail not found in " + filename);
            }
            readSettings(new DataInputStream(zipFile.getInputStream(settingsEntry)), zipFile);
        } catch (IOException e) {
            throw new IOException("Failed to read trail file: " + filename, e);
        }
    }

    public void readFromFile(File file) throws IOException {
        try (ZipFile zipFile = new ZipFile(file)) {
            ZipEntry settingsEntry = zipFile.getEntry("settings.trail");
            if (settingsEntry == null) {
                throw new IOException("settings.trail not found in " + file.getName());
            }
            readSettings(new DataInputStream(zipFile.getInputStream(settingsEntry)), zipFile);
        } catch (IOException e) {
            throw new IOException("Failed to read trail file: " + file.getName(), e);
        }
    }

    public void addPoint(Vector2 position, Vector2 velocity) {
        for (TrailLayer layer : layers) {
            if (layer.getLayerType() == 0) {
                TrailStripe stripe = (TrailStripe) layer;
                stripe.addPoint(position, velocity, totalTime);
            }
        }
    }

    public void update(float deltaTime, Vector2 position, Vector2 velocity) {
        totalTime += deltaTime;
        for (TrailLayer layer : layers) {
            layer.update(deltaTime, position, velocity);
        }
        addPoint(position, velocity);
    }

    public void reset() {
        totalTime = 0.0f;
        for (TrailLayer layer : layers) {
            layer.reset();
        }
    }

    private void readSettings(DataInputStream in, ZipFile zipFile) throws IOException {
        version = readInt4(in);
        name = readString(in);
        author = readString(in);
        description = readString(in);
        lastUpdated = readLong8(in);
        icon = readString(in);
        int imageCount = readInt4(in);
        images = new HashMap<>();
        for (int i = 0; i < imageCount; i++) {
            String key = readString(in);
            String path = readString(in);
            images.put(key, path);
        }

        for (HashMap.Entry<String, String> entry : images.entrySet()) {
            String imageName = entry.getKey();
            String imagePath = entry.getValue();
            ZipEntry imageEntry = zipFile.getEntry(imagePath);
            if (imageEntry == null) {
                throw new IOException("Image not found in zip: " + imagePath);
            }
            try (DataInputStream imageIn = new DataInputStream(zipFile.getInputStream(imageEntry))) {
                BufferedImage image = javax.imageio.ImageIO.read(imageIn);
                loadedImages.put(imageName, image);
            } catch (IOException e) {
                throw new IOException("Failed to read image: " + imagePath, e);
            }
        }

        int layerCount = readInt4(in);
        for (int i = 0; i < layerCount; i++) {
            byte type = in.readByte(); //0 = stripe, 1 = particle, 2 = animation
            int propertyCount = readInt4(in);
            HashMap<String, String> properties = new HashMap<>();
            for (int j = 0; j < propertyCount; j++) {
                String key = readString(in);
                String value = readString(in);
                properties.put(key, value);
            }

            switch (type) {
                case 0 -> {
                    TrailStripe stripe = new TrailStripe();
                    stripe.loadFromHashMap(properties);
                    layers.add(stripe);
                }
                case 1 -> {
                    TrailParticleEmitter emitter = new TrailParticleEmitter();
                    emitter.loadFromHashMap(properties);
                    if (!loadedImages.containsKey(emitter.getImage())) {
                        continue;
                    }
                    // emitter.imageWidth = loadedImages.get(emitter.image).getWidth();
                    // emitter.imageHeight = loadedImages.get(emitter.image).getHeight();
                    emitter.setImageDimensions(loadedImages.get(emitter.getImage()).getWidth(), loadedImages.get(emitter.getImage()).getHeight());
                    layers.add(emitter);
                }
                case 2 -> {
                    TrailAnimation animation = new TrailAnimation();
                    animation.loadFromHashMap(properties);
                    if (!loadedImages.containsKey(animation.getImage())) {
                        continue;
                    }
                    animation.setImageDimensions(loadedImages.get(animation.getImage()).getWidth(), loadedImages.get(animation.getImage()).getHeight());
                    layers.add(animation);
                }
                default -> throw new IOException("Unknown layer type: " + type);
            }
        }

        sortByOrder();

        //from pop
        if (version >= 2) {
            keepDefaultTrail = in.readByte() == 0;
        }
        if (version >= 3) {
            workshopId = readLong8(in);
        }
    }

    private void sortByOrder() {
        layers.sort((a, b) -> Integer.compare(a.order, b.order));
    }

    private int readInt4(DataInputStream in) throws IOException {
        int b1 = in.readUnsignedByte();
        int b2 = in.readUnsignedByte();
        int b3 = in.readUnsignedByte();
        int b4 = in.readUnsignedByte();
        return (b4 << 24) | (b3 << 16) | (b2 << 8) | b1;
    }

    private long readLong8(DataInputStream in) throws IOException {
        long b1 = in.readUnsignedByte();
        long b2 = in.readUnsignedByte();
        long b3 = in.readUnsignedByte();
        long b4 = in.readUnsignedByte();
        long b5 = in.readUnsignedByte();
        long b6 = in.readUnsignedByte();
        long b7 = in.readUnsignedByte();
        long b8 = in.readUnsignedByte();
        return (b8 << 56) | (b7 << 48) | (b6 << 40) | (b5 << 32) | (b4 << 24) | (b3 << 16) | (b2 << 8) | b1;
    }

    private String readString(DataInputStream in) throws IOException {
        byte length = in.readByte();
        if (length == 0) return "";
        byte[] bytes = new byte[length];
        in.readFully(bytes);
        return new String(bytes, "UTF-8");
    }
}