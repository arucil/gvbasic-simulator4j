#include <jni.h>
#include "common_Utilities.h"
#include <stdio.h>
#include <stdlib.h>

/*
 * Class:     common_Utilities
 * Method:    realToString
 * Signature: (D)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_common_Utilities_realToString
  (JNIEnv *env, jclass cls, jdouble a) {
	char s[100];
	int len = sprintf(s, "%G", a);
	jclass strClass = (env)->FindClass("Ljava/lang/String;");
	jmethodID ctorID = (env)->GetMethodID(strClass, "<init>", "([BLjava/lang/String;)V");
	jbyteArray bytes = (env)->NewByteArray(len);//建立byte数组
	(env)->SetByteArrayRegion(bytes, 0, len, (jbyte*) s);//将char* 转换为byte数组
	jstring encoding = (env)->NewStringUTF("GB2312"); /* 设置String, 保存语言类型,用于byte数组转换至String时的参数*/
	return (jstring) (env)->NewObject(strClass, ctorID, bytes, encoding);/*将byte数组转换为java String,并输出*/
}

/*
 * Class:     common_Utilities
 * Method:    byteToString
 * Signature: (B)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_common_Utilities_byteToString
  (JNIEnv *env, jclass cls, jbyte b) {
	char s[1];
	s[0] = b;
	jclass strClass = (env)->FindClass("Ljava/lang/String;");
	jmethodID ctorID = (env)->GetMethodID(strClass, "<init>", "([BLjava/lang/String;)V");
	jbyteArray bytes = (env)->NewByteArray(1);
	(env)->SetByteArrayRegion(bytes, 0, 1, (jbyte*) s);
	jstring encoding = (env)->NewStringUTF("GB2312");
	return (jstring) (env)->NewObject(strClass, ctorID, bytes, encoding);
}

/*
 * Class:     common_Utilities
 * Method:    str2d
 * Signature: (Ljava/lang/String;)D
 */
JNIEXPORT jdouble JNICALL Java_common_Utilities_str2d
  (JNIEnv *env, jclass cls, jstring s) {
	jclass clsstring = env->FindClass("java/lang/String");
	jstring strencode = env->NewStringUTF("utf-8");
	jmethodID mid = env->GetMethodID(clsstring, "getBytes", "(Ljava/lang/String;)[B");
	jbyteArray barr= (jbyteArray)env->CallObjectMethod(s, mid, strencode);
	jbyte* ba = env->GetByteArrayElements(barr, JNI_FALSE);
	double r = strtod((char*) ba, NULL);
	env->ReleaseByteArrayElements(barr, ba, 0);
	return r;
}