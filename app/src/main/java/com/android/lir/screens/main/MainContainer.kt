package com.android.lir.screens.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.android.lir.R
import com.android.lir.data.DataManager
import com.android.lir.utils.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.main_container.*
import javax.inject.Inject

@AndroidEntryPoint
class MainContainer : Fragment(R.layout.main_container) {

    @Inject
    lateinit var dataManager: DataManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) setupBottomBar()
    }

    private fun setupBottomBar() {
        bNavView?.itemIconTintList = null
        val navGraphIds = listOf(R.navigation.maps, R.navigation.contacts, R.navigation.chats, R.navigation.subscribes, R.navigation.settings, R.navigation.sign_in)
        bNavView?.setupWithNavController(
            navGraphIds = navGraphIds,
            fragmentManager = childFragmentManager,
            dataManager = dataManager,
            containerId = R.id.nav_host_container,
            intent = requireActivity().intent
        )
    }

    fun reselectItems() {
        bNavView.selectedItemId = bNavView.selectedItemId
    }
}
