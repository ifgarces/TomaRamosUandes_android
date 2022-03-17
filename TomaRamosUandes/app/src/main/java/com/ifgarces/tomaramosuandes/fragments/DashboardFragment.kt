package com.ifgarces.tomaramosuandes.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ifgarces.tomaramosuandes.R
import com.ifgarces.tomaramosuandes.activities.HomeActivity
import com.ifgarces.tomaramosuandes.adapters.IncomingRamoEventsAdapter
import com.ifgarces.tomaramosuandes.adapters.PrettyAdvicesAdapter
import com.ifgarces.tomaramosuandes.adapters.PrettyLinksAdapter
import com.ifgarces.tomaramosuandes.models.PrettyAdvice
import com.ifgarces.tomaramosuandes.models.PrettyHyperlink
import com.ifgarces.tomaramosuandes.utils.DataMaster
import com.ifgarces.tomaramosuandes.utils.infoDialog
import com.ifgarces.tomaramosuandes.utils.multilineTrim
import kotlinx.android.synthetic.main.ramoevent_item.view.*


class DashboardFragment : Fragment() {

    private class FragmentUI(owner :View) {
        // Incoming events section
        val incomingEventsHeadTxt :TextView = owner.findViewById(R.id.dashboard_incomingEventsHeadTxt)
        val incomingEventsRecycler :RecyclerView = owner.findViewById(R.id.dashboard_incomingEventsRecycler)

        // Useful links section
        val usefulLinksHeadTxt :TextView = owner.findViewById(R.id.dashboard_linksHeadTxt)
        val usefulLinksRecycler :RecyclerView = owner.findViewById(R.id.dashboard_linksRecycler)

        // Advices section
        val advicesHeadTxt :TextView = owner.findViewById(R.id.dashboard_advicesHeadTxt)
        val advicesRecycler :RecyclerView = owner.findViewById(R.id.dashboard_advicesRecycler)
    }

    private lateinit var UI :FragmentUI

    // Auxiliar variables for collapsing/expanding sections
    private var isEventsSectionCollapsed :Boolean = false
    private var isLinksSectionCollapsed :Boolean = false
    private var isAdvicesSectionCollapsed :Boolean = false

