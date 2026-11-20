package com.grandpyanne.daemrevise.data

import kotlin.random.Random

// crafted by Grandpyanne™
// Game rules only (no drawing), so they can be tested on their own.

/** A block shape for Kapi Blocks: the cells it covers (row, col) and its colour (1..6). */
class Piece(val cells: List<Pair<Int, Int>>, val color: Int) {
    val rows: Int = cells.maxOf { it.first } + 1
    val cols: Int = cells.maxOf { it.second } + 1
}

/**
 * Kapi Blocks: drop shapes on an 8×8 board. A full row or column clears.
 * No rotating; when none of the three shapes in hand fits anywhere, the game ends.
 */
class BlocksGame(private val random: Random = Random.Default) {
    val size = 8
    /** 0 = empty, otherwise a colour 1..6. Row-major. */
    val board = IntArray(size * size)
    val hand = arrayOfNulls<Piece>(3)
    var score = 0
        private set
    /** Lines cleared in a row on consecutive moves; adds a bonus. */
    var streak = 0
        private set

    /** Cells the last move cleared, as (board index, colour), for the clear animation. */
    var lastCleared: List<Pair<Int, Int>> = emptyList()
        private set

    /** Points the last move scored. */
    var lastGain = 0
        private set

    /** Board cells the last shape covered (for the landing bounce). */
    var lastPlaced: List<Int> = emptyList()
        private set

    /** True if the last move emptied the whole board. */
    var lastPerfect = false
        private set

    init {
        dealHand()
    }

    fun cell(r: Int, c: Int) = board[r * size + c]

    fun fits(p: Piece, row: Int, col: Int): Boolean = p.cells.all { (dr, dc) ->
        val r = row + dr
        val c = col + dc
        r in 0 until size && c in 0 until size && board[r * size + c] == 0
    }

    fun fitsAnywhere(p: Piece): Boolean =
        (0..size - p.rows).any { r -> (0..size - p.cols).any { c -> fits(p, r, c) } }

    /** Rows and columns that would be full after placing [p] at row/col (for highlighting). */
    fun linesIfPlaced(p: Piece, row: Int, col: Int): Pair<List<Int>, List<Int>> {
        val filled = BooleanArray(size * size) { board[it] != 0 }
        p.cells.forEach { (dr, dc) -> filled[(row + dr) * size + col + dc] = true }
        val rows = (0 until size).filter { r -> (0 until size).all { c -> filled[r * size + c] } }
        val cols = (0 until size).filter { c -> (0 until size).all { r -> filled[r * size + c] } }
        return rows to cols
    }

    /** Places hand[index] at row/col. Returns the number of lines cleared, or -1 if it does not fit. */
    fun place(index: Int, row: Int, col: Int): Int {
        val p = hand[index] ?: return -1
        if (!fits(p, row, col)) return -1
        val (rows, cols) = linesIfPlaced(p, row, col)
        p.cells.forEach { (dr, dc) -> board[(row + dr) * size + col + dc] = p.color }
        lastPlaced = p.cells.map { (dr, dc) -> (row + dr) * size + col + dc }
        val clearing = LinkedHashSet<Int>()
        rows.forEach { r -> for (c in 0 until size) clearing.add(r * size + c) }
        cols.forEach { c -> for (r in 0 until size) clearing.add(r * size + c) }
        lastCleared = clearing.map { it to board[it] }
        val before = score
        rows.forEach { r -> for (c in 0 until size) board[r * size + c] = 0 }
        cols.forEach { c -> for (r in 0 until size) board[r * size + c] = 0 }
        val lines = rows.size + cols.size
        hand[index] = null
        score += p.cells.size
        if (lines > 0) {
            streak++
            // 10 for one line, 30 for two, 60 for three… plus a streak bonus.
            score += 10 * lines * (lines + 1) / 2 + 5 * (streak - 1)
        } else {
            streak = 0
        }
        lastPerfect = lines > 0 && board.all { it == 0 }
        if (lastPerfect) score += PERFECT_BONUS
        lastGain = score - before
        if (hand.all { it == null }) dealHand()
        return lines
    }

