package org.techtown.unsserialport

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import org.techtown.unsserialport.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val tag: String = javaClass.simpleName
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}