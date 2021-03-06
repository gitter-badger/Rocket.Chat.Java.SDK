package io.rocketchat.core.callback;

import io.rocketchat.common.data.model.ErrorObject;
import io.rocketchat.common.listener.Listener;
import io.rocketchat.core.model.RocketChatMessage;

/**
 * Created by sachin on 22/7/17.
 */

public class MessageListener {
    public interface SubscriptionListener extends Listener {
        void onMessage(String roomId, RocketChatMessage message);
    }

    public interface MessageAckListener extends Listener {
        void onMessageAck(RocketChatMessage message, ErrorObject error);
    }
}
