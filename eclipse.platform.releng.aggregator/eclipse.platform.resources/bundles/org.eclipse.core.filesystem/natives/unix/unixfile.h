/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
#include <jni.h>

#undef UNICODE_SUPPORTED
#define UNICODE_SUPPORTED 1L
#undef CHFLAGS_SUPPORTED
#define CHFLAGS_SUPPORTED 2L

/*
 * Get a null-terminated byte array from a java byte array. The returned bytearray
 * needs to be freed when not used anymore. Use free(result) to do that.
 */
jbyte* getByteArray(JNIEnv *, jbyteArray);

/*
 * Fills StructStat object with data from struct stat.
 */
jint convertStatToObject(JNIEnv *, struct stat, jobject);

/* DO NOT EDIT THIS FILE - it is machine generated */

/* Header for class org_eclipse_core_internal_filesystem_local_unix_UnixFileNatives */

#ifndef _Included_org_eclipse_core_internal_filesystem_local_unix_UnixFileNatives
#define _Included_org_eclipse_core_internal_filesystem_local_unix_UnixFileNatives
#ifdef __cplusplus
extern "C" {
#endif

/*
 * Class:     Java_org_eclipse_core_internal_filesystem_local_unix_UnixFileNatives_initializeStructStatFieldIDs
 * Method:    initializeStructStatFieldIDs
 * Signature: ()V
 */
JNIEXPORT jint JNICALL Java_org_eclipse_core_internal_filesystem_local_unix_UnixFileNatives_initializeStructStatFieldIDs
  (JNIEnv *env, jclass clazz);

/*
 * Class:     org_eclipse_core_internal_filesystem_local_unix_UnixFileNatives
 * Method:    chmod
 * Signature: ([BI)I
 */
JNIEXPORT jint JNICALL Java_org_eclipse_core_internal_filesystem_local_unix_UnixFileNatives_chmod
  (JNIEnv *, jclass, jbyteArray, jint);

/*
 * Class:     org_eclipse_core_internal_filesystem_local_unix_UnixFileNatives
 * Method:    chflags
 * Signature: ([BI)I
 */
JNIEXPORT jint JNICALL Java_org_eclipse_core_internal_filesystem_local_unix_UnixFileNatives_chflags
  (JNIEnv *, jclass, jbyteArray, jint);

/*
 * Class:     org_eclipse_core_internal_filesystem_local_unix_UnixFileNatives
 * Method:    stat
 * Signature: ([BLorg/eclipse/core/internal/filesystem/local/unix/StructStat;)I
 */
JNIEXPORT jint JNICALL Java_org_eclipse_core_internal_filesystem_local_unix_UnixFileNatives_stat
  (JNIEnv *, jclass, jbyteArray, jobject);

/*
 * Class:     org_eclipse_core_internal_filesystem_local_unix_UnixFileNatives
 * Method:    lstat
 * Signature: ([BLorg/eclipse/core/internal/filesystem/local/unix/StructStat;)I
 */
JNIEXPORT jint JNICALL Java_org_eclipse_core_internal_filesystem_local_unix_UnixFileNatives_lstat
  (JNIEnv *, jclass, jbyteArray, jobject);

/*
 * Class:     org_eclipse_core_internal_filesystem_local_unix_UnixFileNatives
 * Method:    readlink
 * Signature: ([B[BJ)I
 */
JNIEXPORT jint JNICALL Java_org_eclipse_core_internal_filesystem_local_unix_UnixFileNatives_readlink
  (JNIEnv *, jclass, jbyteArray, jbyteArray, jlong);

/*
 * Class:     org_eclipse_core_internal_filesystem_local_unix_UnixFileNatives
 * Method:    errno
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_eclipse_core_internal_filesystem_local_unix_UnixFileNatives_errno
  (JNIEnv *, jclass);

/*
 * Class:     org_eclipse_core_internal_filesystem_local_unix_UnixFileNatives
 * Method:    libattr
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_eclipse_core_internal_filesystem_local_unix_UnixFileNatives_libattr
  (JNIEnv *, jclass);

/*
 * Class:     org_eclipse_core_internal_filesystem_local_unix_UnixFileNatives
 * Method:    tounicode
 * Signature: ([C)[B
 */
JNIEXPORT jbyteArray JNICALL Java_org_eclipse_core_internal_filesystem_local_unix_UnixFileNatives_tounicode
  (JNIEnv *, jclass, jcharArray);

/*
 * Class:     org_eclipse_core_internal_filesystem_local_unix_UnixFileNatives
 * Method:    getflag
 * Signature: ([B)I
 */
JNIEXPORT jint JNICALL Java_org_eclipse_core_internal_filesystem_local_unix_UnixFileNatives_getflag
  (JNIEnv *, jclass, jbyteArray);

#ifdef __cplusplus
}
#endif
#endif
