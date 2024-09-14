#include <jni.h>
#include <string>
#include <vector>
#include <map>
#include <android/bitmap.h>
#include "ass.h"
#include <cmath>
#include <android/log.h>

bool isDebug = false;

#define  TAG "AssAndroid"
#define  VLOGV(...)  __android_log_vprint(ANDROID_LOG_VERBOSE,TAG,__VA_ARGS__) // 定义LOGV类型
#define  LOGV(...)  __android_log_print(ANDROID_LOG_VERBOSE,TAG,__VA_ARGS__) // 定义LOGV类型

using namespace std;

ASS_Library *library;

#define _r(c)  ((c)>>24)
#define _g(c)  (((c)>>16)&0xFF)
#define _b(c)  (((c)>>8)&0xFF)
#define _a(c)  ((c)&0xFF)

class AssWorkGroup {
public:
    ASS_Renderer *render;
    ASS_Track *track;
    u_int32_t w;
    u_int32_t h;

    ~AssWorkGroup() {
        LOGV("AssWorkGroup ~: track Name: %s", track->name);
        ass_free_track(track);
        ass_renderer_done(render);
    }
};

map<jlong, AssWorkGroup *> workGroups;

static jobject convert(JNIEnv *env, jclass clazz, jbyteArray buffer, jint length, jint resolution_x,
                       jint resolution_y) {
    LOGV("convert operation length:%d width:%d height:%d", length, resolution_x, resolution_y);
    auto *group = new AssWorkGroup();
    jbyte *bytes = env->GetByteArrayElements(buffer, JNI_FALSE);
    group->track = ass_read_memory(library, reinterpret_cast<char *>(bytes), length, nullptr);
    group->w = resolution_x;
    group->h = resolution_y;
    group->render = ass_renderer_init(library);
    ass_set_cache_limits(group->render, 0, 16);
    ass_set_frame_size(group->render, group->w, group->h);
    ass_set_fonts(group->render, nullptr, nullptr, ASS_FONTPROVIDER_AUTODETECT, nullptr, 1);
    jclass jAssTrackClz = env->FindClass("com/cv/media/lib/ass/AssTrack");
    jmethodID jAssTrackInitMethodId = env->GetMethodID(jAssTrackClz, "<init>", "()V");
    jfieldID jAssTrackPtrId = env->GetFieldID(jAssTrackClz, "ptr", "J");
    jobject jAssTrack = env->NewObject(jAssTrackClz, jAssTrackInitMethodId);
    env->SetLongField(jAssTrack, jAssTrackPtrId, reinterpret_cast<jlong>(group));
    workGroups.insert(pair<jlong, AssWorkGroup *>(reinterpret_cast<jlong>(group), group));
    env->ReleaseByteArrayElements(buffer, bytes, 0);
    return jAssTrack;
}

static AssWorkGroup *getWorkGroup(JNIEnv *env, jobject jAssTrack) {
    jclass jAssTrackClz = env->FindClass("com/cv/media/lib/ass/AssTrack");
    jfieldID jAssTrackPtrId = env->GetFieldID(jAssTrackClz, "ptr", "J");
    jlong ptr = env->GetLongField(jAssTrack, jAssTrackPtrId);
    if (workGroups.find(ptr) != workGroups.end()) {
        auto *group = reinterpret_cast<AssWorkGroup *>((char *) ptr);
        return group;
    }
    return nullptr;
}

static void assTrack_Destory(JNIEnv *env, jobject thiz) {
    AssWorkGroup *group = getWorkGroup(env, thiz);
    if (group != nullptr) {
        LOGV("track Destroy: track Name: %s", group->track->name);
        workGroups.erase(reinterpret_cast<const jlong>(group));
        delete (group);
    }
}

static void tear(JNIEnv *env, jobject obj) {
    jclass jClassLibAss = env->FindClass("com/cv/media/lib/ass/Ass");
    jmethodID jmethodLibAss_Tear = env->GetStaticMethodID(jClassLibAss, "tear",
                                                          "(Ljava/lang/Object;)V");
    env->CallStaticVoidMethod(jClassLibAss, jmethodLibAss_Tear, obj);
}

