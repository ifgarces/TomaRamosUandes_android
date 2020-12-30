# Simulador de toma de ramos Uandes - Android
Esta es una app para Android que permite planificar o simular la toma de ramos de la Universidad de los Andes.

Desarrollado en Android Studio (Kotlin).
Autor: Ignacio F. Garcés Santander.

## Funcionalidades
- Construcción automática de horario.
- Exportar horario como imagen (a la galería del teléfono).
- Detección de conflictos de horario y de evaluaciones entre ramos.
- Exportar evaluaciones al calendario del teléfono.
- Los ramos tomados volverán a aparecer al reabrir la app.
- Sólo se dispone de la malla de las carreras de ingeniería (no se pudo coordinación con las Facultades).

## Dependencias
- Versión mínima de Android: 8.0 (Oreo)

## Permisos de la aplicación
​- Acceso a internet: lectura del catálogo vigente (CSV) y revisar actualizaciones de la app al iniciar.
- Acceso al almacenamiento (escritura): para guardar el horario como imagen e recordar el conjunto de ramos tomados.
- Acceso al calendario (lectura y escritura): para que el usuario pueda exportar las evaluaciones al calendario que elija.

## Log
- Como no hay mucho output personalizado al Log, todo irá etiquetado bajo `_DEBUGLOG_`.