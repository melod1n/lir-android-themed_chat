package com.android.lir.utils

open class Size(sizeInBytes: Int) {
    val value = sizeInBytes
}

open class KilobyteSize(size: Int) : Size(size * 1024)
open class MegabyteSize(size: Int) : KilobyteSize(size * 1024)