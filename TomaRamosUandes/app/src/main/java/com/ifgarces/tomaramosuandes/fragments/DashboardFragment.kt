package com.ifgarces.tomaramosuandes.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.ifgarces.tomaramosuandes.R
import com.ifgarces.tomaramosuandes.activities.HomeActivity
import com.ifgarces.tomaramosuandes.adapters.IncomingRamoEventsAdapter
import com.ifgarces.tomaramosuandes.adapters.CareerAdvicesAdapter
import com.ifgarces.tomaramosuandes.adapters.PrettyLinksAdapter
import com.ifgarces.tomaramosuandes.models.CareerAdvice
import com.ifgarces.tomaramosuandes.models.PrettyHyperlink
import com.ifgarces.tomaramosuandes.models.RamoEvent
import com.ifgarces.tomaramosuandes.utils.DataMaster
import com.ifgarces.tomaramosuandes.utils.infoDialog
import com.ifgarces.tomaramosuandes.utils.multilineTrim
import com.ifgarces.tomaramosuandes.utils.toggleCollapseViewButton
import java.time.LocalDate
import java.time.temporal.ChronoUnit


class DashboardFragment : Fragment() {

    private class FragmentUI(owner :View) {
        // Incoming events section
        val incomingEventsHeadButton :MaterialButton = owner.findViewById(R.id.dashboard_eventsHeadButton)
        val incomingEventsRecycler :RecyclerView = owner.findViewById(R.id.dashboard_eventsRecycler)
        val incomingEventsContainer :View = owner.findViewById(R.id.dashboard_eventsContainer)

        // Useful links section
        val usefulLinksHeadButton :MaterialButton = owner.findViewById(R.id.dashboard_linksHeadButton)
        val usefulLinksRecycler :RecyclerView = owner.findViewById(R.id.dashboard_linksRecycler)
        val usefulLinksContainer :View = owner.findViewById(R.id.dashboard_linksContainer)

        // Advices section
        val careerAdvicesHeadButton :MaterialButton = owner.findViewById(R.id.dashboard_advicesHeadButton)
        val careerAdvicesRecycler :RecyclerView = owner.findViewById(R.id.dashboard_advicesRecycler)
        val careerAdvicesContainer :View = owner.findViewById(R.id.dashboard_advicesContainer)
    }

    private lateinit var UI :FragmentUI

    // Auxiliar variables for collapsing/expanding sections
    private var isEventsSectionCollapsed :Boolean = false
    private var isLinksSectionCollapsed :Boolean = false
    private var isAdvicesSectionCollapsed :Boolean = false

