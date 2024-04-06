package com.kemp.utils

import io.kubernetes.client.openapi.models.V1DeleteOptions
import io.kubernetes.client.util.generic.options.DeleteOptions
import io.kubernetes.client.util.generic.options.GetOptions
import io.kubernetes.client.util.generic.options.ListOptions
import io.kubernetes.client.util.generic.options.PatchOptions

fun PatchOptions.default(): PatchOptions {
    return this.fieldManager("kemp").force(false)
}

fun DeleteOptions.default(): DeleteOptions {
    return this
}

fun GetOptions.default() : GetOptions {
    return this
}

fun ListOptions.default() : ListOptions {
    return this
}