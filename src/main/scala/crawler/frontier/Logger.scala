package crawler.frontier

import java.io.PrintWriter

/**
 * @author andrei
 */
final class Logger(fileName: String) {
  private val printer = new PrintWriter(fileName)

  private def timeNow: String = {
    import java.util.Calendar

    val now = Calendar.getInstance
    val year = now.get(Calendar.YEAR)
    val month = now.get(Calendar.MONTH) + 1
    val day = now.get(Calendar.DAY_OF_MONTH)
    val hour = now.get(Calendar.HOUR_OF_DAY)
    val minute = now.get(Calendar.MINUTE)
    val second = now.get(Calendar.SECOND)
    f"$year%d-$month%02d-$day%02d $hour%02d:$minute%02d:$second%02d"
  }

  def log(message: String) = synchronized {
    printer.println(timeNow + ": " + message)
    printer.flush()
  }
}