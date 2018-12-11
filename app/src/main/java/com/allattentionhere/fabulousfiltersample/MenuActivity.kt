package com.allattentionhere.fabulousfiltersample

import android.content.Intent
import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity

import android.view.View

import com.allattentionhere.fabulousfiltersample.R.id.fab

class MenuActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        findViewById<View>(R.id.btn_bottom).setOnClickListener {
            val i = Intent(this@MenuActivity, MainActivity::class.java)
            i.putExtra("fab", 1)
            startActivity(i)
        }

        findViewById<View>(R.id.btn_top).setOnClickListener {
            val i = Intent(this@MenuActivity, MainActivity::class.java)
            i.putExtra("fab", 2)
            startActivity(i)
        }

        findViewById<View>(R.id.btn_understanding).setOnClickListener {
            val i = Intent(this@MenuActivity, MainSampleActivity::class.java)
            startActivity(i)
        }

        findViewById<View>(R.id.btn_fragment).setOnClickListener {
            val i = Intent(this@MenuActivity, FragmentExampleActivity::class.java)
            startActivity(i)
        }
    }


}
