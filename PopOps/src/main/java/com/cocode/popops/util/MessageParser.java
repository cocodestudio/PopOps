package com.cocode.popops.util;

import com.cocode.popops.model.Message;
import com.cocode.popops.model.MessageType;
import com.cocode.popops.model.PresentationType;
import com.cocode.popops.model.UpdateMode;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Parse server JSON into Message objects.
 */
public final class MessageParser {
    private MessageParser() {
    }

    public static List<Message> parseMessages(JSONObject res) {
        List<Message> messageList = new ArrayList<>();
        if (res == null) return messageList;

        JSONArray arr = res.optJSONArray("messages");
        if (arr == null) return messageList;

        for (int i = 0; i < arr.length(); i++) {

            JSONObject jsonObject = arr.optJSONObject(i);
            if (jsonObject == null) continue;

            Message message = new Message();

            message.id = jsonObject.optString("id", null);
            message.messageId = jsonObject.optString("messageId", String.valueOf(System.currentTimeMillis()));
            message.title = jsonObject.optString("title", "");
            message.body = jsonObject.optString("body", "");
            message.actionUrl = jsonObject.optString("actionUrl", null);
            message.type = MessageType.fromString(jsonObject.optString("type", null));
            message.presentation = PresentationType.fromString(jsonObject.optString("presentation", null));
            message.topic = jsonObject.optString("topic", null);

            if (message.type == MessageType.APP_UPDATE) {
                message.newAppVersion = jsonObject.optString("newAppVersion", null);
                message.updateMode = UpdateMode.fromString(jsonObject.optString("updateMode", null));
            } else {
                message.targetVersion = jsonObject.optString("targetVersion", null);
            }
            
            messageList.add(message);
        }
        return messageList;
    }
}
