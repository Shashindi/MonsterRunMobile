package com.example.monsterrunmobile

import  android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import android.view.View
import com.example.monsterrunmobile.GameTask
import com.example.monsterrunmobile.R

class GameView(var c: Context, var gameTask: GameTask) : View(c) {
    private var myPaint: Paint? = null
    private var speed = 1
    private var time = 0
    private var score = 0
    private var manPosition = 0
    private val monsters = ArrayList<HashMap<String, Any>>()



    var viewWidth = 0
    var viewHeight = 0
    var gameOver = false

    init {
        myPaint = Paint()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        viewWidth = this.measuredWidth
        viewHeight = this.measuredHeight

        if (!gameOver) {
            if (time % 700 < 10 + speed) {
                val map = HashMap<String, Any>()
                map["lane"] = (0..2).random()
                map["startTime"] = time
                monsters.add(map)
            }
            time = time + 10 + speed
            val manWidth = viewWidth / 5
            val manHeight = manWidth + 10
            myPaint!!.style = Paint.Style.FILL

            // Draw the player
            val playerDrawable = resources.getDrawable(R.drawable.man, null)
            playerDrawable.setBounds(
                manPosition * viewWidth / 3 + viewWidth / 15 + 25,
                viewHeight - 2 - manHeight,
                manPosition * viewWidth / 3 + viewWidth / 15 + manWidth - 25,
                viewHeight - 2
            )
            playerDrawable.draw(canvas)

            // Draw the monsters
            myPaint!!.color = Color.RED
            for (i in monsters.indices) {
                try {
                    val monX = monsters[i]["lane"] as Int * viewWidth / 3 + viewWidth / 15
                    var monY = time - monsters[i]["startTime"] as Int
                    val monsterDrawable = resources.getDrawable(R.drawable.monster, null)
                    monsterDrawable.setBounds(
                        monX + 25, monY - manHeight, monX + manWidth - 25, monY
                    )
                    monsterDrawable.draw(canvas)

                    if (monsters[i]["lane"] as Int == manPosition && monY > viewHeight - 2 - manHeight && monY < viewHeight - 2) {
                        gameTask.closeGame(score)
                        gameOver = true
                    }

                    if (monY > viewHeight + manHeight) {
                        monsters.removeAt(i)
                        score++
                        speed = 1 + Math.abs(score / 8)
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Draw score and speed
            myPaint!!.color = Color.WHITE
            myPaint!!.textSize = 40f
            canvas.drawText("Score : $score", 80f, 80f, myPaint!!)
            canvas.drawText("Speed : $speed", 380f, 80f, myPaint!!)
        } else {
            // Game over logic
            // You can add game over message or any other UI elements here
            myPaint!!.color = Color.RED
            myPaint!!.textSize = 80f
            canvas.drawText("Game Over", 150f, 300f, myPaint!!)
        }

        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (!gameOver) {
            when (event!!.action) {
                MotionEvent.ACTION_DOWN -> {
                    val x1 = event.x
                    if (x1 < viewWidth / 2) {
                        if (manPosition > 0) {
                            manPosition--
                        }
                    }
                    if (x1 > viewWidth / 2) {
                        if (manPosition < 2) {
                            manPosition++
                        }
                    }
                    invalidate()
                }
                MotionEvent.ACTION_UP -> {
                }
            }
        } else {
            // If game over, restart the game on touch
            if (event!!.action == MotionEvent.ACTION_DOWN) {
                restartGame()
                gameOver = false
            }
        }
        return true
    }

    private fun restartGame() {
        score = 0
        time = 0
        speed = 1
        manPosition = 0
        monsters.clear()
    }
}