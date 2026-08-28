package com.zhiwei.primaryschool

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ResolveInfo
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONObject
import kotlin.math.roundToInt

class LauncherActivity : AppCompatActivity() {
    private lateinit var web: WebView
    private lateinit var grid: RecyclerView
    private lateinit var barBal: TextView
    private lateinit var barTime: TextView
    private lateinit var btnPlay: Button
    private lateinit var adapter: AppAdapter
    private var usingCacheFallback = false
    private var awaitingPerm = false
    private val askedPerms = mutableSetOf<String>()
    private val uiTick = Handler(Looper.getMainLooper())
    private val uiLoop = object : Runnable {
        override fun run() {
            val play = Prefs.isPlayActive()
            updateBar()
            if (!play && grid.visibility == View.VISIBLE) refreshUi()
            if (play) uiTick.postDelayed(this, 500)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Prefs.init(this)
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        setContentView(R.layout.activity_launcher)

        barBal = findViewById(R.id.barBal)
        barTime = findViewById(R.id.barTime)
        btnPlay = findViewById(R.id.btnPlay)
        web = findViewById(R.id.web)
        grid = findViewById(R.id.grid)

        adapter = AppAdapter { launchApp(it) }
        grid.layoutManager = GridLayoutManager(this, 4)
        grid.adapter = adapter

        btnPlay.setOnClickListener { tryPlay() }
        findViewById<View>(R.id.btnParent).setOnClickListener { parentGate() }

        val st = web.settings
        st.javaScriptEnabled = true
        st.domStorageEnabled = true
        st.databaseEnabled = true
        st.javaScriptCanOpenWindowsAutomatically = false
        st.mediaPlaybackRequiresUserGesture = false
        st.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        st.userAgentString = st.userAgentString + " PrimarySchoolTablet/1"
        st.useWideViewPort = true
        st.loadWithOverviewMode = true
        st.setSupportZoom(false)
        st.builtInZoomControls = false
        st.displayZoomControls = false
        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(web, true)
        web.addJavascriptInterface(JsBridge(), "PrimarySchool")
        web.webChromeClient = WebChromeClient()
        web.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(v: WebView, req: WebResourceRequest): Boolean {
                return false
            }

            override fun onReceivedError(
                v: WebView,
                req: WebResourceRequest,
                error: WebResourceError
            ) {
                if (!req.isForMainFrame || usingCacheFallback) return
                usingCacheFallback = true
                v.settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
                v.loadUrl(req.url.toString())
            }

            override fun onPageFinished(v: WebView, url: String) {
                if (!usingCacheFallback && isOnline()) {
                    v.settings.cacheMode = WebSettings.LOAD_DEFAULT
                }
                v.evaluateJavascript(
                    "window.PlayWallet?PlayWallet.get():-1"
                ) { raw ->
                    val n = raw?.trim('"')?.toDoubleOrNull() ?: -1.0
                    if (n >= 0) {
                        Prefs.setBalance(n)
                        updateBar()
                    }
                }
            }
        }
        loadStudy()
        Kiosk.setupOwner(this)
        refreshUi()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Perms.REQ_NOTIFY) stepPerms()
    }

    private fun isOnline(): Boolean {
        val cm = getSystemService(ConnectivityManager::class.java) ?: return false
        val net = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(net) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun loadStudy() {
        usingCacheFallback = !isOnline()
        web.settings.cacheMode = if (usingCacheFallback)
            WebSettings.LOAD_CACHE_ELSE_NETWORK
        else
            WebSettings.LOAD_NO_CACHE
        web.loadUrl(Prefs.studyUrl())
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra("expired", false)) {
            Prefs.endPlay()
            PlayTimerService.stop(this)
        }
        refreshUi()
    }

    override fun onResume() {
        super.onResume()
        if (!Prefs.isPlayActive()) PlayTimerService.stop(this)
        refreshUi()
        uiTick.removeCallbacks(uiLoop)
        uiTick.post(uiLoop)
        if (awaitingPerm) {
            awaitingPerm = false
            stepPerms()
        } else if (!Kiosk.isOwner(this) && !Prefs.sawPerms() && Perms.nextMissing(this) != null) {
            Prefs.setSawPerms()
            explainPerms()
        }
    }

    override fun onPause() {
        uiTick.removeCallbacks(uiLoop)
        super.onPause()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (!Prefs.isPlayActive() && web.canGoBack()) {
            web.goBack()
            return
        }
    }

    private fun refreshUi() {
        val play = Prefs.isPlayActive()
        web.visibility = if (play) View.GONE else View.VISIBLE
        grid.visibility = if (play) View.VISIBLE else View.GONE
        if (play) {
            adapter.submit(launchables())
            PlayTimerService.start(this)
            uiTick.removeCallbacks(uiLoop)
            uiTick.post(uiLoop)
        }
        Kiosk.setPlayMode(this, play)
        updateBar()
    }

    private fun updateBar() {
        val b = Prefs.balance()
        barBal.text = "积分 ${trim(b)}"
        val play = Prefs.isPlayActive()
        btnPlay.isEnabled = !play && b + 1e-9 >= Prefs.POINT_COST
        btnPlay.text = if (play) "正在玩" else "玩 10 分钟"
        if (play) {
            val ms = Prefs.playRemainingMs()
            barTime.text = "剩余 ${fmt(ms)}"
            barTime.visibility = View.VISIBLE
        } else {
            barTime.visibility = View.GONE
        }
    }

    private fun explainPerms() {
        askedPerms.clear()
        awaitingPerm = false
        AlertDialog.Builder(this)
            .setTitle("打开计时权限")
            .setMessage("游戏上显示倒计时、到点拉回练习，需要系统授权。每一项都会先在这里问你，再进设置。")
            .setPositiveButton("开始") { _, _ -> stepPerms() }
            .setNegativeButton("稍后", null)
            .show()
    }

    private fun stepPerms() {
        val kind = Perms.nextMissing(this, askedPerms)
        if (kind == null) {
            awaitingPerm = false
            if (Perms.nextMissing(this) == null) {
                Toast.makeText(this, "权限已就绪", Toast.LENGTH_SHORT).show()
            }
            return
        }
        askedPerms.add(kind)
        if (kind == "notify") {
            Perms.open(this, kind)
            return
        }
        AlertDialog.Builder(this)
            .setTitle(Perms.label(kind))
            .setMessage(Perms.hint(kind))
            .setPositiveButton("去打开") { _, _ ->
                awaitingPerm = true
                try {
                    stopLockTask()
                } catch (_: Exception) {
                }
                Perms.open(this, kind)
            }
            .setNegativeButton("跳过") { _, _ -> stepPerms() }
            .show()
    }

    private fun tryPlay() {
        if (Prefs.isPlayActive()) return
        if (!Perms.readyForPlay(this)) {
            Toast.makeText(this, "先打开通知和悬浮窗权限", Toast.LENGTH_LONG).show()
            explainPerms()
            return
        }
        if (Prefs.balance() + 1e-9 < Prefs.POINT_COST) {
            Toast.makeText(this, "满 ${Prefs.POINT_COST.toInt()} 分才能玩", Toast.LENGTH_SHORT).show()
            return
        }
        web.evaluateJavascript("window.PlayWallet?PlayWallet.spend(${Prefs.POINT_COST}):-1") { raw ->
            val n = raw?.trim('"')?.toDoubleOrNull() ?: -1.0
            runOnUiThread {
                if (n >= 0) Prefs.setBalance(n)
                else Prefs.setBalance(Prefs.balance() - Prefs.POINT_COST)
                Prefs.startPlay()
                PlayTimerService.start(this)
                refreshUi()
            }
        }
    }

    private fun launchApp(info: ResolveInfo) {
        val name = info.activityInfo
        startActivity(
            Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LAUNCHER)
                .setClassName(name.packageName, name.name)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    private fun launchables(): List<ResolveInfo> {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryIntentActivities(intent, 0)
            .filter { it.activityInfo.packageName != packageName }
            .filter { it.activityInfo.packageName !in Kiosk.homePackages(this) }
            .sortedBy { it.loadLabel(pm).toString() }
    }

    private fun parentGate() {
        if (!Prefs.hasPin()) {
            askPin("设置家长密码（至少 4 位）", set = true) { parentMenu() }
            return
        }
        askPin("输入家长密码") { parentMenu() }
    }

    private fun askPin(title: String, set: Boolean = false, ok: () -> Unit) {
        val box = EditText(this).apply {
            inputType =
                android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
            hint = "密码"
        }
        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(box)
            .setPositiveButton("确定") { _, _ ->
                val pin = box.text.toString().trim()
                if (set) {
                    if (pin.length < 4) {
                        Toast.makeText(this, "至少 4 位", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    Prefs.setPin(pin)
                    ok()
                } else if (Prefs.checkPin(pin)) ok()
                else Toast.makeText(this, "密码不对", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun parentMenu() {
        val items = arrayOf(
            "打开计时权限",
            "打开系统设置",
            "赠送 40 分",
            "改练习网址",
            "更换家长密码",
            "结束当前自由时间"
        )
        AlertDialog.Builder(this)
            .setTitle("家长")
            .setItems(items) { _, which ->
                when (which) {
                    0 -> explainPerms()
                    1 -> {
                        Kiosk.allowExtra(this, "com.android.settings")
                        startActivity(Intent(android.provider.Settings.ACTION_SETTINGS))
                    }
                    2 -> {
                        Prefs.setBalance(Prefs.balance() + Prefs.POINT_COST)
                        web.evaluateJavascript(
                            "window.PlayWallet&&PlayWallet.add(${Prefs.POINT_COST})",
                            null
                        )
                        updateBar()
                    }
                    3 -> editUrl()
                    4 -> askPin("新的家长密码", set = true) {}
                    5 -> {
                        Prefs.endPlay()
                        PlayTimerService.stop(this)
                        refreshUi()
                    }
                }
            }
            .setNegativeButton("关闭", null)
            .show()
    }

    private fun editUrl() {
        val box = EditText(this).apply {
            setText(Prefs.studyUrl())
            inputType = android.text.InputType.TYPE_TEXT_VARIATION_URI
        }
        AlertDialog.Builder(this)
            .setTitle("练习网址")
            .setView(box)
            .setPositiveButton("保存") { _, _ ->
                val u = box.text.toString().trim()
                if (u.startsWith("http")) {
                    Prefs.setStudyUrl(u)
                    web.loadUrl(u)
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    inner class JsBridge {
        @JavascriptInterface
        fun onWallet(json: String) {
            try {
                val n = JSONObject(json).optDouble("balance", Prefs.balance())
                runOnUiThread {
                    Prefs.setBalance(n)
                    updateBar()
                }
            } catch (_: Exception) {
            }
        }
    }

    private class AppAdapter(
        val onClick: (ResolveInfo) -> Unit
    ) : RecyclerView.Adapter<AppAdapter.H>() {
        private var items: List<ResolveInfo> = emptyList()

        fun submit(list: List<ResolveInfo>) {
            items = list
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): H {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_app, parent, false)
            return H(v)
        }

        override fun onBindViewHolder(holder: H, position: Int) {
            val info = items[position]
            val pm = holder.itemView.context.packageManager
            holder.icon.setImageDrawable(info.loadIcon(pm))
            holder.label.text = info.loadLabel(pm)
            holder.itemView.setOnClickListener { onClick(info) }
        }

        override fun getItemCount() = items.size

        class H(v: View) : RecyclerView.ViewHolder(v) {
            val icon: ImageView = v.findViewById(R.id.icon)
            val label: TextView = v.findViewById(R.id.label)
        }
    }

    companion object {
        fun trim(n: Double): String {
            val s = ((n * 4).roundToInt() / 4.0).toString()
            return s.replace(Regex("\\.0$"), "")
        }

        fun fmt(ms: Long): String {
            val sec = (ms / 1000).coerceAtLeast(0)
            return "${sec / 60}:${(sec % 60).toString().padStart(2, '0')}"
        }
    }
}