    override fun onCreateView(
        inflater :LayoutInflater, container :ViewGroup?, savedInstanceState :Bundle?
    ) :View {
        val fragView :View = inflater.inflate(
            if (DataMaster.getUserStats().nightModeOn) R.layout.night_fragment_dashboard
            else R.layout.fragment_dashboard,
            container, false
        )
        this.UI = FragmentUI(owner=fragView)

        (this.requireActivity() as HomeActivity).let { homeActivity :HomeActivity ->
            homeActivity.setBottomNavItemSelected(this::class)
            homeActivity.setTopToolbarValues(
                title = "Inicio",
                subtitle = "Dashboard principal",
                onHelpClick = this::showHelp
            )

            // Applying stored user preferences on sections collapse/expanded status
            DataMaster.getUserStats().let {
                this.isEventsSectionCollapsed = it.dashboardEvalsSectionCollapsed
                this.isLinksSectionCollapsed = it.dashboardLinksSectionCollapsed
                this.isAdvicesSectionCollapsed = it.dashboardAdvicesSectionCollapsed
            }
            this.applyVisibilityUserSettings(homeActivity)

            // Setting up recyclers
            UI.incomingEventsRecycler.layoutManager = LinearLayoutManager(
                homeActivity, LinearLayoutManager.HORIZONTAL, false
            )
            // Displaying events in the next 31 days
            UI.incomingEventsRecycler.adapter = IncomingRamoEventsAdapter(
                data = DataMaster.getUserEvaluations().map { event :RamoEvent ->
                    Pair(event, LocalDate.now().until(event.date!!, ChronoUnit.DAYS)) // Ref: https://discuss.kotlinlang.org/t/calculate-no-of-days-between-two-dates-kotlin/9826
                }.filter { (it.second <= 31L) && (it.second >= 0L) }
            )
            UI.usefulLinksRecycler.layoutManager = LinearLayoutManager(
                homeActivity, LinearLayoutManager.HORIZONTAL, false
            )
            UI.usefulLinksRecycler.adapter = PrettyLinksAdapter(
                data = listOf(
                    PrettyHyperlink(
                        image = ContextCompat.getDrawable(this.requireContext(), R.drawable.webicon_canvas)!!,
                        name = "Canvas",
                        uri = "https://uandes.instructure.com"
                    ),
                    PrettyHyperlink(
                        image = ContextCompat.getDrawable(this.requireContext(), R.drawable.webicon_banner)!!,
                        name = "Banner MiUandes",
                        uri = "https://mi.uandes.cl"
                    ),
                    PrettyHyperlink(
                        image = ContextCompat.getDrawable(this.requireContext(), R.drawable.uandes_logo_simple)!!,
                        name = "SAF",
                        uri = "https://saf.uandes.cl/ing"
                    )
                ),
                activity = homeActivity
            )
            UI.careerAdvicesRecycler.layoutManager = LinearLayoutManager(
                homeActivity, LinearLayoutManager.VERTICAL, false
            )
            UI.careerAdvicesRecycler.adapter = CareerAdvicesAdapter(
                _rawData = listOf(
                    CareerAdvice(
                        title = "Beneficios cuenta de Google Uandes",
                        description = """\
La cuenta de Gmail @miuandes de cada estudiante es de tipo "Gsuite" y cuenta con espacio "ilimitado" \
en Google Drive para almacenar archivos en la nube. Además, aproveche el calendario de Google para \
anotar sus pruebas/eventos, y Google Keep para tomar notas rápidas. Sáquele el jugo.\
""".multilineTrim(),
                        image = ContextCompat.getDrawable(this.requireContext(), R.drawable.webicon_gsuite)!!,
                        uri = "https://drive.google.com"
                    ),
                    CareerAdvice(
                        title = "Office 365 y OneDrive",
                        description = """\
La Uandes también le orotga una cuenta Office 365 (Microsoft) @miuandes vinculada a la de Google. \
Recuerde reclamar su Office gratis y usar OneDrive.\
""".multilineTrim(),
                        image = ContextCompat.getDrawable(this.requireContext(), R.drawable.webicon_office365)!!,
                        uri = "https://www.office.com"
                    ),
                    CareerAdvice(
                        title = "Acceso a software de JetBrains",
                        description = """\
Con la cuenta @miuandes puede acceder a licencias de estudiante en herramientas de programación de \
JetBrains (para Python, etc.). Inicie sesión con su cuenta en jetbrains.com.\
""".multilineTrim(),
                        image = ContextCompat.getDrawable(this.requireContext(), R.drawable.webitem_jetbrains)!!,
                        uri = "https://www.jetbrains.com"
                    ),
                    CareerAdvice(
                        title = "AutoDesk Fusion 360",
                        description = """\
Con la cuenta @miuandes se puede obtener la licencia de estudiante de Fusion 360, un software para \
hacer modelos en 3D.\
""".multilineTrim(),
                        image = ContextCompat.getDrawable(this.requireContext(), R.drawable.webicon_fusion360)!!,
                        uri = "https://www.autodesk.com"
                    ),
                    CareerAdvice(
                        title = "Resúmenes y material ING varios ⚙",
                        description = """\
Great Ayuda es una página con resúmenes y material de apoyo de varios ramos de ing. civil plan común \
Uandes, además de material educacional de colegio.\
""".multilineTrim(),
                        image = ContextCompat.getDrawable(this.requireContext(), R.drawable.webicon_greatayuda)!!,
                        uri = "http://www.g-ayuda.net"
                    ),
                    CareerAdvice(
                        title = "Material académico CDI",
                        description = """\
Carpeta de Google Drive con material de estudio variado de muchos ramos de ingeniería Uandes de \
todas las especialidades.\
""".multilineTrim(),
                        image = ContextCompat.getDrawable(this.requireContext(), R.drawable.webicon_cdi)!!,
                        uri = "https://drive.google.com/drive/folders/0B7fLfOr3MydSVGY5MGhJTHJUcGs?resourcekey=0-Nd0xQYNHlDUh4utj-K-9tA&usp=sharing"
                    ),
                    CareerAdvice(
                        title = "SCI-HUB y Library Genesis 📑",
                        description = """\
Existen sitios web para descargar gratis millones de libros y artículos de investigación \
informativos/científicos (en inglés) que normalmente son pagados, en favor de la divulgación \
científica libre. Uno es SCI-HUB y otro es Library Genesis ;)\
""".multilineTrim(),
                        image = ContextCompat.getDrawable(this.requireContext(), R.drawable.webicon_scihub)!!,
                        uri = "https://sci-hub.hkvisa.net"
                    ),
                    CareerAdvice(
                        title = "CamScanner 📷",
                        description = """\
Una app móbil gratuita que convierte el teléfono en un escáner de documentos. Además, la licencia de \
estudiante es baratísima y es un pago único que mejora la calidad y quita marcas de agua de la app. \
Muy buena, qué puedo decir.\
""".multilineTrim(),
                        image = ContextCompat.getDrawable(this.requireContext(), R.drawable.webicon_camscanner)!!,
                        uri = "https://play.google.com/store/apps/details?id=com.intsig.lic.camscanner"
                    ),
                    CareerAdvice(
                        title = "WolframAlpha",
                        description = """\
Calculadora científica inteligente que resuelve problemas de cálculo, sistemas de ecuaciones, \
trigonometría, etc. y hasta puede graficar funciones. Incluso es capaz de mostrar los pasos a seguir \
para resolver el problema, pero esa última es una característica de pago, ojo.\
""".multilineTrim(),
                        image = ContextCompat.getDrawable(this.requireContext(), R.drawable.webicon_wolframalpha)!!,
                        uri = "https://www.wolframalpha.com"
                    ),
                    CareerAdvice(
                        title = "Symbolab",
                        description = """\
Similar a WolframAlpha, un potente solucionador matemático general, pero mucho más sencillo de usar \
y no cobra por ver la solución paso a paso de un problema. Es excelente, muy recomendado para \
practicar problemas matemáticos de todo tipo.\
""".multilineTrim(),
                        image = ContextCompat.getDrawable(this.requireContext(), R.drawable.webicon_symbolab)!!,
                        uri = null
                    ),
                    CareerAdvice(
                        title = "MyBib 📚",
                        description = """\
MyBib es un generador de bibliografías y citaciones en varios formatos distintos a elección. \
Sirve muchísimo para los informes de ingeniería que exigen un formato (e.g. Harvard). Mucho mejor \
que el gestor de bibliografías que tiene incorporado Microsoft Word. Sí.\
""".multilineTrim(),
                        image = ContextCompat.getDrawable(this.requireContext(), R.drawable.webicon_mybib)!!,
                        uri = "https://www.mybib.com"
                    ),
                    CareerAdvice(
                        title = "draw.io",
                        description = """\
Sitio web para dibujar diagramas (colaborativamente en tiempo real). Sencillo y rápido, solo poner \
"draw.io" en el navegador.\
""".multilineTrim(),
                        image = ContextCompat.getDrawable(this.requireContext(), R.drawable.webicon_drawio)!!,
                        uri = "https://draw.io"
                    ),
                    CareerAdvice(
                        title = "DroidCam",
                        description = """\
Si ud. no tiene una webcam, con esta app puede usar un teléfono Android como webcam, conectado por \
cable al PC o inalámbricamente por WI-FI.\
""".multilineTrim(),
                        image = ContextCompat.getDrawable(this.requireContext(), R.drawable.webicon_droidcam)!!,
                        uri = "https://play.google.com/store/apps/details?id=com.dev47apps.droidcam"
                    ),
                    CareerAdvice(
                        title = "NightEye 🌙",
                        description = """\
Con esta extensión, puede convertir cualquier página web en modo oscuro para que sea más cómodo a la \
vista, sobre todo ahora que pasamos más tiempo que nunca mirando pantallas... Por desgracia es \
gratis sólo por tres meses, pero luego vale la pena pagar unos pocos dólares al año, es una \
maravilla.\
""".multilineTrim(),
                        image = ContextCompat.getDrawable(this.requireContext(), R.drawable.webicon_nighteye)!!,
                        uri = "https://nighteye.app"
                    ),
                    CareerAdvice(
                        title = "Sincronización de archivos importantes con la nube ☁",
                        description = """\
Existen apps para PC de Google Drive y OneDrive para sincronizar carpetas. Úselo para evitar perder \
archivos y fácilmente pasar datos importantes de un computador a otro mediante la nube. Si el PC \
se le muere, así va a poder acceder a los archivos desde otro dispositivo mediante la nube, y \
evitar una tragedia como perder el progreso en una tarea.\
""".multilineTrim(),
                        image = ContextCompat.getDrawable(this.requireContext(), R.drawable.cloud_sync_icon)!!,
                        uri = null
                    )
//                    CareerAdvice(
//                        title = "",
//                        description = """\
//
//""".multilineTrim(),
//                        image = null,
//                        uri = null
//                    )
                ),
                activity = homeActivity
            )

            // Setting on-click toggle events for sections (not easy to avoid boilerplate!)
            UI.incomingEventsHeadButton.setOnClickListener {
                homeActivity.toggleCollapseViewButton(
                    isCollapsed = this.isEventsSectionCollapsed,
                    toggleButton = UI.incomingEventsHeadButton,
                    targetContainer = UI.incomingEventsContainer
                )
                this.isEventsSectionCollapsed = !this.isEventsSectionCollapsed
                DataMaster.toggleSectionCollapsed(RamoEvent::class)
            }
            UI.usefulLinksHeadButton.setOnClickListener {
                homeActivity.toggleCollapseViewButton(
                    isCollapsed = this.isLinksSectionCollapsed,
                    toggleButton = UI.usefulLinksHeadButton,
                    targetContainer = UI.usefulLinksContainer
                )
                this.isLinksSectionCollapsed = !this.isLinksSectionCollapsed
                DataMaster.toggleSectionCollapsed(PrettyHyperlink::class)
            }
            UI.careerAdvicesHeadButton.setOnClickListener {
                homeActivity.toggleCollapseViewButton(
                    isCollapsed = this.isAdvicesSectionCollapsed,
                    toggleButton = UI.careerAdvicesHeadButton,
                    targetContainer = UI.careerAdvicesContainer
                )
                this.isAdvicesSectionCollapsed = !this.isAdvicesSectionCollapsed
                DataMaster.toggleSectionCollapsed(CareerAdvice::class)
            }
        }

        return fragView
    }

