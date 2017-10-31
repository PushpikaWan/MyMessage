package com.example.pushpika.mymessage;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.bassaer.chatmessageview.models.Message;
import com.github.bassaer.chatmessageview.models.User;
import com.github.bassaer.chatmessageview.utils.ChatBot;
import com.github.bassaer.chatmessageview.views.ChatView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.xml.datatype.XMLGregorianCalendar;

public class chatActivity extends AppCompatActivity {

    private ChatView mChatView;

    // Creating Volley RequestQueue.
    RequestQueue requestQueue;
    String HttpUrl = MainActivity.baseUrl+"chat";
    String HttpUrlget = MainActivity.baseUrl+"chat/";
    private ProgressDialog progressDialog;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        //clear data
        RoomsActivity.newMessageNodes.clear();
        // Creating Volley newRequestQueue .
        requestQueue = Volley.newRequestQueue(chatActivity.this);
        progressDialog= new ProgressDialog(chatActivity.this);

        //User id
        int myId = 0;
        //User icon
       // Bitmap myIcon = BitmapFactory.decodeResource(getResources(), R.drawable.face_2);
        //User name
        String myName = MainActivity.fName;

        int yourId = 1;
        //Bitmap yourIcon = BitmapFactory.decodeResource(getResources(), R.drawable.face_1);
        String yourName = "mmmm";

        final User me = new User(myId, myName, null);
        final User you = new User(yourId, yourName, null);

        mChatView = (ChatView)findViewById(R.id.chat_view);

        //Set UI parameters if you need
        mChatView.setRightBubbleColor(ContextCompat.getColor(this, R.color.green500));
        mChatView.setLeftBubbleColor(Color.WHITE);
        mChatView.setBackgroundColor(ContextCompat.getColor(this, R.color.blueGray500));
        mChatView.setSendButtonColor(ContextCompat.getColor(this, R.color.cyan900));
        mChatView.setSendIcon(R.drawable.ic_action_send);
        mChatView.setMessageStatusTextColor(Color.WHITE);
        mChatView.setRightMessageTextColor(Color.WHITE);
        mChatView.setLeftMessageTextColor(Color.BLACK);
        mChatView.setUsernameTextColor(Color.WHITE);
        mChatView.setSendTimeTextColor(Color.WHITE);
        mChatView.setDateSeparatorColor(Color.WHITE);
        mChatView.setInputTextHint("new message...");
        mChatView.setMessageMarginTop(5);
        mChatView.setMessageMarginBottom(5);

