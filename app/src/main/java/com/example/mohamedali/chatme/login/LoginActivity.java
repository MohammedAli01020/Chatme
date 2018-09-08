package com.example.mohamedali.chatme.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.mohamedali.chatme.R;
import com.example.mohamedali.chatme.constanst.Concrete;
import com.example.mohamedali.chatme.main.MainActivity;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class LoginActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 123;
    private static final int REQUEST_IMAGE_OPEN = 155;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private StorageReference mUsersProfilePhotoStorageReference;
    private DatabaseReference mUsersDatabaseReference;

    private ProgressDialog mProgressDialog;

    @BindView(R.id.bt_continue)
    Button mContinueButton;

    @BindView(R.id.img_account_profile)
    CircleImageView mCircleImageView;

    @BindView(R.id.et_profile_user_name)
    EditText mUsernameEditText;

    @BindView(R.id.fab_update_profile_photo)
    FloatingActionButton mUpdateProfilePhotoFab;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();
        mUsersProfilePhotoStorageReference = FirebaseStorage.getInstance().getReference().child(Concrete.PROFILE_PHOTOS);
        mUsersDatabaseReference = FirebaseDatabase.getInstance().getReference().child(Concrete.USERS);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("loading");
        mProgressDialog.setCancelable(true);

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // already signed in
                    mUsersDatabaseReference.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                startMainActivity();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                } else {
                    // not signed in
                    List<AuthUI.IdpConfig> providers = Arrays.asList(
                            new AuthUI.IdpConfig.PhoneBuilder().build(),
                            new AuthUI.IdpConfig.GoogleBuilder().build(),
                            new AuthUI.IdpConfig.FacebookBuilder().build());

                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setAvailableProviders(providers)
                                    .build(),
                            RC_SIGN_IN);
                }

            }
        };

        mUpdateProfilePhotoFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), REQUEST_IMAGE_OPEN);
            }
        });

        mContinueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewUser();
            }
        });

    }

    private void addNewUser() {
        if (!validateForm()) return;

        Map<String, Object> updates = new HashMap();

        updates.put("/" + Concrete.getUid() + "/" + User.UID + "/", Concrete.getUid());
        updates.put("/" + Concrete.getUid() + "/" + User.REGISTRATION_TOKEN + "/", Concrete.getRegistrationToken());
        updates.put("/" + Concrete.getUid() + "/" + User.USERNAME + "/", mUsernameEditText.getText().toString());

        mUsersDatabaseReference.updateChildren(updates).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                startMainActivity();
            }
        });

    }


    private void startMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    Toast.makeText(this, "Sign in successfully " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
                }
                // ...
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        } else if (requestCode == REQUEST_IMAGE_OPEN) {
            if (resultCode == RESULT_OK) {
                Uri selectedImageUri = data.getData();

                if (selectedImageUri != null) {

                    showProgressDialog();
                    if (mAuth != null) {
                        final StorageReference photoRef = mUsersProfilePhotoStorageReference.child(Concrete.getUid());

                        UploadTask uploadTask = photoRef.putFile(selectedImageUri);

                        Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if (!task.isSuccessful()) {
                                    throw task.getException();
                                }

                                return photoRef.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    Uri downloadUri = task.getResult();

                                    Log.d("downloaduri", downloadUri.toString());

                                    Picasso.get().load(downloadUri).into(mCircleImageView);
                                    mUsersDatabaseReference.child(Concrete.getUid()).child(User.PHOTO_URL).setValue(downloadUri.toString())
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    hideProgressDialog();
                                                }
                                            });
                                }
                            }
                        });

                    }
                }



            }
        }

    }

    private boolean validateForm() {
        boolean result = true;
        if (TextUtils.isEmpty(mUsernameEditText.getText().toString())) {
            mUsernameEditText.setError(getString(R.string.required));
            result = false;
        } else {
            mUsernameEditText.setError(null);
        }

        return result;
    }

    private void hideProgressDialog() {
        mProgressDialog.dismiss();
    }

    private void showProgressDialog() {
        mProgressDialog.show();
    }


    @Override
    protected void onPause() {
        super.onPause();
        mAuth.removeAuthStateListener(mAuthStateListener);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mAuth.addAuthStateListener(mAuthStateListener);
    }
}
