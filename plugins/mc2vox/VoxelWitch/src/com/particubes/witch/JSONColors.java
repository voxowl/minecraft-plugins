package com.particubes.witch;

import org.json.JSONException;
import org.json.JSONObject;
import ovh.nemesis.cauldron.*;

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

    public static MaterialList getMaterials() {
        MaterialList list = new MaterialList();
        JSONObject jsonObject = getJSON();
        if (jsonObject == null) return list;
        for (int i = 1; i < 256; i++) {
            try {
                JSONObject object = jsonObject.getJSONObject("colors").getJSONObject(String.valueOf(i));
                if (!object.getString("material").equalsIgnoreCase("_diffuse")) {
                    Material material = new Material();
                    material.setMaterialType(getType(object.getString("material")));
                    for (MaterialProperty prop : material.getList()) {
                        try {
                            material.setValue(prop.getKey(), object.getFloat(prop.getKey()));
                        } catch (JSONException ignored) {

                        }
                    }
                    list.setMaterial(i, material);
                }
            } catch (JSONException ignored) {

            }
        }
        return list;
    }

    public static JSONObject getJSON() {
        try {
            return new JSONObject(new String(Files.readAllBytes(Paths.get(Witch.instance.getDataFolder() + "/colors.json"))));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static MaterialType getType(String string) {
        string = string.toLowerCase().replaceAll("_", "");
        switch (string) {
            case "metal":
                return MaterialType.METAL;
            case "plastic":
                return MaterialType.PLASTIC;
            case "glass":
                return MaterialType.GLASS;
            case "cloud":
                return MaterialType.CLOUD;
            case "emission":
                return MaterialType.EMIT;
            default:
                return MaterialType.DIFFUSE;
        }
    }
}