    val isOver: Boolean get() = hand.filterNotNull().none { fitsAnywhere(it) }

    private fun dealHand() {
        for (i in hand.indices) hand[i] = randomPiece()
    }

    /** Rescue (once a game): throw away the shapes in hand and get three new ones. */
    var rescueLeft = true
        private set

    fun newHand(): Boolean {
        if (!rescueLeft) return false
        rescueLeft = false
        dealHand()
        return true
    }

    private fun randomPiece(): Piece {
        var roll = random.nextInt(TOTAL_WEIGHT)
        val shape = WEIGHTED.first { (_, w) -> roll -= w; roll < 0 }.first
        return Piece(shape, 1 + random.nextInt(6))
    }

    companion object {
        const val PERFECT_BONUS = 300

        private fun shape(vararg rows: String): List<Pair<Int, Int>> =
            rows.flatMapIndexed { r, line -> line.mapIndexedNotNull { c, ch -> if (ch == '#') r to c else null } }

        /**
         * Shapes and how often they come up. Squares, Ts, Ls and corners come up most; singles,
         * dominoes and short bars are there to rescue a tight board; big blocks are rare.
         */
        private val WEIGHTED: List<Pair<List<Pair<Int, Int>>, Int>> = listOf(
            shape("#") to 3,
            shape("##") to 3, shape("#", "#") to 3,
            shape("###") to 4, shape("#", "#", "#") to 4,
            shape("####") to 3, shape("#", "#", "#", "#") to 3,
            shape("#####") to 1, shape("#", "#", "#", "#", "#") to 1,
            shape("##", "##") to 7,
            shape("###", "###", "###") to 1,
            shape("###", "###") to 1, shape("##", "##", "##") to 1,
            // small corners
            shape("##", "#.") to 3, shape("##", ".#") to 3, shape("#.", "##") to 3, shape(".#", "##") to 3,
            // Ls
            shape("#.", "#.", "##") to 3, shape(".#", ".#", "##") to 3, shape("##", "#.", "#.") to 3, shape("##", ".#", ".#") to 3,
            shape("###", "#..") to 3, shape("###", "..#") to 3, shape("#..", "###") to 3, shape("..#", "###") to 3,
            // Ts
            shape("###", ".#.") to 3, shape(".#.", "###") to 3, shape("#.", "##", "#.") to 3, shape(".#", "##", ".#") to 3,
            // Zs and Ss
            shape("##.", ".##") to 2, shape(".##", "##.") to 2, shape("#.", "##", ".#") to 2, shape(".#", "##", "#.") to 2,
            // big corners
            shape("###", "#..", "#..") to 1, shape("###", "..#", "..#") to 1, shape("#..", "#..", "###") to 1, shape("..#", "..#", "###") to 1,
        )
        private val TOTAL_WEIGHT = WEIGHTED.sumOf { it.second }

        val SHAPES: List<List<Pair<Int, Int>>> = WEIGHTED.map { it.first }
    }
}

enum class Swipe { UP, DOWN, LEFT, RIGHT }

/**
 * Kapi Jewels: swap two neighbouring tiles to line up 3 or more of the same kind in a row or
 * column. Matched tiles clear, the ones above fall, new ones drop in, and new lines clear too
 * (a cascade, worth more each level). Bigger matches leave a power-up tile behind:
 *
 *  - 4 in a line      → Jet: clears its whole row (made from a row) or column (from a column)
 *  - L or T shape     → Bomb: clears the 3×3 around it
 *  - 5 in a line      → Splash: swap it with any tile to clear every tile of that kind
 *
 * Power-ups go off when they are cleared. Swapping two power-ups together sets off a combo.
 * A swap that does nothing is undone and costs no move. The game ends when the moves run out.
 */
