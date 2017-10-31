package com.example.pushpika.mymessage;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomsActivity extends AppCompatActivity {

    private static final String TAG = RoomsActivity.class.getSimpleName();

    private RecyclerView recyclerView;
    private ContactAdapter adapter;
    private SharedPreferences sharedPreferences;
    private ProgressDialog progressDialog;
    // Creating Volley RequestQueue.
    RequestQueue requestQueue;
    String HttpUrlget = MainActivity.baseUrl+"chat/all/sent";

    private Gson gson;
    private List<Contact> contactList;
    public static Contact currentUser;
    private Handler handler;
    public static List<String> newMessageNodes = new ArrayList<>();
    // Storing server url into String variable.
    String HttpUrl = MainActivity.baseUrl+"user/id/";
    View mainview;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rooms);
        contactList = new ArrayList<>();
        gson = new Gson();
        //clear data
        newMessageNodes.clear();
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        sharedPreferences = getSharedPreferences("rooms", Context.MODE_PRIVATE);
        // Creating Volley newRequestQueue .
        requestQueue = Volley.newRequestQueue(RoomsActivity.this);
        adapter = new ContactAdapter(this, getContacts());
        final Intent intentchat = new Intent(this,chatActivity.class);
        adapter.setOnClickListener(new ContactAdapter.OnClickListener() {
            @Override
            public void onClick(int position) {
                openChatWith(adapter.getContacts().get(position));
                if (contactList.size()> position){
                    currentUser = contactList.get(position);
                    startActivity(intentchat);
                }
                //Toast.makeText(RoomsActivity.this, "contact clicked", Toast.LENGTH_SHORT).show();

            }

        });
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        getPrvMessagesFromServer();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.rooms, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            new AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Are you sure wants to logout?")
                    .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Qiscus.clearUser();
                            sharedPreferences.edit().putString("isLogged","no").apply();
                            sharedPreferences.edit().clear().apply();
                            startActivity(new Intent(RoomsActivity.this, MainActivity.class));
                            finish();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        }
        return super.onOptionsItemSelected(item);
    }

    public void createNewChat(View view) {
        NewChatDialog.newInstance(new NewChatDialog.Listener() {
            @Override
            public void onSubmit(String fname, String lname, String email,String id, String message, boolean state) {
                Toast.makeText(RoomsActivity.this, message, Toast.LENGTH_LONG).show();
                if (state){
                    openChatWith(new Contact(email,fname,lname,id));
                }
            }
        }).show(getSupportFragmentManager(), TAG);
    }

    private void openChatWith(final Contact contact) {
        saveContact(contact);
        //showLoading();
//        Qiscus.buildChatWith(contact.getEmail())
//                .withTitle(contact.getName())
//                .build(this, new Qiscus.ChatActivityBuilderListener() {
//                    @Override
//                    public void onSuccess(Intent intent) {
//                        saveContact(contact);
//                        startActivity(intent);
//                        dismissLoading();
//                    }
//
//                    @Override
//                    public void onError(Throwable throwable) {
//                        if (throwable instanceof HttpException) { //Error response from server
//                            HttpException e = (HttpException) throwable;
//                            try {
//                                String errorMessage = e.response().errorBody().string();
//                                Log.e(TAG, errorMessage);
//                                showError(errorMessage);
//                            } catch (IOException e1) {
//                                e1.printStackTrace();
//                            }
//                        } else if (throwable instanceof IOException) { //Error from network
//                            showError("Can not connect to qiscus server!");
//                        } else { //Unknown error
//                            showError("Unexpected error!");
//                        }
//                        throwable.printStackTrace();
//                        dismissLoading();
//                    }
//                });
    }

    private List<Contact> getContacts() {
        String json = sharedPreferences.getString("contacts", "");
        Type type = new TypeToken<List<Contact>>(){}.getType();
        contactList = new ArrayList<>();
        contactList.clear();
        contactList= gson.fromJson(json, type);
        return gson.fromJson(json, new TypeToken<List<Contact>>() {}.getType());
    }

    private void saveContact(Contact contact) {
        List<Contact> contacts = getContacts();
        if (contacts == null) {
            contacts = new ArrayList<>();
        }

        if (!contacts.contains(contact)) {

            if(!checkAlreadyadded(contact)){
                contacts.add(contact);
                sharedPreferences.edit().putString("contacts", gson.toJson(contacts)).apply();
                updateList(contact);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private boolean checkAlreadyadded(Contact contact){
        boolean alreadyIn = false;
        if(contactList == null){
            return alreadyIn;
        }
      for(int i = 0; i < contactList.size(); i++){
          if (contact.getID().equals(contactList.get(i).getID())){
              alreadyIn = true;
          }
      }
        return alreadyIn;
    }

    private boolean checkAlreadyAddedUsingUserID(String uid){
        boolean alreadyIn = false;
        if(contactList == null){
            return alreadyIn;
        }
        for(int i = 0; i < contactList.size(); i++){
            if (uid.equals(contactList.get(i).getID())){
                alreadyIn =true;
            }
        }
        return alreadyIn;
    }
    private void updateList(Contact contact) {
        if (!adapter.getContacts().contains(contact)) {
            adapter.getContacts().add(contact);
            adapter.notifyDataSetChanged();
        }
    }

    public void showError(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    public void showLoading() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Please wait...");
        }
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public void getPrvMessagesFromServer(){

        StringRequest stringRequest = new StringRequest(Request.Method.GET, HttpUrlget,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String ServerResponse) {
                        String state,userID,message, fname, lname;
                        // Hiding the progress dialog after all task complete.
                        //progressDialog.setMessage("Loading...");
                        //progressDialog.show();
                        Log.d("Room Activ","server response is"+ServerResponse);
                        JSONObject jsonObject = null, userObject = null;
                        try {
                            jsonObject = new JSONObject(ServerResponse);
                            JSONArray result = jsonObject.getJSONArray("messages");
                            String from, to, messageBody, createdAt;
                            for(int i = 0; i < result.length(); i++) {
                                from = result.getJSONObject(i).getString("from");
                                to = result.getJSONObject(i).getString("to");
                                messageBody = result.getJSONObject(i).getString("message_body");
                                createdAt = result.getJSONObject(i).getString("createdAt");
                                Log.d("Room chat new req","msg body"+messageBody);
                                if(!checkAlreadyAddedUsingUserID(from)){
                                    getAndAddUser(from);
                                }
                                getAndAddUser(from);
                                boolean isalreadyhave = false;
                                for (int count = 0; count < newMessageNodes.size(); count++) {
                                    if(from.equals(newMessageNodes.get(count))){
                                        isalreadyhave =true;
                                    }
                                }
                                if(!isalreadyhave){
                                    newMessageNodes.add(from);
                                }
                            }
                            //progressDialog.dismiss();
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

    private void getAndAddUser(final String userId){
        StringRequest stringRequest = new StringRequest(Request.Method.GET, HttpUrl+userId,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String ServerResponse) {
                        String state,userID,message, fname, lname, email;
                        // Hiding the progress dialog after all task complete.
                        //progressBar.setVisibility(View.GONE);
                        Log.d("MainActivity","server response is"+ServerResponse);
                        JSONObject jsonObject = null, userObject = null;
                        try {
                            jsonObject = new JSONObject(ServerResponse);
                            state =  jsonObject.get("success").toString();
                            message = jsonObject.get("message").toString();

                            if(state.equals("true")){
                                userObject = (JSONObject) jsonObject.get("user");

                                userID = userObject.get("_id").toString();
                                email = userObject.get("email").toString();
                                fname = userObject.get("fname").toString();
                                lname = userObject.get("lname").toString();

                                openChatWith(new Contact(email,fname,lname,userID));
                            }

                            else{
                               //false
                            }

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
                       // progressBar.setVisibility(View.GONE);
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
                params.put("email", userId);
                return params;
            }

        };

        // Creating RequestQueue.
        // RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        // Adding the StringRequest object into requestQueue.
        requestQueue.add(stringRequest);
    }

    public void updateView(int indexid){

    }
    public void dismissLoading() {
        progressDialog.dismiss();
    }
}
