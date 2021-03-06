package io.rocketchat.livechat.callback;

import io.rocketchat.common.data.model.ErrorObject;
import io.rocketchat.common.listener.Listener;
import io.rocketchat.livechat.model.LiveChatMessage;

/**
 * Created by sachin on 9/6/17.
 */

/**
 *  Used to get message, which is returned after SubType to particular room
 */

public class MessageListener  {
    public interface SubscriptionListener extends Listener {
        void onMessage(String roomId, LiveChatMessage object);
        void onAgentDisconnect(String roomId, LiveChatMessage object);
    }
    public interface MessageAckListener extends Listener{
        void onMessageAck(LiveChatMessage object, ErrorObject error);
    }
    public interface OfflineMessageListener extends Listener{
        void onOfflineMesssageSuccess(Boolean success,ErrorObject error);
    }
}