class CrushGame(
    private val random: Random = Random.Default,
    val size: Int = 7,
    val kinds: Int = 5,
    moves: Int = 20,
    /** Level goals: tile kind -> how many to collect. Empty = no goals (play for score). */
    val goals: Map<Int, Int> = emptyMap(),
) {
    /** Tiles of each kind cleared so far. */
    val collected = IntArray(kinds)

    /** True once every goal is reached. */
    val goalsMet: Boolean get() = goals.isNotEmpty() && goals.all { (k, n) -> collected[k] >= n }

    val won: Boolean get() = goalsMet

    /** Points added at the end for moves left over. */
    var bonus = 0
        private set

    /** Tile kind 0 until [kinds]; a Splash tile has kind [SPLASH_KIND]. Row-major. */
    val board = IntArray(size * size)
    /** The power-up on each cell: [NONE], [JET_ROW], [JET_COL], [BOMB] or [SPLASH]. */
    val special = IntArray(size * size)
    var score = 0
        private set
    var movesLeft = moves
        private set

    val SPLASH_KIND: Int get() = kinds

    /**
     * One clearing step of the last move, for the animation: the board before it, what cleared,
     * the power-ups made, the board after the tiles fell, how far each tile fell, and what to cheer.
     */
    class Step(
        val before: IntArray,
        val beforeSpecial: IntArray,
        val cleared: Set<Int>,
        val after: IntArray,
        val afterSpecial: IntArray,
        val fall: IntArray,
        val level: Int,
        val events: List<String>,
        /** Power-ups that went off in this step: (cell, type), for beams and shockwaves. */
        val blasts: List<Pair<Int, Int>> = emptyList(),
        /** Points this step scored. */
        val points: Int = 0,
    )

    var lastSteps: List<Step> = emptyList()
        private set

    init {
        fill()
    }

    fun cell(r: Int, c: Int) = board[r * size + c]

    val isOver: Boolean get() = movesLeft <= 0 || goalsMet

    /** Rescue: more moves after running out. */
    fun addMoves(n: Int) {
        if (!goalsMet) movesLeft += n
    }

    private fun neighbours(a: Int, b: Int): Boolean {
        val ar = a / size; val ac = a % size; val br = b / size; val bc = b % size
        return kotlin.math.abs(ar - br) + kotlin.math.abs(ac - bc) == 1
    }

    /** Swaps cells a and b if they are neighbours and it does something. Returns false (board unchanged) otherwise. */
    fun swap(a: Int, b: Int): Boolean {
        if (isOver || a !in board.indices || b !in board.indices || !neighbours(a, b)) return false
        val sa = special[a]
        val sb = special[b]
        val events = ArrayList<String>()
        val start: Set<Int>
        when {
            sa == SPLASH || sb == SPLASH -> {
                start = HashSet<Int>()
                if (sa == SPLASH && sb == SPLASH) {
                    start.addAll(board.indices)
                    events.add("Board wipe! 🌊")
                } else {
                    val splashAt = if (sa == SPLASH) a else b
                    val other = if (sa == SPLASH) b else a
                    val kind = board[other]
                    val otherSpecial = special[other]
                    start.add(splashAt)
                    special[splashAt] = NONE
                    board.indices.filter { board[it] == kind }.forEach { i ->
                        start.add(i)
                        // Splash + Jet/Bomb: every tile of that kind becomes that power-up and goes off.
                        if (otherSpecial == JET_ROW || otherSpecial == JET_COL) special[i] = if (random.nextBoolean()) JET_ROW else JET_COL
                        if (otherSpecial == BOMB) special[i] = BOMB
                    }
                    events.add(if (otherSpecial != NONE) "Mega Prism!" else "Prism!")
                }
            }
            sa != NONE && sb != NONE -> {
                exchange(a, b)
                val r = b / size
                val c = b % size
                val area = HashSet<Int>()
                val jets = listOf(sa, sb).count { it == JET_ROW || it == JET_COL }
                val bombs = listOf(sa, sb).count { it == BOMB }
                when {
                    jets == 2 -> { area.addAll(rowCells(r)); area.addAll(colCells(c)); events.add("Double Beam!") }
                    jets == 1 && bombs == 1 -> {
                        for (d in -1..1) { if (r + d in 0 until size) area.addAll(rowCells(r + d)); if (c + d in 0 until size) area.addAll(colCells(c + d)) }
                        events.add("Beam Burst!")
                    }
                    else -> { area.addAll(square(r, c, 2)); events.add("Mega Blast! 💥💥") }
                }
                special[a] = NONE
                special[b] = NONE
                area.add(a); area.add(b)
                start = area
            }
            else -> {
                exchange(a, b)
                if (findRuns().isEmpty()) {
                    exchange(a, b)
                    return false
                }
                start = emptySet()
            }
        }
        movesLeft--
        resolve(if (start.isEmpty()) null else start, a to b, events)
        if (!hasMove()) fill()
        if (goalsMet && bonus == 0) {
            bonus = movesLeft * 60
            score += bonus
        }
        return true
    }

    /** A swap that would do something, if there is one (for hints and to avoid dead boards). */
    fun hint(): Pair<Int, Int>? {
        for (i in board.indices) {
            val r = i / size
            val c = i % size
            for (j in listOfNotNull(if (c + 1 < size) i + 1 else null, if (r + 1 < size) i + size else null)) {
                if (special[i] == SPLASH || special[j] == SPLASH) return i to j
                if (special[i] != NONE && special[j] != NONE) return i to j
                swapKinds(i, j)
                val ok = findRuns().isNotEmpty()
                swapKinds(i, j)
                if (ok) return i to j
            }
        }
        return null
    }

    fun hasMove(): Boolean = hint() != null

    private fun swapKinds(a: Int, b: Int) {
        val t = board[a]; board[a] = board[b]; board[b] = t
    }

    private fun exchange(a: Int, b: Int) {
        swapKinds(a, b)
        val t = special[a]; special[a] = special[b]; special[b] = t
    }

    private fun rowCells(r: Int) = (0 until size).map { r * size + it }
    private fun colCells(c: Int) = (0 until size).map { it * size + c }
    private fun square(r: Int, c: Int, radius: Int): List<Int> =
        (r - radius..r + radius).flatMap { rr -> (c - radius..c + radius).map { cc -> rr to cc } }
            .filter { (rr, cc) -> rr in 0 until size && cc in 0 until size }
            .map { (rr, cc) -> rr * size + cc }

    /** Every straight run of 3+ equal (ordinary-kind) tiles, as lists of board indices. */
    fun findRuns(): List<List<Int>> {
        val runs = ArrayList<List<Int>>()
        fun same(x: Int, y: Int) = board[x] == board[y] && board[x] in 0 until kinds
        for (r in 0 until size) {
            var c = 0
            while (c < size) {
                var e = c
                while (e + 1 < size && same(r * size + e + 1, r * size + c)) e++
                if (e - c + 1 >= 3 && board[r * size + c] in 0 until kinds) runs.add((c..e).map { r * size + it })
                c = e + 1
            }
        }
        for (c in 0 until size) {
            var r = 0
            while (r < size) {
                var e = r
                while (e + 1 < size && same((e + 1) * size + c, r * size + c)) e++
                if (e - r + 1 >= 3 && board[r * size + c] in 0 until kinds) runs.add((r..e).map { it * size + c })
                r = e + 1
            }
        }
        return runs
    }

    /** Groups touching runs (so L and T shapes count as one) and decides which power-up each group leaves. */
    private fun powerUps(runs: List<List<Int>>, swapped: Pair<Int, Int>?): Map<Int, Int> {
        val groups = ArrayList<MutableList<List<Int>>>()
        for (run in runs) {
            val touching = groups.filter { g -> g.any { other -> other.any { it in run } } }
            val merged = mutableListOf(run)
            touching.forEach { merged.addAll(it); groups.remove(it) }
            groups.add(merged)
        }
        val made = HashMap<Int, Int>()
        for (g in groups) {
            val cells = g.flatten().toSet()
            val longest = g.maxOf { it.size }
            val horizontal = g.any { it.size > 1 && it[1] - it[0] == 1 }
            val vertical = g.any { it.size > 1 && it[1] - it[0] == size }
            val crossing = if (horizontal && vertical) cells.firstOrNull { cell -> g.count { cell in it } > 1 } else null
            val type = when {
                longest >= 5 -> SPLASH
                crossing != null -> BOMB
                longest == 4 -> if (g.first { it.size == 4 }.let { it[1] - it[0] == 1 }) JET_ROW else JET_COL
                else -> NONE
            }
            if (type == NONE) continue
            val pivot = swapped?.toList()?.firstOrNull { it in cells }
                ?: crossing
                ?: g.maxBy { it.size }.let { it[it.size / 2] }
            made[pivot] = type
        }
        return made
    }

    /** Cells a power-up clears when it goes off at [i]. */
    private fun blast(i: Int, type: Int): List<Int> {
        val r = i / size
        val c = i % size
        return when (type) {
            JET_ROW -> rowCells(r)
            JET_COL -> colCells(c)
            BOMB -> square(r, c, 1)
            SPLASH -> {
                val common = (0 until kinds).maxByOrNull { k -> board.count { it == k } } ?: 0
                board.indices.filter { board[it] == common } + i
            }
            else -> listOf(i)
        }
    }

    private fun resolve(firstClear: Set<Int>?, swapped: Pair<Int, Int>, firstEvents: List<String>) {
        val steps = ArrayList<Step>()
        var level = 1
        while (true) {
            val events = ArrayList<String>()
            val cleared = HashSet<Int>()
            val made: Map<Int, Int>
            if (level == 1 && firstClear != null) {
                cleared.addAll(firstClear)
                events.addAll(firstEvents)
                made = emptyMap()
            } else {
                val runs = findRuns()
                if (runs.isEmpty()) break
                made = powerUps(runs, if (level == 1) swapped else null)
                runs.forEach { cleared.addAll(it) }
                made.values.forEach {
                    events.add(
                        when (it) {
                            SPLASH -> "Prism made!"
                            BOMB -> "Burst made!"
                            else -> "Beam made!"
                        },
                    )
                }
            }
            // Power-ups caught in the clear go off, and can set each other off.
            val queue = ArrayDeque(cleared.filter { special[it] != NONE && it !in made })
            val fired = HashSet<Int>()
            val blasts = ArrayList<Pair<Int, Int>>()
            while (queue.isNotEmpty()) {
                val i = queue.removeFirst()
                if (!fired.add(i)) continue
                blasts.add(i to special[i])
                blast(i, special[i]).forEach { j ->
                    if (cleared.add(j) && special[j] != NONE && j !in made) queue.addLast(j)
                }
            }
            if (fired.isNotEmpty() && events.isEmpty()) {
                events.add(if (fired.size > 1) "Chain reaction! ⚡" else "Kaboom! 💥")
            }
            made.keys.forEach { cleared.remove(it) }

            val before = board.copyOf()
            val beforeSpecial = special.copyOf()
            val points = cleared.size * 10 * level + made.values.fold(0) { sum, t -> sum + if (t == SPLASH) 60 else if (t == BOMB) 40 else 20 }
            score += points

            cleared.forEach { if (board[it] in 0 until kinds) collected[board[it]]++ }
            cleared.forEach { board[it] = -1; special[it] = NONE }
            made.forEach { (i, type) ->
                special[i] = type
                if (type == SPLASH) board[i] = SPLASH_KIND
            }
            // Tiles fall into the gaps; new tiles drop in from the top.
            val fall = IntArray(size * size)
            for (c in 0 until size) {
                val kept = (size - 1 downTo 0).map { it * size + c }.filter { board[it] >= 0 }
                val kinds0 = kept.map { board[it] }
                val specials0 = kept.map { special[it] }
                val fromRow = kept.map { it / size }
                val newCount = size - kept.size
                for (k in 0 until size) {
                    val r = size - 1 - k
                    val i = r * size + c
                    if (k < kept.size) {
                        board[i] = kinds0[k]
                        special[i] = specials0[k]
                        fall[i] = r - fromRow[k]
                    } else {
                        board[i] = random.nextInt(kinds)
                        special[i] = NONE
                        fall[i] = newCount
                    }
                }
            }
            steps.add(Step(before, beforeSpecial, cleared, board.copyOf(), special.copyOf(), fall, level, events, blasts, points))
            level++
            if (level > 50) break // safety: never loop forever
        }
        lastSteps = steps
    }

    /** Fills the board with ordinary tiles, no ready-made lines, and at least one possible move. */
    private fun fill() {
        do {
            for (i in board.indices) {
                val r = i / size
                val c = i % size
                special[i] = NONE
                var k: Int
                do {
                    k = random.nextInt(kinds)
                } while ((c >= 2 && board[i - 1] == k && board[i - 2] == k) ||
                    (r >= 2 && board[i - size] == k && board[i - 2 * size] == k)
                )
                board[i] = k
            }
        } while (!hasMove())
    }

    companion object {
        /** A level: its moves and goals. Goals grow and add kinds as the levels go up. */
        fun forLevel(level: Int, random: Random = Random.Default): CrushGame {
            val pick = Random(level * 7919)
            val kindsNeeded = when {
                level < 3 -> 1
                level < 8 -> 2
                else -> 3
            }
            val chosen = (0 until 5).shuffled(pick).take(kindsNeeded)
            val perKind = (12 + level - (kindsNeeded - 1) * 3).coerceIn(12, 26)
            val moves = 18 + kindsNeeded * 3 + (level % 3)
            return CrushGame(random = random, moves = moves, goals = chosen.associateWith { perKind })
        }

        const val NONE = 0
        const val JET_ROW = 1
        const val JET_COL = 2
        const val BOMB = 3
        const val SPLASH = 4
    }
}

