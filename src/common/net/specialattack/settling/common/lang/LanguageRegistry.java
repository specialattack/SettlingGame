
package net.specialattack.settling.common.lang;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.IllegalFormatException;
import java.util.TreeMap;

public final class LanguageRegistry {

    private static TreeMap<String, String> entries;

    public static void loadLang(String language) {
        BufferedReader reader = openResource("/lang/" + language + ".lang");

        entries = new TreeMap<String, String>();

        if (reader != null) {
            String line = "";

            try {
                while ((line = reader.readLine()) != null) {
                    if (!line.startsWith("#")) {
                        String[] split = line.split("=", 2);
                        if (split.length == 2) {
                            entries.put(split[0], split[1]);
                        }
                    }
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static BufferedReader openResource(String path) {
        URL url = LanguageRegistry.class.getResource(path);
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return reader;
    }

    public static String translate(String key) {
        if (entries == null) {
            return "No language loaded";
        }
        return entries.get(key);
    }

    public static String translate(String key, Object... args) {
        if (entries == null) {
            return "No language loaded";
        }
        try {
            return String.format(entries.get(key), args);
        }
        catch (IllegalFormatException e) {
            return "Illegal key: " + entries.get(key);
        }
    }

}