static jobjectArray getImage(JNIEnv *env, jclass clazz, jobject track, jlong ts) {
//    LOGV("getImage ts: %lld", ts);
    AssWorkGroup *group = getWorkGroup(env, track);
    if (group) {
        ASS_Image *currentAssImg = ass_render_frame(group->render, group->track, ts, nullptr);
        vector<jobject> vectorAssImages;
        jclass jClassAssImage = env->FindClass("com/cv/media/lib/ass/AssImage");
        if (currentAssImg == nullptr) {
//            LOGV("getImage 失败");
            return nullptr;
        }
//        LOGV("getImage 成功 w:%d h:%d", currentAssImg->w, currentAssImg->h);
        while (currentAssImg != nullptr) {
            unsigned char a = _a(currentAssImg->color);
            unsigned char r = _r(currentAssImg->color);
            unsigned char g = _g(currentAssImg->color);
            unsigned char b = _b(currentAssImg->color);
            unsigned char *src = currentAssImg->bitmap;
//            unsigned int dst_stride = currentAssImg->w * 4;
            auto *dst = static_cast<jint *>(calloc(sizeof(jint),
                                                   currentAssImg->h * currentAssImg->w));
            vector<char> bitmapNodes;
//            LOGV("============title================");
//            LOGV("基础RGBA R:%d G:%d B:%d A:%d  width: %d height: %d", r, g, b, a, currentAssImg->w, currentAssImg->h);
            for (int y = 0; y < currentAssImg->h; ++y) {
                for (int x = 0; x < currentAssImg->w; ++x) {
                    int dstIndex = y * currentAssImg->w + x;
                    int srcIndex = y * currentAssImg->stride + x;
                    unsigned char srcAlpha = src[srcIndex];
//                    LOGV("alpha:%d ", srcAlpha);
                    //  int color = (A & 0xff) << 24 | (B & 0xff) << 16 | (G & 0xff) << 8 | (R & 0xff);
                    dst[dstIndex] = ((srcAlpha * (255 - a) / 255) & 0xff) << 24 |
                                    ((srcAlpha * r / 255) & 0xff) << 16 |
                                    ((srcAlpha * g / 255) & 0xff) << 8 |
                                    ((srcAlpha * b / 255) & 0xff);
//                    dst[dstIndex + 3] = srcAlpha * (255 - a) / 255;
//                    dst[dstIndex + 2] = srcAlpha * b / 255;
//                    dst[dstIndex + 1] = srcAlpha * g / 255;
//                    dst[dstIndex] = srcAlpha * r / 255;
                    // LOGV("a:%d b:%d g:%d r:%d", dst[dstIndex], dst[dstIndex + 1], dst[dstIndex + 2],dst[dstIndex + 3]);
                }
            }
            jclass bitmapConfig = env->FindClass("android/graphics/Bitmap$Config");
            jfieldID rgba8888FieldID = env->GetStaticFieldID(bitmapConfig, "ARGB_8888",
                                                             "Landroid/graphics/Bitmap$Config;");
            jobject rgba8888Obj = env->GetStaticObjectField(bitmapConfig, rgba8888FieldID);
            jclass bitmapPoolClass = env->FindClass("com/cv/media/lib/ass/AssBitmapPool");
            jclass bitmapClass = env->FindClass("android/graphics/Bitmap");
            jmethodID createBitmapMethodID = env->GetStaticMethodID(bitmapPoolClass, "get",
                                                                    "(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;");
            jmethodID setPixelsMethodID = env->GetMethodID(bitmapClass, "setPixels", "([IIIIIII)V");
            jobject bitmapObj = env->CallStaticObjectMethod(bitmapPoolClass, createBitmapMethodID,
                                                            currentAssImg->w,
                                                            currentAssImg->h, rgba8888Obj);
            if (!bitmapObj) {
                LOGV("create bitmap fail w:%d h:%d", currentAssImg->w, currentAssImg->h);
                continue;
            }
            jintArray pixels = env->NewIntArray(currentAssImg->w * currentAssImg->h);
            env->SetIntArrayRegion(pixels, 0, currentAssImg->w * currentAssImg->h,
                                   dst);
            free(dst);
            env->CallVoidMethod(bitmapObj, setPixelsMethodID, pixels, 0, currentAssImg->w, 0, 0,
                                currentAssImg->w, currentAssImg->h);
            tear(env, bitmapObj);
            jmethodID jAssImageInitMethodId = env->GetMethodID(jClassAssImage, "<init>", "()V");
            jobject jObjectAssImage = env->NewObject(jClassAssImage, jAssImageInitMethodId);
            jfieldID jAssImageXFieldId = env->GetFieldID(jClassAssImage, "x", "I");
            jfieldID jAssImageYFieldId = env->GetFieldID(jClassAssImage, "y", "I");
            jfieldID jAssImageBitmapFieldId = env->GetFieldID(jClassAssImage, "bitmap",
                                                              "Landroid/graphics/Bitmap;");
            env->SetIntField(jObjectAssImage, jAssImageXFieldId, currentAssImg->dst_x);
            env->SetIntField(jObjectAssImage, jAssImageYFieldId, currentAssImg->dst_y);
            env->SetObjectField(jObjectAssImage, jAssImageBitmapFieldId, bitmapObj);
            vectorAssImages.push_back(jObjectAssImage);
            currentAssImg = currentAssImg->next;
        }
        jobjectArray jArrayAssImage = env->NewObjectArray(vectorAssImages.size(), jClassAssImage,
                                                          nullptr);

        for (int i = 0; i < vectorAssImages.size(); ++i) {
            env->SetObjectArrayElement(jArrayAssImage, i, vectorAssImages[i]);
        }
        return jArrayAssImage;
    }
    return nullptr;
}


