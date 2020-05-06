package com.example.getsend;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.scaledrone.lib.Listener;
import com.scaledrone.lib.Room;
import com.scaledrone.lib.RoomListener;
import com.scaledrone.lib.Scaledrone;

import java.util.Random;

public class ChatActivity extends AppCompatActivity implements RoomListener {

    private static final String DELIMITER = "@";
    // replace this with a real channelID from Scaledrone dashboard
        private String channelID = "ifROvUFv1iok6T8b";
        private String prefix = "observable-", contactName, packageId, userKey, roomName;
        private int messageTimeStamp;
        private User currUser;
        private EditText editText;
        private Scaledrone scaledrone;
        private MessageAdapter messageAdapter;
        private ListView messagesView;
        private SharedPreferences sharedPref;
        DatabaseReference refChat;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_chat);
            // store from local memory the current user
            sharedPref = getSharedPreferences("userDetails", MODE_PRIVATE);
            Gson gson = new Gson();
            String json = sharedPref.getString("currUser", "");
            currUser = gson.fromJson(json, User.class);
            userKey = sharedPref.getString("userKey", "");

            refChat = FirebaseDatabase.getInstance().getReference().child("ChatRooms");


            //getting the current username from the sp
            // store from local memory the current user
            Bundle mBundle = getIntent().getExtras();
            if (mBundle != null) {
                String[] chatDetails = mBundle.getString("chat").split(DELIMITER);
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
            retrieveChatHistory(data);

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

    private void retrieveChatHistory(MemberData data) {
        // retrieve chat history from db
        refChat.child(roomName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot datas : dataSnapshot.getChildren()){
                    if(datas.exists()){
                        if(datas.getKey().split(DELIMITER)[1].equals(userKey)){
                            final Message message = new Message(datas.getValue().toString(), data, true);
                            messageAdapter.add(message);
                            messageAdapter.notifyDataSetChanged();
                            messageTimeStamp = Integer.parseInt(datas.getKey().split(DELIMITER)[0])+1;
                        }else {
                            final Message message = new Message(datas.getValue().toString(), data, false);
                            messageAdapter.add(message);
                            messageAdapter.notifyDataSetChanged();
                            messageTimeStamp = Integer.parseInt(datas.getKey().split(DELIMITER)[0])+1;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });
    }

    public void sendMessage(View view) {
            String message = editText.getText().toString();
            if (message.length() > 0) {
                scaledrone.publish(roomName, message);

                // Firebase Database keys are strings ordered lexicographically,
                // therefor used format that the key will be ordered by the timestamp value.
                String sameLenght = String.format("%05d", messageTimeStamp); // 0 fill to 5 digits num
                refChat.child(roomName).child(sameLenght + DELIMITER + userKey).setValue(message);
                messageTimeStamp+=1;
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