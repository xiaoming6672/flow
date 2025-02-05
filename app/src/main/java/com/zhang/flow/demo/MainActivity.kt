package com.zhang.flow.demo

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v , insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left , systemBars.top , systemBars.right , systemBars.bottom)
            insets
        }

//        timer(1000).collect(this) {
//            Log.d("ZHANG" , it.toString())
//        }
//        timer(1000) { "++$it" }.collect(this) {
//            Log.d("ZHANG" , it.toString())
//        }
//        timer(1 , TimeUnit.MINUTES).collect(this) {
//            Log.d("ZHANG" , it.toString())
//        }
    }
}