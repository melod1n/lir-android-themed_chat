package com.android.lir.screens.main.contacts.contactscreen

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.android.lir.R
import com.android.lir.base.vm.BaseVMFragment
import com.android.lir.base.vm.Event
import com.android.lir.dataclases.Contact
import com.android.lir.dataclases.User
import com.reddit.indicatorfastscroll.FastScrollItemIndicator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_contacts.*
import kotlinx.android.synthetic.main.view_search.*
import javax.inject.Inject

@AndroidEntryPoint
class ContactsFragment : BaseVMFragment<ContactsViewModel>(R.layout.fragment_contacts) {

    @Inject
    lateinit var adapter: ContactsAdapter

    @Inject
    lateinit var favAdapter: FavoriteAdapter

    override val viewModel: ContactsViewModel by viewModels()

    private var selectedContact: Contact? = null

    companion object {
        const val PERMISSIONS_REQUEST_READ_CONTACTS = 1
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter.setListener { viewModel.goToChat(it) }
        adapter.setLongListener { showPopup(it) }
        favAdapter.setLongListener { showFavoritePopup(it) }

        prepareFavoriteActions()
        loadFavorites()

        etSearch.hint = "Поиск по контактам"

        adapter.setConnectListener {
            it.numbers.firstOrNull()?.let { firstNum ->
                Toast.makeText(
                    context,
                    "Отправляем приглашение по номеру $firstNum",
                    Toast.LENGTH_SHORT
                ).show()
            } ?: run {
                Toast.makeText(context, "У него нет номера", Toast.LENGTH_SHORT).show()
            }
        }

        fastscroller.setupWithRecyclerView(
            contacts,
            { position ->
                val item = adapter.currentList[position]
                FastScrollItemIndicator.Text(item.name?.substring(0, 1)?.uppercase() ?: "#")
            }
        )
        favAdapter.setActions(
            onAdd = {
                Toast.makeText(context, "Добавляем в избранное", Toast.LENGTH_SHORT).show()
            },
            onStartChat = { viewModel.goToChat(it) }
        )
        ivClose.setOnClickListener {
            etSearch.text = null
            viewModel.getUsers(requireContext())
        }

        etSearch.doAfterTextChanged {
            message.isVisible = it.isNullOrBlank()
            favorites.isVisible = it.isNullOrBlank()

            viewModel.setSearchStr(it?.toString() ?: "")

            ivClose.isVisible = !it.isNullOrBlank()
        }

        favorites.adapter = favAdapter
        contacts.adapter = adapter

        viewModel.users.observe(viewLifecycleOwner) {
            it?.let {
                adapter.submitList(it)
            }
        }
        viewModel.users.value ?: requestContactPermission()
    }

    private fun prepareFavoriteActions() {
        startMessage.setOnClickListener {
            selectedContact?.let { contact ->
                viewModel.goToChat(
                    contact.serverId?.toIntOrNull() ?: contact.id?.toIntOrNull() ?: -1
                )
            }
        }

        favorite.setOnClickListener {
            selectedContact?.let { contact ->
                it.isClickable = false
                selectedContact = null
                favActions.isVisible = false

                if (favAdapter.containsId(
                        contact.serverId?.toIntOrNull() ?: contact.id?.toIntOrNull() ?: -1
                    )
                )
                    viewModel.deleteFromFavorites(
                        contact.serverId?.toIntOrNull() ?: contact.id?.toIntOrNull() ?: -1
                    )
                else
                    viewModel.addToFavorites(contact)
            }
        }
    }

    private fun loadFavorites() {
        viewModel.loadFavorites()
    }

    private fun showPopup(contact: Contact) {
        selectedContact = contact
        favActions.isVisible = true

        favorite.icon = ContextCompat.getDrawable(
            requireContext(),
            if (favAdapter.containsId(
                    contact.serverId?.toIntOrNull() ?: contact.id?.toIntOrNull() ?: -1
                )
            )
                R.drawable.ic_baseline_favorite_24 else
                R.drawable.ic_favorite_outline
        )
        return
        val items = arrayOf("Добавить в избранное")
        val builder = AlertDialog.Builder(requireContext())

        builder.setItems(items) { _, i ->
            when (i) {
                0 -> viewModel.addToFavorites(contact)
            }
        }
        builder.show()
    }

    private fun showFavoritePopup(fav: FavInfo) {
        selectedContact = Contact(fav.id.toString(), fav.text, listOf(), fav.imageUrl)
        favActions.isVisible = true
        favorite.icon =
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_favorite_24)
        return
        val items = arrayOf("Удалить из избранного")
        val builder = AlertDialog.Builder(requireContext())

        builder.setItems(items) { _, i ->
            when (i) {
                0 -> viewModel.deleteFromFavorites(id)
            }
        }
        builder.show()
    }

    private fun requestContactPermission() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.READ_CONTACTS),
                PERMISSIONS_REQUEST_READ_CONTACTS
            )
        } else {
            viewModel.getUsers(context)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (permissions[0].equals(Manifest.permission.READ_CONTACTS) &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                viewModel.getUsers(context)
            } else
                Toast.makeText(
                    context,
                    "Пожалуйста разрешите использование списка контактов",
                    Toast.LENGTH_SHORT
                ).show()
        }
    }

    override fun onEvent(event: Event) {
        super.onEvent(event)
        when (event) {

            is AddToFavoritesEvent -> addToFavorites(event.contact)
            is DeleteFromFavoritesEvent -> deleteFromFavorites(event.id)
            is GoToChatEvent -> goToChat(event.id)
            is GetFavoritesEvent -> fillFavorites(event.favoriteUsers)
            is StartProgress -> progress.isVisible = true
            is StopProgress -> progress.isVisible = false
        }
    }

    private fun goToChat(id: Int) {
        hideKeyboard()
        findNavController().navigate(R.id.toPrivateChat, bundleOf("chat_id" to id))
    }

    private fun addToFavorites(contact: Contact) {
        favorite.isClickable = true
        favAdapter.list.add(
            FavInfo(
                contact.serverId?.toIntOrNull() ?: -1,
                contact.serverPhoto ?: "",
                contact.serverName ?: ""
            )
        )
        favAdapter.notifyDataSetChanged()
    }

    private fun deleteFromFavorites(id: Int) {
        favorite.isClickable = true
        for (i in favAdapter.list.indices) {
            val fav = favAdapter.list[i]
            if (fav.id == id) {
                favAdapter.list.removeAt(i)
                favAdapter.notifyDataSetChanged()
                break
            }
        }
    }

    private fun fillFavorites(favorites: List<User>) {
        val items = arrayListOf<FavInfo>()
        favorites.forEach { items.add(FavInfo(it.id, it.photo, it.name)) }

        favAdapter.submitList(items)
        favAdapter.notifyDataSetChanged()
    }
}
