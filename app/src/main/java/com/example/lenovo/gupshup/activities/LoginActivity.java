package com.example.lenovo.gupshup.activities;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.lenovo.gupshup.InternetChecker;
import com.example.lenovo.gupshup.R;
import com.example.lenovo.gupshup.firebase.FcmId;
import com.example.lenovo.gupshup.firebase.URLEndPoints;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String mVerificationId = "";
    PhoneAuthProvider.ForceResendingToken mResendToken;
    private static final String TAG = "LoginActivity";

    private EditText mName, mEmail, mPhone;
    private InternetChecker checker;
    private Button register;

    private String checkPhoneNo = "";

    public static final String USER_TOKEN = "token";
    public static final String USER_NAME = "user_name";
    public static final String USER_PHONE = "user_phone";
    public static final String USER_EMAIL = "user_email";

    private AlertDialog dialog;
    private AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectAll().penaltyLog().build()
        );

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mName = (EditText) findViewById(R.id.name);
        mEmail = (EditText) findViewById(R.id.email);
        mPhone = (EditText) findViewById(R.id.phone);
        register = (Button) findViewById(R.id.btn_login);
        register.setBackgroundColor(Color.BLACK);
        Log.d(TAG, "onCreate: Froze");
        Log.d(TAG, "onCreate: Released");

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verificaiton without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:" + credential);

                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.d(TAG, "onVerificationFailed", e);

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // ...
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // ...
                }

//                Intent i = new Intent(LoginActivity.this, MainActivity.class);
//                startActivity(i);

                // Show a message and update the UI
                // ...
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;
//                Intent i = new Intent(LoginActivity.this, MainActivity.class);
//                startActivity(i);
            }
        };

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mName.getText().toString().isEmpty()
                        || mPhone.getText().toString().isEmpty()
                        || mEmail.getText().toString().isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Required Fields cannot be left vacant."
                            , Toast.LENGTH_SHORT).show();
                } else {
                    String name = mName.getText().toString();
                    String phone = mPhone.getText().toString();
                    String email = mEmail.getText().toString();

                    checkPhoneNo = phone;

                    SharedPreferences preferences = getSharedPreferences(FcmId.FCM_TOKEN_SAVE, MODE_PRIVATE);
                    String token = preferences.getString(USER_TOKEN, null);
                    if (token == null) {
                        token = FirebaseInstanceId.getInstance().getToken();
                    }
                    registerUser(name, phone, email, token);
                }
            }
        });
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {

        mAuth = FirebaseAuth.getInstance();

        Log.d(TAG, "signInValePE");
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");

                            Intent i = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(i);

                            FirebaseUser user = task.getResult().getUser();
                            // ...
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                        }
                    }
                });
    }

    private void isRegistered(final String name, final String phone, final String email, final String token) {

        Log.d(TAG, "Url:" + URLEndPoints.EndPoints.UPDATE_FCM_ID + phone);
        RequestQueue que = Volley.newRequestQueue(LoginActivity.this);
        StringRequest req = new StringRequest(
                Request.Method.PUT,
                URLEndPoints.EndPoints.UPDATE_FCM_ID + phone,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Log.d(FcmId.FCM_TOKEN_SAVE, "Token Update login: " + new JSONObject(response).getString("message"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Log.d(TAG, "Error occurred " + error.networkResponse.statusCode + " " + error.getMessage());
                        if (error instanceof TimeoutError) {
                            Toast.makeText(LoginActivity.this, "Connection Timed Out. Keep Trying :)", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        } else Log.d(TAG, "onErrorResponse: x122");
                        //error.printStackTrace();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("gcm_id", token);
                Log.d(TAG, "Map: " + map);
                return map;
            }

            @Override
            public String getBodyContentType() {
                Log.d(TAG, "ContentBodyTypeloginact");
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }
        };
        que.add(req);

        SharedPreferences preferences = getSharedPreferences(
                FcmId.FCM_TOKEN_SAVE,
                MODE_PRIVATE
        );
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(USER_NAME, name);
        editor.putString(USER_PHONE, phone);
        editor.putString(USER_EMAIL, email);
        editor.putString(USER_TOKEN, token);
        editor.apply();

        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void registerUser(final String name, final String phone, final String email, final String token) {

        Log.d(TAG, "checkPhoneNo.: " + checkPhoneNo);
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                checkPhoneNo,        // Phone number to verify
                50,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);
        // OnVerificationStateChangedCallbacks
        Log.d(TAG, "registerUser: adgagadga");

        Log.d(TAG, "checkPhoneNo.: " + checkPhoneNo);

        Log.d(TAG, "registerUser: " + name + phone + email + token);
    }

    @Override
    protected void onResume() {
        super.onResume();
        builder = new AlertDialog.Builder(this);
        dialog = builder.setMessage("Internet not connected")
                .setTitle("Alert")
                .setCancelable(false)
                .create();

        checker = new InternetChecker();
        checker.setListener(new InternetChecker.OnDialogShowListener() {
            @Override
            public void showDialog() {
                dialog.show();
            }

            @Override
            public void dismissDialog() {
                if (dialog.isShowing()) dialog.dismiss();
            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(checker, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(checker);
    }
}
