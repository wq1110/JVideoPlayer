/* $Id$ */
/* 
 * Copyright (C) 2008-2011 Valoroso ltd.
 * Copyright (C) 2003-2008 Jaylon Cheng <jaylon.cheng@valorosoltd.com>
 *
 * Author: Jaylon.Cheng
 * Data: 2019-02-14
 */

#ifndef __JNI_COMMON_H__
#define __JNI_COMMON_H__

#include <stdio.h>
#include <stdlib.h>
#include <string>


#if defined(__ANDROID__)

#ifdef __cplusplus
#  define JNI_BEGIN_DECL     extern "C" {
#  define JNI_END_DECL       }
#else
#  define JNI_BEGIN_DECL
#  define JNI_END_DECL
#endif

#endif   /* end of  __ANDROID__*/


#if 0  // for testing
//两个用于测试的宏
#define PI 3.1415926
#define MAX(a,b) (a)>(b) ? (a) :(b)


//首先定义两个辅助宏
#define PRINT_MACRO_HELPER(x) #x 
#define PRINT_MACRO(x) #x"="PRINT_MACRO_HELPER(x) 

//编译阶段打印宏内容
#pragma message(PRINT_MACRO(PI))
#pragma message(PRINT_MACRO(PI2))
#pragma message(PRINT_MACRO(MAX(a,b)))
#pragma message(PRINT_MACRO(MAX(x,y)))
#endif



#endif
