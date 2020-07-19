package com.sakurajiang.plugin

import java.io.File
import java.io.FileOutputStream

/**
 * Created by JDK on 2019-11-14.
 */
object DebugUtils {

    private fun getLastName(originName: String): String {
        var name = originName.lastIndexOf("/")
        if (name == -1) {
            name = 0
        }
        return originName.substring(name, originName.length)
    }

    fun copy2Computer(
        originName: String,
        byteArray: ByteArray,
        localLastDirName: String,
        localAddress: String = "/Users/dingkuijiang/TestASM/"
    ) {
        val dir = File("$localAddress$localLastDirName")
        if(!dir.exists()){
            dir.mkdir()
        }
        val file = File(dir,"${getLastName(originName)}.class")
        println("copy file path${file.absolutePath}")
        val fileOutputStream = FileOutputStream(file)
        fileOutputStream.write(byteArray)
    }
}