package project.ecse428.mcgill.ca.snowmore;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import backend.Registration;
import backend.User;

public class UserRegistration extends AppCompatActivity {

    private EditText fullname;
    private EditText email;
    private EditText password;
    private TextView error_message_password;
    private TextView error_message_email;
    private TextView error_message_fullname;
    private Button registration_button;
    private Button signin_button;
    private User user;
    private FirebaseUser fb_user;
    private Dialog dialog = null;
    private Context context = null;

    private static final String TAG = "EmailPassword";
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase myFirebaseDatabase;
    private DatabaseReference myRef;
    private String userID;
    private static boolean success_flag = false;
    private ProgressBar progbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_registration);

        // get an Authentication instance, handles authentication
        mAuth = FirebaseAuth.getInstance();

        // get a database instance
        myFirebaseDatabase = FirebaseDatabase.getInstance();

        // get a reference to the database
        myRef = myFirebaseDatabase.getReference();

        //FirebaseUser user = mAuth.getCurrentUser();
        //userID = user.getUid();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Snow More");
        context = UserRegistration.this;

        progbar = new ProgressBar(this);

        setUpVariables();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        fb_user = mAuth.getCurrentUser();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_registration, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //UI Initialization
    public void setUpVariables() {
        fullname = (EditText) findViewById(R.id.fullname);
        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        error_message_password = (TextView) findViewById(R.id.error_message_password);
        error_message_email = (TextView) findViewById(R.id.error_message_email);
        error_message_fullname = (TextView) findViewById(R.id.error_message_fullname);

        signin_button = (Button) findViewById(R.id.signinbutton);
        registration_button = (Button) findViewById(R.id.registerbutton);
        user = new User();
    }

    //Sign In button action
    public void signInButton(View view) {
        Intent sign_in = new Intent(this , Login.class);
        startActivity(sign_in);
    }

    //Registration button action
    public void registerButton(View view) {

        // if email field is empty, show error message
        if(TextUtils.isEmpty(email.getText().toString())) {
            error_message_email.setText("Please enter email");
            error_message_email.setVisibility(View.VISIBLE);
        }
        // check if email form is valid
        else {
            if(!user.check_email(email.getText().toString())) {
                error_message_email.setText("Invalid email");
                error_message_email.setVisibility(View.VISIBLE);
            }
            else {
                error_message_email.setVisibility(View.INVISIBLE);
            }
        }
        if(TextUtils.isEmpty(password.getText().toString())) {
            error_message_password.setText("Please enter password");
            error_message_password.setVisibility(View.VISIBLE);;
        }
        else {
            if(!user.check_password((password.getText().toString()))) {
                error_message_password.setText("Password should be at least 8 characters long, and must contain uppercase and lowercase letters, at least one digit and one special character");
                error_message_password.setVisibility(View.VISIBLE);
            }
            else {
                error_message_password.setVisibility(View.INVISIBLE);
            }
        }
        if(TextUtils.isEmpty(fullname.getText().toString())) {
            error_message_fullname.setText("Please enter your full name");
            error_message_fullname.setVisibility(View.VISIBLE);
        }
        else {
            if (!user.check_name(fullname.getText().toString())) {
                error_message_fullname.setText("Please enter a valid full name");
                error_message_fullname.setVisibility(View.VISIBLE);
            }
            else {
                error_message_fullname.setVisibility(View.INVISIBLE);
            }
        }
        if(user.check_email(email.getText().toString()) && user.check_password((password.getText().toString())) && user.check_name(fullname.getText().toString())) {
            createDialog();
        }
    }

    //Dialog cancel button
    public void cancelDialog(View view) {
        dialog.dismiss();
    }

    //Dialog confirm button
    public void confirmDialog(View view) {
        Intent signin = new Intent(this , Login.class);
        startActivity(signin);
    }

//    private void addNewUser() {
//        //results in permission denied!
//        myRef.child("new item").setValue("something");
//        mAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
//                .addOnSuccessListener(this, new OnSuccessListener<AuthResult>() {
//
//                    @Override
//                    public void onSuccess(AuthResult authResult) {
//                            // Sign in success, update UI with the signed-in user's information
//                            Log.d(TAG, "signInWithEmail:success");
//                            UserRegistration.success_flag = true;
//                    }
//                });

    private void addNewUser() {
        //results in permission denied!
        myRef.child("new item").setValue("something");
        mAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            UserRegistration.success_flag = true;
                            Intent signin = new Intent(context , Login.class);
                            startActivity(signin);


                            User user = new User(fullname.getText().toString(), email.getText().toString());
                            FirebaseUser fb_user = mAuth.getCurrentUser();
                            userID = fb_user.getUid();
                            DatabaseReference userRef = myRef.child("Users").child(userID);
                            Map<String, Object> dataMap = new HashMap<String, Object>();
                            dataMap.put("user_info", user.toMap());
                            userRef.updateChildren(dataMap);
                        }
                        else{
                            Log.d(TAG, "signInWithEmail:failure");
                            UserRegistration.success_flag = false;
                            Toast.makeText(UserRegistration.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void createDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm email?");
        builder.setMessage(email.getText().toString());
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        addNewUser();

                    }
                });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
