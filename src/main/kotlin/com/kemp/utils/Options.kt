package com.kemp.utils

import io.kubernetes.client.util.generic.options.DeleteOptions
import io.kubernetes.client.util.generic.options.GetOptions
import io.kubernetes.client.util.generic.options.ListOptions
import io.kubernetes.client.util.generic.options.PatchOptions

fun PatchOptions.default(): PatchOptions {
    return this.dryRun("none").fieldManager("kemp").force(false)
}

fun DeleteOptions.default() {

}

fun GetOptions.default() {

}

fun ListOptions.default() {

}