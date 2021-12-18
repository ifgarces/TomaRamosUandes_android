package com.ifgarces.tomaramosuandes.navigators

import androidx.navigation.findNavController
import com.ifgarces.tomaramosuandes.R
import com.ifgarces.tomaramosuandes.activities.HomeActivity


class HomeNavigator(private val homeActivity :HomeActivity) {
    fun up() = this.homeActivity.supportFragmentManager.popBackStack()

    fun toUserRamos() = this.homeActivity.findNavController(R.id.homeActivity_fragmentContainer)
        .navigate(R.id.userRamosFragment)

    fun toCatalog() = this.homeActivity.findNavController(R.id.homeActivity_fragmentContainer)
        .navigate(R.id.catalogFragment)

    fun toSchedulePortrait() = this.homeActivity.findNavController(R.id.homeActivity_fragmentContainer)
        .navigate(R.id.schedulePortraitFragment)

    fun toEvaluations() = this.homeActivity.findNavController(R.id.homeActivity_fragmentContainer)
        .navigate(R.id.evaluationsFragment)
}