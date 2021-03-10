package com.example.dawnlightclinicalstudy.usecases.service;

import org.json.JSONObject;

public interface ServiceListener {
    void onEvent(String event, JSONObject object);
}
