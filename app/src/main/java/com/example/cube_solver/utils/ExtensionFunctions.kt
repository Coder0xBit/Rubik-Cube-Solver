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

fun String.toSafeInt(defaultValue: Int = -1): Int {
    return try {
        toInt()
    } catch (e: Exception) {
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


fun <T> List<List<T>>.log(tag: String = "message") {
    val message = buildString {
        this@log.forEach { row ->
            append("[")
            row.forEachIndexed { index, element ->
                append(element.toString())
                if (index != lastIndex) {
                    append("  ")
                }
            }
            append("]")
            appendLine()
        }
    }
    Log.e(tag, message)
}

fun List<Int>.logList(tag: String = "message") {
    val message = buildString {
        this@logList.forEach {
            append(it.toString())
            append("  ")
        }
    }
    Log.e(tag, message)
}

fun <T> List<T>.toMatrix(rows: Int, cols: Int): List<List<T>> {
    require(this.size == rows * cols) { "List size must be exactly $rows x $cols = ${rows * cols}." }
    return List(rows) { row ->
        List(cols) { col -> this[row * cols + col] }
    }
}

fun <T> List<T>.toSquareMatrix(size: Int): List<List<T>> {
    require(this.size == size * size) { "Can not create Square Matrix" }
    return toMatrix(rows = size, cols = size)
}

fun <T> List<List<T>>.toFlatList(): List<T> {
    val list = mutableListOf<T>()
    forEach { list.addAll(it) }
    return list
}

fun <T> List<List<T>>.rotateClockwise(rotationCount: Int): List<List<T>> {
    var result = this
    repeat(rotationCount) {
        result = result.rotateClockwiseOnce()
    }
    return result
}

fun <T> List<List<T>>.rotateCounterclockwise(rotationCount: Int): List<List<T>> {
    var result = this
    repeat(rotationCount) {
        result = result.rotateCounterclockwiseOnce()
    }
    return result
}

fun <T> List<List<T>>.rotateClockwiseOnce(): List<List<T>> {
    val rows = this.size
    val cols = this[0].size
    return List(cols) { col ->
        List(rows) { row ->
            this[rows - row - 1][col]
        }
    }
}

fun <T> List<List<T>>.rotateCounterclockwiseOnce(): List<List<T>> {
    val rows = this.size
    val cols = this[0].size
    return List(cols) { col ->
        List(rows) { row ->
            this[row][cols - col - 1]
        }
    }
}