/**
 * Kapi Mahjong (tile match): tiles are stacked in layers. Tap a free tile (nothing lying on
 * top, and an open side, left or right) and it flies up into a 4-slot tray. Two matching tiles
 * in the tray clear. If the tray fills up with no match, the game is lost. Clear every tile to
 * finish the level. Every deal can be cleared. Help: Undo, Hint (3), Shuffle (2), and one
 * rescue a game that sends the tray's tiles back to the board.
 *
 * Positions are in half-tiles, so upper layers can sit across two tiles below, like a real stack.
 */
class MahjongGame(val level: Int = 1, private val random: Random = Random.Default) {
    /** A tile position: layer, and top-left corner in half-tile units. A tile covers 2×2 half-units. */
    class Slot(val layer: Int, val x: Int, val y: Int)

    val slots: List<Slot> = layoutFor(level)
    /** Face of the tile in each slot (0 until [FACES]). */
    val face = IntArray(slots.size) { -1 }
    /** Where each tile is: [ON_BOARD], [IN_TRAY] or [CLEARED]. */
    val state = IntArray(slots.size) { ON_BOARD }
    /** Tiles in the tray, in the order they arrived. */
    val tray = ArrayList<Int>()
    var score = 0
        private set
    var shufflesLeft = 2
        private set
    var hintsLeft = 3
        private set
    var rescueLeft = true
        private set
    /** Pairs cleared in a row; adds a bonus. */
    var streak = 0
        private set
    /** The pair the last pick cleared (for the animation), or null. */
    var lastCleared: Pair<Int, Int>? = null
        private set
    private val history = ArrayList<IntArray>() // tile, partner (-1 if none), points

