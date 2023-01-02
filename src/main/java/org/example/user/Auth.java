package org.example.user;

import lombok.Data;
import org.example.config.Constants;

import java.util.Map;

public class Auth {
    public static String token = "";
    public static Map<String,String> authCredentials = Map.ofEntries(
            Map.entry("email", Constants.Email),
            Map.entry("password", Constants.Password)
    );

    public static Map<String,String> authFailedCredentials = Map.ofEntries(
            Map.entry("email", Constants.Email)
    );

    public static Map<String,String> contentHeaders = Map.ofEntries(
            Map.entry("content-type","application/json")
    );

    public static Map<String,String> authHeaders = Map.ofEntries(
            Map.entry("Authorization", "Bearer" + token),
            Map.entry("Content-Type", "application/json")
    );

    public static Map<String,String> deleteHeaders = Map.ofEntries(Map.entry("Authorization", token), Map.entry("Cookie", "token=" + token));
}
