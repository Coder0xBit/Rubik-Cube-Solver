package com.example.cube_solver.utils

import com.example.cube_solver.min2phase.Search

object MinTwoPhase {
    fun simpleSolve(scrambledCube: String?) {
        val result = Search().solution(scrambledCube, 21, 100000000, 0, 0)
        println(result)
        // R2 U2 B2 L2 F2 U' L2 R2 B2 R2 D  B2 F  L' F  U2 F' R' D' L2 R'
    }

    fun outputControl(scrambledCube: String?) {
        var result = Search().solution(scrambledCube, 21, 100000000, 0, Search.APPEND_LENGTH)
        println(result)

        // R2 U2 B2 L2 F2 U' L2 R2 B2 R2 D  B2 F  L' F  U2 F' R' D' L2 R' (21f)
        result = Search().solution(
            scrambledCube,
            21,
            100000000,
            0,
            Search.USE_SEPARATOR or Search.INVERSE_SOLUTION
        )
        println(result)
        // R  L2 D  R  F  U2 F' L  F' .  B2 D' R2 B2 R2 L2 U  F2 L2 B2 U2 R2
    }

    fun findShorterSolutions(scrambledCube: String?) {
        //Find shorter solutions (try more probes even a solution has already been found)
        //In this example, we try AT LEAST 10000 phase2 probes to find shorter solutions.
        val result = Search().solution(scrambledCube, 21, 100000000, 10000, 0)
        println(result)
        // L2 U  D2 R' B  U2 L  F  U  R2 D2 F2 U' L2 U  B  D  R'
    }

    fun continueSearch(scrambledCube: String?) {
        //Continue to find shorter solutions
        val searchObj = Search()
        var result = searchObj.solution(scrambledCube, 21, 500, 0, 0)
        println(result)

        // R2 U2 B2 L2 F2 U' L2 R2 B2 R2 D  B2 F  L' F  U2 F' R' D' L2 R'
        result = searchObj.next(500, 0, 0)
        println(result)

        // D2 L' D' L2 U  R2 F  B  L  B  D' B2 R2 U' R2 U' F2 R2 U' L2
        result = searchObj.next(500, 0, 0)
        println(result)

        // L' U  B  R2 F' L  F' U2 L  U' B' U2 B  L2 F  U2 R2 L2 B2
        result = searchObj.next(500, 0, 0)
        println(result)

        // Error 8, no solution is found after 500 phase2 probes. Let's try more probes.
        result = searchObj.next(500, 0, 0)
        println(result)
        // L2 U  D2 R' B  U2 L  F  U  R2 D2 F2 U' L2 U  B  D  R'
    }

    fun test() {
        val startTime = System.nanoTime()
        Search.init()
        val scrambledCube: String = "DUUBULDBFRBFRRULLLBRDFFFBLURDBFDFDRFRULBLUFDURRBLBDUDL"
        simpleSolve(scrambledCube)
        println("Init time: " + (System.nanoTime() - startTime) / 1.0E6 + " ms")
    }
}
