package com.android.lir.data

import com.android.lir.dataclases.Contact
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactHolder @Inject constructor() {

    var contacts: List<Contact>? = null
}