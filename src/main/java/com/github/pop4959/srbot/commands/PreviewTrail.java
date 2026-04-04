package com.github.pop4959.srbot.commands;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import org.jetbrains.annotations.NotNull;

import com.github.pop4959.srbot.trail.Camera;
import com.github.pop4959.srbot.trail.Color;
import com.github.pop4959.srbot.trail.Path;
import com.github.pop4959.srbot.trail.RendererCpu;
import com.github.pop4959.srbot.trail.Trail;
import com.github.pop4959.srbot.trail.TrailAnimation;
import com.github.pop4959.srbot.trail.TrailLayer;
import com.github.pop4959.srbot.trail.TrailParticleEmitter;
import com.github.pop4959.srbot.trail.TrailStripe;
import com.github.pop4959.srbot.trail.Vector2;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.utils.FileUpload;

public class PreviewTrail extends Command {
    public PreviewTrail() {
        super("preview_trail", "Get a preview of the trail you will get when you use the command");
    }

    private void cpuRender(RendererCpu cpu, Trail trail) {
        for (TrailLayer layer : trail.layers) {
            if (layer.getLayerType() == 0) {
                TrailStripe stripe = (TrailStripe) layer;
                if (stripe.getEnabled() == 0 || !stripe.getIsVisible())
                    continue;
                cpu.setTexture(trail.getLoadedImages().getOrDefault(stripe.getImage(), null));
                for (int i = 0; i < stripe.getSegmentCount(); i++) {
                    TrailStripe.Segment segment = stripe.getSegment(i);
                    cpu.drawTriangleStrip(stripe.vertexArray, segment.startIndex, segment.endIndex);
                }
            } else if (layer.getLayerType() == 1) {
                TrailParticleEmitter emitter = (TrailParticleEmitter) layer;
                if (emitter.getEnabled() == 0 || !emitter.getIsVisible())
                    continue;
                cpu.setTexture(trail.getLoadedImages().getOrDefault(emitter.getImage(), null));
                cpu.drawQuads(emitter.vertexArray);
            } else if (layer.getLayerType() == 2) {
                TrailAnimation animation = (TrailAnimation) layer;
                if (animation.getEnabled() == 0 || !animation.getIsVisible())
                    continue;
                cpu.setTexture(trail.getLoadedImages().getOrDefault(animation.getImage(), null));
                cpu.drawQuads(animation.vertexArray);
            }
        }
    }

    private BufferedImage renderTrail(String filePath, Color clearColor) throws IOException {
        Trail trail = new Trail(filePath);

        Camera camera = new Camera();
        RendererCpu cpu = new RendererCpu(1024, 512);
        cpu.camera = camera;
        camera.position = new Vector2(-512, -256);

        float startTime = 5.2f;
        float endTime = (float) Math.PI * 2.0f - 0.05f;
        int pointCount = 600;
        float timeStep = (endTime - startTime) / (float) pointCount;
        Path pathA = new Path(timeStep);

        float startSpeed = Trail.AFTERIMAGE_THRESHOLD;
        float endSpeed = Trail.SUPERSPEED_THRESHOLD * 1.35f;
        float speedStep = (endSpeed - startSpeed) / (float) pointCount;

        for (int i = 0; i < pointCount; i++) {
            float currentTime = startTime + i * timeStep;
            float currentSpeed = startSpeed + i * speedStep;
            Vector2 position = pathA.infinityPath(currentTime, 430, new Vector2(0, 0), 3.0f);
            Vector2 velocity = pathA.calculateVelocity(position);
            velocity = Vector2.normalize(velocity);
            velocity = Vector2.scale(velocity, currentSpeed);

            trail.update(timeStep, position, velocity);
        }

        cpu.clear(clearColor);
        cpuRender(cpu, trail);
        return cpu.getFramebuffer();
    }

    @Override
    public SlashCommandData getSlashCommand() {
        return super.getSlashCommand()
            .addOption(OptionType.STRING, "message_id", "The message id of the message you want to preview the trail of", true)
            .addOption(OptionType.BOOLEAN, "transparent", "Whether the background should be transparent or not", true);
    }

    @Override
    public void execute(@NotNull SlashCommandInteractionEvent event) {
        String messageId = event.getOption("message_id").getAsString();

        long id;
        try {
            id = Long.parseLong(messageId);
        } catch (NumberFormatException e) {
            event.reply("Invalid message id").setEphemeral(true).queue();
            return;
        }

        event.getChannel().retrieveMessageById(id).queue(
            message -> processMessage(event, message),
            error -> event.reply("Message not found.").setEphemeral(true).queue()
        );
    }

    private void processMessage(SlashCommandInteractionEvent event, Message message) {
        message.getAttachments().stream()
            .filter(a -> a.getFileName().toLowerCase().endsWith(".srt"))
            .findFirst()
            .ifPresentOrElse(
                attachment -> processAttachment(event, attachment),
                () -> event.reply("No .srt attachment found.").setEphemeral(true).queue()
            );
    }

    private void processAttachment(SlashCommandInteractionEvent event, Message.Attachment attachment) {
        java.nio.file.Path tempPath;
        boolean transparent = event.getOption("transparent") != null && event.getOption("transparent").getAsBoolean();
        try {
            tempPath = Files.createTempFile("attachment", ".srt");
            System.out.println("Created temporary file: " + tempPath.toString());
        } catch (IOException e) {
            event.reply("Failed to create temporary file.").setEphemeral(true).queue();
            return;
        }
        attachment.getProxy().downloadToFile(tempPath.toFile()).thenRun(() -> {
            try {
                Color clearColor = transparent ? new Color(0, 0, 0, 0) : new Color(0.2f, 0.2f, 0.2f, 1.0f);
                BufferedImage preview = renderTrail(tempPath.toString(), clearColor);
                
                try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                    ImageIO.write(preview, "png", os);
                    byte[] imageData = os.toByteArray();
                    FileUpload upload = FileUpload.fromData(imageData, "preview.png");
                    ArrayList<MessageEmbed> embeds = new ArrayList<MessageEmbed>();

                    EmbedBuilder embed = new EmbedBuilder()
                            .setImage("attachment://preview.png")
                            .setColor(event.getGuild().getSelfMember().getColor());
                    embeds.add(embed.build());

                    event.replyEmbeds(embeds).addFiles(upload).queue();
                } catch (IOException e) {
                    event.reply("Failed to create temporary image file.").setEphemeral(true).queue();
                    return;
                }

            } catch (IOException e) {
                event.reply("Failed to read trail file.").setEphemeral(true).queue();
            }
        });
    }
}
