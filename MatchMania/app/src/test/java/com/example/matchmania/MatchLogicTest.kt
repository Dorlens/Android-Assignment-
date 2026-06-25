package com.example.matchmania

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

// ─────────────────────────────────────────────────────────────────────────────
// MatchLogicTest.kt  —  Part D.  STARTER FILE.
//
// These run on a plain JVM (no emulator): right-click ▸ Run, or
//   ./gradlew testDebugUnitTest
//
// Two example tests are PROVIDED and already pass (they only check structure that
// is true even before you finish the logic). Below them is your TODO list — write
// the remaining tests. As you implement newBoard/flip/reset in MatchLogic.kt, your
// tests should turn green; while a function is still a stub, its test will fail —
// that's the test doing its job.
// ─────────────────────────────────────────────────────────────────────────────
class MatchLogicTest {

    // ── PROVIDED example #1: a fresh board has the right number of tiles. ──────
    @Test
    fun newBoard_has16Tiles() {
        assertEquals(CELL_COUNT, newBoard(DEFAULT_SYMBOLS).tiles.size)
    }

    // ── PROVIDED example #2: a fresh board is all face-down and not yet solved. ─
    @Test
    fun newBoard_startsUnsolvedAndFaceDown() {
        val board = newBoard(DEFAULT_SYMBOLS)
        assertEquals(0, board.moves)
        assertFalse(board.isSolved())
        assertTrue(board.tiles.all { it.state == TileState.FaceDown })
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  ✍️  YOUR TESTS (Part D) — write at least these (see hw7 for the full list).
    // ═══════════════════════════════════════════════════════════════════════════

    // 1. composition — PAIR_COUNT distinct faces, each used exactly twice.
    @Test
    fun newBoard_composition_isCorrect() {
        val board = newBoard(DEFAULT_SYMBOLS)
        val faceCounts = board.tiles.groupBy { it.face }.mapValues { it.value.size }
        assertEquals(PAIR_COUNT, faceCounts.size)
        assertTrue(faceCounts.all { it.value == 2 })
    }

    // 2. determinism — the same seed (Random(n)) gives the same board twice.
    @Test
    fun newBoard_isDeterministic() {
        val b1 = newBoard(DEFAULT_SYMBOLS, Random(42))
        val b2 = newBoard(DEFAULT_SYMBOLS, Random(42))
        assertEquals(b1, b2)
    }

    // 3. flip reveals — flipping a face-down tile makes it FaceUp.
    @Test
    fun flip_revealsFaceDownTile() {
        val board = newBoard(DEFAULT_SYMBOLS).flip(0)
        assertEquals(TileState.FaceUp, board.tiles[0].state)
    }

    // 4. flip matches — a tile + its twin both become Matched; moves == 1.
    @Test
    fun flip_matchesTwinTiles() {
        val b0 = newBoard(DEFAULT_SYMBOLS, Random(42))
        val face = b0.tiles[0].face
        val twinIndex = b0.tiles.indices.first { it != 0 && b0.tiles[it].face == face }

        val b1 = b0.flip(0)
        val b2 = b1.flip(twinIndex)

        assertEquals(TileState.Matched, b2.tiles[0].state)
        assertEquals(TileState.Matched, b2.tiles[twinIndex].state)
        assertEquals(1, b2.moves)
    }

    // 5. flip mismatch — two different tiles both stay FaceUp (moves == 1); the
    //                    next flip clears them back to FaceDown (moves stays 1).
    @Test
    fun flip_handlesMismatch() {
        val b0 = newBoard(DEFAULT_SYMBOLS, Random(42))
        val face0 = b0.tiles[0].face
        val mismatchIndex = b0.tiles.indices.first { b0.tiles[it].face != face0 }

        val b1 = b0.flip(0)
        val b2 = b1.flip(mismatchIndex)

        assertEquals(TileState.FaceUp, b2.tiles[0].state)
        assertEquals(TileState.FaceUp, b2.tiles[mismatchIndex].state)
        assertEquals(1, b2.moves)

        val thirdIndex = b0.tiles.indices.first { it != 0 && it != mismatchIndex }
        val b3 = b2.flip(thirdIndex)

        assertEquals(TileState.FaceDown, b3.tiles[0].state)
        assertEquals(TileState.FaceDown, b3.tiles[mismatchIndex].state)
        assertEquals(TileState.FaceUp, b3.tiles[thirdIndex].state)
        assertEquals(1, b3.moves)
    }

    // 6. immutability — flip() returns a NEW board; the original is unchanged.
    @Test
    fun flip_isImmutable() {
        val b0 = newBoard(DEFAULT_SYMBOLS)
        val b1 = b0.flip(0)
        assertTrue(b0 !== b1)
        assertEquals(TileState.FaceDown, b0.tiles[0].state)
    }

    // 7. win & reset — a fully-matched board isSolved(); reset() puts every
    //                  tile FaceDown, moves == 0, same faces in the same order.
    @Test
    fun winAndReset_workCorrectly() {
        var b = newBoard(DEFAULT_SYMBOLS, Random(42))
        val originalFaces = b.tiles.map { it.face }

        // Solve it
        val faces = b.tiles.map { it.face }.distinct()
        for (f in faces) {
            val currentB = b
            val indices = currentB.tiles.indices.filter { currentB.tiles[it].face == f && currentB.tiles[it].state == TileState.FaceDown }
            if (indices.size == 2) {
                b = b.flip(indices[0]).flip(indices[1])
            }
        }

        assertTrue(b.isSolved())

        val r = b.reset()
        assertFalse(r.isSolved())
        assertEquals(0, r.moves)
        assertTrue(r.tiles.all { it.state == TileState.FaceDown })
        assertEquals(originalFaces, r.tiles.map { it.face })
    }
}
