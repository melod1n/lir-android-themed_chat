package com.android.lir.screens.main.thematic

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.android.lir.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ThematicChatCreateActivity : AppCompatActivity(R.layout.activity_themed_chat_create_parent) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportFragmentManager.beginTransaction()
            .replace(
                R.id.fragmentContainer,
                ThematicChatCreateDialog().apply { arguments = intent.extras }).commit()
    }

}