    override fun onCreateView(
        inflater :LayoutInflater, container :ViewGroup?, savedInstanceState :Bundle?
    ) :View {
        val fragView :View = inflater.inflate(R.layout.fragment_dashboard, container, false)
        this.UI = FragmentUI(owner=fragView)

        (this.requireActivity() as HomeActivity).let { homeActivity :HomeActivity ->
            homeActivity.setBottomNavItemSelected(this::class)
            homeActivity.setTopToolbarValues(
                title = "Inicio",
                subtitle = "Dashboard principal",
                onHelpClick = this::showHelp
            )

            // Setting up recyclers
            UI.incomingEventsRecycler.layoutManager = LinearLayoutManager(
                homeActivity, LinearLayoutManager.HORIZONTAL, false
            )
            UI.incomingEventsRecycler.adapter = IncomingRamoEventsAdapter(
                data = DataMaster.getUserEvaluations() //TODO: only show events in the next 31 days
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
            UI.advicesRecycler.layoutManager = LinearLayoutManager(
                homeActivity, LinearLayoutManager.VERTICAL, false
            )
            UI.advicesRecycler.adapter = PrettyAdvicesAdapter(
                data = listOf(
                    PrettyAdvice(
                        title = "Beneficios cuenta de Google Uandes",
                        description = """\
La cuenta de Gmail @miuandes de cada estudiante es de tipo "Gsuite" y cuenta con espacio "ilimitado" \
en Google Drive para almacenar archivos en la nube. Además, aproveche el calendario de Google para \
anotar sus pruebas/eventos, y Google Keep para tomar notas rápidas. Sáquele el jugo.\
""".multilineTrim(),
                        image = ContextCompat.getDrawable(this.requireContext(), R.drawable.webicon_gsuite)!!,
                        uri = "https://drive.google.com"
                    ),
                    PrettyAdvice(
                        title = "Office 365 y OneDrive",
                        description = """\
La Uandes también le orotga una cuenta Office 365 (Microsoft) @miuandes vinculada a la de Google. \
Recuerde reclamar su Office gratis y usar OneDrive.\
""".multilineTrim(),
                        image = ContextCompat.getDrawable(this.requireContext(), R.drawable.webicon_office365)!!,
                        uri = "https://www.office.com"
                    ),
                    PrettyAdvice(
                        title = "Acceso a software de JetBrains 🧠",
                        description = """\
Con la cuenta @miuandes puede acceder a licencias de estudiante en herramientas de programación de \
JetBrains (para Python, etc.). Inicie sesión con su cuenta en jetbrains.com.\
""".multilineTrim(),
                        image = ContextCompat.getDrawable(this.requireContext(), R.drawable.webitem_jetbrains)!!,
                        uri = "https://www.jetbrains.com"
                    ),
                    PrettyAdvice(
                        title = "AutoDesk Fusion 360",
                        description = """\
Con la cuenta @miuandes se puede obtener la licencia de estudiante de Fusion 360, un software para \
hacer modelos en 3D.\
""".multilineTrim(),
                        image = ContextCompat.getDrawable(this.requireContext(), R.drawable.webicon_fusion360)!!,
                        uri = "https://www.autodesk.com"
                    ),
                    PrettyAdvice(
                        title = "Resúmenes y material ING varios ⚙",
                        description = """\
Great Ayuda es una página con resúmenes y material de apoyo de varios ramos de ing. civil plan común \
Uandes, además de material educacional de colegio.\
""".multilineTrim(),
                        image = ContextCompat.getDrawable(this.requireContext(), R.drawable.webicon_greatayuda)!!,
                        uri = "http://www.g-ayuda.net"
                    ),
                    PrettyAdvice(
                        title = "Material académico CDI",
                        description = """\
Carpeta de Google Drive con material de estudio variado de muchos ramos de ingeniería Uandes de \
todas las especialidades.\
""".multilineTrim(),
                        image = ContextCompat.getDrawable(this.requireContext(), R.drawable.webicon_cdi)!!,
                        uri = "https://drive.google.com/drive/folders/0B7fLfOr3MydSVGY5MGhJTHJUcGs?resourcekey=0-Nd0xQYNHlDUh4utj-K-9tA&usp=sharing"
                    ),
                    PrettyAdvice(
                        title = "SCI-HUB y Library Genesis 📑",
                        description = """\
Existen sitios web para descargar gratis millones de libros y artículos de investigación \
informativos/científicos (en inglés) que normalmente son pagados, en favor de la divulgación \
científica libre. Uno es SCI-HUB y otro es Library Genesis ;)\
""".multilineTrim(),
                        image = ContextCompat.getDrawable(this.requireContext(), R.drawable.webicon_scihub)!!,
                        uri = "https://sci-hub.hkvisa.net"
                    ),
                    PrettyAdvice(
                        title = "CamScanner 📷",
                        description = """\
Una app móbil gratuita que convierte el teléfono en un escáner de documentos. Además, la licencia de \
estudiante es baratísima y es un pago único que mejora la calidad y quita marcas de agua de la app. \
Muy buena, qué puedo decir.\
""".multilineTrim(),
                        image = ContextCompat.getDrawable(this.requireContext(), R.drawable.webicon_camscanner)!!,
                        uri = "https://play.google.com/store/apps/details?id=com.intsig.lic.camscanner"
                    ),
                    PrettyAdvice(
                        title = "WolframAlpha",
                        description = """\
Calculadora científica inteligente que resuelve problemas de cálculo, sistemas de ecuaciones, \
trigonometría, etc. y hasta puede graficar funciones. Incluso es capaz de mostrar los pasos a seguir \
para resolver el problema, pero es una característica de pago.\
""".multilineTrim(),
                        image = ContextCompat.getDrawable(this.requireContext(), R.drawable.webicon_wolframalpha)!!,
                        uri = "https://www.wolframalpha.com"
                    ),
                    PrettyAdvice(
                        title = "Symbolab",
                        description = """\
Similar a WolframAlpha, un potente solucionador matemático, pero más sencillo de usar y no cobra por \
ver la solución paso a paso de un problema! Es genial, muy recomendado para practicar problemas \
matemáticos de todo tipo.\
""".multilineTrim(),
                        image = ContextCompat.getDrawable(this.requireContext(), R.drawable.webicon_symbolab)!!,
                        uri = null
                    ),
                    PrettyAdvice(
                        title = "MyBib 📚",
                        description = """\
MyBib es un generador de bibliografías y citaciones en varios formatos distintos que se elija. \
Sirve muchísimo para los informes de ingeniería que exigen un formato (e.g. Harvard). Mucho mejor \
que el gestor de bibliografías que tiene incorporado Microsoft Word. Sí.\
""".multilineTrim(),
                        image = ContextCompat.getDrawable(this.requireContext(), R.drawable.webicon_mybib)!!,
                        uri = "https://www.mybib.com"
                    ),
                    PrettyAdvice(
                        title = "draw.io",
                        description = """\
Sitio web para dibujar diagramas (colaborativamente en tiempo real). Sencillo y rápido, solo poner \
draw.io en el navegador.\
""".multilineTrim(),
                        image = ContextCompat.getDrawable(this.requireContext(), R.drawable.webicon_drawio)!!,
                        uri = "https://draw.io"
                    ),
                    PrettyAdvice(
                        title = "DroidCam",
                        description = """\
Si no tienes una webcam, con esta app puedes usar un teléfono Android como webcam, conectado por \
cable al PC o inalámbricamente por WI-FI.\
""".multilineTrim(),
                        image = ContextCompat.getDrawable(this.requireContext(), R.drawable.webicon_droidcam)!!,
                        uri = "https://play.google.com/store/apps/details?id=com.dev47apps.droidcam"
                    ),
                    PrettyAdvice(
                        title = "NightEye 🌙",
                        description = """\
Con esta extensión, puede convertir cualquier página web en modo oscuro para que sea más cómodo a la \
vista, sobre todo ahora que pasamos más tiempo que nunca mirando pantallas... Por desgracia es \
gratis sólo por tres meses, pero luego vale la pena pagar unos pocos dólares al año, es una \
maravilla.\
""".multilineTrim(),
                        image = ContextCompat.getDrawable(this.requireContext(), R.drawable.webicon_nighteye)!!,
                        uri = "https://nighteye.app"
                    )
//                    PrettyAdvice(
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
        }

        return fragView
    }

    private fun showHelp() {
        this.requireContext().infoDialog(
            title = "Ayuda — Inicio",
            message = """\
Este es el tablero principal donde aparecen secciones útiles, como las evaluaciones cercanas, \
links útiles y consejos varios. Si tienes algo que decir, o quieres agregar un consejo, por ejemplo, \
rellena el formulario de feedback! Está en la barra superior, en la esquina, presiona los tres \
puntitos.""".multilineTrim(),
            onDismiss = {},
            icon = null
        )
    }
}
