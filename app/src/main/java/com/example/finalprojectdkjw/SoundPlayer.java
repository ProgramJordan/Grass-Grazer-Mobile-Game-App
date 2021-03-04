package com.example.finalprojectdkjw;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.util.Log;

public class SoundPlayer {

    private AudioAttributes audioAttributes;
    final int SOUND_POOL_MAX = 2;

    private static SoundPool soundPool;
    private static int hitSound;
    private static int overSound;
    private static int menuMusic;
    private static int arrowSound;
    private static int jumpSound;

    public SoundPlayer(Context context) {

        // SoundPool is deprecated in API level 21. (Lollipop)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();

            soundPool = new SoundPool.Builder()
                    .setAudioAttributes(audioAttributes)
                    .setMaxStreams(SOUND_POOL_MAX)
                    .build();

        } else {
            //SoundPool (int maxStreams, int streamType, int srcQuality)
            soundPool = new SoundPool(SOUND_POOL_MAX, AudioManager.STREAM_MUSIC, 0);
        }

        menuMusic = soundPool.load(context, R.raw.mainmusic, 1);
        arrowSound = soundPool.load(context, R.raw.arrowdmg, 1);

        jumpSound = soundPool.load(context,R.raw.jumping,1);

        hitSound = soundPool.load(context, R.raw.arrowdmg, 1);
        overSound = soundPool.load(context, R.raw.bowarrow, 1);

    }

    public void playHitSound() {

        // play(int soundID, float leftVolume, float rightVolume, int priority, int loop, float rate)
        soundPool.play(hitSound, 1.0f, 1.0f, 1, 0, 1.0f);
    }

    public void playOverSound() {
        soundPool.play(overSound, 1.0f, 1.0f, 1, 0, 1.0f);
    }

    public void playMenuMusic() {
        soundPool.play(menuMusic, 1.0f, 1.0f, 1, 0, 1.0f);
    }

    public void playJumpSound() {
        soundPool.play(jumpSound, 0.7f, 0.7f, 1, 0, 1.0f);
    }


}
