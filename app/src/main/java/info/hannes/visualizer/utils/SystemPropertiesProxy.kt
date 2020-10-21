package info.hannes.visualizer.utils

import android.content.Context
import android.util.Log

object SystemPropertiesProxy {
    private const val TAG = "SystemPropertiesProxy"

    /**
     * Get the value for the given key, returned as a boolean. Values 'n', 'no',
     * '0', 'false' or 'off' are considered false. Values 'y', 'yes', '1', 'true'
     * or 'on' are considered true. (case insensitive). If the key does not exist,
     * or has any other value, then the default result is returned.
     *
     * @param key the key to lookup
     * @param def a default value to return
     * @return the key parsed as a boolean, or def if the key isn't found or is
     * not able to be parsed as a boolean.
     * @throws IllegalArgumentException if the key exceeds 32 characters
     */
    @Throws(IllegalArgumentException::class)
    fun getBoolean(context: Context, key: String, def: Boolean): Boolean {
        return getBoolean(context.classLoader, key, def)
    }

    @Throws(IllegalArgumentException::class)
    fun getBoolean(cl: ClassLoader, key: String, def: Boolean): Boolean {
        var ret: Boolean
        try {
            val systemProperties = cl.loadClass("android.os.SystemProperties")

            // Parameters Types
            val paramTypes = arrayOfNulls<Class<*>?>(2)
            paramTypes[0] = String::class.java
            paramTypes[1] = Boolean::class.javaPrimitiveType
            val getBoolean = systemProperties.getMethod("getBoolean", *paramTypes)

            // Parameters
            val params = arrayOfNulls<Any>(2)
            params[0] = key
            params[1] = java.lang.Boolean.valueOf(def)
            ret = getBoolean.invoke(systemProperties, *params) as Boolean
        } catch (iAE: IllegalArgumentException) {
            throw iAE
        } catch (e: Exception) {
            Log.e(TAG, "getBoolean(context, key: $key, def:$def)", e)
            ret = def
        }
        return ret
    }
}