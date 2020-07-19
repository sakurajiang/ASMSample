package com.sakurajiang.plugin

import org.objectweb.asm.*
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.commons.AdviceAdapter


/**
 * Created by JDK on 2020/7/18.
 */
object InsertCode2ClassByASM {
    var isAlreadyInsertMethod = false
    var isAlreadyInsertVariable = false

    fun changeByteCodeByASM(classReader: ClassReader, classWriter: ClassWriter):ByteArray?{
        if("com/sakurajiang/asmsample/OriginClass"==classReader.className) {
            val insertMethod2ClassVisitor = InsertMethod2ClassVisitor(Opcodes.ASM7, classWriter)
            classReader.accept(insertMethod2ClassVisitor, ClassReader.EXPAND_FRAMES)
            val result = classWriter.toByteArray()
            DebugUtils.copy2Computer(classReader.className, result, "TestASM")
            return result
        }
        return null
    }

    class InsertMethod2ClassVisitor(api:Int,val classWriter: ClassWriter) : ClassVisitor(api,classWriter){

        var className:String? = null
        override fun visit(
            version: Int,
            access: Int,
            name: String?,
            signature: String?,
            superName: String?,
            interfaces: Array<out String>?
        ) {
            className = name
            super.visit(version, access, name, signature, superName, interfaces)
        }
        override fun visitField(
            access: Int,
            name: String?,
            descriptor: String?,
            signature: String?,
            value: Any?
        ): FieldVisitor {
            if(ConstantVariable.VARIABLE_INSERTED==name){
                isAlreadyInsertVariable = true
            }
            return super.visitField(access, name, descriptor, signature, value)
        }

        override fun visitMethod(
            access: Int,
            name: String?,
            descriptor: String?,
            signature: String?,
            exceptions: Array<out String>?
        ): MethodVisitor {
            val methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions)
            if("<init>"==name && "()V"==descriptor){
                return InitVariableAdviceAdapter(api,methodVisitor,access,name,descriptor,className)
            }
            if("add"==name&&"(II)I"==descriptor){
                return InsertMethodHeadAdviceAdapter(api,methodVisitor,access,name,descriptor)
            }
            if("insertMethod"==name&&"(Ljava/lang/String;)V"==descriptor){
                isAlreadyInsertMethod = true
            }
            return methodVisitor
        }
        override fun visitEnd() {
            if(!isAlreadyInsertVariable){
                insertVariable(classWriter)
            }
            if(!isAlreadyInsertMethod){
                insertMethod(classWriter)
            }
        }
    }

    class InitVariableAdviceAdapter (
        api: Int, val methodVisitor: MethodVisitor,
        access: Int, name: String?, desc: String?,val classFullName: String?
    ) : AdviceAdapter(api, methodVisitor, access, name, desc){
        override fun onMethodExit(opcode: Int) {
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 0)
            methodVisitor.visitIntInsn(Opcodes.SIPUSH, 666)
            methodVisitor.visitFieldInsn(
                Opcodes.PUTFIELD,
                classFullName,
                "sakurajiang",
                "I"
            )
        }
    }

    fun insertVariable(classWriter: ClassWriter){
        val fieldVisitor = classWriter.visitField(0, "sakurajiang", "I", null, null);
        fieldVisitor.visitEnd();
    }

    fun insertMethod(classWriter: ClassWriter){
        val methodVisitor =
            classWriter.visitMethod(ACC_PUBLIC, "insertMethod", "(Ljava/lang/String;)V", null, null)
        methodVisitor.visitCode()
        val label0 = Label()
        methodVisitor.visitLabel(label0)
        methodVisitor.visitLineNumber(12, label0)
        methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
        methodVisitor.visitTypeInsn(NEW, "java/lang/StringBuilder")
        methodVisitor.visitInsn(DUP)
        methodVisitor.visitMethodInsn(
            INVOKESPECIAL,
            "java/lang/StringBuilder",
            "<init>",
            "()V",
            false
        )
        methodVisitor.visitLdcInsn("value =")
        methodVisitor.visitMethodInsn(
            INVOKEVIRTUAL,
            "java/lang/StringBuilder",
            "append",
            "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
            false
        )
        methodVisitor.visitVarInsn(ALOAD, 1)
        methodVisitor.visitMethodInsn(
            INVOKEVIRTUAL,
            "java/lang/StringBuilder",
            "append",
            "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
            false
        )
        methodVisitor.visitMethodInsn(
            INVOKEVIRTUAL,
            "java/lang/StringBuilder",
            "toString",
            "()Ljava/lang/String;",
            false
        )
        methodVisitor.visitMethodInsn(
            INVOKEVIRTUAL,
            "java/io/PrintStream",
            "println",
            "(Ljava/lang/String;)V",
            false
        )
        val label1 = Label()
        methodVisitor.visitLabel(label1)
        methodVisitor.visitLineNumber(13, label1)
        methodVisitor.visitInsn(RETURN)
        val label2 = Label()
        methodVisitor.visitLabel(label2)
        methodVisitor.visitLocalVariable(
            "this",
            "Lcom/sakurajiang/asmsample/OriginClass;",
            null,
            label0,
            label2,
            0
        )
        methodVisitor.visitLocalVariable("value", "Ljava/lang/String;", null, label0, label2, 1)
        methodVisitor.visitMaxs(3, 2)
        methodVisitor.visitEnd()
    }

    class InsertMethodHeadAdviceAdapter (
        api: Int, val methodVisitor: MethodVisitor,
        access: Int, name: String?, desc: String?
    ) : AdviceAdapter(api, methodVisitor, access, name, desc){
        override fun onMethodEnter() {
            methodVisitor.visitInsn(Opcodes.ICONST_1)
            methodVisitor.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                "com/sakurajiang/asmsample/LoggerHelper",
                "log",
                "(Z)V",
                false
            )
        }
    }
}

