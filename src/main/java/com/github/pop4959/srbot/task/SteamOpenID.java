package com.github.pop4959.srbot.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pop4959.srbot.Main;
import com.github.pop4959.srbot.data.Data;
import com.github.pop4959.srbot.models.Ranking;
import com.github.pop4959.srbot.util.Utils;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import org.glassfish.jersey.internal.util.ExceptionUtils;

import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Path("/account")
public class SteamOpenID {

    private static LinkedHashMap<String, String> LANGUAGE = Data.config().getLanguage();
    private static final Map<Integer, Long> leagues = new HashMap<>();

    static {
        List<Long> roleValues = Data.config().getRoleIds();
        for (int i = 0; i < 9; ++i) {
            leagues.put(i, roleValues.get(i));
        }
    }

    @Path("/verify")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response requestIndirect(@QueryParam("context") String context, @QueryParam("snowflake") String snowflake) {
        return request(context, snowflake);
    }

    @Path("/verify")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response request(@QueryParam("context") String context, @QueryParam("snowflake") String snowflake) {
        URI uri = URI.create(Data.config().getWeb().getSteamLogin());
        return Response
                .temporaryRedirect(uri)
                .cookie(new NewCookie("context", context))
                .cookie(new NewCookie("snowflake", snowflake))
                .build();
    }

    @Path("/confirm")
    @GET
    public String response(@CookieParam("context") int context, @CookieParam("snowflake") String snowflake, @QueryParam("openid.claimed_id") String claimedId) {
        if (context != snowflake.hashCode()) {
            return "An error occurred.";
        }
        try {
            String id = Utils.resolveID64(null, claimedId);
            ObjectMapper mapper = new ObjectMapper();
            Ranking ranking = mapper.readValue(Utils.getJson(Utils.getConnection(new URL(LANGUAGE.get("apiRank") + id))), Ranking.class);
            Guild guild = Main.getJda().getGuildById(Data.config().getServers().getMain());
            Member member = guild.getMemberById(snowflake);
            boolean updatedRank = AutoRank.manageRankRoles(guild, member, leagues.get(ranking.getTier()));
            if (updatedRank) {
                guild.getTextChannelById(Data.config().getMainChannel()).sendMessage(String.format("%s %s!",
                        member.getAsMention(),
                        "verified their account and updated their discord role")).queue();
            }
            return "Your account has been successfully verified!";
        } catch (Exception e) {
            return "An error occurred. Please report this to a bot developer.\n\n" +
                    ExceptionUtils.exceptionStackTraceAsString(e);
        }
    }
}
