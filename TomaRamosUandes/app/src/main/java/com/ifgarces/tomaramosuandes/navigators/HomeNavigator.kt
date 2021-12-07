package com.ifgarces.tomaramosuandes.navigators

import androidx.navigation.findNavController
import com.ifgarces.tomaramosuandes.HomeActivity
import com.ifgarces.tomaramosuandes.R


class HomeNavigator(private val homeActivity :HomeActivity) {
    fun up() = this.homeActivity.supportFragmentManager.popBackStack()

    fun toRamosCatalog() = this.homeActivity.findNavController(R.id.homeActivity_fragmentContainer)
        .navigate(R.id.action_homeFragment_to_ramosCatalogFragment)
}