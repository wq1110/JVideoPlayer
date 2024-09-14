/* $Id$ */
/* 
 * Copyright (C) 2008-2011 Valoroso ltd.
 * Copyright (C) 2003-2008 Jaylon Cheng <jaylon.cheng@valorosoltd.com>
 *
 * Author: Jaylon.Cheng
 * Data: 2019-02-14
 */

#ifndef __JNI_LOG_H__
#define __JNI_LOG_H__

#include <android/log.h>
#include "jni_common.h"
//#include "rs_log.h"

JNI_BEGIN_DECL

#define  TAG "NativeLib-Ass"
#ifdef TAG
#define  VLOGV(...)  __android_log_vprint(ANDROID_LOG_VERBOSE,TAG,__VA_ARGS__) // 定义LOGV类型
#define  LOGV(...)  __android_log_print(ANDROID_LOG_VERBOSE,TAG,__VA_ARGS__) // 定义LOGV类型
#else
#define VLOGV(...)  /\
/VLOGV
#define  LOGV(...)  /\
/LOGV

#endif
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,TAG,__VA_ARGS__) // 定义LOGD类型
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,TAG,__VA_ARGS__) // 定义LOGI类型
#define  LOGW(...)  __android_log_print(ANDROID_LOG_WARN,TAG,__VA_ARGS__) // 定义LOGW类型
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,TAG,__VA_ARGS__) // 定义LOGE类型
#define  LOGF(...)  __android_log_print(ANDROID_LOG_FATAL,TAG,__VA_ARGS__) // 定义LOGF类型

/*#define JNI_LOGV(FMT, ...) rs_log(LOG_LEVEL_TRACE,  "L:%d %s()" FMT,  __LINE__, __FUNCTION__, ##__VA_ARGS__)
#define JNI_LOGD(FMT, ...) rs_log(LOG_LEVEL_DEBUG,  "L:%d %s()" FMT,  __LINE__, __FUNCTION__, ##__VA_ARGS__)
#define JNI_LOGI(FMT, ...) rs_log(LOG_LEVEL_RECORD,   "L:%d %s()" FMT,  __LINE__, __FUNCTION__, ##__VA_ARGS__)
#define JNI_LOGW(FMT, ...) rs_log(LOG_LEVEL_RECORD, "L:%d %s()" FMT,  __LINE__, __FUNCTION__, ##__VA_ARGS__)
#define JNI_LOGE(FMT, ...) rs_log(LOG_LEVEL_ERR,    "L:%d %s()" FMT,  __LINE__, __FUNCTION__, ##__VA_ARGS__)
#define JNI_LOGF(FMT, ...) rs_log(LOG_LEVEL_FATERR, "L:%d %s()" FMT,  __LINE__, __FUNCTION__, ##__VA_ARGS__)*/

//void jni_log_init(const char *work_path);

JNI_END_DECL

#endif

