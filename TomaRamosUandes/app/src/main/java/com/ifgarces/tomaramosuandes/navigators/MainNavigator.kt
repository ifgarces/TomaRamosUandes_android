package com.ifgarces.tomaramosuandes.navigators

import com.ifgarces.tomaramosuandes.MainActivity



class MainNavigator(private val mainActivity :MainActivity) {
    fun up() = this.mainActivity.supportFragmentManager.popBackStack() // <==> this.mainActivity.onBackPressed()




}