package com.services.APIGateway.services;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenStore {
    private final ConcurrentHashMap<String, String> tokenMap = new ConcurrentHashMap<>();

    public void storeToken(String username, String token) {
        tokenMap.put(username, token);
    }

    public String getToken(String username) {
        return tokenMap.get(username);
    }

    public void clearToken(String username) {
        tokenMap.remove(username);
    }
}