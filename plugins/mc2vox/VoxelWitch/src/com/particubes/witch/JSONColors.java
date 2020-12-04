package com.particubes.witch;

import org.json.JSONException;
import org.json.JSONObject;
import ovh.nemesis.cauldron.*;

import java.nio.file.Files;
import java.nio.file.Paths;

public class JSONColors {

    public static Palette getPalette() {
        Palette palette = new Palette();  // Create new palette
        JSONObject jsonObject = getJSON();
        if (jsonObject == null) return palette; // If colors.json is empty, return default palette
        for (int i = 1; i < 256; i++) {
            int cs = Integer.parseInt(jsonObject.getJSONObject("colors").getJSONObject(String.valueOf(i)).getString("color").replace("#", ""), 16); // Convert hexadecimal color string to int
            Color color = new Color(((cs >> 16) & 0xff), ((cs >> 8) & 0xff), (cs & 0xff)); //Convert int to RGB Color
            palette.setColor(i, color); // Add color to palette with index i
        }
        return palette;
    }

    public static MaterialList getMaterials() {
        MaterialList list = new MaterialList(); // Create new Material list
        JSONObject jsonObject = getJSON();
        if (jsonObject == null) return list; // If colors.json is empty, return default palette
        for (int i = 1; i < 256; i++) {
            try {
                JSONObject object = jsonObject.getJSONObject("colors").getJSONObject(String.valueOf(i)); // Get JSONObject color
                if (!object.getString("material").equalsIgnoreCase("_diffuse")) { // If material type isn't diffuse (because diffuse is the default type)
                    Material material = new Material(); // Create new voxel material
                    material.setMaterialType(getType(object.getString("material"))); // Get material type with string
                    for (MaterialProperty prop : material.getList()) { // Set property values with config's values
                        try {
                            material.setValue(prop.getKey(), object.getFloat(prop.getKey()));
                        } catch (JSONException ignored) {

                        }
                    }
                    list.setMaterial(i, material); // Add material to material list, with color index i
                }
            } catch (JSONException ignored) {

            }
        }
        return list;
    }

    public static JSONObject getJSON() {
        try {
            return new JSONObject(new String(Files.readAllBytes(Paths.get(Witch.instance.getDataFolder() + "/colors.json")))); // Parse colors.json file
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static MaterialType getType(String string) {
        string = string.toLowerCase().replaceAll("_", ""); //Remove underscores
        switch (string) { // Convert string to MaterialType
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
