package com.github.pop4959.srbot.util;

import com.github.pop4959.srbot.data.Data;
import net.dv8tion.jda.core.EmbedBuilder;

import java.awt.*;

public class EmbedTemplates {

    private static final Color DEFAULT_COLOR = new Color(Data.config().getEmbedColor().getR(), Data.config().getEmbedColor().getG(), Data.config().getEmbedColor().getB());

    public static EmbedBuilder empty(String title) {
        return (new EmbedBuilder()).setColor(DEFAULT_COLOR).setAuthor(null, null, null).setTitle(title);
    }

    public static EmbedBuilder plaintext(String title, String description) {
        return empty(title).setDescription(description);
    }

    public static EmbedBuilder points(String description, String name, String authorURL, String iconURL) {
        return plaintext(null, description).setAuthor(name, authorURL, iconURL);
    }

    public static Color getDefaultColor() {
        return DEFAULT_COLOR;
    }

}
