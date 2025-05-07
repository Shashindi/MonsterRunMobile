package com.example.monsterrunmobile

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class GameOver : AppCompatActivity() {
    lateinit var score: TextView
    lateinit var highestScoreText: TextView
    lateinit var startButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_over)

        score = findViewById(R.id.score)
        highestScoreText = findViewById(R.id.highestScore)
        startButton = findViewById(R.id.startButton)

        val scoreValue = intent.getStringExtra("score")
        val highestScoreValue = intent.getStringExtra("highestScore")

        score.text = "Score: $scoreValue"
        highestScoreText.text = "Highest Score: $highestScoreValue"

        startButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Finish the GameOver activity
        }
    }
}
