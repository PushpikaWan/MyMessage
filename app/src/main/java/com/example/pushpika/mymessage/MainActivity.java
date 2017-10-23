package com.example.pushpika.mymessage;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

    private EditText emailField;
    private EditText userKeyField;

    // Creating Volley RequestQueue.
    RequestQueue requestQueue;

    // Storing server url into String variable.
    public static String baseUrl = "http://10.42.0.210:3002/api/";
    String HttpUrl = baseUrl+"authenticate";
    public static String jwtToken = "";
    public static String userID ="";
    public static String loginState = "";
    public static String fName="";

    private ProgressDialog progressDialog;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        emailField = (EditText) findViewById(R.id.input_email);
        userKeyField = (EditText) findViewById(R.id.input_password);
        progressDialog= new ProgressDialog(MainActivity.this);
        String storedUserName, storedPassword;
        // Creating Volley newRequestQueue .
        requestQueue = Volley.newRequestQueue(MainActivity.this);
        sharedPreferences = getSharedPreferences("rooms", Context.MODE_PRIVATE);
        String state = sharedPreferences.getString("isLogged", "no");
        storedUserName = sharedPreferences.getString("userName", "");
        storedPassword = sharedPreferences.getString("password", "");
        if(state.equals("yes")){
            authUser(storedUserName,storedPassword);
        }
    }

    public void register(View view){
        Intent intent = new Intent(this,RegisterActivity.class);
        startActivity(intent);
        finish();

    }

    public void setupUser(View view) {
        String email = emailField.getText().toString();
        String key = userKeyField.getText().toString();
        if (!PatternsCompat.EMAIL_ADDRESS.matcher(email).matches()) {
            emailField.setError("Please insert a valid email!");
            emailField.requestFocus();
        } else if (key.isEmpty()) {
            userKeyField.setError("Please insert your user key!");
            userKeyField.requestFocus();
        } else {
            authUser(email,key);
        }
    }

    private void authUser(final String email, final String password){
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
                            loginState =  jsonObject.get("success").toString();
                            jwtToken = jsonObject.get("token").toString();
                            userObject = (JSONObject) jsonObject.get("user");
                            userID = userObject.get("_id").toString();
                            fName = userObject.get("fname").toString();
                            sharedPreferences.edit().putString("userName",email).apply();
                            sharedPreferences.edit().putString("password",password).apply();
                            Log.d("MainAct userid",userObject.get("_id").toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        // Showing response message coming from server.
                        Toast.makeText(MainActivity.this, "Successful", Toast.LENGTH_LONG).show();
                        if (loginState.equals("true")){
                            sharedPreferences.edit().putString("isLogged","yes").apply();
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
                        progressDialog.dismiss();
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
                params.put("password", password);

                return params;
            }

        };

        // Creating RequestQueue.
        // RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        // Adding the StringRequest object into requestQueue.
        requestQueue.add(stringRequest);

    }


    private void showError(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

}
