package com.android.lir.screens.splash

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.android.lir.R
import com.android.lir.base.vm.BaseVMFragment
import com.android.lir.base.vm.Event
import com.android.lir.data.ContactHolder
import com.android.lir.screens.main.contacts.contactscreen.ContactsFragment
import com.android.lir.utils.AppExtensions.getContactList
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class SplashFragment : BaseVMFragment<SplashViewModel>(R.layout.fragment_splash) {

    override val viewModel: SplashViewModel by viewModels()

    @Inject
    lateinit var contactHolder: ContactHolder

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (activity?.intent?.extras != null) goToMainScreen() else viewModel.startTimer()
        requestContactPermission()
    }

    override fun onEvent(event: Event) {
        super.onEvent(event)
        when(event) {
            TimerOver -> goToMainScreen()
        }
    }

    private fun setContactList() {
        lifecycleScope.launchWhenStarted {
            withContext(Dispatchers.Default) {
                contactHolder.contacts = context?.getContactList()
            }
        }
    }

    private fun requestContactPermission() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS),
                ContactsFragment.PERMISSIONS_REQUEST_READ_CONTACTS
            )
        } else {
            setContactList()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == ContactsFragment.PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (permissions[0] == Manifest.permission.READ_CONTACTS && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setContactList()
            } else
                Toast.makeText(context, "Пожалуйста разрешите использование списка контактов", Toast.LENGTH_SHORT).show()
        }
    }

    private fun goToMainScreen() {
        findNavController().navigate(R.id.toMain)
    }
}