    /** Width and height of the layout in half-units (for drawing). */
    val width: Int = slots.maxOf { it.x } + 2
    val height: Int = slots.maxOf { it.y } + 2
    val layers: Int = slots.maxOf { it.layer } + 1

    private val above: List<List<Int>> = slots.map { s ->
        slots.indices.filter { j -> slots[j].layer == s.layer + 1 && overlaps(s, slots[j]) }
    }
    private val leftOf: List<List<Int>> = slots.map { s ->
        slots.indices.filter { j -> slots[j].layer == s.layer && slots[j].x == s.x - 2 && kotlin.math.abs(slots[j].y - s.y) < 2 }
    }
    private val rightOf: List<List<Int>> = slots.map { s ->
        slots.indices.filter { j -> slots[j].layer == s.layer && slots[j].x == s.x + 2 && kotlin.math.abs(slots[j].y - s.y) < 2 }
    }

    init {
        require(slots.size % 2 == 0) { "layout must have an even number of tiles" }
        deal(slots.indices.toList(), (0 until slots.size / 2).map { it % FACES })
    }

    val tilesLeft: Int get() = state.count { it != CLEARED }
    val won: Boolean get() = tilesLeft == 0
    /** The tray is full and holds no pair. */
    val lost: Boolean get() = tray.size >= TRAY_SIZE
    val isOver: Boolean get() = won || lost
    val canUndo: Boolean get() = history.isNotEmpty() && !won

