package com.goodayapps.avatarview.list_screen

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.goodayapps.avatarview.databinding.ActivityListBinding
import com.goodayapps.avatarview.list_screen.adapters.AvatarListAdapter

class ListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityListBinding

    private val listAdapter = createListAdapter()

    private fun createListAdapter(): AvatarListAdapter {
        return AvatarListAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "List Sample"

        initViews()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }

            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    private fun initViews() = with(binding) {
        list.run {
            layoutManager = LinearLayoutManager(this@ListActivity)
            adapter = listAdapter
            setHasFixedSize(true)
        }
    }

}
