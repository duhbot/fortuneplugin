package org.duh102.duhbot.fortune;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.pircbotx.hooks.*;
import org.pircbotx.hooks.events.*;

import org.duh102.duhbot.functions.*;

public class FortunePlugin extends ListenerAdapter implements ListeningPlugin {
    static final String PREFIX = ".fortune";
    static final String FORTUNE_DIR = "fortunes";
    static final File fortuneDir = new File(FORTUNE_DIR);
    static final File[] fortuneLocations = new File[]{new File("/usr/share" +
            "/games/fortunes"), new File("/usr/share/games/fortunes/off"),
            fortuneDir};
    static final Pattern FORTUNE_COMMAND =
            Pattern.compile("^" + Pattern.quote(PREFIX) + "([ \t]+(?<db>[^ " +
                    "\t]+))?");
    static final Pattern WHITESPACE_REPLACE = Pattern.compile("[ \t\n]+");

    private Map<String, File> fortuneDBMap;

    private static String listFortuneFiles(Map<String, File> fortunes) {
        return fortunes.keySet().stream().sorted()
                .reduce((a, b) -> a + ", " + b).get();
    }

    public static List<String> commandBuilder(File dbFile) {
        List<String> toRet = new ArrayList<>();
        toRet.add("fortune");
        if( dbFile == null ) {
            toRet.add("-s");
            toRet.add("fortunes");
        } else {
            toRet.add(dbFile.toString().replace(".dat", ""));
        }
        return toRet;
    }

    public FortunePlugin() {
        if( !fortuneDir.exists() || !fortuneDir.isDirectory() ) {
            System.err.printf("You can create a directory at %s to put custom" +
                    " fortunes in\n", fortuneDir.getAbsoluteFile().toString());
        }
        for( File fortuneLoc : fortuneLocations) {
            if(!(fortuneLoc.exists() && fortuneLoc.isDirectory())) {
                continue;
            }
            File[] foundFortunes =
                    Arrays.stream(fortuneLoc.listFiles()).filter((file) -> file.getName().endsWith(".dat")).toArray(File[]::new);
            if( foundFortunes.length == 0 ) {
                continue;
            }
            if( fortuneDBMap == null ) {
                fortuneDBMap = new HashMap<>();
            }
            for( File fortuneDB : foundFortunes) {
                fortuneDBMap.put(fortuneDB.getName().replace(".dat", ""),
                        fortuneDB);
            }
        }
        if( fortuneDBMap != null && fortuneDBMap.size() > 0 ) {
            System.err.printf("Found fortune databases %s\n",
                    listFortuneFiles(fortuneDBMap));
        }
    }

    public HashMap<String, String> getHelpFunctions() {
        HashMap<String, String> helpFunctions = new HashMap<String, String>();
        helpFunctions.put(PREFIX, "Responds with a fortune from the unix program 'fortune'");
        if (fortuneDBMap != null && fortuneDBMap.size() > 0) {
            helpFunctions.put(String.format("%s (database)", PREFIX),
                    String.format("Use a specific fortune database, one of: %s",
                            listFortuneFiles(fortuneDBMap)));
        }
        return helpFunctions;
    }

    public String getPluginName() {
        return "fortune";
    }

    public ListenerAdapter getAdapter() {
        return this;
    }

    private String getAFortune() {
        return getAFortune(null);
    }
    private String getAFortune(String db) {
        StringBuilder toRet = new StringBuilder();
        try {
            ProcessBuilder builder =
                    new ProcessBuilder(commandBuilder(fortuneDBMap.get(db)));
            builder.redirectErrorStream(true);
            Process proc = builder.start();
            Reader out = null;
            try {
                out = new InputStreamReader(proc.getInputStream(), "UTF-8");
            } catch (UnsupportedEncodingException uee) {
                System.err.printf("This shouldn't happen, we don't support UTF-8 encoding!");
            }
            char[] buffer = new char[1024];
            int readChars = 0;
            do {
                readChars = out.read(buffer, 0, 1024);
                if (readChars > 0) {
                    toRet.append(buffer, 0, readChars);
                }
            } while (readChars > 0);
        } catch (IOException ioe) {
            System.err.printf("Caught exception: %s", ioe.toString());
        }
        return WHITESPACE_REPLACE.matcher(toRet.toString().trim())
                .replaceAll(" ");
    }

    static String message;

    public void onMessage(MessageEvent event) {
        message = org.pircbotx.Colors.removeFormattingAndColors(event.getMessage()).trim();
        Matcher match = FORTUNE_COMMAND.matcher(message);
        if (match.find()) {
            try {
                String db = match.group("db");
                if(db != null) {
                    if( fortuneDBMap == null || !fortuneDBMap.containsKey(db) ) {
                        throw getException(db);
                    }
                }
                event.respond(getAFortune(db));
            } catch( NotAValidDB navd) {
                event.respond(navd.getMessage());
            }
        }
    }

    private NotAValidDB getException(String invalidDB) {
        String message;
        if(fortuneDBMap != null && fortuneDBMap.size() > 0) {
            message = String.format("Invalid fortune db %s, must choose one " +
                            "of %s", invalidDB,
                    listFortuneFiles(fortuneDBMap));
        } else {
            message = "Cannot select specific db, no custom db loaded";
        }
        return new NotAValidDB(message);
    }

    private class NotAValidDB extends Exception {
        public NotAValidDB(String message) {
            super(message);
        }
        public NotAValidDB(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
