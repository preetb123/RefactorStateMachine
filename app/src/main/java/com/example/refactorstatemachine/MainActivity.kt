package com.example.refactorstatemachine

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.mycmpapp.AppData
import org.mycmpapp.AppModel
import org.mycmpapp.ModelEffect
import org.mycmpapp.observe

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        AppModel.observe(this, ::onStateChanged, ::onEffect)
    }

    private fun onEffect(modelEffect: ModelEffect) {
        Log.d(TAG, "onEffect() called with: modelEffect = $modelEffect")
    }

    private fun onStateChanged(appData: AppData) {
        Log.d(TAG, "onStateChanged: ")
    }
}