    private fun onBoard(i: Int) = state[i] == ON_BOARD

    fun isFree(i: Int): Boolean = onBoard(i) && freeAmong(i) { onBoard(it) }

    private fun freeAmong(i: Int, present: (Int) -> Boolean): Boolean {
        if (above[i].any(present)) return false
        return leftOf[i].none(present) || rightOf[i].none(present)
    }

    /** Moves a free tile into the tray; a matching tile already there clears with it. */
    fun pick(i: Int): Boolean {
        lastCleared = null
        if (isOver || !isFree(i)) return false
        val partner = tray.firstOrNull { face[it] == face[i] }
        var points = 0
        if (partner != null) {
            tray.remove(partner)
            state[partner] = CLEARED
            state[i] = CLEARED
            streak++
            points = 20 + 5 * (streak - 1)
            if (won) points += 300 + 100 * shufflesLeft + 50 * level
            lastCleared = partner to i
        } else {
            state[i] = IN_TRAY
            tray.add(i)
            if (tray.size >= 2) streak = 0
        }
        score += points
        history.add(intArrayOf(i, partner ?: -1, points))
        return true
    }

    /** Takes back the last pick. Returns the tile that went back to the board. */
    fun undo(): Int? {
        if (!canUndo) return null
        val (tile, partner, points) = history.removeAt(history.lastIndex).let { Triple(it[0], it[1], it[2]) }
        if (partner >= 0) {
            state[partner] = IN_TRAY
            tray.add(partner)
        } else {
            tray.remove(tile)
        }
        state[tile] = ON_BOARD
        score -= points
        streak = 0
        return tile
    }

