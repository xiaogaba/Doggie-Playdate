package com.example.xin.pre_project;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatList extends AppCompatActivity {

    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;


    private FirebaseRecyclerAdapter<SingleChat, ChatsViewHolder>
            mFirebaseAdapter;

    public class ChatsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView messageTextView;
        TextView messengerTextView;
        CircleImageView messengerImageView;
        TextView UserIdView;

        public ChatsViewHolder(View v) {
            super(v);
            messageTextView = (TextView) itemView.findViewById(R.id.user_single_status);
            messengerTextView = (TextView) itemView.findViewById(R.id.user_single_name);
            messengerImageView = (CircleImageView) itemView.findViewById(R.id.user_single_image);
            UserIdView = itemView.findViewById(R.id.single_user_id);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            String target_id = UserIdView.getText().toString();
            Intent intent = new Intent(ChatList.this, message.class);
            intent.putExtra("user_id", FirebaseAuth.getInstance().getCurrentUser().getUid());
            intent.putExtra("target_id", target_id);
            startActivity(intent);
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chats);
        mMessageRecyclerView = (RecyclerView) findViewById(R.id.conv_list);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);

        String path = "chats/"+FirebaseAuth.getInstance().getCurrentUser().getUid() +"/";
        Log.d("Path: ", path);
        Query mFirebaseDatabaseReference = FirebaseDatabase.getInstance()
                .getReference(path).orderByChild("timestamp");
        SnapshotParser<SingleChat> parser = new SnapshotParser<SingleChat>() {
            @Override
            public SingleChat parseSnapshot(DataSnapshot dataSnapshot) {
                SingleChat chat = dataSnapshot.getValue(SingleChat.class);
                chat.setId(dataSnapshot.getKey());
                chat.setId(dataSnapshot.getKey());
                Log.d("aaaa", "key is: " + dataSnapshot.getKey());
                return chat;
            }
        };

        // Construct child name.
        FirebaseRecyclerOptions<SingleChat> options =
                new FirebaseRecyclerOptions.Builder<SingleChat>()
                        .setQuery(mFirebaseDatabaseReference, parser)
                        .build();
        mFirebaseAdapter = new FirebaseRecyclerAdapter<SingleChat, ChatsViewHolder>(options) {
            @Override
            public ChatsViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                return new ChatsViewHolder(inflater.inflate(R.layout.users_single_layout, viewGroup, false));
            }

            @Override
            protected void onBindViewHolder(final ChatsViewHolder viewHolder,
                                            int position,
                                            SingleChat singleChat) {
                viewHolder.messageTextView.setText(singleChat.getLastMessage());
                viewHolder.messengerTextView.setText(singleChat.getName());
                viewHolder.UserIdView.setText(singleChat.getId());
            }
        };

        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = mFirebaseAdapter.getItemCount();
                int lastVisiblePosition =
                        mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                // of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    mMessageRecyclerView.scrollToPosition(positionStart);
                }
            }
        });

        mMessageRecyclerView.setAdapter(mFirebaseAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onPause() {
        mFirebaseAdapter.stopListening();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mFirebaseAdapter.startListening();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
