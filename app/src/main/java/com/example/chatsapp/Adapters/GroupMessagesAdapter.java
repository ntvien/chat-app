package com.example.chatsapp.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatsapp.Models.Message;
import com.example.chatsapp.Models.User;
import com.example.chatsapp.R;
import com.example.chatsapp.databinding.ItemReceiveBinding;
import com.example.chatsapp.databinding.ItemReceiveGroupBinding;
import com.example.chatsapp.databinding.ItemSendBinding;
import com.example.chatsapp.databinding.ItemSendGroupBinding;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GroupMessagesAdapter extends RecyclerView.Adapter{

    Context context;
    ArrayList<Message> messages;

    private int ITEM_SENT = 1;
    private int ITEM_RECEIVE = 2;

    public GroupMessagesAdapter(Context context, ArrayList<Message> messages){
        this.context = context;
        this.messages = messages;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_SENT){
            View view = LayoutInflater.from(context).inflate(R.layout.item_send_group, parent, false);
            return new SentViewHolder(view);
        }else{
            View view = LayoutInflater.from(context).inflate(R.layout.item_receive_group, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    // return the view type of the item at position for the purposes of view recycling
    @Override
    public int getItemViewType(int position) {

        Message message = messages.get(position);
        if (FirebaseAuth.getInstance().getUid().equals(message.getSenderId())) {
            return ITEM_SENT;
        } else {
            return ITEM_RECEIVE;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        Message message = messages.get(position);

        int reactions[] = new int[]{
                R.drawable.ic_fb_like,
                R.drawable.ic_fb_love,
                R.drawable.ic_fb_laugh,
                R.drawable.ic_fb_wow,
                R.drawable.ic_fb_sad,
                R.drawable.ic_fb_angry
        };

        ReactionsConfig config = new ReactionsConfigBuilder(context)
                .withReactions(reactions)
                .build();

        // display reactions to choose
        ReactionPopup popup = new ReactionPopup(context, config, (pos) -> {
            if (holder.getClass() == SentViewHolder.class){
                SentViewHolder viewHolder = (SentViewHolder) holder;
                viewHolder.binding.imgFeelingSend.setImageResource(reactions[pos]);
                viewHolder.binding.imgFeelingSend.setVisibility(View.VISIBLE);
            } else {
                ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
                viewHolder.binding.imgFeelingReceive.setImageResource(reactions[pos]);
                viewHolder.binding.imgFeelingReceive.setVisibility(View.VISIBLE);
            }

            message.setFeeling(pos);

            FirebaseDatabase.getInstance().getReference()
                    .child("public")
                    .child(message.getMessageId()).setValue(message);

            return true; // true is closing popup, false is requesting a new selection
        });

        if (holder.getClass() == SentViewHolder.class){
            SentViewHolder viewHolder = (SentViewHolder) holder;

            // check image visible and text gone
            if (message.getMessage().equals("photo")){
                viewHolder.binding.imageSend.setVisibility(View.VISIBLE);
                viewHolder.binding.tvMessageSend.setVisibility(View.GONE);
                Glide.with(context)
                        .load(message.getImageUrl())
                        .placeholder(R.drawable.placeholder)
                        .into(viewHolder.binding.imageSend);
            }

            FirebaseDatabase.getInstance().getReference().child("users")
                    .child(message.getSenderId())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                User user = snapshot.getValue(User.class);
                                viewHolder.binding.name.setText("@" + user.getUserName());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

            viewHolder.binding.tvMessageSend.setText(message.getMessage());

            // visible reaction sender
            if (message.getFeeling() >= 0 ) {
//                message.setFeeling(reactions[(int) message.getFeeling()]);
                // display correct reaction among 2 person
                viewHolder.binding.imgFeelingSend.setImageResource(reactions[message.getFeeling()]);
                viewHolder.binding.imgFeelingSend.setVisibility(View.VISIBLE);
            }else{
                viewHolder.binding.imgFeelingSend.setVisibility(View.GONE);
            }

            // touch reaction sender
            viewHolder.binding.tvMessageSend.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v, event);
                    return false;
                }
            });

            // touch reaction sender
            viewHolder.binding.imageSend.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v, event);
                    return false;
                }
            });

        }else{
            ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;

            // check image visible and text gone
            if (message.getMessage().equals("photo")){
                viewHolder.binding.imageReceive.setVisibility(View.VISIBLE);
                viewHolder.binding.tvMessageReceive.setVisibility(View.GONE);
                Glide.with(context)
                        .load(message.getImageUrl())
                        .placeholder(R.drawable.placeholder)
                        .into(viewHolder.binding.imageReceive);
            }

            FirebaseDatabase.getInstance().getReference().child("users")
                    .child(message.getSenderId())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                User user = snapshot.getValue(User.class);
                                viewHolder.binding.name.setText("@" + user.getUserName());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

            viewHolder.binding.tvMessageReceive.setText(message.getMessage());

            // visible reaction receiver
            if (message.getFeeling() >= 0 ) {
//                message.setFeeling(reactions[(int) message.getFeeling()]);
                viewHolder.binding.imgFeelingReceive.setImageResource(reactions[message.getFeeling()]);
                viewHolder.binding.imgFeelingReceive.setVisibility(View.VISIBLE);
            }else{
                viewHolder.binding.imgFeelingReceive.setVisibility(View.GONE);
            }

            // touch reaction receiver
            viewHolder.binding.tvMessageReceive.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v, event);
                    return false;
                }
            });

            viewHolder.binding.imageReceive.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v, event);
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return messages == null ? 0: messages.size();
    }

    public class SentViewHolder extends RecyclerView.ViewHolder {

        ItemSendGroupBinding binding;
        public SentViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemSendGroupBinding.bind(itemView);

        }
    }

    public class ReceiverViewHolder extends RecyclerView.ViewHolder {

        ItemReceiveGroupBinding binding;
        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemReceiveGroupBinding.bind(itemView);
        }
    }
}
