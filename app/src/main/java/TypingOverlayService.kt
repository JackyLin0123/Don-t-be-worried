package com.example.typingoverlay

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.LinearLayout

class TypingOverlayService : Service() {

    companion object {
        const val ACTION_START = "start"
        const val ACTION_STOP = "stop"
        private const val CHANNEL_ID = "typing_overlay"
        private const val NOTI_ID = 42
    }

    private lateinit var wm: WindowManager
    private lateinit var lp: WindowManager.LayoutParams
    private var bubble: View? = null
    private val animators = mutableListOf<ObjectAnimator>()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> { stopSelf(); return START_NOT_STICKY }
            else -> startOverlay()
        }
        return START_STICKY
    }

    private fun startOverlay() {
        if (bubble != null) return
        startAsForeground()

        wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val view = buildBubble()
        bubble = view

        lp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,   // API 26+
            // 关键：不抢焦点 + 窗口外触摸穿透给微信，否则会挡住对方打字/点击
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = dp(16)
            y = (resources.displayMetrics.heightPixels * 0.62f).toInt()
        }

        attachDrag(view)
        wm.addView(view, lp)
        startDots(view as LinearLayout)
    }

    // ====== 浮窗外观：白色圆角气泡 + 三个灰点（对应网页的 .bubble.typing） ======
    private fun buildBubble(): View {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(13), dp(16), dp(13))
            background = GradientDrawable().apply {
                cornerRadius = dp(20).toFloat()
                setColor(Color.WHITE)
                setStroke(dp(1), Color.parseColor("#11000000"))
            }
            elevation = dp(6).toFloat()
        }
        // 想加文字「对方正在输入」：在这里先 addView 一个 TextView 即可
        val size = dp(8)
        repeat(3) { i ->
            val dot = View(this).apply {
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(Color.parseColor("#A2A9B4"))
                }
                alpha = 0.45f
            }
            val params = LinearLayout.LayoutParams(size, size).apply {
                if (i > 0) leftMargin = dp(6)
            }
            container.addView(dot, params)
        }
        return container
    }

    // ====== 三点交错跳动 + 明暗（对应网页的 @keyframes blink） ======
    private fun startDots(group: LinearLayout) {
        for (i in 0 until group.childCount) {
            val dot = group.getChildAt(i)
            val move = ObjectAnimator.ofFloat(dot, "translationY", 0f, -dp(7).toFloat(), 0f).apply {
                duration = 700
                repeatCount = ObjectAnimator.INFINITE
                startDelay = i * 140L          // 三个点相位错开，形成滚动感
                interpolator = AccelerateDecelerateInterpolator()
            }
            val fade = ObjectAnimator.ofFloat(dot, "alpha", 0.45f, 1f, 0.45f).apply {
                duration = 700
                repeatCount = ObjectAnimator.INFINITE
                startDelay = i * 140L
            }
            move.start(); fade.start()
            animators.add(move); animators.add(fade)
        }
    }

    // ====== 拖动浮窗 ======
    @SuppressLint("ClickableViewAccessibility")
    private fun attachDrag(view: View) {
        var startX = 0; var startY = 0; var touchX = 0f; var touchY = 0f
        view.setOnTouchListener { _, e ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = lp.x; startY = lp.y; touchX = e.rawX; touchY = e.rawY; true
                }
                MotionEvent.ACTION_MOVE -> {
                    lp.x = startX + (e.rawX - touchX).toInt()
                    lp.y = startY + (e.rawY - touchY).toInt()
                    wm.updateViewLayout(view, lp); true
                }
                else -> false
            }
        }
    }

    // ====== 前台服务 + 常驻通知（点通知即可关闭浮窗） ======
    private fun startAsForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "浮窗", NotificationManager.IMPORTANCE_LOW)
            )
        }
        val stopPI = PendingIntent.getService(
            this, 0,
            Intent(this, TypingOverlayService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE
        )
        val noti = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("正在输入浮窗")
            .setContentText("显示中 · 点这里关闭")
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentIntent(stopPI)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {  // API 34+
            startForeground(NOTI_ID, noti, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTI_ID, noti)
        }
    }

    override fun onDestroy() {
        animators.forEach { it.cancel() }
        animators.clear()
        bubble?.let { v -> runCatching { wm.removeView(v) } }
        bubble = null
        super.onDestroy()
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
}
