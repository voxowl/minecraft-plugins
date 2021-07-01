package com.particubes.witch;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class httpRequest {

    public static String uploadVox (File file, String name) {
        String url = "https://" + Witch.instance.getConfig().getString("donjon-hostname") + "/upload/" + name; // URL
        int response = 0; // Response code
        StringBuilder result = new StringBuilder("@UE"); // String Builder with "Unknown Error" as default string
        String boundary = Long.toHexString(System.currentTimeMillis()); // Random Boundary string
        try {
            URLConnection connection = new URL(url).openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            OutputStream outputStream = connection.getOutputStream();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true);

            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(file.getName()).append("\"\r\n");
            writer.append("Content-Type: application/octet-stream\r\n");
            writer.append("\r\n").flush();
            Files.copy(file.toPath(), outputStream);
            outputStream.flush();
            writer.append("\r\n").flush();
            writer.append("--").append(boundary).append("--\r\n").flush();
            response = ((HttpURLConnection) connection).getResponseCode();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String out; // Temp string
            result = new StringBuilder(); // Reset result String Builder
            while ((out = bufferedReader.readLine()) != null) {
                result.append(out); // write line to result stringbuilder
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (response != 200) {
            return "@UE"; // Return "Unknown error"
        }

        if (result.toString().startsWith("Success")) {
            return result.toString().replaceAll("Success", ""); // If upload is valid, return file code
        } else {
            return result.toString(); // Else return result code
        }
    }

}