    /**
     * Sets visibility for sections according to the data stored in `UserStats` table (Room).
     */
    private fun applyVisibilityUserSettings(context :Context) {
        listOf(
            Triple(this.isEventsSectionCollapsed, UI.incomingEventsHeadButton, UI.incomingEventsContainer),
            Triple(this.isLinksSectionCollapsed, UI.usefulLinksHeadButton, UI.usefulLinksContainer),
            Triple(this.isAdvicesSectionCollapsed, UI.careerAdvicesHeadButton, UI.careerAdvicesContainer)
        ).forEach { (collapsed :Boolean, toggleButton :MaterialButton, container :View) ->
            container.visibility = if (collapsed) View.GONE else View.VISIBLE
            toggleButton.icon = ContextCompat.getDrawable(
                context, if (collapsed) R.drawable.arrow_tip_right else R.drawable.arrow_tip_down
            )
        }
    }

    private fun showHelp() {
        this.requireContext().infoDialog(
            title = "Ayuda — Inicio",
            message = """\
Este es el tablero principal donde aparecen secciones útiles, como las evaluaciones cercanas, \
links útiles y consejos varios. Puede hacer clic en algún consejo expandido para ir al link del que \
hace referencia.\
""".multilineTrim(),
            icon = R.drawable.help_icon
        )
    }
}
