/*******************************************************************************
 * Copyright (c) 2010, 2011 IBM Corporation and others.
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

#include "swt.h"
#include "webkit_win32.h"
#include "webkit_win32_stats.h"

#define WebKit_win32_NATIVE(func) Java_org_eclipse_swt_internal_webkit_WebKit_1win32_##func

#ifndef NO_willPerformClientRedirectToURL_1CALLBACK
static jlong willPerformClientRedirectToURL_CALLBACK;
static HRESULT CALLBACK willPerformClientRedirectToURL(void* ppVTable, jlong webView, BSTR url, double delaySeconds, DATE fireDate, jlong frame)
{
	return ((HRESULT (CALLBACK *)(void*, jlong, BSTR, double*, DATE*, jlong))willPerformClientRedirectToURL_CALLBACK)(ppVTable, webView, url, &delaySeconds, &fireDate, frame);
}
extern "C" JNIEXPORT jlong JNICALL WebKit_win32_NATIVE(willPerformClientRedirectToURL_1CALLBACK) (JNIEnv *env, jclass that, jlong func);
JNIEXPORT jlong JNICALL WebKit_win32_NATIVE(willPerformClientRedirectToURL_1CALLBACK) (JNIEnv *env, jclass that, jlong func)
{
	willPerformClientRedirectToURL_CALLBACK = func;
	return (jlong)willPerformClientRedirectToURL;
}
#endif

#ifndef NO_JSObjectCallAsFunctionProc_1CALLBACK
static jlong JSObjectCallAsFunctionProc_CALLBACK;
static JSValueRef JSObjectCallAsFunctionProc(JSContextRef ctx, JSObjectRef function, JSObjectRef thisObject, size_t argumentCount, const JSValueRef arguments[], JSValueRef* exception)
{
	return ((JSValueRef (CALLBACK *)(JSContextRef, JSObjectRef, JSObjectRef, size_t, const JSValueRef[], JSValueRef*))JSObjectCallAsFunctionProc_CALLBACK)(ctx, function, thisObject, argumentCount, arguments, exception);
}
extern "C" JNIEXPORT jlong JNICALL WebKit_win32_NATIVE(JSObjectCallAsFunctionProc_1CALLBACK) (JNIEnv *env, jclass that, jlong func);
JNIEXPORT jlong JNICALL WebKit_win32_NATIVE(JSObjectCallAsFunctionProc_1CALLBACK) (JNIEnv *env, jclass that, jlong func)
{
	JSObjectCallAsFunctionProc_CALLBACK = func;
	return (jlong)JSObjectCallAsFunctionProc;
}
#endif

#ifndef NO_JSObjectGetPropertyProc_1CALLBACK
static jlong JSObjectGetPropertyProc_CALLBACK;
static JSValueRef JSObjectGetPropertyProc(JSContextRef ctx, JSObjectRef object, JSStringRef propertyName, JSValueRef* exception)
{
	return ((JSValueRef (CALLBACK *)(JSContextRef, JSObjectRef, JSStringRef, JSValueRef*))JSObjectGetPropertyProc_CALLBACK)(ctx, object, propertyName, exception);
}
extern "C" JNIEXPORT jlong JNICALL WebKit_win32_NATIVE(JSObjectGetPropertyProc_1CALLBACK) (JNIEnv *env, jclass that, jlong func);
JNIEXPORT jlong JNICALL WebKit_win32_NATIVE(JSObjectGetPropertyProc_1CALLBACK) (JNIEnv *env, jclass that, jlong func)
{
	JSObjectGetPropertyProc_CALLBACK = func;
	return (jlong)JSObjectGetPropertyProc;
}
#endif

#ifndef NO_JSObjectHasPropertyProc_1CALLBACK
static jlong JSObjectHasPropertyProc_CALLBACK;
static bool JSObjectHasPropertyProc(JSContextRef ctx, JSObjectRef object, JSStringRef propertyName)
{
	return ((bool (CALLBACK *)(JSContextRef, JSObjectRef, JSStringRef))JSObjectHasPropertyProc_CALLBACK)(ctx, object, propertyName);
}
extern "C" JNIEXPORT jlong JNICALL WebKit_win32_NATIVE(JSObjectHasPropertyProc_1CALLBACK) (JNIEnv *env, jclass that, jlong func);
JNIEXPORT jlong JNICALL WebKit_win32_NATIVE(JSObjectHasPropertyProc_1CALLBACK) (JNIEnv *env, jclass that, jlong func)
{
	JSObjectHasPropertyProc_CALLBACK = func;
	return (jlong)JSObjectHasPropertyProc;
}
#endif

#ifndef NO_WebKitCreateInstance
extern "C" JNIEXPORT jint JNICALL WebKit_win32_NATIVE(WebKitCreateInstance)(JNIEnv *env, jclass that, jbyteArray arg0, jlong arg1, jbyteArray arg2, jlongArray arg3);
JNIEXPORT jint JNICALL WebKit_win32_NATIVE(WebKitCreateInstance)
	(JNIEnv *env, jclass that, jbyteArray arg0, jlong arg1, jbyteArray arg2, jlongArray arg3)
{
	jbyte *lparg0=NULL;
	jbyte *lparg2=NULL;
	jlong *lparg3=NULL;
	jint rc = 0;
	WebKit_win32_NATIVE_ENTER(env, that, WebKitCreateInstance_FUNC);
	if (arg0) if ((lparg0 = env->GetByteArrayElements(arg0, NULL)) == NULL) goto fail;
	if (arg2) if ((lparg2 = env->GetByteArrayElements(arg2, NULL)) == NULL) goto fail;
	if (arg3) if ((lparg3 = env->GetIntLongArrayElements(arg3, NULL)) == NULL) goto fail;
/*
	rc = (jint)WebKitCreateInstance(lparg0, arg1, lparg2, lparg3);
*/
	{
		WebKit_LOAD_FUNCTION(fp, WebKitCreateInstance)
		if (fp) {
			rc = (jint)((jint (*)(jbyte *, jlong, jbyte *, jlong *))fp)(lparg0, arg1, lparg2, lparg3);
		}
	}
fail:
	if (arg3 && lparg3) env->ReleaseIntLongArrayElements(arg3, lparg3, 0);
	if (arg2 && lparg2) env->ReleaseByteArrayElements(arg2, lparg2, 0);
	if (arg0 && lparg0) env->ReleaseByteArrayElements(arg0, lparg0, 0);
	WebKit_win32_NATIVE_EXIT(env, that, WebKitCreateInstance_FUNC);
	return rc;
}
#endif
