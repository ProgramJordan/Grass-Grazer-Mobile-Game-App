package com.example.finalprojectdkjw;
        import androidx.annotation.NonNull;
        import androidx.appcompat.app.AppCompatActivity;
        import androidx.core.app.NotificationCompat;
        import androidx.core.app.NotificationManagerCompat;

        import android.app.Notification;
        import android.content.Context;
        import android.content.SharedPreferences;
        import android.media.MediaPlayer;
        import android.os.Bundle;
        import android.content.Intent;
        import android.graphics.Point;
        import android.os.CountDownTimer;
        import android.os.Handler;
        import android.text.TextUtils;
        import android.util.Log;
        import android.view.Display;
        import android.view.KeyEvent;
        import android.view.MotionEvent;
        import android.view.View;
        import android.view.WindowManager;
        import android.view.inputmethod.EditorInfo;
        import android.view.inputmethod.InputMethodManager;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.FrameLayout;
        import android.widget.ImageView;
        import android.widget.LinearLayout;
        import android.widget.TextView;
        import android.widget.Toast;

        import com.google.firebase.database.DataSnapshot;
        import com.google.firebase.database.DatabaseError;
        import com.google.firebase.database.DatabaseReference;
        import com.google.firebase.database.ValueEventListener;

        import java.lang.reflect.Array;
        import java.util.ArrayList;
        import java.util.Collection;
        import java.util.Timer;
        import java.util.TimerTask;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, TextView.OnEditorActionListener {

    private LinearLayout resultScreen;
    private LinearLayout resultScreen_nosave;
    private LinearLayout gameScreen;
    private Button btnSubmitInitial;
    private Button btnContinue;
    private TextView txtScore;
    private TextView txtScore_nosave;
    private EditText txtInitial;

    private InputMethodManager imm;
    private DatabaseReference.CompletionListener removeListener;

    private String difficulty;
    private ArrayList<Player> playerList;

    //Game Labels & Objects
    private TextView scoreLabel;
    private ImageView startLabel;
    private ImageView packyak;
    private ImageView dirtyGrass;
    private ImageView cleanGrass;
    private ImageView arrows;
    // Game Score
    private int score = 0;
    // Objects X/Y Position
    private int packyakY;
    private int dirtyGrassX;
    private int dirtyGrassY;
    private int cleanGrassX;
    private int cleanGrassY;
    private int arrowsX;
    private int arrowsY;

    // Object Size + Screen emulator size
    private int frameHeight;
    private int yakSize;
    private int screenWidth;
    private int screenHeight;

    // Object speed
    private int yakSpeed;
    private int dirtyGrassSpeed;
    private int cleanGrassSpeed;
    private int arrowSpeed;
    private int speedFactor;
    private int speedFactorDirty;
    private int speedFactorClean;

    // Initialize Class
    private Handler handler = new Handler();
    private Timer timer = new Timer();
    private SoundPlayer sound;
    private SoundPlayer menuSound;
    private MediaPlayer mediaPlayer;

    // Status Check
    private boolean action_flg = false;
    private boolean start_flg = false;

    // Notification
    private NotificationManagerCompat notificationManager;
    private long timeLeftInMilliseconds = 6000;
    public static boolean isActivityVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        notificationManager = NotificationManagerCompat.from(this);
        isActivityVisible = true;

        resultScreen = findViewById(R.id.resultScreen);
        resultScreen_nosave = findViewById(R.id.resultScreen_nosave);
        gameScreen = findViewById(R.id.gameScreen);
        btnSubmitInitial = findViewById(R.id.btnSubmitInitial);
        btnContinue = findViewById(R.id.btnContinue);
        txtScore = findViewById(R.id.txtScore);
        txtScore_nosave = findViewById(R.id.txtScore_nosave);
        txtInitial = findViewById(R.id.txtInitial);

        imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        removeListener = new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError error, DatabaseReference ref) {
                if (error == null) {
                    Log.d("ON REMOVE", "Successfully Removed: " + ref);
                } else {
                    Log.e("ON REMOVE", "Remove of " + ref + " failed: " + error.getMessage());
                }
            }
        };

        btnSubmitInitial.setOnClickListener(this);
        txtInitial.setOnEditorActionListener(this);
        btnContinue.setOnClickListener(this);

        //hide that action bar
        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }

        //sound
        sound = new SoundPlayer(this);

        //a.studio fixed itself.
        scoreLabel =  findViewById(R.id.scoreLabel);
        startLabel =  findViewById(R.id.startLabel);
        packyak =  findViewById(R.id.packyack);
        dirtyGrass =  findViewById(R.id.dirtygrass);
        cleanGrass =  findViewById(R.id.cleangrass);
        arrows =  findViewById(R.id.arrows);

        // Position objects out of screen.
        dirtyGrass.setX(-80);
        dirtyGrass.setY(-80);
        cleanGrass.setX(-80);
        cleanGrass.setY(-80);
        arrows.setX(-80);
        arrows.setY(-80);

        // Get screen size.
        WindowManager wm = getWindowManager();
        Display disp = wm.getDefaultDisplay();
        Point size = new Point();
        disp.getSize(size);

        screenWidth = size.x;
        screenHeight = size.y;

        // Resolution Example: (Width:768 x Height:1280)
        // Speed Pack Yak:20 / Dirty Grass:12 /  Clean Grass:20 / Arrows:16
        // Greater Number -> Greater Speed
        difficulty = getIntent().getStringExtra("difficulty");
        if (difficulty == null) {
            difficulty = "easy";
        }
        if (difficulty.equals("hard")) {
            speedFactorClean = 6;
            speedFactorDirty = 25;
            speedFactor = 15;
        } else {
            speedFactorClean = 0;
            speedFactorDirty = 0;
            speedFactor = 0;
        }

        // Get playerlist
        playerList = getPlayerList();

        yakSpeed = Math.round(screenHeight / 60);  // 1280 / 60 = 21.333... => 21
        dirtyGrassSpeed = Math.round(screenWidth / (60 - speedFactorDirty)); // 768 / 60 = 12.8 => 13
        cleanGrassSpeed = Math.round(screenWidth / (36 - speedFactorClean));   // 768 / 36 = 21.333... => 21
        arrowSpeed = Math.round(screenWidth / (45 - speedFactor)); // 768 / 45 = 17.06... => 17

        // Position starting objects out of view.
        dirtyGrass.setX(-80);
        dirtyGrass.setY(-80);
        cleanGrass.setX(-80);
        cleanGrass.setY(-80);
        arrows.setX(-80);
        arrows.setY(-80);

        //New game score
        scoreLabel.setText("0");
    }

    @Override
    protected void onStart() {
        super.onStart();
        resultScreen.setVisibility(View.GONE);
        resultScreen_nosave.setVisibility(View.GONE);
        gameScreen.setVisibility(View.VISIBLE);

        if (StartActivity.timer != null) {
            StartActivity.timer.cancel();
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
    }

    public void changePos() {

        hitCheck();

        // Dirty Grass -  Random Position based on Frame height and current height of object
        dirtyGrassX -= dirtyGrassSpeed;
        if (dirtyGrassX < 0) {
            dirtyGrassX = screenWidth + 20;
            dirtyGrassY = (int) Math.floor(Math.random() * (frameHeight - dirtyGrass.getHeight()));
        }
        dirtyGrass.setX(dirtyGrassX);
        dirtyGrass.setY(dirtyGrassY);


        // Arrows
        arrowsX -= arrowSpeed;
        if (arrowsX < 0) {
            arrowsX = screenWidth + 10;
            arrowsY = (int) Math.floor(Math.random() * (frameHeight - arrows.getHeight()));
        }
        arrows.setX(arrowsX);
        arrows.setY(arrowsY);


        // Clean Grass
        cleanGrassX -= cleanGrassSpeed;
        if (cleanGrassX < 0) {
            cleanGrassX = screenWidth + 5000;
            cleanGrassY = (int) Math.floor(Math.random() * (frameHeight - cleanGrass.getHeight()));
        }
        cleanGrass.setX(cleanGrassX);
        cleanGrass.setY(cleanGrassY);


        // Move Pack Yak
        if (action_flg == true) {
            // Touch
            packyakY -= yakSpeed;

        } else {
            // Release
            packyakY += yakSpeed;
        }

        // Check packyak position.
        if (packyakY < 0) packyakY = 0;
        if (packyakY > frameHeight - yakSize) packyakY = frameHeight - yakSize;
        packyak.setY(packyakY);

        // Display score
        scoreLabel.setText(String.valueOf(score));

    }
    public void hitCheck() {

        // If the center of the object is in the packyak, it counts as a hit.

        // Dirty Grass
        int dirtyGrassCenterX = dirtyGrassX + dirtyGrass.getWidth() / 2;
        int dirtyGrassCenterY = dirtyGrassY + dirtyGrass.getHeight() / 2;

        // 0 <= dirtyGrassCenterX <= boxWidth
        // packyakY <= dirtyGrassCenterY <= packyakY + boxHeight

        if (0 <= dirtyGrassCenterX && dirtyGrassCenterX <= yakSize &&
                packyakY <= dirtyGrassCenterY && dirtyGrassCenterY <= packyakY + yakSize) {
            score += 10;
            dirtyGrassX = -10;
            sound.playHitSound();
        }

        // Clean Grass
        int cleanGrassCenterX = cleanGrassX + cleanGrass.getWidth() / 2;
        int cleanGrassCenterY = cleanGrassY + cleanGrass.getHeight() / 2;

        if (0 <= cleanGrassCenterX && cleanGrassCenterX <= yakSize &&
                packyakY <= cleanGrassCenterY && cleanGrassCenterY <= packyakY + yakSize) {

            score += 30;
            cleanGrassX = -10;
            sound.playHitSound();
        }

        // Arrows
        int arrowCenterX = arrowsX + arrows.getWidth() / 2;
        int arrowCenterY = arrowsY + arrows.getHeight() / 2;

        if (0 <= arrowCenterX && arrowCenterX <= yakSize &&
                packyakY <= arrowCenterY && arrowCenterY <= packyakY + yakSize) {

            // Stop Timer!!
            timer.cancel();
            timer = null;

            sound.playOverSound();

            // Show Result Screen
            gameScreen.setVisibility(View.GONE);
            if (isTopFive(score)) {
                resultScreen.setVisibility(View.VISIBLE);
                resultScreen_nosave.setVisibility(View.GONE);
                txtScore.setText(String.valueOf(score));
            } else {
                resultScreen_nosave.setVisibility(View.VISIBLE);
                resultScreen.setVisibility(View.GONE);
                txtScore_nosave.setText(String.valueOf(score));
            }
        }
    }

    // Frames
    public boolean onTouchEvent(MotionEvent player) {

        if (!start_flg ) {

            start_flg = true;

            // get frame height and packyak height here because
            // the UI has not been set on the screen in OnCreate()

            FrameLayout frame = findViewById(R.id.frame);
            frameHeight = frame.getHeight();

            packyakY = (int) packyak.getY();

            // The packyak is a square.(height and width are the same.)
            yakSize = packyak.getHeight();

            startLabel.setVisibility(View.GONE);

            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            changePos();
                        }
                    });
                }
            }, 0, 20);
        } else {
            if (player.getAction() == MotionEvent.ACTION_DOWN) {
                action_flg = true;
                sound.playJumpSound();

            } else if (player.getAction() == MotionEvent.ACTION_UP) {
//                 sound.playJumpSound();
                action_flg = false;
            }
        }
        return true;
    }

    // Disable Return Button while game active
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

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        int keyCode = -1;
        if (event != null) {
            keyCode = event.getKeyCode();
        }
        if (actionId == EditorInfo.IME_ACTION_DONE ||
                actionId == EditorInfo.IME_ACTION_UNSPECIFIED ||
                keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                keyCode == KeyEvent.KEYCODE_ENTER) {
            btnSubmitInitial.requestFocus();
            imm.hideSoftInputFromWindow(txtInitial.getWindowToken(), 0);
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSubmitInitial:
                String name = txtInitial.getText().toString().toUpperCase().trim();
                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(this, "Please enter your initial", Toast.LENGTH_SHORT).show();
                } else if (txtInitial.getText().toString().length() < 3) {
                    Toast.makeText(this, "Please enter 3 characters", Toast.LENGTH_SHORT).show();
                } else {
                    String mode = "highScore";
                    StartActivity.btnTryAgain.setVisibility(View.VISIBLE);
                    if (isTopFive(score)) updateScore(name);
                    Intent intent = new Intent(getApplicationContext(), StartActivity.class);
                    intent.putExtra("mode", mode);
                    intent.putExtra("difficulty", difficulty);
                    startActivity(intent);
                }
                break;
            case R.id.btnContinue:
                String mode = "highScore";
                StartActivity.btnTryAgain.setVisibility(View.VISIBLE);
                Intent intent = new Intent(getApplicationContext(), StartActivity.class);
                intent.putExtra("mode", mode);
                intent.putExtra("difficulty", difficulty);
                startActivity(intent);
                break;
        }
    }

    private void updateScore(String name) {
        Player p = getPlayerByName(name);

        if (difficulty.equals("hard")) {
            if (p == null) {
                // add
                String id = StartActivity.hardPlayerDB.push().getKey();
                p = new Player(id, name, score);
                StartActivity.hardPlayerDB.child(id).setValue(p);
                if (playerList.size() > 4) {
                    ArrayList<Player> sortedList = sortPlayerList(playerList);
                    int size = sortedList.size();
                    for (int i = 4; i < size; i++) {
                        StartActivity.hardPlayerDB.child(sortedList.get(i).get_id()).removeValue(removeListener);
                    }
                }
                Toast.makeText(this, "Player added successfully", Toast.LENGTH_SHORT).show();
            } else {
                // update
                if (p.getScore() < score) StartActivity.hardPlayerDB.child(String.valueOf(p.get_id())).child("score").setValue(score);
                Toast.makeText(this, "Player updated successfully", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (p == null) {
                // add
                String id = StartActivity.easyPlayerDB.push().getKey();
                p = new Player(id, name, score);
                StartActivity.easyPlayerDB.child(id).setValue(p);
                if (playerList.size() > 4) {
                    ArrayList<Player> sortedList = sortPlayerList(playerList);
                    int size = sortedList.size();
                    for (int i = 4; i < size; i++) {
                        StartActivity.easyPlayerDB.child(sortedList.get(i).get_id()).removeValue(removeListener);
                        playerList.remove(sortedList.get(i));
                    }
                }
                Toast.makeText(this, "Player added successfully", Toast.LENGTH_SHORT).show();
            } else {
                // update
                if (p.getScore() < score) StartActivity.easyPlayerDB.child(String.valueOf(p.get_id())).child("score").setValue(score);
                Toast.makeText(this, "Player updated successfully", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Player getPlayerByName(String name) {
        for (Player p : playerList) {
            if (p.getName().equals(name)) {
                return p;
            }
        }
        return null;
    }

    private boolean isTopFive(int score) {
        if (difficulty.equals("hard")) {
            if (playerList.size() < 5) {
                return true;
            }

            ArrayList<Player> list = sortPlayerList(playerList);
            for (int i = 5; i < list.size(); i++) {
                StartActivity.hardPlayerDB.child(list.get(i).get_id()).removeValue(removeListener);
                playerList.remove(list.get(i));
            }

            for (int i = 0; i < 5; i++) {
                if (list.get(i).getScore() < score) {
                    return true;
                }
            }
            return false;
        } else {
            if (playerList.size() < 5) {
                return true;
            }

            ArrayList<Player> list = sortPlayerList(playerList);
            for (int i = 5; i < list.size(); i++) {
                StartActivity.easyPlayerDB.child(list.get(i).get_id()).removeValue(removeListener);
                playerList.remove(list.get(i));
            }

            for (int i = 0; i < 5; i++) {
                if (list.get(i).getScore() < score) {
                    return true;
                }
            }
            return false;
        }
    }

    private ArrayList<Player> sortPlayerList(ArrayList<Player> players){
        ArrayList<Player> sortedList = new ArrayList<>();
        ArrayList<Player> sorted = new ArrayList<>();
        for (Player p : players) {
            sorted.add(p);
        }
        int size = sorted.size();
        for (int i = 0; i < size - 1; i++) {
            Player maxP = new Player();
            maxP.setScore(-1);
            for (Player p : sorted) {
                if (p.getScore() > maxP.getScore()) {
                    maxP = p;
                }
            }
            sortedList.add(maxP);
            sorted.remove(maxP);
        }
        sortedList.add(sorted.get(0));

        return sortedList;
    }

    private ArrayList<Player> getPlayerList() {
        ArrayList<Player> playerList = new ArrayList<>();
        int i = 0;
        String pString = getIntent().getStringExtra(String.valueOf(i));
        while (pString != null && !pString.equals("")) {
            String[] pArray = pString.split(",");
            Player p = new Player(pArray[0], pArray[1], Integer.parseInt(pArray[2]));
            playerList.add(p);
            i++;
            pString = getIntent().getStringExtra(String.valueOf(i));
        }
        return playerList;
    }

    public void stopPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopPlayer();
        isActivityVisible = false;
        if (!StartActivity.isActivityVisible) {
            startTimer();
        }
    }private void startTimer() {
        StartActivity.timer = new CountDownTimer(timeLeftInMilliseconds, 1000) {
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