    /** Rescue (once a game): every tile in the tray goes back to its place on the board. */
    fun rescue(): Boolean {
        if (!rescueLeft || tray.isEmpty() || won) return false
        rescueLeft = false
        tray.forEach { state[it] = ON_BOARD }
        tray.clear()
        history.clear()
        return true
    }

    /** A good next pick: a free tile matching one in the tray, else a free matching pair. */
    fun hint(): List<Int>? {
        val free = slots.indices.filter { isFree(it) }
        free.firstOrNull { f -> tray.any { face[it] == face[f] } }?.let { return listOf(it) }
        for (x in free.indices) for (y in x + 1 until free.size) {
            if (face[free[x]] == face[free[y]]) return listOf(free[x], free[y])
        }
        return null
    }

    fun useHint(): List<Int>? {
        if (hintsLeft <= 0) return null
        val h = hint() ?: return null
        hintsLeft--
        return h
    }

    /** Re-deals the tiles still on the board (the tray stays as it is). */
    fun shuffle(): Boolean {
        if (shufflesLeft <= 0 || won) return false
        shufflesLeft--
        val board = slots.indices.filter { onBoard(it) }
        val faces = board.map { face[it] }.shuffled(random)
        board.forEachIndexed { k, i -> face[i] = faces[k] }
        history.clear()
        return true
    }

