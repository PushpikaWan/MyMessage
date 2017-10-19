package com.example.pushpika.mymessage;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.util.PatternsCompat;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class NewChatDialog extends DialogFragment implements View.OnClickListener {

    private TextView emailField;
    private View submitButton;
    private View cancelButton;

    private Listener listener;


    private ProgressBar progressBar;

    // Creating Volley RequestQueue.
    RequestQueue requestQueue;

    // Storing server url into String variable.
    String HttpUrl = MainActivity.baseUrl+"user/";

    public static NewChatDialog newInstance(Listener listener) {
        NewChatDialog dialog = new NewChatDialog();
        dialog.listener = listener;
        dialog.setCancelable(false);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_new_chat, container, false);
        emailField = (TextView) view.findViewById(R.id.email);
        submitButton = view.findViewById(R.id.tv_submit);
        cancelButton = view.findViewById(R.id.tv_cancel);

        submitButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);

        progressBar = (ProgressBar) view.findViewById(R.id.progress);
        // Creating Volley newRequestQueue .
        requestQueue = Volley.newRequestQueue(getContext());


        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_submit:
                String email = emailField.getText().toString();
                if (!PatternsCompat.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailField.setError("Please insert a valid email!");
                    emailField.requestFocus();
                } else {
                    checkUser(email);
                    dismiss();
                }
                break;
            case R.id.tv_cancel:
                dismiss();
                break;
        }
    }


    public void checkUser(final String email){

        StringRequest stringRequest = new StringRequest(Request.Method.GET, HttpUrl+email,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String ServerResponse) {
                        String state,userID,message, fname, lname;
                        // Hiding the progress dialog after all task complete.
                        progressBar.setVisibility(View.GONE);
                        Log.d("MainActivity","server response is"+ServerResponse);
                        JSONObject jsonObject = null, userObject = null;
                        try {
                            jsonObject = new JSONObject(ServerResponse);
                            state =  jsonObject.get("success").toString();
                            message = jsonObject.get("message").toString();

                            if(state.equals("true")){
                                userObject = (JSONObject) jsonObject.get("user");
                                userID = userObject.get("_id").toString();
                                fname = userObject.get("fname").toString();
                                lname = userObject.get("lname").toString();

                                if (listener != null) {
                                    listener.onSubmit(fname, lname, email,userID,message,true);
                                }
                            }

                            else{
                                if (listener != null) {
                                    listener.onSubmit("", "", email,"",message,false);
                                }
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
                        progressBar.setVisibility(View.GONE);
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
                params.put("email", email);
                return params;
            }

        };

        // Creating RequestQueue.
        // RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        // Adding the StringRequest object into requestQueue.
        requestQueue.add(stringRequest);

    }

    public interface Listener {
        void onSubmit(String fname, String lname, String email,String id, String message, boolean state);
    }
}
