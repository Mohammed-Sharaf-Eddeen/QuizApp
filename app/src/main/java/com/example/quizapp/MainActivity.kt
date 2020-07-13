package com.example.quizapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders

private const val CURRENT_INDEX = "Current Index"
private const val USER_ANSWERS = "User Answers"
private const val REQUEST_CODE_CHEAT = 0

class MainActivity : AppCompatActivity() {
    private lateinit var trueButton: Button
    private lateinit var falseButton: Button
    private lateinit var nextButton: ImageButton
    private lateinit var backButton: ImageButton
    private lateinit var questionTextView: TextView
    private lateinit var chronometer: Chronometer

    private val quizViewModel: QuizViewModel by lazy {
        ViewModelProviders.of(this).get(QuizViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        quizViewModel.currentIndex = savedInstanceState?.getInt(CURRENT_INDEX) ?: 0
        val tempArray = savedInstanceState?.getBooleanArray(USER_ANSWERS)
        /*If tempArray not null and answered questions are null, this means that a process death has
         happened, and here we need to do the work*/
        if (tempArray != null && quizViewModel.answeredQuestions.isEmpty() ) {
            tempArray.forEachIndexed { index, b ->
                quizViewModel.answeredQuestions.add(index,b)
            }
        }

        trueButton = findViewById(R.id.true_button)
        falseButton = findViewById(R.id.false_button)
        nextButton = findViewById(R.id.next_button)
        backButton = findViewById(R.id.back_button)
        questionTextView = findViewById(R.id.question_text_view)
        chronometer = findViewById(R.id.chronometer)


        /*
        Chronometer is not immune against rotations, so the solution is that: it has a base value
        this value represents the value it considers zero and start counting from it
        It gets this value from the SystemClock.elapsedRealtime(). This value represent the time
        that has passed since the last openning of the system (shutting down and sratring the phone)
        The problem is that chronometer asks for this value after each rotation, so it will get a
        new value every time. So, you should ask for this value one time at the start of the app
        and always set the base to this value

         To understand, do the following logs
         Log.d("Chronometer", "Base: " + chronometer.base)
         Log.d("Chronometer", "SystemClock Elapsed Time: " + SystemClock.elapsedRealtime())
         Log.d("Chronometer", "elapsed time: " + (SystemClock.elapsedRealtime()- chronometer.base))
         */
        if (!quizViewModel.isQuizFinished){
            chronometer.base = quizViewModel.systemClockElapsedTime
            chronometer.start()
        } else{
            chronometer.text = quizViewModel.solvingTime
        }

        if (quizViewModel.isQuizFinished){
            nextButton.visibility = View.VISIBLE
            backButton.visibility = View.VISIBLE
        }

        trueButton.setOnClickListener {
            checkAnswer(true)
            if (quizViewModel.currentIndex == quizViewModel.questionBank.size - 1) {
                finishQuiz()
            } else {
                Handler().postDelayed({
                    quizViewModel.currentIndex++
                    updateNewQuestions()
                }, 500)
            }
        }
        falseButton.setOnClickListener {
            checkAnswer(false)
            if (quizViewModel.currentIndex == quizViewModel.questionBank.size - 1) {
                finishQuiz()
            } else {
                Handler().postDelayed({
                    quizViewModel.currentIndex++
                    updateNewQuestions()
                },500)
            }
        }
        nextButton.setOnClickListener {
            when {
                quizViewModel.currentIndex == (quizViewModel.questionBank.size - 1) -> {
                    Toast.makeText(this,
                        "You Got ${quizViewModel.showScore()}"
                                +" out of ${quizViewModel.questionBank.size}"
                                +" in ${chronometer.text} mm:ss", Toast.LENGTH_LONG)
                        .show()
                }
                quizViewModel.currentIndex == (quizViewModel.answeredQuestions.size-1) -> {
                    quizViewModel.currentIndex = quizViewModel.currentIndex + 1
                    updateNewQuestions()
                }
                quizViewModel.currentIndex < (quizViewModel.answeredQuestions.size-1) -> {
                    quizViewModel.currentIndex = quizViewModel.currentIndex + 1
                    updatePreviousQuestions()
                }
            }
        }
        backButton.setOnClickListener {
            when (quizViewModel.currentIndex) {
                0 -> {
                    Toast.makeText(this, R.string.back_button_toast, Toast.LENGTH_SHORT).show()
                }
                else -> {
                    quizViewModel.currentIndex = quizViewModel.currentIndex - 1
                    updatePreviousQuestions()
                }
            }
        }
            when {
                quizViewModel.currentIndex == (quizViewModel.answeredQuestions.size-1) -> {
                    updatePreviousQuestions()
                }
                quizViewModel.currentIndex < (quizViewModel.answeredQuestions.size-1) -> {
                    updatePreviousQuestions()
                } else -> {
                    updateNewQuestions()
                 }
            }

            }

    private fun finishQuiz() {
        Toast.makeText(
            this,
            "Your Got ${quizViewModel.showScore()}" +
                    " out of ${quizViewModel.questionBank.size}"+ " in ${chronometer.text} mm:ss",
            Toast.LENGTH_LONG
        ).show()
        quizViewModel.isQuizFinished = true
        quizViewModel.solvingTime = chronometer.text
        nextButton.visibility = View.VISIBLE
        backButton.visibility = View.VISIBLE
        chronometer.stop()
    }

    private fun deactivateButtons() {
        falseButton.setClickable(false)
        trueButton.setClickable(false)
        trueButton.setBackgroundResource(0)
        falseButton.setBackgroundResource(0)
    }

    private fun updateNewQuestions() {
        val questionTextResId = quizViewModel.currentQuestionText
        questionTextView.setText(questionTextResId)
        falseButton.setClickable(true)
        trueButton.setClickable(true)
        trueButton.setBackgroundResource(R.color.black)
        falseButton.setBackgroundResource(R.color.black)
    }

    private fun checkAnswer(userAnswer: Boolean) {
        quizViewModel.answeredQuestions.add(userAnswer)
        deactivateButtons()
        val correctAnswer = quizViewModel.currentQuestionAnswer
        val messageResId: Int
        if(userAnswer == correctAnswer) {
            if (correctAnswer == true) {
                trueButton.setBackgroundResource(R.color.holo_green_light)
            }
            if (correctAnswer == false) {
                falseButton.setBackgroundResource(R.color.holo_green_light)
            }
        } else {
            if (correctAnswer == true) {
                falseButton.setBackgroundResource(R.color.holo_red_light)
            }
            if (correctAnswer == false) {
                trueButton.setBackgroundResource(R.color.holo_red_light)
            }
        }
    }
    private fun updatePreviousQuestions(){
        deactivateButtons()
        val correctAnswer = quizViewModel.currentQuestionAnswer
        val userAnswer = quizViewModel.currentUserAnswer
        if(userAnswer == correctAnswer) {
            if (correctAnswer) {
                trueButton.setBackgroundResource(R.color.holo_green_light)
            }
            if (!correctAnswer) {
                falseButton.setBackgroundResource(R.color.holo_green_light)
            }
        } else {
            if (correctAnswer) {
                trueButton.setBackgroundResource(R.color.holo_red_light)
            }
            if (!correctAnswer) {
                falseButton.setBackgroundResource(R.color.holo_red_light)
            }
        }
        val questionTextResId = quizViewModel.currentQuestionText
        questionTextView.setText(questionTextResId)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(CURRENT_INDEX, quizViewModel.currentIndex)
        outState.putBooleanArray(USER_ANSWERS, quizViewModel.answeredQuestions.toBooleanArray())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) {return}
        if (quizViewModel.isCheater) {return}
        if (requestCode == REQUEST_CODE_CHEAT){
            quizViewModel.isCheater = data?.getBooleanExtra(EXTRA_ANSWER_SHOWN, false) ?: false
        }
    }
}