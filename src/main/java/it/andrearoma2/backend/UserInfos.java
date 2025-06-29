package it.andrearoma2.backend;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class UserInfos {
    public static String username(File file) throws Exception {
        try (FileReader reader = new FileReader(file)) {
            Object o = new JSONParser().parse(reader);
            JSONObject j = (JSONObject) o;
            return (String) j.get("displayName");
        }
    }
    public static URL accountImage(File file) throws Exception {
        try (FileReader reader = new FileReader(file)) {
            Object o = new JSONParser().parse(reader);
            JSONObject j = (JSONObject) o;
            JSONObject accountImage = (JSONObject) j.get("accountImage");
            String imageUrl = (String) accountImage.get("url");
            return URI.create(imageUrl).toURL();
        }

    }
    public static URL accountUrl(File file) throws Exception {
        try (FileReader reader = new FileReader(file)){
            Object o = new JSONParser().parse(reader);
            JSONObject j = (JSONObject) o;
            String username = (String) j.get("accountUrl");
            return URI.create(username).toURL();
        }
    }
}
