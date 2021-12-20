package com.ifgarces.tomaramosuandes.utils

import android.util.Log
import com.ifgarces.tomaramosuandes.utils.Logf.LOGF_TAG
import kotlin.reflect.KClass


/**
 * "Package" for encapsulating custom logging utility functions, just because I don't like Android's
 * `Log` library as it is. All methods provide simplified logging with old-school Java-style string
 * formatting. Allows to easily use Logcat for reading the entire custom logging of the app for when
 * the output is not too heavy, as all of it will be tagged the same.
 * @author Ignacio F. Garcés.
 * @property LOGF_TAG The tag for all logging on this "package".
 */
object Logf {
    public const val LOGF_TAG :String = "LOGF"

    /**
     * DEBUG log level.
     * @author Ignacio F. Garcés.
     * @param scopeClass The class of the Fragment, Activity, Object, etc. of the scope that
     * desires to output to log.
     * @param format String format.
     * @param args String format args.
     */
    public fun debug(scopeClass :KClass<*>, format :String, vararg args :Any?) {
        Log.d(this.LOGF_TAG, "[%s] %s".format(scopeClass.simpleName!!, format.format(*args)))
    }

    /**
     * ERROR log level. For function documentation, see `Logf.debug`.
     */
    public fun error(scopeClass :KClass<*>, format :String, vararg args :Any?) {
        Log.e(this.LOGF_TAG, "[%s] %s".format(scopeClass.simpleName!!, format.format(*args)))
    }

    /**
     * WARNING log level. For function documentation, see `Logf.debug`.
     */
    public fun warn(scopeClass :KClass<*>, format :String, vararg args :Any?) {
        Log.w(this.LOGF_TAG, "[%s] %s".format(scopeClass.simpleName!!, format.format(*args)))
    }

    /**
     * INFO log level. For function documentation, see `Logf.debug`.
     */
    public fun info(scopeClass :KClass<*>, format :String, vararg args :Any?) {
        Log.i(this.LOGF_TAG, "[%s] %s".format(scopeClass.simpleName!!, format.format(*args)))
    }
}