package com.example.skinsmart

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        
        // Smart routing based on Firebase Auth state
        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            navGraph.setStartDestination(R.id.homeFragment)
        } else {
            navGraph.setStartDestination(R.id.loginFragment)
        }
        navController.graph = navGraph

        bottomNav.setupWithNavController(navController)

        val fab = findViewById<View>(R.id.fab_add_post)
        val bottomAppBar = findViewById<View>(R.id.bottom_app_bar)

        fab.setOnClickListener {
            navController.navigate(R.id.createPostFragment)
        }

        // Hide Navigation on Auth Screens
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val isAuthScreen = destination.id == R.id.loginFragment || destination.id == R.id.registerFragment
            bottomNav.visibility = if (isAuthScreen) View.GONE else View.VISIBLE
            bottomAppBar.visibility = if (isAuthScreen) View.GONE else View.VISIBLE
            fab.visibility = if (isAuthScreen) View.GONE else View.VISIBLE
        }
    }
}