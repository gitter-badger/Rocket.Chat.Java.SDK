package io.rocketchat.livechat.rpc;

import io.rocketchat.common.data.rpc.RPC;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
/**
 * Created by sachin on 9/6/17.
 */

public class LiveChatHistoryRPC extends RPC{


    public static String HISTORY="loadHistory";
    /**
     *
     * @param integer
     * @param roomId
     * @param oldestMessageTimestamp Used to do pagination (null means latest timestamp)
     * @param count The message quantity, messages are loaded having timestamp older than @param oldestMessageTimestamp
     * @param lastTimestamp Date of the last time when client got data (Used to calculate unread)[unread count suggests number of unread messages having timestamp above @param lastTimestamp]
     * @return
     */

    public static String loadHistory(int integer, String roomId, Date oldestMessageTimestamp,Integer count, Date lastTimestamp){
//        return "{\n" +
//                "    \"msg\": \"method\",\n" +
//                "    \"method\": \"loadHistory\",\n" +
//                "    \"id\": \""+integer+"\",\n" +
//                "    \"params\": [ \""+roomId+"\", { \"$date\": "+((int)lastTimestamp.getTime()/1000)+"} ,"+count+",null]\n" +
//                "}";

        JSONObject lastTs = null;
        JSONObject oldestTs=null;
        try {
            lastTs=new JSONObject();
            lastTs.put("$date",((int) lastTimestamp.getTime() / 1000));
            if (oldestMessageTimestamp!=null){
                oldestTs=new JSONObject();
                oldestTs.put("$date",((int) oldestMessageTimestamp.getTime() / 1000));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return getRemoteMethodObject(integer,HISTORY,roomId,oldestTs,count,lastTs).toString();
//        if (oldestMessageTimestamp!=null) {
//            return "{\n" +
//                    "    \"msg\": \"method\",\n" +
//                    "    \"method\": \"loadHistory\",\n" +
//                    "    \"id\": \"" + integer + "\",\n" +
//                    "    \"params\": [ \"" + roomId + "\", { \"$date\": " + ((int) oldestMessageTimestamp.getTime() / 1000) + " }, " + count + ", { \"$date\": " + ((int) lastTimestamp.getTime() / 1000) + " } ]\n" +
//                    "}";
//
    }
}
