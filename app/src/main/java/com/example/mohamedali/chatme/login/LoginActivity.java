package com.example.mohamedali.chatme.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.mohamedali.chatme.R;
import com.example.mohamedali.chatme.main.MainActivity;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class LoginActivity extends AppCompatActivity implements LoginMVP.View{
    private static final int RC_SIGN_IN = 123;
    private static final int REQUEST_IMAGE_OPEN = 155;


    private ProgressDialog mProgressDialog;

    @BindView(R.id.bt_continue)
    Button mContinueButton;

    @BindView(R.id.img_account_profile)
    CircleImageView mCircleImageView;

    @BindView(R.id.et_profile_user_name)
    EditText mUsernameEditText;

    @BindView(R.id.fab_update_profile_photo)
    FloatingActionButton mUpdateProfilePhotoFab;

    private LoginMVP.Presenter presenter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        presenter = new LoginPresenter();
        presenter.setView(this);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("loading");
        mProgressDialog.setCancelable(true);

        presenter.checkAuth();

        mUpdateProfilePhotoFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               presenter.pickImageFromGallery();
            }
        });

        mContinueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.addNewUser();
            }
        });

    }

    @Override
    public void startMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public void startGalleryActivity() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/jpeg");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(Intent.createChooser(intent, "Complete action using"), REQUEST_IMAGE_OPEN);
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
            }
        } else if (requestCode == REQUEST_IMAGE_OPEN) {
            if (resultCode == RESULT_OK) {
                Uri selectedImageUri = data.getData();

                if (selectedImageUri != null) {
                    presenter.storeProfileImage(selectedImageUri);
                }
            }
        }

    }

    @Override
    public void hideProgressDialog() {
        mProgressDialog.dismiss();
    }

    @Override
    public void startSignInActivity() {
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

    @Override
    public void setImageProfile(Uri downloadUri) {
        Picasso.get().load(downloadUri).into(mCircleImageView);
    }

    @Override
    public void showProgressDialog() {
        mProgressDialog.show();
    }

    @Override
    public String getUserName() {
        return mUsernameEditText.getText().toString();
    }

    @Override
    public void setUserNameError() {
        mUsernameEditText.setError(getString(R.string.required));
    }

    @Override
    public void setUserNameErrorNull() {
        mUsernameEditText.setError(null);
    }
}
