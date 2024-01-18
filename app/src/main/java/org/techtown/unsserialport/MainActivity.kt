package org.techtown.unsserialport

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import org.techtown.unsserialport.databinding.ActivityMainBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val tag: String = javaClass.simpleName
    private var port: UsbSerialPort? = null
    private val usbReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (AppData.ACTION_USB_PERMISSION != intent.action) return
            synchronized(this) {
                if (!intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false))
                    connect()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initView()
    }

    override fun onDestroy() {
        AppData.debug(tag, "onDestroy called.")
        port?.close()
        port = null
        super.onDestroy()
    }

    private fun initView() = with(binding) {
        val method = Thread.currentThread().stackTrace[2].methodName
        AppData.debug(tag, "$method called. SDK: ${Build.VERSION.SDK_INT}")

        resultText.movementMethod = ScrollingMovementMethod()

        connectButton.setOnClickListener { connect() }
        disconnectButton.setOnClickListener { disconnect() }
        openButton.setOnClickListener { command("open") }
        closeButton.setOnClickListener { command("close") }
    }

    @SuppressLint("InlinedApi")
    private fun connect() {
        val method = Thread.currentThread().stackTrace[2].methodName
        printLog("$method called.")
        
        if (port != null) {
            printLog("Already ${method}ed.")
            return
        }

        val permissionIntent = PendingIntent.getBroadcast(
            this,
            0,
            Intent(AppData.ACTION_USB_PERMISSION),
            PendingIntent.FLAG_IMMUTABLE
        )
        val filter = IntentFilter(AppData.ACTION_USB_PERMISSION)
        registerReceiver(usbReceiver, filter, RECEIVER_NOT_EXPORTED)

        val manager = getSystemService(USB_SERVICE) as UsbManager
        val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager)
        if (availableDrivers.isEmpty()) {
            printLog("connection failed: no driver for device")
            return
        }

        // Open a connection to the first available driver.
        val driver = availableDrivers.first()
        val connection = manager.openDevice(driver.device)
        if (connection == null) {
            if (!manager.hasPermission(driver.device)) {
                printLog("connection failed: permission denied")
                manager.requestPermission(driver.device, permissionIntent)
            } else printLog("connection failed: open failed")
            return
        }

        port = driver.ports[0] // Most devices have just one port (port 0)
        port?.open(connection)
        port?.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
        printLog("It has been successfully connected.")
        binding.statusText.text = method
    }

    private fun disconnect() {
        val method = Thread.currentThread().stackTrace[2].methodName
        printLog("$method called.")
        if (port != null) {
            command("close")
            port?.close()
            port = null
            printLog("Just ${method}ed.")
        } else printLog("Already ${method}ed.")
        binding.statusText.text = method
    }

    private fun command(command: String) {
        val method = Thread.currentThread().stackTrace[2].methodName
        AppData.debug(tag, "$method called. command: $command")
        printLog("$method called. command: $command")

        if (port == null) {
            printLog("The port is not initialized.")
            return
        }

        try {
            port?.write(command.toByteArray(), 1500)
            printLog("[$command] was executed successfully.")
            binding.statusText.text = command
        } catch (e: Exception) {
            printLog("$method failed: $command failed")
        }
    }

    private fun printLog(message: String) = runOnUiThread {
        val formatter = DateTimeFormatter.ofPattern("MM-dd HH:mm:ss")
        val now = LocalDateTime.now().format(formatter)
        val log = "[$now] $message"
        if (AppData.logList.size > 1000) AppData.logList.removeAt(1)
        AppData.logList.add(log)
        val sb = StringBuilder()
        AppData.logList.forEach { sb.appendLine(it) }
        binding.resultText.text = sb
        moveToBottom(binding.resultText)
    }

    private fun moveToBottom(textView: TextView) = textView.post {
        val scrollAmount = try {
            textView.layout.getLineTop(textView.lineCount) - textView.height
        } catch (_: NullPointerException) {
            0
        }
        if (scrollAmount > 0) textView.scrollTo(0, scrollAmount)
        else textView.scrollTo(0, 0)
    }
}