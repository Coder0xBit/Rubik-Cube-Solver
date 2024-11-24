package com.example.cube_solver.rubiksCube

import com.example.cube_solver.utils.toSafeInt

object RubiksCube {
    val numberFindingRegex = Regex("^(\\d+)_.*")
    val cubeFindingRegex = Regex("^\\d+_(?i)(Front|Back|Right|Left|Up|Down)$")
}

/*
 this enum is providing hardcoded array of indentifiers for faces and each individual cubies (from 3d model look up in blender)
 FRONT_CUBIES are 9 each individual cubes facing front
 MIDDLE_CUBIES are 8 each individual cubes in middle of Cube
 BACK_CUBIES are 9 each individual cubes facing back

 this will only work when the cube is first loaded

 each small cubes in 3d model of rubiks cube is named like 2_Black_0 , etc.. so this indentifier are the leading Number
 */
enum class Rubiks(val identifiers: MutableList<Int>) {
    FRONT_CUBIES(identifiers = mutableListOf(12, 24, 1, 15, 16, 4, 17, 23, 7)),
    MIDDLE_CUBIES(identifiers = mutableListOf(13, 22, 2, 19, -1, 9, 10, 20, 8)),
    BACK_CUBIES(identifiers = mutableListOf(14, 26, 3, 18, 21, 5, 11, 25, 6));

    companion object {
        val initialCubieIdentifiers =
            FRONT_CUBIES.identifiers + MIDDLE_CUBIES.identifiers + BACK_CUBIES.identifiers
    }
}