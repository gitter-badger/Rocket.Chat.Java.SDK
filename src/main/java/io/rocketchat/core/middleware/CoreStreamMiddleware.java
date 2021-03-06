package io.rocketchat.core.middleware;

import io.rocketchat.common.listener.SubscribeListener;
import io.rocketchat.common.listener.TypingListener;
import io.rocketchat.core.callback.MessageListener;
import io.rocketchat.core.model.RocketChatMessage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by sachin on 21/7/17.
 */

public class CoreStreamMiddleware {


    public enum SubType {
        SUBSCRIBEROOMMESSAGE,
        SUBSCRIBEROOMTYPING,
        OTHER
    }


    public static CoreStreamMiddleware middleware=new CoreStreamMiddleware();

    MessageListener.SubscriptionListener subscriptionListener;
    TypingListener typingListener;

    ConcurrentHashMap<String,SubscribeListener> subcallbacks;

    private CoreStreamMiddleware(){
        subcallbacks=new ConcurrentHashMap<>();
    }

    public static CoreStreamMiddleware getInstance(){
        return middleware;
    }

    public void subscribeRoomMessage(MessageListener.SubscriptionListener subscription) {
        this.subscriptionListener = subscription;
    }

    public void subscribeRoomTyping(TypingListener callback){
        typingListener =callback;
    }

    public void createSubCallback(String id, SubscribeListener callback){
        if (callback!=null) {
            subcallbacks.put(id, callback);
        }
    }

    public void processCallback(JSONObject object){
        String s = object.optString("collection");
        JSONArray array=object.optJSONObject("fields").optJSONArray("args");

        switch (parse(s)) {
            case SUBSCRIBEROOMMESSAGE:
                if (subscriptionListener!=null) {
                    RocketChatMessage message = new RocketChatMessage(array.optJSONObject(0));
                    String roomId = object.optJSONObject("fields").optString("eventName");
                    subscriptionListener.onMessage(roomId, message);
                }
                break;
            case SUBSCRIBEROOMTYPING:
                if (typingListener !=null) {
                    typingListener.onTyping(object.optJSONObject("fields").optString("eventName"), array.optString(0), array.optBoolean(1));
                }
                break;
            case OTHER:
                break;
        }

    }

    public void processSubSuccess(JSONObject subObj){
        if (subObj.optJSONArray("subs")!=null) {
            String id = subObj.optJSONArray("subs").optString(0);
            if (subcallbacks.containsKey(id)) {
                SubscribeListener subscribeListener = subcallbacks.remove(id);
                subscribeListener.onSubscribe(true,id);
            }
        }
    }

    public void processUnsubSuccess(JSONObject unsubObj){
        String id= unsubObj.optString("id");
        if (subcallbacks.containsKey(id)) {
            SubscribeListener subscribeListener = subcallbacks.remove(id);
            subscribeListener.onSubscribe(false,id);
        }
    }

    public static SubType parse(String s){
        if (s.equals("stream-room-messages")) {
            return SubType.SUBSCRIBEROOMMESSAGE;
        }else if (s.equals("stream-notify-room")){
            return SubType.SUBSCRIBEROOMTYPING;
        }
        return SubType.OTHER;
    }
}
