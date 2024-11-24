package com.example.cube_solver.utils

import android.content.Context
import android.text.BoringLayout
import android.util.Log
import com.google.android.filament.utils.Float3
import com.google.android.filament.utils.Mat4
import java.nio.ByteBuffer
import java.util.concurrent.Executors

fun Context.readAsset(assetName: String): ByteBuffer {
    val input = assets.open(assetName)
    val bytes = ByteArray(input.available())
    input.read(bytes)
    return ByteBuffer.wrap(bytes)
}

fun String.isEmptyOrNull(): Boolean {
    return isNullOrEmpty() || this == "null" || this == "NULL"
}

fun String.isNotEmptyOrNull() = isEmptyOrNull().not()

fun String.toSafeInt(defaultValue : Int = -1) : Int {
    return try {
        toInt()
    }catch (e : Exception) {
        defaultValue
    }
}

fun Any?.log(tag: String = "message") {
    Log.e(tag, toString())
}

fun Float3.log(tag: String = "message") {
    Log.e(tag, "x = $x , y = $y , z = $z")
}

fun Mat4.log(tag: String = "message") {
    val message = """
    [ ${x.x}, ${x.y}, ${x.z}, ${x.w},
      ${y.x}, ${y.y}, ${y.z}, ${y.w},
      ${z.x}, ${z.y}, ${z.z}, ${z.w},
      ${w.x}, ${w.y}, ${w.z}, ${w.w} ]
    """.trimIndent()

    Log.e(tag, message)
}


fun FloatArray.log(tag: String = "message") {
    var message = ""
    for (i in indices) {
        if (i == 0) {
            message += "["
        }

        message += "${this[i]} ${if (i != lastIndex) "," else ""}"

        if (i == lastIndex) {
            message += "]"
        }

        if ((i + 1) % 4 == 0) {
            message += "\n"
        }
    }

    Log.e(tag, message)
}