        //Click Send Button
        mChatView.setOnClickSendButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //new message
                Message message = new Message.Builder()
                        .setUser(me)
                        .setRightMessage(true)
                        .setMessageText(mChatView.getInputText())
                        .setStatusIconFormatter(new MyMessageStatusFormatter(chatActivity.this))
                        //  .setStatusTextFormatter(new MyMessageStatusFormatter(this))
                        .setMessageStatusType(Message.MESSAGE_STATUS_ICON)
                        .setStatus(MyMessageStatusFormatter.STATUS_DELIVERING)
                        .hideIcon(true)
                        .build();
                //Set to chat view
                mChatView.send(message);
                String curMessage = mChatView.getInputText();
                sendMessageToServer(curMessage);
                //Reset edit text
                mChatView.setInputText("");
            }

        });
        getPrvMessagesFromServer();
    }

    public void setSendMessages(String sendMessage, String createdat, String status){
        //User id
        int myId = 0;
        //User icon
       //Bitmap myIcon = BitmapFactory.decodeResource(getResources(), R.drawable.face_2);
        //User name
        String myName = MainActivity.fName;

        int yourId = 1;
        //Bitmap yourIcon = BitmapFactory.decodeResource(getResources(), R.drawable.face_1);
        String yourName = RoomsActivity.currentUser.getFname();

        final User me1 = new User(myId, myName, null);
        final User you1 = new User(yourId, yourName, null);
        Calendar calendar = null;
        ISO8601 iso8601 = new ISO8601();
        try {
             calendar = ISO8601.toCalendar(createdat);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Message message = new Message.Builder()
                .setUser(me1)
                .setRightMessage(true)
                .setMessageText(sendMessage)
                .hideIcon(true)
                .setCreatedAt(calendar)
                .setStatusIconFormatter(new MyMessageStatusFormatter(this))
              //  .setStatusTextFormatter(new MyMessageStatusFormatter(this))
                .setMessageStatusType(Message.MESSAGE_STATUS_ICON)
                .setStatus(MyMessageStatusFormatter.STATUS_DELIVERED)
                .build();
        //Set to chat view
        if (status.equals("sent")){
            message.setStatus(MyMessageStatusFormatter.STATUS_DELIVERING);
        }
        mChatView.send(message);

    }

    public void setReceivedMessages(String recMessage, String createdat){

        int yourId = 1;
        //Bitmap yourIcon = BitmapFactory.decodeResource(getResources(), R.drawable.face_1);
        String yourName = RoomsActivity.currentUser.getFname();

        final User you1 = new User(yourId, yourName, null);

        Calendar calendar = null;
        ISO8601 iso8601 = new ISO8601();
        try {
            calendar = ISO8601.toCalendar(createdat);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //Receive message
        final Message receivedMessage = new Message.Builder()
                .setUser(you1)
                .setRightMessage(false)
                .setMessageText(recMessage)
                .hideIcon(true)
                .setCreatedAt(calendar)
                .build();
        mChatView.receive(receivedMessage);
    }

    public void sendMessageToServer(final String message) {

        progressDialog.setMessage("Loading...");
        progressDialog.show();
        //set volley call
        // Creating string request with post method.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, HttpUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String ServerResponse) {
                        // Hiding the progress dialog after all task complete.
                        progressDialog.dismiss();
                        Log.d("MainActivity","server response is"+ServerResponse);
                        JSONObject jsonObject = null, userObject = null;
                        try {
                            jsonObject = new JSONObject(ServerResponse);

                            Log.d("chat response ",jsonObject.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        // Showing response message coming from server.
                        Toast.makeText(chatActivity.this, ServerResponse, Toast.LENGTH_LONG).show();

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                            // Hiding the progress dialog after all task complete.
                        progressDialog.dismiss();
                        // Showing error message if something goes wrong.
                        Toast.makeText(chatActivity.this, volleyError.toString(), Toast.LENGTH_LONG).show();
                    }
                }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    String auth = MainActivity.jwtToken;
                    headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                    headers.put("Authorization", auth);
                    return headers;
                }
                @Override
                public String getBodyContentType() {
                    return "application/x-www-form-urlencoded; charset=UTF-8";
                }

                @Override
                protected Map<String, String> getParams() {

                    // Creating Map String Params.
                    Map<String, String> params = new HashMap<String, String>();

                    // Adding All values to Params.
                    //params.put("name", name);
                    Log.d("Chat act m","map string msg"+message);
                    params.put("to", RoomsActivity.currentUser.getID());
                    params.put("message_body", message);


                    return params;
                }

            };

            // Creating RequestQueue.
            // RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

            // Adding the StringRequest object into requestQueue.
            requestQueue.add(stringRequest);

        }

    @Override
    public void onBackPressed() {

        handler.removeCallbacksAndMessages(null);
        finish();
        Intent intent = new Intent(this,RoomsActivity.class);
        startActivity(intent);
    }

    public void getPrvMessagesFromServer(){

        StringRequest stringRequest = new StringRequest(Request.Method.GET, HttpUrlget+RoomsActivity.currentUser.getID(),
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String ServerResponse) {
                        String state,userID,message, fname, lname;
                        // Hiding the progress dialog after all task complete.
                        //progressDialog.setMessage("Loading...");
                        //progressDialog.show();
                        Log.d("MainActivity","server response is"+ServerResponse);
                        JSONObject jsonObject = null, userObject = null;
                        try {
                            jsonObject = new JSONObject(ServerResponse);
                            JSONArray result = jsonObject.getJSONArray("messages");
                            String from, to, messageBody, createdAt, status;
                            for(int i = 0; i < result.length(); i++) {
                                from = result.getJSONObject(i).getString("from");
                                to = result.getJSONObject(i).getString("to");
                                messageBody = result.getJSONObject(i).getString("message_body");
                                createdAt = result.getJSONObject(i).getString("createdAt");
                                status = result.getJSONObject(i).getString("status");
                                if (from.equals(MainActivity.userID)){
                                    setSendMessages(messageBody,createdAt,status);
                                    Log.d("ChatActivity","send messages"+messageBody);
                                }
                                else{
                                    setReceivedMessages(messageBody,createdAt);
                                    Log.d("ChatActivity","received messages"+messageBody);
                                }
                            }
                            //progressDialog.dismiss();
                           HttpUrlget = MainActivity.baseUrl+"chat/one/";
                            handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    getPrvMessagesFromServer();
                                }
                            },1000);
//                            getPrvMessagesFromServer();
                            // Log.d("MainAct userid",userObject.get("_id").toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        // Showing response message coming from server.
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                        // Hiding the progress dialog after all task complete.
                        //progressDialog.dismiss();
                        Log.d("Newchat error",volleyError.toString());
                        // Showing error message if something goes wrong.
                        //Toast.makeText(getContext(), volleyError.toString(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String auth = MainActivity.jwtToken;
                headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                headers.put("Authorization", auth);
                return headers;
            }
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }

            @Override
            protected Map<String, String> getParams() {

                // Creating Map String Params.
                Map<String, String> params = new HashMap<String, String>();

                // Adding All values to Params.
                params.put("user_id", RoomsActivity.currentUser.getID());
                return params;
            }

        };



        // Creating RequestQueue.
        // RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        // Adding the StringRequest object into requestQueue.
        requestQueue.add(stringRequest);

    }

}





