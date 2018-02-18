package com.github.pop4959.srbot.data;

import org.json.JSONPointer;
import org.json.JSONObject;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;

public class Data {

    public static String fromFile(String path) {
        try {
            return new String(Files.readAllBytes(new File(path).toPath()), Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Object asJSON(String path, String resource) {
        JSONPointer.Builder p = JSONPointer.builder();
        String[] tokens = StringUtils.split(resource, ".");
        for (String token : tokens)
            p.append(token);
        return p.build().queryFrom(new JSONObject(Data.fromFile(path)));
    }

    public static Object asJSON(String resource) {
        return asJSON("data/config.json", resource);
    }

    public static String fromJSON(String path, String resource) {
        return asJSON(path, resource).toString();
    }

    public static String fromJSON(String resource) {
        return asJSON("data/config.json", resource).toString();
    }

}
