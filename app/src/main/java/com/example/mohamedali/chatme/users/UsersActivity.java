package com.example.mohamedali.chatme.users;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.example.mohamedali.chatme.R;
import com.example.mohamedali.chatme.constanst.Concrete;
import com.example.mohamedali.chatme.login.User;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {

    @BindView(R.id.lv_users)
    ListView mUsersListView;

    private DatabaseReference mUsersDatabaseReference;

    private FirebaseListAdapter<User> mUsersFirebaseListAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        ButterKnife.bind(this);

        mUsersDatabaseReference = FirebaseDatabase.getInstance().getReference().child(Concrete.USERS);


        Query query = mUsersDatabaseReference.limitToLast(50);

        FirebaseListOptions<User> options = new FirebaseListOptions.Builder<User>()
                .setQuery(query, User.class)
                .setLayout(R.layout.user_list_item)
                .build();

        mUsersFirebaseListAdapter = new FirebaseListAdapter<User>(options) {
            @Override
            protected void populateView(View v, User user, int position) {
                TextView username = v.findViewById(R.id.tv_user_item_name);
                username.setText(user.getUsername());

                CircleImageView userPhoto = v.findViewById(R.id.img_user_item_photo);

                if (!TextUtils.isEmpty(user.getPhotoUrl())) {
                    Picasso.get().load(Uri.parse(user.getPhotoUrl())).into(userPhoto);
                } else {
                    userPhoto.setImageResource(R.drawable.ic_account_circle_black_24dp);
                }

            }
        };
        mUsersListView.setAdapter(mUsersFirebaseListAdapter);

    }

    @Override
    protected void onStart() {
        super.onStart();
        mUsersFirebaseListAdapter.startListening();

    }

    @Override
    protected void onStop() {
        super.onStop();
        mUsersFirebaseListAdapter.stopListening();
    }
}


