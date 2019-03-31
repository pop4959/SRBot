package com.github.pop4959.srbot.util;

import com.github.pop4959.srbot.data.Data;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;

import java.awt.*;

public class EmbedTemplates {

	private static final Color DEFAULT_COLOR = new Color(
			Data.config().getEmbedColor().getR(),
			Data.config().getEmbedColor().getG(),
			Data.config().getEmbedColor().getB()
	);

	public static EmbedBuilder empty(Guild guild) {
		return (new EmbedBuilder()).setColor(guild == null ? DEFAULT_COLOR : guild.getSelfMember().getColor());
	}

	public static EmbedBuilder title(Guild guild, String title) {
		return empty(guild).setAuthor(null, null, null).setTitle(title);
	}

	public static EmbedBuilder plaintext(Guild guild, String title, String description) {
		return title(guild, title).setDescription(description);
	}

	public static EmbedBuilder points(Guild guild, String description, String name, String authorURL, String iconURL) {
		return plaintext(guild, null, description).setAuthor(name, authorURL, iconURL);
	}

	public static Color getDefaultColor() {
		return DEFAULT_COLOR;
	}

}
