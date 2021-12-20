package com.ifgarces.tomaramosuandes.utils


/**
 * Container of the intent keys for communication between activities/fragments.
 * @author Ignacio F. Garcés.
 * @property RAMO_NRC Primary key for `Ramo`.
 * @property RAMO_IS_INSCRIBED Display mode of `Ramo` (launched from catalog or home).
 */
object IntentKeys {
    const val RAMO_NRC :String = "nrc"
    const val RAMO_IS_INSCRIBED :String = "ramo_is_inscribed"
}