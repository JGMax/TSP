
fun main() {
    /*data.generateNewData(4)*/

    val array: Array<MutableList<Double>> = arrayOf(MutableList(0) { 0.0 },
        MutableList(0) { 0.0 }, MutableList(0) { 0.0 })

    /*val numOfDots = readLine()?.toInt() ?: 0

    for (i in 0 until numOfDots) {
        val str = readLine() as String
        array[0].add(str.split(" ")[0].toDouble())
        array[1].add(str.split(" ")[1].toDouble())
        array[2].add(str.split(" ")[2].toDouble())
    }

    val data = DataManager(array)*/
    val numOfDots = 2000
    val data = DataManager(numOfDots, maxCoordinate = 1000, seed = 123)

    data.writeDataToFile("Input$numOfDots.txt", data.getData())

    val defaultHead = Node(data.getData(), "Default")

    val defaultLen = defaultHead.getPathLen()

    var startTime = System.currentTimeMillis()

    data.writeDataToFile("DefaultOutput$numOfDots.txt", defaultHead.getAsArray())

    data.greedySort()

    val greedyTime = System.currentTimeMillis() - startTime

    val greedyHead = Node(data.getData(), "Greedy")

    val greedyLen = greedyHead.getPathLen()

    val localHead = Node(data.getData(), "Local")

    startTime = System.currentTimeMillis()

    localHead.localSearch()

    val localTime = System.currentTimeMillis() - startTime

    val localSearchLen = localHead.getPathLen()

    val iteratedHead = Node(data.getData(), "Iterated")

    startTime = System.currentTimeMillis()

    iteratedHead.iteratedLocalSearch()

    val iteratedTime = System.currentTimeMillis() - startTime

    val iteratedLen = iteratedHead.getPathLen()

    val guidedHead = Node(data.getData(), "Guided")

    startTime = System.currentTimeMillis()

    guidedHead.guidedLocalSearch()

    val guidedTime = System.currentTimeMillis() - startTime

    val guidedSearchLen = guidedHead.getPathLen()

    println("Default length: $defaultLen")
    println("Greedy length: $greedyLen")
    println("Greedy time: ${greedyTime/1000.0}")
    println("Local Search length: $localSearchLen")
    println("Local Search time: ${localTime/1000.0}")
    println("Iterated Local Search len: $iteratedLen")
    println("Iterated Local Search time: ${iteratedTime/1000.0}")
    println("Guided Local Search len: $guidedSearchLen")
    println("Guided Search time: ${guidedTime/1000.0}")

    data.writeDataToFile("GreedyOutput$numOfDots.txt", greedyHead.getAsArray())
    data.writeDataToFile("LocalOutput$numOfDots.txt", localHead.getAsArray())
    data.writeDataToFile("iteratedOutput$numOfDots.txt", iteratedHead.getAsArray())
    data.writeDataToFile("GuidedOutput$numOfDots.txt", guidedHead.getAsArray())


}