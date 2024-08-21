package com.dyland.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.accessibilityservice.GestureDescription
import android.accessibilityservice.GestureDescription.StrokeDescription
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Rect
import android.media.AudioManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Button
import android.widget.FrameLayout


class GestureAccessibilityService : AccessibilityService() {
    var mLayout: FrameLayout? = null
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var speechRecognizerIntent: Intent
    private var isListening = false

    companion object {
        private const val TAG = "GestureAccessibilityService"
    }

    override fun onServiceConnected() {
        super.onServiceConnected()

        val info = AccessibilityServiceInfo()
        info.eventTypes =
            AccessibilityEvent.TYPE_VIEW_CLICKED or AccessibilityEvent.TYPE_VIEW_FOCUSED
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN
        info.notificationTimeout = 100
        this.serviceInfo = info

        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        mLayout = FrameLayout(this)
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        layoutParams.format = PixelFormat.TRANSLUCENT
        layoutParams.flags = layoutParams.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        layoutParams.gravity = Gravity.START
        val inflater = LayoutInflater.from(this)
        inflater.inflate(R.layout.floating_bar, mLayout)
        windowManager.addView(mLayout, layoutParams)

        configurePowerButton()
        configureSimulateTouch()
        configureVolumeUpButton()
        configureVolumeDownButton()
        configureScrollButton()
        configureSwipeButton()
        configureSpeechRecognizer()
        configureVoiceButton()
    }

    private fun configureSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
        }

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {
                isListening = false
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val request = matches?.get(0) ?: "None"
                isListening = false
                // Handle Request contain keyword
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    private fun configureVoiceButton() {
        val voiceButton = mLayout!!.findViewById<View>(R.id.voiceButton) as Button
        voiceButton.setText(R.string.voice)

        voiceButton.setOnClickListener {
            if (isListening) {
                speechRecognizer.stopListening()
                voiceButton.setText(R.string.stop)
                isListening = false

            } else {
                isListening = true
                voiceButton.setText(R.string.voice)
                speechRecognizer.startListening(speechRecognizerIntent)
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val packageName: String = event?.packageName.toString()
        val packageManager = this.packageManager
        try {
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            val applicationLabel = packageManager.getApplicationLabel(applicationInfo)
            Log.e(TAG, "app name is: $applicationLabel")
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }

    override fun onInterrupt() {
        Log.d("TAG", "onInterrupt")
    }

    private fun configurePowerButton() {
        val power = mLayout!!.findViewById<View>(R.id.power) as Button
        power.setOnClickListener { performGlobalAction(GLOBAL_ACTION_POWER_DIALOG) }
    }

    private fun configureVolumeUpButton() {
        val volumeUpButton = mLayout!!.findViewById<View>(R.id.volume_up) as Button
        volumeUpButton.setOnClickListener {
            val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
            audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI
            )
        }
    }

    private fun configureVolumeDownButton() {
        val volumeDownButton = mLayout!!.findViewById<View>(R.id.volume_down) as Button
        volumeDownButton.setOnClickListener {
            val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
            audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI
            )
        }
    }

    private fun configureSimulateTouch() {
        val btnSimulateTouch = mLayout!!.findViewById<View>(R.id.simulateTouch) as Button
        btnSimulateTouch.setOnClickListener {
            Log.e(TAG, "onClick: Simulate Touch")
            val tap: Path = Path()
            tap.moveTo(110f, 50f)
            val tapBuilder = GestureDescription.Builder()
            tapBuilder.addStroke(StrokeDescription(tap, 0, 500))
            dispatchGesture(tapBuilder.build(), null, null)
        }
    }

    private fun findScrollableNode(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        val deque: ArrayDeque<AccessibilityNodeInfo> = ArrayDeque()
        deque.add(root)
        while (!deque.isEmpty()) {
            val node = deque.removeFirst()
            if (node.actionList.contains(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD)) {
                return node
            }
            for (i in 0 until node.childCount) {
                deque.addLast(node.getChild(i))
            }
        }
        return null
    }

    private fun configureScrollButton() {
        val scrollButton = mLayout!!.findViewById<View>(R.id.scroll) as Button
        scrollButton.setOnClickListener {
            val scrollable = findScrollableNode(rootInActiveWindow)
            scrollable?.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD.id)
        }
    }

    private fun configureSwipeButton() {
        val swipeButton = mLayout!!.findViewById<View>(R.id.swipe) as Button
        swipeButton.setOnClickListener {
            val swipePath: Path = Path()
            swipePath.moveTo(1000f, 1000f)
            swipePath.lineTo(100f, 1000f)
            val gestureBuilder = GestureDescription.Builder()
            gestureBuilder.addStroke(StrokeDescription(swipePath, 0, 500))
            dispatchGesture(gestureBuilder.build(), null, null)
        }
    }

    private fun findNodeAt(
        rootNode: AccessibilityNodeInfo?,
        x: Float,
        y: Float
    ): AccessibilityNodeInfo? {
        if (rootNode == null) return null

        val rect = Rect()
        rootNode.getBoundsInScreen(rect)

        if (rect.contains(x.toInt(), y.toInt())) {
            for (i in 0 until rootNode.childCount) {
                val child = rootNode.getChild(i)
                val result = findNodeAt(child, x, y)
                if (result != null) {
                    return result
                }
            }
            return rootNode
        }

        return null
    }
}