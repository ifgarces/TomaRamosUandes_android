package com.ifgarces.tomaramosuandes.utils

import android.util.Log
import kotlin.reflect.KClass


/**
 * Simple "package"-like object for encapsulating custom logging utility functions, just because I
 * don't like Android's `Log` library as it is. All methods provide simplified logging with
 * old-school Java-style string formatting and an appended "tag" that simplifies traicing for the
 * context (i.e. outputting Activity, Fragment, etc.). Also allows the developer to easily use
 * Logcat for reading the entire custom logging of the app for when the output is not too heavy, as
 * all of it will be tagged the same.
 * @author Ignacio F. Garcés.
 * @property LOGF_TAG The common tag for all logging on this "package".
 */
object Logf {
    public const val LOGF_TAG :String = "LOGF"

    /**
     * DEBUG log level.
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
     * @param scopeClass The class of the Fragment, Activity, Object, etc. of the scope that
     * desires to output to log.
     * @param format String format.
     * @param args String format args.
     */
    public fun error(scopeClass :KClass<*>, format :String, vararg args :Any?) {
        Log.e(this.LOGF_TAG, "[%s] %s".format(scopeClass.simpleName!!, format.format(*args)))
    }

    /**
     * WARNING log level. For function documentation, see `Logf.debug`.
     * @param scopeClass The class of the Fragment, Activity, Object, etc. of the scope that
     * desires to output to log.
     * @param format String format.
     * @param args String format args.
     */
    public fun warn(scopeClass :KClass<*>, format :String, vararg args :Any?) {
        Log.w(this.LOGF_TAG, "[%s] %s".format(scopeClass.simpleName!!, format.format(*args)))
    }

    /**
     * INFO log level. For function documentation, see `Logf.debug`.
     * @param scopeClass The class of the Fragment, Activity, Object, etc. of the scope that
     * desires to output to log.
     * @param format String format.
     * @param args String format args.
     */
    public fun info(scopeClass :KClass<*>, format :String, vararg args :Any?) {
        Log.i(this.LOGF_TAG, "[%s] %s".format(scopeClass.simpleName!!, format.format(*args)))
    }
}
