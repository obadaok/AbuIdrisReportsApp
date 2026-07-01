package com.abuidris.reports.util;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import com.abuidris.reports.R;

/**
 * SoundManager: يدير الأصوات في التطبيق (نقرة الزرار، إلخ).
 * 
 * يستخدم SoundPool بدل MediaPlayer لأن:
 * - SoundPool مصمم للأصوات القصيرة (click, beep) ولا يمنع الـ UI thread أبدًا
 * - play() غير متزامن (asynchronous) ويرجع فورًا بدون أي تأخير
 * - حتى لو الصوت ما تحمل بعد، play() ترجع 0 بهدوء وما تعلق
 * - ما في أي عملية I/O على الـ main thread
 * 
 * MediaPlayer كان يسبب مشاكل لأن create() يقرأ من القرص (disk I/O)
 * على الـ main thread، مما يمنع الـ press animation ويسبب جف (jank).
 * 
 * نمط التصميم: Singleton
 * - instance واحدة للمشروع كامل.
 * - synchronized عشان التعددية.
 */
public class SoundManager {

    private static SoundManager instance;
    private SoundPool soundPool;
    private int clickSoundId;

    /**
     * Constructor: ينشئ SoundPool ويبدأ تحميل الصوت (غير متزامن).
     * الصوت يكون جاهز عادة خلال أول 100ms من فتح التطبيق.
     * لو المستخدم نقر قبل ما يتحمل، play() ترجع 0 بهدوء (ما في صوت).
     * هذا أفضل من منع الـ UI thread.
     */
    private SoundManager(Context context) {
        AudioAttributes attrs = new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build();
        soundPool = new SoundPool.Builder()
            .setMaxStreams(3)
            .setAudioAttributes(attrs)
            .build();
        // load() غير متزامن — يرجع فورًا ويحمل في خلفية
        clickSoundId = soundPool.load(context.getApplicationContext(), R.raw.click, 1);
    }

    public static synchronized SoundManager getInstance(Context context) {
        if (instance == null) {
            instance = new SoundManager(context);
        }
        return instance;
    }

    /**
     * تشغيل صوت النقرة.
     * SoundPool.play() غير متزامن ولا يمنع الـ UI thread مطلقًا.
     * إذا الصوت ما تحمل بعد (أول نقرة بعد فتح التطبيق)، play() ترجع 0
     * وما يصير أي صوت — بدون تعليق ولا crash.
     * 
     * @param volumeLevel مستوى الصوت (0.0 إلى 1.0)
     */
    /**
     * preload: يبدأ تحميل الصوت من البداية (في خلفية) عشان يكون جاهز
     * أول ما المستخدم ينقر. ينادى من AbuIdrisApp.onCreate().
     */
    public void preload() {
        // الـ constructor شغل load()، مجرد نطمن أن المثبت أنشئ
    }

    public void playClick() {
        playClick(1.0f);
    }

    public void playClick(float volumeLevel) {
        try {
            if (soundPool != null && clickSoundId != 0) {
                soundPool.play(clickSoundId, volumeLevel, volumeLevel, 1, 0, 1.0f);
            }
        } catch (Exception ignored) {
            // ما يوقف التطبيق لو صار خطأ غير متوقع
        }
    }
}
