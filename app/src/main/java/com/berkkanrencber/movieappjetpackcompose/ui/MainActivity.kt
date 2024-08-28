package com.berkkanrencber.movieappjetpackcompose.ui

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.findNavController
import com.berkkanrencber.movieappjetpackcompose.R
import com.berkkanrencber.movieappjetpackcompose.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        hideSystemBars()
        setBottomNavigation()
        setBottomNavigationViewForLandscape()
    }

    private fun setBottomNavigationViewForLandscape() {
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            val textAppearanceStyle = R.style.BottomNavigationActiveLandscape
            val textAppearanceStyleInactive = R.style.BottomNavigationInactiveLandscape
            binding.bottomNavigation.itemTextAppearanceActive = textAppearanceStyle
            binding.bottomNavigation.itemTextAppearanceInactive = textAppearanceStyleInactive
            binding.bottomNavigation.itemIconSize = resources.getDimensionPixelSize(R.dimen.bottom_navigation_icon_size_landscape)
            binding.bottomNavigation.layoutParams.height = resources.getDimensionPixelSize(R.dimen.bottom_navigation_height_landscape)
        }
    }

    private fun setBottomNavigation() {
        val navView: BottomNavigationView = binding.bottomNavigation
        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    findNavController(R.id.fragmentContainerView).navigate(R.id.homePage)
                    true
                }
                R.id.navigation_favorites -> {
                    findNavController(R.id.fragmentContainerView).navigate(R.id.favoritePage)
                    true
                }
                R.id.navigation_search -> {
                    findNavController(R.id.fragmentContainerView).navigate(R.id.searchPage)
                    true
                }
                else -> {
                    false
                }
            }
        }
    }

    fun changeVisibilityBottomBar(isActive: Boolean) {
        binding.bottomNavigation.visibility = if (isActive) View.VISIBLE else View.GONE
    }


    private fun hideSystemBars() {
        val windowInsetsController = WindowCompat.getInsetsController(window, binding.root)
        windowInsetsController?.let {
            it.hide(WindowInsetsCompat.Type.statusBars())
            it.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}