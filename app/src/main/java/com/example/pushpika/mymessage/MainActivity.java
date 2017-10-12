package com.example.pushpika.mymessage;

import android.content.Context;
import android.content.Intent;
import android.support.v4.util.PatternsCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {


    private EditText nameField;
    private EditText emailField;
    private EditText userKeyField;
    private ProgressBar progressBar;

    // Creating Volley RequestQueue.
    RequestQueue requestQueue;

    // Storing server url into String variable.
    String HttpUrl = "http://192.168.1.101:3002/api/authenticate";
    public static String jwtToken = "";
    public static String userID ="";
    public static String loginState = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nameField = (EditText) findViewById(R.id.name);
        emailField = (EditText) findViewById(R.id.email);
        userKeyField = (EditText) findViewById(R.id.key);
        progressBar = (ProgressBar) findViewById(R.id.progress);

        // Creating Volley newRequestQueue .
        requestQueue = Volley.newRequestQueue(MainActivity.this);

    }

    public void setupUser(View view) {
        final String name = nameField.getText().toString();
        final String email = emailField.getText().toString();
        final String key = userKeyField.getText().toString();
        if (name.isEmpty()) {
            nameField.setError("Please insert your name!");
            nameField.requestFocus();
        } else if (!PatternsCompat.EMAIL_ADDRESS.matcher(email).matches()) {
            emailField.setError("Please insert a valid email!");
            emailField.requestFocus();
        } else if (key.isEmpty()) {
            userKeyField.setError("Please insert your user key!");
            userKeyField.requestFocus();
        } else {
            progressBar.setVisibility(View.VISIBLE);
            //set volley call
            // Creating string request with post method.
            StringRequest stringRequest = new StringRequest(Request.Method.POST, HttpUrl,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String ServerResponse) {

                            // Hiding the progress dialog after all task complete.
                            progressBar.setVisibility(View.GONE);
                            Log.d("MainActivity","server response is"+ServerResponse);
                            JSONObject jsonObject = null, userObject = null;
                            try {
                                jsonObject = new JSONObject(ServerResponse);
                                loginState =  jsonObject.get("success").toString();
                                jwtToken = jsonObject.get("token").toString();
                                userObject = (JSONObject) jsonObject.get("user");
                                userID = userObject.get("_id").toString();
                                Log.d("MainAct success is",loginState+" Token is-- >"+jwtToken);
                                Log.d("MainAct userid",userObject.get("_id").toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            // Showing response message coming from server.
                            Toast.makeText(MainActivity.this, ServerResponse, Toast.LENGTH_LONG).show();
                            if (loginState.equals("true")){
                                Intent intent = new Intent(MainActivity.this, RoomsActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {

                            // Hiding the progress dialog after all task complete.
                            progressBar.setVisibility(View.GONE);

                            // Showing error message if something goes wrong.
                            Toast.makeText(MainActivity.this, volleyError.toString(), Toast.LENGTH_LONG).show();
                        }
                    }) {

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
                    params.put("email", email);
                    params.put("password", key);

                    return params;
                }

            };

            // Creating RequestQueue.
           // RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

            // Adding the StringRequest object into requestQueue.
            requestQueue.add(stringRequest);

        }
    }


    private void showError(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

}