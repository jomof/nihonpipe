package com.jomof.nihonpipe.groveler


import java.io.ByteArrayInputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import kotlin.experimental.and

private fun encodeHex(digest: ByteArray): String {
    val sb = StringBuilder()
    for (aDigest in digest) {
        sb.append(Integer.toString(
                (aDigest and 0xff.toByte()) + 0x100,
                16).substring(1))
    }
    return sb.toString()
}

fun getSHA256OfString(string: String): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val `in` = ByteArrayInputStream(string.toByteArray(StandardCharsets.UTF_8))
    return hashToHex(digest, `in`)
}

private fun hashToHex(digest: MessageDigest, input: InputStream): String {
    val block = ByteArray(4096)
    var length = input.read(block)
    while (length > 0) {
        digest.update(block, 0, length)
        length = input.read(block)
    }
    return encodeHex(digest.digest())
}