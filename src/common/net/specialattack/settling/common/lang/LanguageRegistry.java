
package net.specialattack.settling.common.lang;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.TreeMap;
import java.util.logging.Level;

import net.specialattack.settling.client.util.Settings;
import net.specialattack.settling.common.Settling;

public final class LanguageRegistry {

    private static TreeMap<String, String> entries;
    private static HashMap<String, TreeMap<String, String>> langs = new HashMap<String, TreeMap<String, String>>();
    private static TreeMap<String, String> availableLangs;
    //With this being the standard english

    static {
        BufferedReader reader = openResource("/lang/lang.registery");

        availableLangs = new TreeMap<String, String>();

        if (reader != null) {
            String line = "";

            try {
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.startsWith("#")) {
                        String[] split = line.split("=", 2);
                        if (split.length == 2) {
                            availableLangs.put(split[0].trim(), split[1].trim());
                        }
                    }
                }
            }
            catch (IOException e) {
                Settling.log.log(Level.SEVERE, "Failed reading language registery", e);
            }
            finally {
                try {
                    reader.close();
                }
                catch (IOException e) {}
            }
        }
        Settling.log.log(Level.INFO, "Loaded (" + availableLangs.size() + ") languages");
    }

    public static void loadLang(String language) {
        if (langs.containsKey(language)) {
            entries = langs.get(language);
        }
        else {
            BufferedReader reader = openResource("/lang/" + language + ".lang");

            entries = new TreeMap<String, String>();

            if (reader != null) {
                String line = "";

                try {
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (!line.startsWith("#")) {
                            String[] split = line.split("=", 2);
                            if (split.length == 2) {
                                entries.put(split[0].trim(), split[1].trim());
                            }
                        }
                    }
                }
                catch (IOException e) {
                    Settling.log.log(Level.SEVERE, "Failed reading localization file for language '" + language + "'", e);
                }
                finally {
                    try {
                        reader.close();
                    }
                    catch (IOException e) {}
                }
            }
            // Yay! Cache
            langs.put(language, entries);
        }
        Settings.language.set(language);
    }

    public static int getIndexFromLangName(String name) {
        int i = 0;
        for (String lang : availableLangs.values()) {
            if (lang.equalsIgnoreCase(name)) {
                return i;
            }
            i++;
        }

        return -1;
    }

    public static String getCurrentLanguage() {
        return (String) langs.keySet().toArray()[Settings.language.getIndex()];
    }

    public static TreeMap<String, String> getLanguages() {
        return availableLangs;
    }

    public static boolean hasLanguage(String lang) {
        return availableLangs.containsKey(lang);
    }

    private static BufferedReader openResource(String path) {
        URL url = LanguageRegistry.class.getResource(path);
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
        }
        catch (IOException e) {
            Settling.log.log(Level.SEVERE, "Failed opening resource '" + path + "'", e);
        }

        return reader;
    }

    public static String translate(String key) {
        if (entries == null) {
            return "No language loaded";
        }

        if (!entries.containsKey(key)) {
            return key;
        }

        return entries.get(key);
    }

    public static String translate(String key, Object... args) {
        if (entries == null) {
            return "No language loaded";
        }

        if (!entries.containsKey(key)) {
            return key;
        }
        try {
            for (int i = 0; i < args.length; i++) {
                try {
                    if (entries.containsKey(args[i])) {
                        args[i] = translate((String) args[i]);
                    }
                }
                catch (ClassCastException e) {}
            }

            return String.format(entries.get(key), args);
        }
        catch (IllegalFormatException e) {
            return "Illegal key: " + entries.get(key);
        }
    }

    public static int getCurrentLanguageIndex() {
        return Settings.language.getIndex();
    }

    public static void loadLang(int selectedIndex) {
        if (selectedIndex >= availableLangs.size() && selectedIndex > -1) {
            return;
        }
        String lang = (String) availableLangs.values().toArray()[selectedIndex];
        loadLang(lang);
    }

}
