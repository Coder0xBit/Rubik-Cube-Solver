package com.example.cube_solver.utils

import android.content.Context
import android.text.BoringLayout
import android.util.Log
import com.google.android.filament.utils.Float3
import com.google.android.filament.utils.Float4
import com.google.android.filament.utils.Mat3
import com.google.android.filament.utils.Mat4
import com.google.android.filament.utils.Quaternion
import com.google.android.filament.utils.rotation
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


fun Mat4.translate(translation: Float3): Mat4 {
    val translationMatrix = Mat4.identity()
    translationMatrix[3, 0] = translation.x
    translationMatrix[3, 1] = translation.y
    translationMatrix[3, 2] = translation.z
    return this * translationMatrix
}

fun Mat4.scale(scale: Float3): Mat4 {
    val scaleMatrix = Mat4.identity()
    scaleMatrix[0, 0] = scale.x
    scaleMatrix[1, 1] = scale.y
    scaleMatrix[2, 2] = scale.z
    return this * scaleMatrix
}

fun Mat4.rotate(quaternion: Quaternion): Mat4 {
    val rotationMatrix = rotation(quaternion)
    return this * rotationMatrix
}

fun Mat4.getRotation() = Mat3(x = right, y = up, z = forward)

fun Mat3.toMat4(): Mat4 {
    return Mat4(
        x = Float4(x = x.x, y = x.y, z = x.z, w = 0f),
        y = Float4(x = y.y, y = y.y, z = y.z, w = 0f),
        z = Float4(x = z.z, y = z.y, z = z.z, w = 0f),
        w = Float4(x = 0f, y = 0f, z = 0f, w = 1f),
    )
}

fun Float.toRadians(): Float = Math.toRadians(this.toDouble()).toFloat()