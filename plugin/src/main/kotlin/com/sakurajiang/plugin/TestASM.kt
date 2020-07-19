package com.sakurajiang.plugin

import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.ClassWriter.COMPUTE_MAXS

/**
 * Created by JDK on 2020/7/13.
 */
object TestASM {
    fun changeBytesByASM(target: Project, byteArray: ByteArray): ByteArray {
        val classReader = ClassReader(byteArray)
        val classWriter = ClassWriter(COMPUTE_MAXS)
        val result = InsertCode2ClassByASM.changeByteCodeByASM(classReader,classWriter)
        return result?:byteArray
}
}