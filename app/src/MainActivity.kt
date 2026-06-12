package com.example.typingoverlay

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pad = dp(24)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
            gravity = Gravity.CENTER_HORIZONTAL
        }

        val title = TextView(this).apply {
            text = "「对方正在输入」浮窗"
            textSize = 20f
            setPadding(0, 0, 0, pad)
        }
        val tip = TextView(this).apply {
            text = "首次点「显示浮窗」会跳到系统设置授权，授权后回来再点一次。"
            textSize = 13f
            setPadding(0, 0, 0, pad)
        }

        val btnShow = Button(this).apply { text = "显示浮窗" }
        val btnHide = Button(this).apply { text = "关闭浮窗" }

        btnShow.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "请先授予「显示在其它应用上层」权限", Toast.LENGTH_SHORT).show()
                startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                )
                return@setOnClickListener
            }
            ensureNotificationPermission()
            val i = Intent(this, TypingOverlayService::class.java)
                .setAction(TypingOverlayService.ACTION_START)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(i)
            else startService(i)
        }

        btnHide.setOnClickListener {
            startService(
                Intent(this, TypingOverlayService::class.java)
                    .setAction(TypingOverlayService.ACTION_STOP)
            )
        }

        root.addView(title)
        root.addView(tip)
        root.addView(btnShow)
        root.addView(btnHide)
        setContentView(root)
    }

    /** Android 13+ 需运行时申请通知权限，前台通知才能正常显示 */
    private fun ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
        }
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
}
