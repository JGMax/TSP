import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

class Node private constructor(private var defaultId: Double, private var x: Double, private var y: Double) {
    private var nextNode: Node? = null
    private var previousNode: Node? = null

    private var headNode: Node? = null
    private var id: Int = 0

    private var rt: RunTimeVisualization? = null

    private var penalties: MutableList<MutableList<Double>>? = null

    private var size: Int = 1
    private var pathLen: Double = 0.0
    private var edgeLen: Double = 0.0
    private var penaltiesPathLen: Double = 0.0

    private var lambda: Double = 0.0
    private var isGuided: Boolean = false

    constructor(array: Array<MutableList<Double>>, seriesName: String) : this(array[0][0], array[1][0], array[2][0]) {
        val arr = copy(array)

        penalties = createIdList(arr)

        arr[0].removeAt(0)
        arr[1].removeAt(0)
        arr[2].removeAt(0)

        add(arr)
        setCycle()
        headNode?.pathLen = calculatePathLen()
        headNode?.penaltiesPathLen = headNode?.pathLen ?: 0.0
        rt = RunTimeVisualization(asArrayForVisualisation() as Array<MutableList<Double>>, seriesName)
        rt?.displayChart()
    }

    private fun createIdList(arr: Array<MutableList<Double>>): MutableList<MutableList<Double>> {
        return MutableList(arr[0].size){ MutableList(arr[0].size) { 0.0 } }
    }

    private fun copy(arr: Array<MutableList<Double>>): Array<MutableList<Double>>{
        return Array(arr.size) { i ->
            MutableList(arr[i].size) { j ->
                arr[i][j]
            }
        }
    }

    private fun setCycle() {
        if (this.headNode == this) {
            val tall = getForward(size - 1) as Node
            this.previousNode = tall
            tall.nextNode = this

            tall.edgeLen = calculateEdgeLen(tall, this)
            this.pathLen += edgeLen
        }
    }

    private fun calculatePathLen(): Double {
        var sum = edgeLen
        if (nextNode != headNode) {
            sum += (nextNode?.calculatePathLen() ?: 0.0)
        }
        return sum
    }

    private fun add(id: Double, x: Double, y: Double) {
        if (headNode == null)
            headNode = this
        this.size += 1

        if (nextNode == null) {
            edgeLen = sqrt((x - this.x).pow(2) + (y - this.y).pow(2))

            nextNode = Node(id, x, y)
            nextNode?.previousNode = this
            nextNode?.id = this.id + 1
            nextNode?.size = this.size
            nextNode?.headNode = this.headNode
        } else {
            nextNode?.add(id, x, y)
        }
    }

    private fun add(arr: Array<MutableList<Double>>) {
        if (headNode == null)
            headNode = this
        for (i in arr[0].indices) {
            this.add(arr[0][i], arr[1][i], arr[2][i])
        }
    }

    private fun calculateNewDistance(node1: Node, node2: Node): Double {
        val thisNextNode = node1.nextNode as Node
        val firstNextNode = node2.nextNode as Node
        val newPenalties = headNode!!.lambda *
                (getPenalty(node1, node2)+ getPenalty(thisNextNode, firstNextNode))

        val newEdgesLen = calculateEdgeLen(node1, node2) + calculateEdgeLen(thisNextNode, firstNextNode) + newPenalties

        val oldPenalties = headNode!!.lambda *
                (getPenalty(node1) + getPenalty(node2))

        val oldEdgesLen = node1.edgeLen + node2.edgeLen + oldPenalties

        return headNode!!.penaltiesPathLen - oldEdgesLen + newEdgesLen
    }

    private fun calculateNewDistanceWithoutPenalties(node1: Node, node2: Node): Double {
        val thisNextNode = node1.nextNode as Node
        val firstNextNode = node2.nextNode as Node
        val newEdgesLen = calculateEdgeLen(node1, node2) + calculateEdgeLen(thisNextNode, firstNextNode)

        val oldEdgesLen = node1.edgeLen + node2.edgeLen

        return headNode!!.pathLen - oldEdgesLen + newEdgesLen
    }

    private fun calculateEdgeLen(node1: Node, node2: Node): Double {
        return sqrt((node1.x - node2.x).pow(2) +
                (node1.y - node2.y).pow(2))
    }

    private fun findNodeToSwap(firstNode: Node): Node? {
        when {
            abs(this.id - firstNode.id) > 1 && abs(this.id - firstNode.id) < size - 1 -> {
                val newPath = if (headNode!!.isGuided) calculateNewDistance(this, firstNode)
                else calculateNewDistanceWithoutPenalties(this, firstNode)

                return if (newPath < headNode!!.penaltiesPathLen) {
                    this
                } else {
                    nextNode?.findNodeToSwap(firstNode)
                }
            }
            firstNode.previousNode == this -> {
                return null
            }
            else -> {
                return nextNode?.findNodeToSwap(firstNode)
            }
        }
    }

