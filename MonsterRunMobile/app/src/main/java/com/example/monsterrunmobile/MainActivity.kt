package com.example.monsterrunmobile

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : AppCompatActivity(), GameTask {
    lateinit var rootlayout: LinearLayout
    lateinit var startButton: Button
    lateinit var mGameView: GameView
    lateinit var score: TextView
    lateinit var highestScoreText: TextView
    private lateinit var sharedPreferences: SharedPreferences
    private var highestScore: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startButton = findViewById(R.id.startButton)
        rootlayout = findViewById(R.id.rootLayout)
        score = findViewById(R.id.score)
        highestScoreText = findViewById(R.id.highestScore)
        mGameView = GameView(this,this)

        sharedPreferences = getSharedPreferences("MonsterRun", Context.MODE_PRIVATE)
        highestScore = sharedPreferences.getInt("highestScore", 0)
        highestScoreText.text = "Highest Score : $highestScore"

        startButton.setOnClickListener {
            if (startButton.text.toString() == "Start") {
                startGame()
            } else {
                restartGame()
            }
        }
    }

    override fun closeGame(mScore: Int) {
        if (mScore > highestScore) {
            highestScore = mScore
            highestScoreText.text = "Highest Score : $highestScore"
            val editor = sharedPreferences.edit()
            editor.putInt("highestScore", highestScore)
            editor.apply()
        }
        score.text = "Score : $mScore"
        val intent = Intent(this, GameOver::class.java)
        intent.putExtra("score", mScore.toString())
        intent.putExtra("highestScore", highestScore.toString())
        startActivity(intent)
        finish() // Finish the MainActivity
    }

    private fun startGame() {
        mGameView.setBackgroundResource(R.drawable.road)
        rootlayout.addView(mGameView)
        startButton.visibility = View.GONE
        score.visibility = View.GONE
    }

    private fun restartGame() {
        rootlayout.removeView(mGameView)
        mGameView = GameView(this, this)
        startGame()
    }
}
