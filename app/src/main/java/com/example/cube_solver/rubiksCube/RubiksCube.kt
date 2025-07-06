package com.example.cube_solver.rubiksCube

import com.example.cube_solver.rubiksCube.RubiksCubeManager.RubiksCubeFace
import com.example.cube_solver.utils.toSafeInt
import com.google.android.filament.utils.Float3

object RubiksCube {
    val numberFindingRegex = Regex("^(\\d+)_.*")
}

/**
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

enum class RubiksMoveType {
    NONE, CLOCKWISE, ANTI_CLOCKWISE
}

enum class RubiksMove(
    val value: String,
    val face: RubiksCubeFace,
    val axis: Float3,
    val rotationCount: Int,
    val rubiksMoveType: RubiksMoveType
) {
    // Front Face Rotations
    F(
        value = "F",
        face = RubiksCubeFace.FRONT,
        axis = Float3(x = 1.0f, y = 0.0f, z = 0.0f),
        rotationCount = 1,
        rubiksMoveType = RubiksMoveType.CLOCKWISE
    ),
    F_APOSTROPHE(
        value = "F'",
        face = RubiksCubeFace.FRONT,
        axis = Float3(x = -1.0f, y = 0.0f, z = 0.0f),
        rotationCount = 1,
        rubiksMoveType = RubiksMoveType.ANTI_CLOCKWISE
    ),
    F_TWO(
        value = "F2",
        face = RubiksCubeFace.FRONT,
        axis = Float3(x = 1.0f, y = 0.0f, z = 0.0f),
        rotationCount = 2,
        rubiksMoveType = RubiksMoveType.CLOCKWISE
    ),

    // Back Face Rotations
    B(
        value = "B",
        face = RubiksCubeFace.BACK,
        axis = Float3(x = -1.0f, y = 0.0f, z = 0.0f),
        rotationCount = 1,
        rubiksMoveType = RubiksMoveType.ANTI_CLOCKWISE
    ),
    B_APOSTROPHE(
        value = "B'",
        face = RubiksCubeFace.BACK,
        axis = Float3(x = 1.0f, y = 0.0f, z = 0.0f),
        rotationCount = 1,
        rubiksMoveType = RubiksMoveType.CLOCKWISE
    ),
    B_TWO(
        value = "B2",
        face = RubiksCubeFace.BACK,
        axis = Float3(x = -1.0f, y = 0.0f, z = 0.0f),
        rotationCount = 2,
        rubiksMoveType = RubiksMoveType.ANTI_CLOCKWISE
    ),

    // Left Face Rotations
    L(
        value = "L",
        face = RubiksCubeFace.LEFT,
        axis = Float3(x = 0.0f, y = 0.0f, z = -1.0f),
        rotationCount = 1,
        rubiksMoveType = RubiksMoveType.ANTI_CLOCKWISE
    ),
    L_APOSTROPHE(
        value = "L'",
        face = RubiksCubeFace.LEFT,
        axis = Float3(x = 1.0f, y = 0.0f, z = 1.0f),
        rotationCount = 1,
        rubiksMoveType = RubiksMoveType.CLOCKWISE
    ),
    L_TWO(
        value = "L2",
        face = RubiksCubeFace.LEFT,
        axis = Float3(x = 0.0f, y = 0.0f, z = -1.0f),
        rotationCount = 2,
        rubiksMoveType = RubiksMoveType.ANTI_CLOCKWISE
    ),

    // Right Face Rotations
    R(
        value = "R",
        face = RubiksCubeFace.RIGHT,
        axis = Float3(x = 0.0f, y = 0.0f, z = 1.0f),
        rotationCount = 1,
        rubiksMoveType = RubiksMoveType.CLOCKWISE
    ),
    R_APOSTROPHE(
        value = "R'",
        face = RubiksCubeFace.RIGHT,
        axis = Float3(x = 0.0f, y = 0.0f, z = -1.0f),
        rotationCount = 1,
        rubiksMoveType = RubiksMoveType.ANTI_CLOCKWISE
    ),
    R_TWO(
        value = "R2",
        face = RubiksCubeFace.RIGHT,
        axis = Float3(x = 0.0f, y = 0.0f, z = 1.0f),
        rotationCount = 2,
        rubiksMoveType = RubiksMoveType.CLOCKWISE
    ),

    // Up Face Rotations
    U(
        value = "U",
        face = RubiksCubeFace.UP,
        axis = Float3(x = 0.0f, y = -1.0f, z = 0.0f),
        rotationCount = 1,
        rubiksMoveType = RubiksMoveType.ANTI_CLOCKWISE
    ),
    U_APOSTROPHE(
        value = "U'",
        face = RubiksCubeFace.UP,
        axis = Float3(x = 0.0f, y = 1.0f, z = 0.0f),
        rotationCount = 1,
        rubiksMoveType = RubiksMoveType.CLOCKWISE
    ),
    U_TWO(
        value = "U2",
        face = RubiksCubeFace.UP,
        axis = Float3(x = 0.0f, y = -1.0f, z = 0.0f),
        rotationCount = 2,
        rubiksMoveType = RubiksMoveType.ANTI_CLOCKWISE
    ),

    // Down Face Rotations
    D(
        value = "D",
        face = RubiksCubeFace.BOTTOM,
        axis = Float3(x = 0.0f, y = 1.0f, z = 0.0f),
        rotationCount = 1,
        rubiksMoveType = RubiksMoveType.CLOCKWISE
    ),
    D_APOSTROPHE(
        value = "D'",
        face = RubiksCubeFace.BOTTOM,
        axis = Float3(x = 0.0f, y = -1.0f, z = 0.0f),
        rotationCount = 1,
        rubiksMoveType = RubiksMoveType.ANTI_CLOCKWISE
    ),
    D_TWO(
        value = "D2",
        face = RubiksCubeFace.BOTTOM,
        axis = Float3(x = 0.0f, y = 1.0f, z = 0.0f),
        rotationCount = 2,
        rubiksMoveType = RubiksMoveType.CLOCKWISE
    );
}