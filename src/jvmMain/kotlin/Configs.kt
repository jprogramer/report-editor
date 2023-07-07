import androidx.compose.ui.unit.dp
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import java.util.*

val defaultPadding = 4.dp

val defaultWorkDir = "C:\\Users\\Joseph\\Desktop\\examples"

val defaultFile = "example_01.html"

val parsingLocale = Locale.FRANCE

val fontDirectories = ImmutableList.Builder<String>()
    .add("Amiri")
    .add("Cairo")
    .add("Lateef")
    .add("MarkaziText")
    .add("NotoNaskhArabic")
    .add("ReadexPro")
    .add("ScheherazadeNew")
    .add("Vazirmatn")
    .build()

val environment: ImmutableMap<String, Any> = ImmutableMap.builder<String, Any>()
    .put("left_header", "Sevenit GmbH\nHauptstrabe 40\n77654 Offenburg")
    .put("right_header", "MonsieurJean Dupont\nAcheteur SA\nRue du Château\n34000 MONTPELLIER")
    .put("bill_number", "1001")
    .put("bill_date", "02 July 2023")
    .put("client_number", "321")
    .put("main_font_name", "Noto Naskh Arabic")
    .put("page_color", "#f2f2f2")
    .put("page_width", "595")
    .put(
        "products",
        ImmutableList.builder<ImmutableMap<String, String>>()
            .add(
                ImmutableMap.of(
                    "description", "Main-d'oeuvre",
                    "quantity", "30",
                    "unite", "h.",
                    "price", "40.0",
                    "tax", "20",
                ),
                ImmutableMap.of(
                    "description", "Tracteur",
                    "quantity", "1",
                    "unite", "pce.",
                    "price", "1800",
                    "tax", "20",
                ),
                ImmutableMap.of(
                    "description", "Bois de chauffage",
                    "quantity", "10",
                    "unite", "stére",
                    "price", "80.00",
                    "tax", "10",
                ),
            )
            .build()
    )
    .build()