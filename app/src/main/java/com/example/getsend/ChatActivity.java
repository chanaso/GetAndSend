package com.example.getsend;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.scaledrone.lib.Listener;
import com.scaledrone.lib.Member;
import com.scaledrone.lib.Room;
import com.scaledrone.lib.RoomListener;
import com.scaledrone.lib.Scaledrone;

import java.util.Random;

public class ChatActivity extends AppCompatActivity implements RoomListener {

        // replace this with a real channelID from Scaledrone dashboard
        private String channelID = "ifROvUFv1iok6T8b";
        private String prefix = "observable-", contactName, packageId, roomName = "observable-room";
        private EditText editText;
        private Scaledrone scaledrone;
        private MessageAdapter messageAdapter;
        private ListView messagesView;
        private SharedPreferences sharedPref;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_chat);
            sharedPref = getSharedPreferences("userDetails", MODE_PRIVATE);

            //getting the current username from the sp
            // store from local memory the current user
            Bundle mBundle = getIntent().getExtras();
            if (mBundle != null) {
                String[] chatDetails = mBundle.getString("chat").split("@");
                contactName = chatDetails[0];
                packageId = chatDetails[1];
                getIntent().removeExtra("showMessage");
            }
            roomName = prefix + packageId;
            editText = (EditText) findViewById(R.id.editText);

            messageAdapter = new MessageAdapter(this);
            messagesView = (ListView) findViewById(R.id.messages_view);
            messagesView.setAdapter(messageAdapter);

            MemberData data = new MemberData(getRandomName(), getRandomColor());

            scaledrone = new Scaledrone(channelID, data);
            scaledrone.connect(new Listener() {
                @Override
                public void onOpen() {
                    System.out.println("Scaledrone connection open");
                    scaledrone.subscribe(roomName, ChatActivity.this);
                }

                @Override
                public void onOpenFailure(Exception ex) {
                    System.err.println(ex);
                }

                @Override
                public void onFailure(Exception ex) {
                    System.err.println(ex);
                }

                @Override
                public void onClosed(String reason) {
                    System.err.println(reason);
                }
            });
        }

        public void sendMessage(View view) {
            String message = editText.getText().toString();
            if (message.length() > 0) {
                scaledrone.publish(roomName, message);
                editText.getText().clear();
            }
        }

        @Override
        public void onOpen(Room room) {
            System.out.println("Conneted to room");
        }

        @Override
        public void onOpenFailure(Room room, Exception ex) {
            System.err.println(ex);
        }

        @Override
        public void onMessage(Room room, com.scaledrone.lib.Message receivedMessage) {
            final ObjectMapper mapper = new ObjectMapper();
            try {
                final MemberData data = mapper.treeToValue(receivedMessage.getMember().getClientData(), MemberData.class);
                boolean belongsToCurrentUser = receivedMessage.getClientID().equals(scaledrone.getClientID());
                final Message message = new Message(receivedMessage.getData().asText(), data, belongsToCurrentUser);
                runOnUiThread(() -> {
                    messageAdapter.add(message);
                    messagesView.setSelection(messagesView.getCount() - 1);
                });
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        private String getRandomName() {
            return contactName;
        }

        private String getRandomColor() {
            Random r = new Random();
            StringBuffer sb = new StringBuffer("#");
            while(sb.length() < 7){
                sb.append(Integer.toHexString(r.nextInt()));
            }
            return sb.toString().substring(0, 7);
        }
    }

    // is super minimal and will later be serialized into JSON and sent to users by Scaledrone.
    class MemberData {
        private String name;
        private String color;

        public MemberData(String name, String color) {
            this.name = name;
            this.color = color;
        }

        public MemberData() {
        }

        public String getName() {
            return name;
        }

        public String getColor() {
            return color;
        }

        @Override
        public String toString() {
            return "MemberData{" +
                    "name='" + name + '\'' +
                    ", color='" + color + '\'' +
                    '}';
        }
    }