package com.ifgarces.tomaramosuandes.navigators

import com.ifgarces.tomaramosuandes.HomeActivity


class HomeNavigator(private val homeActivity :HomeActivity) {
    fun up() = this.homeActivity.supportFragmentManager.popBackStack() // <==> this.mainActivity.onBackPressed()


}