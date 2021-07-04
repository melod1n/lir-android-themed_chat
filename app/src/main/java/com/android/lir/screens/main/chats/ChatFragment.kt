package com.android.lir.screens.main.chats

import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.android.lir.R
import com.android.lir.base.vm.BaseVMFragment
import com.android.lir.base.vm.Event
import com.android.lir.screens.main.contacts.contactscreen.StartProgress
import com.android.lir.screens.main.contacts.contactscreen.StopProgress
import com.android.lir.utils.onQueryTextChanged
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_chat.*
import javax.inject.Inject

@AndroidEntryPoint
class ChatFragment : BaseVMFragment<ChatViewModel>(R.layout.fragment_chat) {
    override val viewModel: ChatViewModel by viewModels()

    @Inject
    lateinit var adapter: ChatsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter.setListener { goToChat(it) }
        ((toolbar.menu.findItem(R.id.action_search))?.actionView as? SearchView)?.let(::initSearchView)
        rvChats.adapter = adapter
        viewModel.chats.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
        viewModel.getChats()
    }

    private fun initSearchView(searchView: SearchView) {
        searchView.onQueryTextChanged(viewModel::setQuery)
        searchView.maxWidth = Integer.MAX_VALUE
        searchView.setOnCloseListener { viewModel.setQuery(""); false }
        searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_button)?.setColorFilter(context?.getColor(R.color.white) ?: 0)
        searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)?.setColorFilter(context?.getColor(R.color.white) ?: 0)
        val editText = searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        editText.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
        editText.setBackgroundResource(R.drawable.textfield_searchview_holo_light)
        editText.hint = "Поиск..."
    }

    override fun onEvent(event: Event) {
        super.onEvent(event)
        when (event) {
            is StartProgress -> progressBar.isVisible = true
            is StopProgress -> progressBar.isVisible = false
        }
    }

    private fun goToChat(id: Int) {
        hideKeyboard()
        findNavController().navigate(R.id.toPrivateChat, bundleOf("chat_id" to id))
    }
}