static int
regNatives(JNIEnv *env, const char *classPath, JNINativeMethod *methods, uint32_t methodsSize) {
    _jclass *clazz = env->FindClass(classPath);
    if (clazz == nullptr) {
        return JNI_ERR;
    }
    env->RegisterNatives(clazz, methods, methodsSize);
    return 0;
}

const char *font_provider_labels[] = {
        "None",
        "Autodetect",
        "CoreText",
        "Fontconfig",
        "DirectWrite",
};

static void print_font_providers(ASS_Library *ass_library) {
    int i;
    ASS_DefaultFontProvider *providers;
    size_t providers_size = 0;
    ass_get_available_font_providers(ass_library, &providers, &providers_size);
    LOGV("Available font providers (%zu): ", providers_size);
    for (i = 0; i < providers_size; i++) {
        LOGV("font_provider_labels: %s", font_provider_labels[providers[i]]);
    }
    printf(".\n");
    free(providers);
}

void msg_callback(int level, const char *fmt, va_list va, void *data) {
    if (level > 6)
        return;
    VLOGV(fmt, va);
}

void addFont(JNIEnv *env, jclass clazz, jstring name, jbyteArray buffer, jint length) {
    char *cName = const_cast<char *>(env->GetStringUTFChars(name, JNI_FALSE));
    jbyte *cBuffer = env->GetByteArrayElements(buffer, JNI_FALSE);
    ass_add_font(library, cName, reinterpret_cast<char *>(cBuffer), length);
    env->ReleaseByteArrayElements(buffer, cBuffer, 0);
    LOGV("addFont operation name: %s, length: %d", cName, length);
}

jint setNativeEnvironmentVariable(JNIEnv *env, jclass clazz, jstring variable_name,
                                  jstring variable_value) {
    const char *variableNameString = env->GetStringUTFChars(variable_name, nullptr);
    const char *variableValueString = env->GetStringUTFChars(variable_value, nullptr);
    int rc = setenv(variableNameString, variableValueString, 1);
    env->ReleaseStringUTFChars(variable_name, variableNameString);
    env->ReleaseStringUTFChars(variable_value, variableValueString);
    LOGV("setEnv operation name: %s, value: %s", variableNameString, variableValueString);
    return rc;
}

JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) == JNI_OK) {
        {
            JNINativeMethod methods[] = {
                    {"convert",                      "([BIII)Lcom/cv/media/lib/ass/AssTrack;",                             (void *) convert},
                    {"getImage",                     "(Lcom/cv/media/lib/ass/AssTrack;J)[Lcom/cv/media/lib/ass/AssImage;", (void *) getImage},
                    {"addFont",                      "(Ljava/lang/String;[BI)V",                                           (void *) addFont},
                    {"setNativeEnvironmentVariable", "(Ljava/lang/String;Ljava/lang/String;)I",                            (void *) setNativeEnvironmentVariable},
            };
            regNatives(env, "com/cv/media/lib/ass/Ass", methods,
                       sizeof(methods) / sizeof(methods[0]));
        }
        {
            JNINativeMethod methods[] = {
                    {"destory", "()V", (void *) assTrack_Destory},
            };
            regNatives(env, "com/cv/media/lib/ass/AssTrack", methods,
                       sizeof(methods) / sizeof(methods[0]));
        }

        library = ass_library_init();
        ass_set_message_cb(library, msg_callback, nullptr);
        ass_set_extract_fonts(library, ASS_FONTPROVIDER_AUTODETECT);
        print_font_providers(library);
    }
    return JNI_VERSION_1_6;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_cv_media_lib_ass_Ass_setNativeDebug(JNIEnv *env, jclass clazz, jboolean ref) {
    isDebug = ref;
}