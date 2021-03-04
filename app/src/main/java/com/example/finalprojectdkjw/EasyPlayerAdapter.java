package com.example.finalprojectdkjw;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class EasyPlayerAdapter extends RecyclerView.Adapter<EasyPlayerAdapter.PlayerViewHolder> {

    Context context;
    private ArrayList<Player> players;

    public EasyPlayerAdapter(Context context, ArrayList<Player> players) {
        this.context = context;
        this.players = players;
    }

    @NonNull
    @Override
    public EasyPlayerAdapter.PlayerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PlayerViewHolder(LayoutInflater.from(context).inflate(R.layout.easy_score_row, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull EasyPlayerAdapter.PlayerViewHolder holder, int position) {
        holder.id.setText(String.valueOf(getItemCount() - position));
        holder.name.setText(String.valueOf(players.get(position).getName()));
        holder.score.setText(String.valueOf(players.get(position).getScore()));
    }

    @Override
    public int getItemCount() {
        return players.size();
    }

    public class PlayerViewHolder extends RecyclerView.ViewHolder {

        TextView id, name, score;

        public PlayerViewHolder(@NonNull View itemView) {
            super(itemView);
            id = itemView.findViewById(R.id.txtId);
            name = itemView.findViewById(R.id.txtName);
            score = itemView.findViewById(R.id.txtScore);
        }
    }
}
