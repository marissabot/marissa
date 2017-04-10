package org.marissabot.marissa.modules;

import org.marissabot.libmarissa.Response;
import org.marissabot.libmarissa.model.Context;
import org.marissabot.marissa.modules.bingsearch.BingSearch;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Animate {

    public static void search(Context c, String trigger, Response response) {
        Optional<String> qry = getSearchQuery(trigger);

        if (qry.isPresent()) {
            List<String> results;

            try {
                results = BingSearch.animatedSearch(qry.get());
            } catch (IOException e) {
                response.send("Oops. Some kind of IO error?");
                LoggerFactory.getLogger(Animate.class).error("IO Error on bing animated", e);
                return;
            }

            if (results.isEmpty()) {
                response.send("Sorry.. no results");
            } else {
                // pick a random
                // this is actually pretty annoying as they're sorted by relevance
                int choice = new Random(System.nanoTime()).nextInt(results.size());
                response.send(results.get(choice));
            }

        } else {
            response.send("Sorry I don't really understand");
        }
    }

    protected static Optional<String> getSearchQuery(String trigger) {

        Pattern p = Pattern.compile(".*animate\\s+(me\\s+)?(.*)", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(trigger);

        if (m.matches()) {
            return Optional.of(m.group(2).trim());
        } else {
            return Optional.empty();
        }
    }

}
