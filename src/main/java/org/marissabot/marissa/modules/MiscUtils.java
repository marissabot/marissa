package org.marissabot.marissa.modules;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.marissabot.libmarissa.Response;
import org.marissabot.libmarissa.model.Context;
import org.marissabot.marissa.modules.bingsearch.BingSearch;
import org.slf4j.LoggerFactory;


public class MiscUtils {
    
    private MiscUtils() {}
    
    public static void tellTheTime(Context context, String trigger, Response response)
    {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        df.setTimeZone(tz);
        String nowAsISO = df.format(new Date());
        response.send(nowAsISO); // utc iso860 - don't be a heathen
    }

    public static void selfieStrike(Context c, String trigger, Response response) {
        List<String> selfies = fetchSelfies();
        Collections.shuffle(selfies);
        selfies.stream()
                .limit(5)
                .forEach(response::send);
    }

    public static void selfie(Context context, String trigger, Response response)
    {
        List<String> selfies = fetchSelfies();
        int selfieNo = new Random(System.nanoTime()).nextInt(selfies.size());
        response.send(selfies.get(selfieNo));
    }

    private static List<String> fetchSelfies() {
        final String[] fallbackSelfies = {
                "http://aib.edu.au/blog/wp-content/uploads/2014/05/222977-marissa-mayer.jpg",
                "http://i.huffpost.com/gen/882663/images/o-MARISSA-MAYER-facebook.jpg",
                "http://i2.cdn.turner.com/money/dam/assets/130416164248-marissa-mayer-620xa.png",
                "http://wpuploads.appadvice.com/wp-content/uploads/2013/05/marissa-mayer-yahoo-new-c-008.jpg",
                "https://pbs.twimg.com/profile_images/323982494/marissa_new4.jpg",
                "http://media.idownloadblog.com/wp-content/uploads/2015/01/Marissa-Mayer-Yahoo-001.jpg",
                "https://s-media-cache-ak0.pinimg.com/originals/39/87/26/398726bb39ec252e0291c2b4e9e5dd7b.jpg"
        };

        List<String> selfies;
        try {
            selfies = BingSearch.imageSearch("marissa mayer");
        } catch (Exception e) {
            selfies = new ArrayList<>();
            Collections.addAll(selfies, fallbackSelfies);
        }

        return selfies;
    }
    
    public static void ping(Context context, String trigger, Response response)
    {
        response.send("pong");
    }
    

    public static void echo(Context context, String trigger, Response response)
    {
        LoggerFactory.getLogger(MiscUtils.class).info("echoing '" + trigger + "'");
        response.send(trigger.replaceFirst("[^\\s]+\\s+echo\\s+", ""));
    }
    
}
