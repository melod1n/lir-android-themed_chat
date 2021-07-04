package com.android.lir.screens.main.settings

import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.android.lir.R
import com.android.lir.base.vm.BaseVMFragment
import com.android.lir.data.DataManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_settings.*
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : BaseVMFragment<SettingsViewModel>(R.layout.fragment_settings) {

    override val viewModel: SettingsViewModel by viewModels()

    @Inject
    lateinit var dataManager: DataManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rlSelectLanguage.setOnClickListener { }
        rlSupport.setOnClickListener { }
        rlNotification.setOnClickListener { }
        val isRegister = dataManager.token.isNotBlank()
        rlSignIn.isVisible = !isRegister
        sigInDivider.isVisible = !isRegister
        quit.isGone = !isRegister
        quit.setOnClickListener {
            dataManager.clear()
            sigInDivider.isVisible = true
            rlSignIn.isVisible = true
            quit.isGone = true
        }
        rlSignIn.setOnClickListener {
            findNavController().navigate(R.id.toSignIn)
        }
        rlConfidentiality.setOnClickListener { }
        rlAboutApp.setOnClickListener { }
    }
}
