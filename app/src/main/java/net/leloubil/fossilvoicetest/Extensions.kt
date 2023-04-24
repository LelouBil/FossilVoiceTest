package net.leloubil.fossilvoicetest

import android.os.Build

val Any.TAG: String
    get() {
        return with(javaClass) {
            if (isAnonymousClass) name else simpleName
        }
    }
