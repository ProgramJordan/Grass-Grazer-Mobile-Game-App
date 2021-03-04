package com.example.finalprojectdkjw;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class StartActivity extends AppCompatActivity implements View.OnClickListener {

    private MediaPlayer mediaPlayer;
    private Button onoff;
    private LinearLayout mainMenu;
    private LinearLayout highScoreMenu;
    private LinearLayout difficultyMenu;
    private Button btnStartGame;
    public static Button btnTryAgain;
    private Button btnHomeScreen;
    private Button btnScoreboard;
    private Button btnEasy;
    private Button btnHard;
    private Button btnReturn;
    private Button btnScoreEasy;
    private Button btnScoreHard;
    private String scoreDifficulty = "easy";
    private String difficulty;

    private SharedPreferences prefs;
    private String mode;

    public static boolean isActivityVisible;

    // Database
    // Easy mode
    public static DatabaseReference easyPlayerDB;
    public static ArrayList<Player> easyPlayerList;
    private RecyclerView easyScoreboardRecyclerView;
    private EasyPlayerAdapter easyAdapter;
    // Hard mode
    public static DatabaseReference hardPlayerDB;
    public static ArrayList<Player> hardPlayerList;
    private RecyclerView hardScoreboardRecyclerView;
    private HardPlayerAdapter hardAdapter;

    // Notification
    private NotificationManagerCompat notificationManager;
    public static CountDownTimer timer;
    private long timeLeftInMilliseconds = 6000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        isActivityVisible = true;

        notificationManager = NotificationManagerCompat.from(this);

        mainMenu = findViewById(R.id.mainMenu);
        highScoreMenu = findViewById(R.id.highScoreMenu);
        difficultyMenu = findViewById(R.id.difficultyMenu);
        btnStartGame = findViewById(R.id.btnStartGame);
        btnTryAgain = findViewById(R.id.btnTryAgain);
        btnHomeScreen = findViewById(R.id.btnHomeScreen);
        btnScoreboard = findViewById(R.id.btnScoreboard);
        btnEasy = findViewById(R.id.btnEasy);
        btnHard = findViewById(R.id.btnHard);
        btnReturn = findViewById(R.id.btnReturn);
        btnScoreEasy = findViewById(R.id.btnScoreEasy);
        btnScoreHard = findViewById(R.id.btnScoreHard);

        // Easy mode
        LinearLayoutManager llmEasy = new LinearLayoutManager(this);
        llmEasy.setOrientation(LinearLayoutManager.VERTICAL);
        llmEasy.setReverseLayout(true);
        llmEasy.setStackFromEnd(true);
        easyScoreboardRecyclerView = findViewById(R.id.easyScoreboardRecyclerView);
        easyPlayerList = new ArrayList<>();
        easyScoreboardRecyclerView.setHasFixedSize(true);
        easyScoreboardRecyclerView.setLayoutManager(llmEasy);
        easyPlayerDB = FirebaseDatabase.getInstance().getReference("GrassGrazerEasy");
        easyPlayerDB.keepSynced(true);

        // Hard mode
        LinearLayoutManager llmHard = new LinearLayoutManager(this);
        llmHard.setOrientation(LinearLayoutManager.VERTICAL);
        llmHard.setReverseLayout(true);
        llmHard.setStackFromEnd(true);
        hardScoreboardRecyclerView = findViewById(R.id.hardScoreboardRecyclerView);
        hardPlayerList = new ArrayList<>();
        hardScoreboardRecyclerView.setHasFixedSize(true);
        hardScoreboardRecyclerView.setLayoutManager(llmHard);
        hardPlayerDB = FirebaseDatabase.getInstance().getReference("GrassGrazerHard");
        hardPlayerDB.keepSynced(true);

        btnStartGame.setOnClickListener(this);
        btnTryAgain.setOnClickListener(this);
        btnHomeScreen.setOnClickListener(this);
        btnScoreboard.setOnClickListener(this);
        btnEasy.setOnClickListener(this);
        btnHard.setOnClickListener(this);
        btnReturn.setOnClickListener(this);
        btnScoreEasy.setOnClickListener(this);
        btnScoreHard.setOnClickListener(this);

        prefs = getSharedPreferences("SharedPlace", MODE_PRIVATE);


//-------Mute-UnMute Takes over when clicked once.
        onoff = findViewById(R.id.mutebutton);
        onoff.setOnClickListener(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }


    }

    @Override
    protected void onStart() {
        super.onStart();
        if (timer != null) {
            timer.cancel();
        }

        //------- SOUNDS
        mediaPlayer = MediaPlayer.create(this, R.raw.happyloop);

        //-------temp to make music start when app opens.
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        } else {
            mediaPlayer.start();
            mediaPlayer.setVolume(1.0f, 1.0f);
            mediaPlayer.setLooping(true);
        }

        difficulty = getIntent().getStringExtra("difficulty");
        if (difficulty == null) {
            difficulty = "easy";
        }

        // Hard mode
        hardPlayerDB.orderByChild("score").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                hardPlayerList.clear();
                for (DataSnapshot playerSnapshot : dataSnapshot.getChildren()) {
                    Player player = playerSnapshot.getValue(Player.class);
                    hardPlayerList.add(player);
                }
                hardAdapter = new HardPlayerAdapter(StartActivity.this, hardPlayerList);
                hardScoreboardRecyclerView.setAdapter(hardAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // Easy mode
        easyPlayerDB.orderByChild("score").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                easyPlayerList.clear();
                for (DataSnapshot playerSnapshot : dataSnapshot.getChildren()) {
                    Player player = playerSnapshot.getValue(Player.class);
                    easyPlayerList.add(player);
                }
                easyAdapter = new EasyPlayerAdapter(StartActivity.this, easyPlayerList);
                easyScoreboardRecyclerView.setAdapter(easyAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        scoreDifficulty = getIntent().getStringExtra("difficulty");
        if (scoreDifficulty == null) {
            scoreDifficulty = "easy";
        }
        mode = getIntent().getStringExtra("mode");
        if (TextUtils.isEmpty(mode)) {
            mode = "main";
        }
        setMenuVisibility(mode);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mutebutton:
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    Toast.makeText(getApplicationContext(), "Music Paused", Toast.LENGTH_SHORT).show();
                    onoff.setBackgroundResource(R.drawable.pausets);

                } else {
                    mediaPlayer.start();
                    Toast.makeText(getApplicationContext(), "Music Resumed", Toast.LENGTH_SHORT).show();
                    mediaPlayer.setLooping(true);
                    onoff.setBackgroundResource(R.drawable.playt);
                    mediaPlayer.setVolume(1.0f, 1.0f);
                }
                break;
            case R.id.btnStartGame:
            case R.id.btnTryAgain:
                mode = "difficulty";
                setMenuVisibility(mode);
                prefs.edit().putString("mode", mode).apply();
                break;
            case R.id.btnHomeScreen:
            case R.id.btnReturn:
                mode = "main";
                setMenuVisibility(mode);
                prefs.edit().putString("mode", mode).apply();
                getIntent().removeExtra("mode");
                break;
            case R.id.btnScoreboard:
                btnScoreEasy.performClick();
                mode = "highScore";
                setMenuVisibility(mode);
                btnTryAgain.setVisibility(View.GONE);
                prefs.edit().putString("mode", mode).apply();
                break;
            case R.id.btnEasy:
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("difficulty", "easy");
                sendPlayerList(intent, easyPlayerList);
                startActivity(intent);
                prefs.edit().putString("mode", mode).apply();
                break;
            case R.id.btnHard:
                intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("difficulty", "hard");
                sendPlayerList(intent, hardPlayerList);
                startActivity(intent);
                prefs.edit().putString("mode", mode).apply();
                break;
            case R.id.btnScoreEasy:
                btnScoreHard.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.hardscores, null));
                btnScoreEasy.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.easyscores_hl, null));
                easyScoreboardRecyclerView.setVisibility(View.VISIBLE);
                hardScoreboardRecyclerView.setVisibility(View.GONE);
                break;
            case R.id.btnScoreHard:
                btnScoreEasy.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.easyscores, null));
                btnScoreHard.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.hardscores_hl, null));
                hardScoreboardRecyclerView.setVisibility(View.VISIBLE);
                easyScoreboardRecyclerView.setVisibility(View.GONE);
                break;
        }
    }

    private void sendPlayerList(Intent intent, ArrayList<Player> playerList) {
        int i = 0;
        for (Player p : playerList) {
            intent.putExtra(String.valueOf(i), p.toString());
            i++;
        }
    }

    private void setMenuVisibility(String mode) {
        switch(mode) {
            case "highScore":
                if (difficulty.equals("hard")) {
                    btnScoreHard.performClick();
                } else {
                    btnScoreEasy.performClick();
                }
                highScoreMenu.setVisibility(View.VISIBLE);
                mainMenu.setVisibility(View.GONE);
                difficultyMenu.setVisibility(View.GONE);
                break;
            case "difficulty":
                difficultyMenu.setVisibility(View.VISIBLE);
                highScoreMenu.setVisibility(View.GONE);
                mainMenu.setVisibility(View.GONE);
                break;
            default:
                mainMenu.setVisibility(View.VISIBLE);
                highScoreMenu.setVisibility(View.GONE);
                difficultyMenu.setVisibility(View.GONE);
                break;
        }
    }

    //-------Disable Return Button During Game Play
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_BACK:
                    return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

//------- I do not no why this continues to work without the above...
    public void stopPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
//            Toast.makeText(this, "Media Player Released", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopPlayer();
        isActivityVisible = false;
        if (!MainActivity.isActivityVisible) {
            startTimer();
        }
    }

    private void startTimer() {
        timer = new CountDownTimer(timeLeftInMilliseconds, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                sendNotification();
            }
        }.start();
    }

    private void sendNotification() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Notification notification = new Notification.Builder(this, App.CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setContentTitle("Come Back and Play Again")
                    .setContentText("Come Back and Play Again to Beat the Top 5 High Scores")
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .build();
            notificationManager.notify(1, notification);
        }
    }
}

