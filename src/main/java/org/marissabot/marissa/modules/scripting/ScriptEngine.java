
package org.marissabot.marissa.modules.scripting;

import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.Invocable;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.marissabot.libmarissa.Response;
import org.marissabot.libmarissa.model.Context;

public class ScriptEngine {
    
    private static final javax.script.ScriptEngine engine;
    private static final Invocable invocable;

    static {
        
        engine = new ScriptEngineManager().getEngineByName("nashorn");
        invocable = (Invocable) engine;
        
        try {

            // TODO: Load all resources not just standard.js
            engine.eval(new InputStreamReader(ScriptEngine.class.getResourceAsStream("/standard.js")));
            
        } catch (ScriptException ex) {
            throw new RuntimeException("Nashorn engine failed to load");
        }
        
    }
    
    public static void dispatchToAll(Context context, String in, Response response) {
        
        try {
            invocable.invokeFunction("run", in.replaceFirst("mars", ""), response);
        } catch (ScriptException | NoSuchMethodException ex) {
            Logger.getLogger(ScriptEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

}
