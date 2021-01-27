import java.io.File
import java.lang.Exception
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

class DataManager {
    private var array: Array<MutableList<Double>>? = null
    private var size = 0

    constructor(numOfDots: Int, maxCoordinate: Int = 100, seed: Int = 0) {
        generateNewData(numOfDots, maxCoordinate, seed)
    }

    constructor(array: Array<MutableList<Double>>) {
        if (array.size != 3)
            throw Exception("Incorrect array")
        setData(array)
    }

    constructor(fileName: String) {
        readDataFromFile(fileName)
    }

    private fun generateNewData(numOfDots: Int, maxCoordinate: Int = 100, seed: Int = 0): Array<MutableList<Double>> {
        val random = if (seed != 0)
            Random(seed)
        else
            Random
        array = Array(3) { it ->
            if (it != 0) MutableList(numOfDots) { random.nextInt(-maxCoordinate, maxCoordinate).toDouble() }
            else MutableList(numOfDots) { (it + 1).toDouble() }
        }
        size = numOfDots
        return array!!
    }

    private fun setData(array: Array<MutableList<Double>>) {
        this.array = array.clone()
        this.size = array[0].size
    }

    fun getData() = getDataCopy()

    private fun getDataCopy(): Array<MutableList<Double>> {
        if (array != null) {
            return Array(array!!.size) { i ->
                MutableList(array!![i].size) { j ->
                    array!![i][j]
                }
            }
        } else {
            throw Exception("No data")
        }
    }

    fun getSize() = size

    private fun changeDataStyleForSorting(): MutableList<Array<Double>> {
        if (array != null) {
            return MutableList(size - 1) { arrayOf(array!![0][it], array!![1][it]) }
        }
        throw Exception("Empty data")
    }

    private fun changeDataStyleAfterSorting(arr: List<Array<Double>>) {
        if (array != null) {
            for (i in arr.indices) {
                array!![0][i] = arr[i][0]
                array!![1][i] = arr[i][1]
            }
            array!![0].add(array!![0][0])
            array!![1].add(array!![1][0])
        }
    }

    fun sortData() {
        val arr = changeDataStyleForSorting().sortedWith(compareBy({
            atan2(it[0], it[1])
        }, {
            sqrt(it[0].pow(2) + it[1].pow(2))
        }))
        changeDataStyleAfterSorting(arr)
    }

    private fun swap(id1: Int, id2: Int) {
        if (array == null)
            throw Exception("Empty data")
        if (id1 != id2 && id1 in array!![0].indices && id2 in array!![0].indices) {
            for (i in array!!.indices) {
                val temp = array!![i][id1]
                array!![i][id1] = array!![i][id2]
                array!![i][id2] = temp
            }
        }
    }

    fun greedySort() {
        if (array == null)
            throw Exception("Empty data")

        for (i in array!![0].indices) {
            if (i == array!![0].lastIndex)
                break

            var minJ = i + 1
            var minEdge = sqrt(
                (array!![1][i] - array!![1][i + 1]).pow(2)
                        + (array!![2][i] - array!![2][i + 1]).pow(2)
            )

            for (j in i + 2 until array!![0].size) {
                val edge = sqrt(
                    (array!![1][i] - array!![1][j]).pow(2)
                            + (array!![2][i] - array!![2][j]).pow(2)
                )
                if (edge <= minEdge) {
                    minEdge = edge
                    minJ = j
                }
            }
            swap(minJ, i + 1)
        }
    }

    private fun readDataFromFile(fileName: String) {
        val lines = File(fileName).readLines()
        this.array = arrayOf(MutableList(0) { 0.0 }, MutableList(0) { 0.0 }, MutableList(0) { 0.0 })
        this.size = lines[0].toInt()
        for (line in lines) {
            if (line != lines[0]) {
                this.array!![0].add(line.split(" ")[0].toDouble())
                this.array!![1].add(line.split(" ")[1].toDouble())
                this.array!![2].add(line.split(" ")[2].toDouble())
            }
        }
    }

    fun writeDataToFile(fileName: String, data: Array<MutableList<Double>>? = null) {
        var writableArray = array
        if (data != null) {
            if (data.size != 3)
                throw Exception("Incorrect data")
            writableArray = data
        }

        if (writableArray != null) {
            File(fileName).printWriter().use {
                it.println(writableArray[0].size)
                for (i in writableArray[0].indices) {
                    it.println("${writableArray[0][i].toInt()} ${writableArray[1][i].toInt()} ${writableArray[2][i].toInt()}")
                }
            }
        }
    }

    fun printData() {
        if (array != null) {
            for (i in 0..size) {
                println(
                    "${array!![0][i]} ${array!![1][i]} ${
                        atan2(array!![0][i], array!![1][i])
                    }"
                )
            }
        }
    }
}