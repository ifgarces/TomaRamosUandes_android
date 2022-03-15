package com.ifgarces.tomaramosuandes.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ifgarces.tomaramosuandes.R


class DashboardFragment : Fragment() {
    
    private class FragmentUI(owner :View) {
        
    }
    
    private lateinit var UI :FragmentUI

    override fun onCreateView(
        inflater :LayoutInflater, container :ViewGroup?, savedInstanceState :Bundle?
    ) :View {
        val fragView :View = inflater.inflate(R.layout.fragment_dashboard, container, false)
        this.UI = FragmentUI(owner=fragView)
        
        
        
        return fragView 
    }
}
