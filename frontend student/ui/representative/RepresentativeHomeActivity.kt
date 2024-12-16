package com.example.mess.ui.representative

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.mess.MainActivity
import com.example.mess.R
import com.example.mess.databinding.ActivityRepresentativeHomeBinding
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class RepresentativeHomeActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityRepresentativeHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRepresentativeHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarRepresentative.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_representative)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_mr_dashboard,
                R.id.nav_mr_menu,
                R.id.nav_mr_complaints,
                R.id.nav_mr_announcements
            ), drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Handle logout
        navView.menu.findItem(R.id.nav_mr_logout).setOnMenuItemClickListener {
            // Sign out from Firebase
            FirebaseAuth.getInstance().signOut()
            
            // Create intent to start MainActivity (which has the login flow)
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
            true
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_representative)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
} 