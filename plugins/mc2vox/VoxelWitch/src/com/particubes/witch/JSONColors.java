package com.particubes.witch;

import org.json.JSONException;
import org.json.JSONObject;
import ovh.nemesis.cauldron.Color;
import ovh.nemesis.cauldron.Material;
import ovh.nemesis.cauldron.MaterialList;
import ovh.nemesis.cauldron.Palette;

import java.nio.file.Files;
import java.nio.file.Paths;

public class JSONColors {

    public static Palette getPalette() {
        Palette palette = new Palette();
        JSONObject jsonObject = getJSON();
        if (jsonObject == null) return palette;
        for (int i = 1; i < 256; i++) {
            int cs = Integer.parseInt(jsonObject.getJSONObject("colors").getJSONObject(String.valueOf(i)).getString("color").replace("#", ""), 16);
            Color color = new Color(((cs >> 16) & 0xff), ((cs >> 8) & 0xff), (cs & 0xff));
            palette.setColor(i, color);
        }
        return palette;
    }


    public static JSONObject getJSON() {
        try {
            return new JSONObject(new String(Files.readAllBytes(Paths.get(Witch.instance.getDataFolder() + "/colors.json"))));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
