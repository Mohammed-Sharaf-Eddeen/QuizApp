package com.example.quizapp

import android.os.SystemClock
import androidx.lifecycle.ViewModel

class QuizViewModel: ViewModel() {
    var currentIndex = 0
    var isCheater = false
    var isQuizFinished = false
    var systemClockElapsedTime: Long = SystemClock.elapsedRealtime()
    var solvingTime: CharSequence = "-99:-99" // To indicate an error if something went wrong


    val questionBank = listOf(
        Question(R.string.question_australia, true),
        Question(R.string.question_oceans, true),
        Question(R.string.question_mideast, false),
        Question(R.string.question_africa, false),
        Question(R.string.question_americas, true),
        Question(R.string.question_asia, true),
        Question(R.string.question_7, true),
        Question(R.string.question_8, false),
        Question(R.string.question_9, false),
        Question(R.string.question_10, true),
        Question(R.string.question_11, false),
        Question(R.string.question_12, true),
        Question(R.string.question_13, false),
        Question(R.string.question_14, true),
        Question(R.string.question_15, false)
        )

    var answeredQuestions = arrayListOf<Boolean>()

    val currentQuestionAnswer: Boolean
        get() = questionBank[currentIndex].answer

    val currentQuestionText: Int
        get() = questionBank[currentIndex].textResId

    val currentUserAnswer: Boolean
        get() = answeredQuestions[currentIndex]

    fun showScore(): Int {
        var score: Int = 0
        answeredQuestions.forEachIndexed { index, answeredQuestion ->
            if (answeredQuestion == questionBank[index].answer) {
                score = score + 1
            }
        }
        return score
    }
}