    /**
     * Gives the [positions] faces so the board can be cleared: repeatedly take two tiles that are
     * free right now, give them the same face and lift them off. Played forwards, that order works.
     */
    private fun deal(positions: List<Int>, pairFaces: List<Int>) {
        repeat(300) {
            val present = positions.toHashSet()
            val faces = pairFaces.shuffled(random)
            val assigned = HashMap<Int, Int>()
            var k = 0
            var ok = true
            while (present.isNotEmpty()) {
                val free = present.filter { freeAmong(it) { j -> j in present } }
                if (free.size < 2) { ok = false; break }
                val a = free[random.nextInt(free.size)]
                var b: Int
                do { b = free[random.nextInt(free.size)] } while (b == a)
                assigned[a] = faces[k]
                assigned[b] = faces[k]
                k++
                present.remove(a)
                present.remove(b)
            }
            if (ok) {
                assigned.forEach { (i, f) -> face[i] = f }
                return
            }
        }
        val faces = pairFaces.flatMap { listOf(it, it) }.shuffled(random)
        positions.forEachIndexed { k, i -> face[i] = faces[k] }
    }

    companion object {
        const val FACES = 20
        const val TRAY_SIZE = 4
        const val ON_BOARD = 0
        const val IN_TRAY = 1
        const val CLEARED = 2

        private fun overlaps(a: Slot, b: Slot) = kotlin.math.abs(a.x - b.x) < 2 && kotlin.math.abs(a.y - b.y) < 2

        /** A block of tiles: [cols]×[rows] tiles on [layer], top-left at half-units (x, y). */
        private fun MutableList<Slot>.block(layer: Int, x: Int, y: Int, cols: Int, rows: Int) {
            for (r in 0 until rows) for (c in 0 until cols) add(Slot(layer, x + c * 2, y + r * 2))
        }

        /** The stacks, easiest first. After the last one they repeat. Every layout is 6 tiles wide. */
        val LAYOUTS: List<Pair<String, List<Slot>>> = listOf(
            "Little Pyramid" to buildList {
                block(0, 0, 0, 6, 4)          // 24
                block(1, 2, 2, 4, 2)          // 8, centred on the middle
            },
            "Twin Towers" to buildList {
                block(0, 0, 0, 6, 5)          // 30
                block(1, 1, 1, 2, 4)          // 8, left tower, half a tile in
                block(1, 7, 1, 2, 4)          // 8, right tower
                block(2, 2, 3, 1, 2)          // 2
                block(2, 8, 3, 1, 2)          // 2
            },
            "Kapi's Hill" to buildList {
                block(0, 0, 0, 6, 6)          // 36
                block(1, 1, 1, 5, 5)          // 25, sits across the gaps below
                block(2, 2, 2, 4, 4)          // 16
                block(3, 5, 5, 1, 1)          // 1, the summit  -> 78
            },
            "The Runway" to buildList {
                block(0, 0, 0, 6, 7)          // 42
                block(1, 2, 0, 4, 7)          // 28, a strip down the middle
                block(2, 3, 2, 3, 2)          // 6
                block(2, 3, 8, 3, 2)          // 6
                block(3, 5, 3, 1, 1)          // 1
                block(3, 5, 9, 1, 1)          // 1  -> 84
            },
        )

        fun layoutName(level: Int): String = LAYOUTS[(level - 1).mod(LAYOUTS.size)].first

        fun layoutFor(level: Int): List<Slot> = LAYOUTS[(level - 1).mod(LAYOUTS.size)].second
    }
}
