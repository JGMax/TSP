import org.knowm.xchart.QuickChart
import org.knowm.xchart.SwingWrapper
import org.knowm.xchart.XYChart
import javax.swing.SwingUtilities

class RunTimeVisualization(dataArray: Array<MutableList<Double>>,chartTitle: String) {
    private val chart = QuickChart.getChart(chartTitle, "X", "Y", "path",
        dataArray[0], dataArray[1])
    private val swingWrapper = SwingWrapper(chart)

    fun updateData(dataArray: Array<MutableList<Double>>) {
        SwingUtilities.invokeLater {
            chart.updateXYSeries("path", dataArray[0], dataArray[1], null)
            swingWrapper.repaintChart()
        }
    }

    fun displayChart() {
        swingWrapper.displayChart()
    }
}