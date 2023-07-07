import dz.nexatech.reporter.client.common.AbstractLocalizer
import org.junit.Test
import java.util.Calendar

class Sandbox {

    @Test
    fun epochParsing() {
        println("min: " + AbstractLocalizer.newCalendar(816134400000).get(Calendar.DAY_OF_MONTH))
        println("max: " + AbstractLocalizer.newCalendar(816220799999).get(Calendar.DAY_OF_MONTH))
    }
}