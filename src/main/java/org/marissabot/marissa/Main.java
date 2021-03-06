package org.marissabot.marissa;

import co.paralleluniverse.fibers.SuspendExecution;

import java.util.Arrays;
import org.marissabot.libmarissa.*;
import org.marissabot.marissa.lib.Persist;
import org.marissabot.marissa.modules.Animate;
import org.marissabot.marissa.modules.Search;
import org.marissabot.marissa.modules.define.Define;
import org.marissabot.marissa.modules.scripting.ScriptEngine;
import org.marissabot.marissa.modules.MiscUtils;
import org.marissabot.marissa.modules.Score;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rocks.xmpp.core.XmppException;

public class Main {
    
    public static void main(String[] args) throws XmppException, SuspendExecution, InterruptedException {

        String username = Persist.load("core", "userid");
        String password = Persist.load("core", "password"); 
        String nickname = Persist.load("core", "nickname");
        final String joinRoom = Persist.load("core", "joinroom");

        Router router = new Router("(?i)@?"+nickname, true);

        router.on(".*time.*", MiscUtils::tellTheTime);
        router.on("selfie", MiscUtils::selfie);
        router.on("ping", MiscUtils::ping);
        router.on("echo.*", MiscUtils::echo);

        //router.on(".*", ScriptEngine::dispatchToAll);

        router.on("define\\s+.*", Define::defineWord);

        router.on("(search|image)\\s+.*", Search::search);
        router.on("animate\\s+.*", Animate::search);

        router.on("[-+]\\d+", Score::scoreChange);
        router.on("score", Score::scores);
        router.whenContains("[-+]\\d+\\s+(?i)@?"+nickname,
                            (c, trigger,response) -> {
                                String noNick = trigger.replaceAll("(?i)@?"+nickname, "");
                                Score.scoreChange(c, noNick, response);
                            });

        Marissa marissa = new Marissa(
            username,
            password,
            nickname,
            Arrays.asList(new String[]{joinRoom})
        );

        Runtime.getRuntime().addShutdownHook(
            new Thread() {
                 @Override
                 public void run() {
                     Logger l = LoggerFactory.getLogger(Main.class);
                     l.debug("Shutdown hook triggered");
                     marissa.disconnect();
                     l.debug("Shutdown hook completed");
                 }
             }
        );

        LoggerFactory.getLogger(Main.class).info("Launching...");

        marissa.activate(router);

    }
    
}
