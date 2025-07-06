package com.example.cube_solver.rubiksCube

enum class CubeFace(val value: String, val regex: Regex) {
    FRONT(value = "Front", regex = Regex("^[0-9]+_[fF][rR][oO][nN][tT]$")),
    BACK(value = "Back", regex = Regex("^[0-9]+_[bB][aA][cC][kK]$")),
    RIGHT(value = "Right", regex = Regex("^[0-9]+_[rR][iI][gG][hH][tT]$")),
    LEFT(value = "Left", regex = Regex("^[0-9]+_[lL][eE][fF][tT]$")),
    UP(value = "Up", regex = Regex("^[0-9]+_[uU][pP]$")),
    DOWN(value = "Down", regex = Regex("^[0-9]+_[dD][oO][wW][nN]$"));

    companion object {
        val BLACK_MESHES = Regex(".*[bB][lL][aA][cC][kK]_0$")
    }
}


infix fun String.into(cubeFace: CubeFace) = matches(cubeFace.regex)