    private fun makeBetter(): Double {
        val len = headNode?.pathLen ?: 0.0
        val node = findNodeToSwap(this)
        if (node is Node) {
            do2opt(this, node)
            return len - headNode!!.pathLen
        }
        return if (nextNode != headNode)
            nextNode?.makeBetter() ?: 0.0
        else
            0.0
    }

    private fun setIds() {
        when {
            this == this.headNode -> {
                nextNode?.setIds()
            }
            nextNode != headNode -> {
                this.id = (previousNode?.id ?: 0) + 1
                nextNode?.setIds()
            }
            else -> {
                this.id = (previousNode?.id ?: 0) + 1
            }
        }
    }

    private fun reverse() {
        val temp = this.previousNode
        this.previousNode = this.nextNode
        this.nextNode = temp
        if (this.nextNode != null)
            this.edgeLen = calculateEdgeLen(this, this.nextNode!!)
        this.nextNode?.reverse()
    }

    private fun do2opt(node1: Node, node2: Node) {
        val nextNode1 = node1.nextNode
        val nextNode2 = node2.nextNode

        headNode!!.penaltiesPathLen = calculateNewDistanceWithoutPenalties(node1, node2) +
                if (headNode!!.isGuided) {
                headNode!!.lambda *
                        (getPenalty(node1) + getPenalty(node2) + getPenalty(nextNode1!!) + getPenalty(nextNode2!!))
                } else 0.0

        headNode!!.pathLen = calculateNewDistanceWithoutPenalties(node1, node2)

        node1.nextNode = null
        node2.nextNode = null

        nextNode1!!.previousNode = null
        nextNode2!!.previousNode = null

        node1.reverse()

        node1.previousNode = node2
        node2.nextNode = node1

        nextNode1.previousNode = nextNode2
        nextNode2.nextNode = nextNode1

        node1.edgeLen = calculateEdgeLen(node1, node1.nextNode!!)
        node2.edgeLen = calculateEdgeLen(node2, node2.nextNode!!)
        nextNode1.edgeLen = calculateEdgeLen(nextNode1, nextNode1.nextNode!!)
        nextNode2.edgeLen = calculateEdgeLen(nextNode2, nextNode2.nextNode!!)


        headNode?.setIds()
    }

    private fun perturbation(n: Int = 4) {
        var a: Int
        var b: Int
        for (i in 0 until n) {
            a = Random.nextInt(size)
            b = Random.nextInt(size)
            while (abs(a - b) < 1 || abs(a - b) > size - 1)
                b = Random.nextInt(size)

            do2opt(get(a)!!, get(b)!!)
        }

        headNode?.rt?.updateData(asArrayForVisualisation() as Array<MutableList<Double>>)
    }

    private fun getPenalty(node1: Node, node2: Node) =
        headNode!!.penalties!![node1.defaultId.toInt() - 1][node2.defaultId.toInt() - 1]

    private fun getPenalty(node: Node) = getPenalty(node, node.nextNode!!)

    private fun calcUtil(): Double = edgeLen / (1 + getPenalty(this))

    private fun findMaxUtil(max: Double): Double {
        val util = calcUtil()
        return if (nextNode != headNode) {
            if (max < util)
                nextNode?.findMaxUtil(util) as Double
            else
                nextNode?.findMaxUtil(max) as Double
        } else {
            if (max < util)
                util
            else
                max
        }
    }

    private fun getNodesWithMaxUtil(maxUtil: Double, array: ArrayList<Node>): ArrayList<Node> {
        val util = calcUtil()
        if (util == maxUtil) {
            array.add(this)
        }
        return if (nextNode != headNode) {
            nextNode?.getNodesWithMaxUtil(maxUtil, array) ?: array
        } else {
            array
        }
    }

    private fun setPenalty() {
        val maxUtil = findMaxUtil(0.0)
        val nodes: ArrayList<Node> = ArrayList(0)
        getNodesWithMaxUtil(maxUtil, nodes)

        for (node in nodes) {
            addPenalty(node)
        }
    }

    private fun addPenalty(node: Node) {
        headNode!!.penalties!![node.defaultId.toInt() - 1][node.nextNode?.defaultId!!.toInt() - 1] += 1.0
        headNode!!.penalties!![node.nextNode?.defaultId!!.toInt() - 1][node.defaultId.toInt() - 1] += 1.0
    }

    private fun setPenaltiesToZero() {
        for (arr in penalties!!) {
            for (i in arr.indices) {
                arr[i] = 0.0
            }
        }
    }

    private fun getForwardArray(arr: Array<MutableList<Double>>, withId: Boolean = false): Array<MutableList<Double>>? {
        return if (nextNode == null) {
            arr
        } else {
            var index = 0
            if (withId) {
                index = 1
                arr[index - 1].add(defaultId)
            }
            arr[index].add(this.x)
            arr[index + 1].add(this.y)

            if (nextNode == headNode) {
                if (withId) {
                    arr[index - 1].add(nextNode!!.defaultId)
                }
                arr[index].add(nextNode!!.x)
                arr[index + 1].add(nextNode!!.y)
                arr
            } else {
                nextNode?.getForwardArray(arr, withId)
            }
        }
    }

