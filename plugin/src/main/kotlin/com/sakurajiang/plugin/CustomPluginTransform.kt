package com.sakurajiang.plugin

import com.android.SdkConstants
import com.android.build.api.transform.*
import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.pipeline.TransformManager
import com.google.common.io.Closer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import java.io.File
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Created by JDK on 2020/7/13.
 */
class CustomPluginTransform : Transform(),Plugin<Project>{
    lateinit var target: Project
    lateinit var logger: Logger
    override fun getName(): String {
        return "testASM"
    }

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> {
        return TransformManager.CONTENT_CLASS
    }

    override fun isIncremental(): Boolean {
        return false
    }

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    override fun apply(target: Project) {
        this.target = target
        this.logger = target.logger
        val appExtension = target.extensions.findByType(AppExtension::class.java)
        appExtension?.registerTransform(this)
    }

    override fun transform(transformInvocation: TransformInvocation?) {
        val inputs = transformInvocation?.inputs
        val outputProvider = transformInvocation?.outputProvider
        if (transformInvocation?.isIncremental == false) {
            outputProvider?.deleteAll()
        }

        inputs?.forEach {
            it.jarInputs.forEach { input ->
                val outJar = outputProvider?.getContentLocation(input)
                outJar?.let { it1 -> changeFromJar(input, it1) }
            }
            it.directoryInputs.forEach { dir ->
                val outDir = outputProvider?.getContentLocation(dir)
                outDir?.let { it1 -> changeFromDir(dir, it1) }
            }
        }
    }

    private fun TransformOutputProvider.getContentLocation(input: QualifiedContent): File =
        getContentLocation(
            input.name,
            input.contentTypes,
            input.scopes,
            if (input is JarInput) Format.JAR else Format.DIRECTORY
        )

    private fun changeFromDir(input: DirectoryInput, output: File) {
        for (f: File in input.file.walkTopDown()) {
            try {
                val relativePath = f.toRelativeString(input.file)
                val outFile = File(output, relativePath)
                if (outFile.exists()) {
                    outFile.deleteRecursively()
                }
                if (f.isDirectory) {
                    outFile.mkdir()
                    continue
                }
                if (!f.name.endsWith(SdkConstants.DOT_CLASS)) {
                    f.copyTo(outFile)
                    continue
                }
                val bytes = TestASM.changeBytesByASM(target,f.readBytes())
                outFile.writeBytes(bytes)
            } catch (t: Throwable) {
                logPatchError(t, f.name)
            }
        }
    }

    private fun logPatchError(t: Throwable, tag: String) {
        logger.error("===========================================================>")
        logger.error("An error occurred while patching class[$tag], will skip", t)
        logger.error("<===========================================================")
    }

    private fun changeFromJar(jarInput: JarInput, output: File) {
        Closer.create().use { closer ->
            val inputJar = closer.register(JarFile(jarInput.file))
            val jarOutStream = closer.register(JarOutputStream(output.outputStream()))

            for (entry in inputJar.entries()) {
                val entryName = entry.name
                if (entry.isDirectory || entryName == JarFile.MANIFEST_NAME) {
                    continue
                }
                val originByteArray = inputJar.getInputStream(entry).readBytes()
                val newByteArray = if (entryName.endsWith(SdkConstants.DOT_CLASS)) {
                    TestASM.changeBytesByASM(target,originByteArray)
                } else {
                    originByteArray
                }
                writeEntry(newByteArray, jarOutStream, entryName)
            }
        }
    }

    private fun writeEntry(classBytesArray: ByteArray, zos: ZipOutputStream, entryName: String) {
        try {
            val entry = ZipEntry(entryName)
            zos.putNextEntry(entry)
            zos.write(classBytesArray, 0, classBytesArray.size)
            zos.closeEntry()
            zos.flush()
        } catch (t: Throwable) {
            logger.warn("Error while writing zip entry $entryName", t)
            t.printStackTrace()
        }
    }
}