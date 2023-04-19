package com.example.bus;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.bus.R;
import com.example.bus.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import static android.text.TextUtils.isEmpty;
import static com.example.bus.util.Check.doStringsMatch;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "RegisterActivity";

    //widgets
    private EditText mUsername,mEmail, mPassword, mConfirmPassword,mFullname,mNumberplate,mPhone;
    private ProgressBar mProgressBar;

    //vars
    private FirebaseFirestore mDb;
    private FirebaseAuth mAuth;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        mUsername = findViewById(R.id.input_username);
        mEmail = findViewById(R.id.input_email);
        mPassword = findViewById(R.id.input_password);
        mConfirmPassword = findViewById(R.id.input_confirm_password);
        mFullname = findViewById(R.id.input_full_name);
        mNumberplate = findViewById(R.id.input_number_plate);
        mPhone = findViewById(R.id.input_phone_number);
        mProgressBar = findViewById(R.id.progressBar);

        findViewById(R.id.btn_register).setOnClickListener(this);

        mDb = FirebaseFirestore.getInstance();

        hideSoftKeyboard();
    }

    /*
     * Register a new email and password to Firebase Authentication
     * @param email
     * @param password
     */
    public void registerNewEmail(){
        final String username = mUsername.getText().toString();
        final String email = mEmail.getText().toString();
        final String password = mPassword.getText().toString();
        final String full_name = mFullname.getText().toString();
        final String number_plate = mNumberplate.getText().toString();
        final String phone_number = mPhone.getText().toString();

        if(username.isEmpty()){
            mUsername.setError("Username Required");
            mUsername.requestFocus();
            return;
        }

        if(email.isEmpty()){
            mEmail.setError("Email Required");
            mEmail.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            mEmail.setError("Enter a valid email");
            mEmail.requestFocus();
            return;
        }

        if(password.isEmpty()){
            mPassword.setError("Password Required");
            mPassword.requestFocus();
            return;
        }
        if(doStringsMatch(mPassword.getText().toString(), mConfirmPassword.getText().toString())){
        }else{
            Toast.makeText(RegisterActivity.this,"Passwords do not match",Toast.LENGTH_SHORT).show();
        }

        if(password.length()<6){
            mPassword.setError("Password should be at least 6 characters");
            mPassword.requestFocus();
            return;
        }

        if(full_name.isEmpty()){
            mFullname.setError("Full Name Required");
            mFullname.requestFocus();
            return;
        }
        if(number_plate.isEmpty()){
            mNumberplate.setError("Number Plate Required");
            mNumberplate.requestFocus();
            return;
        }
        if(phone_number.isEmpty()){
            mPhone.setError("Phone Number Required");
            mPhone.requestFocus();
            return;
        }
        if(phone_number.length()<10){
            mPhone.setError("Phone Number should be at least 10 digits");
            mPhone.requestFocus();
            return;
        }

        showDialog();

        mAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        if (task.isSuccessful()){
                            Log.d(TAG, "onComplete: AuthState: " + FirebaseAuth.getInstance().getCurrentUser().getUid());

                            //insert some default data
                            User user = new User();
                            user.setEmail(email);
                            user.setUsername(username);
                            user.setFull_name(full_name);
                            user.setPhone_number(phone_number);
                            user.setNumber_plate(number_plate);
                            user.setUser_id(FirebaseAuth.getInstance().getUid());

                            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                                    .setTimestampsInSnapshotsEnabled(true)
                                    .build();
                            mDb.setFirestoreSettings(settings);

                            DocumentReference newUserRef = mDb
                                    .collection(getString(R.string.collection_users))
                                    .document(FirebaseAuth.getInstance().getUid());
                            newUserRef.set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(RegisterActivity.this,"Registration Successful",Toast.LENGTH_SHORT).show();
                                        redirectLoginScreen();
                                    }else{
                                        View parentLayout = findViewById(android.R.id.content);
                                        Snackbar.make(parentLayout, "Registration Failed.", Snackbar.LENGTH_SHORT).show();
                                    }

                                }
                            });



                        }
                        else {
                            View parentLayout = findViewById(android.R.id.content);
                            Snackbar.make(parentLayout, "Password is not strong enough.", Snackbar.LENGTH_SHORT).show();
                            hideDialog();
                        }

                        // ...
                    }
                });
    }
    /**
     * Redirects the user to the login screen
     */
    private void redirectLoginScreen(){
        Log.d(TAG, "redirectLoginScreen: redirecting to login screen.");

        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }


    private void showDialog(){
        mProgressBar.setVisibility(View.VISIBLE);

    }

    private void hideDialog(){
        if(mProgressBar.getVisibility() == View.VISIBLE){
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void hideSoftKeyboard(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.btn_register){
            Log.d(TAG, "onClick: attempting to register.");
            registerNewEmail();
        }

    }
}