    private fun asArrayForVisualisation(): Array<MutableList<Double>>? {
        val arr: Array<MutableList<Double>> = arrayOf(MutableList(0) { x }, MutableList(0) { y })
        return headNode?.getForwardArray(arr, false)
    }

    private fun changeNodes(arr: Array<MutableList<Double>>){
        this.defaultId = arr[0][0]
        this.x = arr[1][0]
        this.y = arr[2][0]

        arr[0].removeAt(0)
        arr[1].removeAt(0)
        arr[2].removeAt(0)

        if (this != headNode) {
            this.previousNode!!.edgeLen = calculateEdgeLen(this, previousNode!!)
        }

        if (nextNode != headNode)
            nextNode?.changeNodes(arr)
        else {
            this.edgeLen = calculateEdgeLen(this, this.nextNode!!)
        }
    }

    private fun getForward(id: Int): Node? {
        if (id < 0) {
            getBack(id)
        }

        return if (id % size == this.id) {
            this
        } else {
            nextNode?.getForward(id)
        }
    }

    private fun getBack(id: Int): Node? {
        if (id >= 0) {
            getForward(id)
        }

        return if (abs(size + id) % size == this.id) {
            this
        } else {
            previousNode?.getBack(id)
        }
    }

    fun get(id: Int): Node? {
        if (abs(id) % size == this.id)
            return this

        return if (id < 0) {
            if (((-id) % size) < size / 2)
                previousNode?.getBack(id)
            else
                nextNode?.getForward(size + id)
        } else {
            if ((id % size) < size / 2)
                nextNode?.getForward(id)
            else
                previousNode?.getBack(id - size)
        }
    }

    fun changeData(arr: Array<MutableList<Double>>, pathLen: Double = 0.0) {
        if (size != arr[0].size && arr.size != 3)
            throw Exception("Incorrect size")

        this.changeNodes(copy(arr))

        if (pathLen != 0.0) {
            this.pathLen = pathLen
        } else {
            this.pathLen = calculatePathLen()
        }

        rt?.updateData(asArrayForVisualisation() as Array<MutableList<Double>>)
    }

    fun getAsArray(): Array<MutableList<Double>>? {
        val arr: Array<MutableList<Double>> =
            arrayOf(MutableList(0) { defaultId }, MutableList(0) { x }, MutableList(0) { y })
        return headNode?.getForwardArray(arr, true)
    }

    fun getSize() = size
    fun getPathLen() = pathLen

    fun printList() {
        if (nextNode != null) {
            if (nextNode != headNode) {
                println("${defaultId.toInt()} ${x.toInt()} ${y.toInt()}")
                nextNode?.printList()
            } else {
                println("${defaultId.toInt()} ${x.toInt()} ${y.toInt()}")
                println("${nextNode?.defaultId?.toInt()} ${nextNode?.x?.toInt()} ${nextNode?.y?.toInt()}")
            }
        }
    }

    fun localSearch() {
        do {
            val profit = makeBetter()
            println("Profit is $profit")
            println(headNode?.pathLen)
            headNode?.rt?.updateData(asArrayForVisualisation() as Array<MutableList<Double>>)
        } while (profit > 0.0)
    }

    fun iteratedLocalSearch(millis: Int = -1, perturbationComplexity: Int = -1) {
        localSearch()

        var answer = getAsArray() as Array<MutableList<Double>>
        var minLen = getPathLen()

        val startTime = System.currentTimeMillis()
        do {
            perturbation( if (perturbationComplexity >= 0) perturbationComplexity else (size * 0.25).toInt() )
            localSearch()
            if (getPathLen() < minLen) {
                minLen = getPathLen()
                answer = getAsArray() as Array<MutableList<Double>>
            }
        } while (System.currentTimeMillis() - startTime < if(millis >= 0) millis else size * 50)

        answer[0].removeAt(answer[0].lastIndex)
        answer[1].removeAt(answer[1].lastIndex)
        answer[2].removeAt(answer[2].lastIndex)

        this.changeData(answer, minLen)

        //return Node(answer, "Iterated answer")
    }

    fun guidedLocalSearch(millis: Int = -1) {
        var answer = getAsArray() as Array<MutableList<Double>>
        var minLen = getPathLen()

        isGuided = true

        val startTime = System.currentTimeMillis()
        do {
            localSearch()

            if (getPathLen() < minLen) {
                minLen = getPathLen()
                answer = getAsArray() as Array<MutableList<Double>>
            }

            if (lambda == 0.0) {
                lambda = pathLen / size
            }

            setPenalty()

        } while (System.currentTimeMillis() - startTime < if(millis >= 0) millis else size * 70)

        answer[0].removeAt(answer[0].lastIndex)
        answer[1].removeAt(answer[1].lastIndex)
        answer[2].removeAt(answer[2].lastIndex)

        lambda = 0.0
        setPenaltiesToZero()

        isGuided = false

        this.changeData(answer, minLen)
    }

}
