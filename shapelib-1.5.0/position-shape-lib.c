/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

#include <stdlib.h>
#include <string.h>
#include <jni.h>
#include <android/log.h>
#include "shapefil.h"

void
processPointPosition(JNIEnv *env, SHPHandle hSHP, DBFHandle hDBF, jstring file, jobject listObject);

void
processLinePosition(JNIEnv *env, SHPHandle hSHP, DBFHandle hDBF, jstring file, jobject listObject);

void processPolygonPosition(JNIEnv *env, SHPHandle hSHP, DBFHandle hDBF, jstring file,
                            jobject listObject);

int writePointShp(JNIEnv *env, SHPHandle hSHP, DBFHandle hDBF, int index, double latitude,
                  double longitude, jstring date);

int writeLineShp(JNIEnv *env, SHPHandle hSHP, DBFHandle hDBF, int index, int numberVertices,
                 double *latitude, double *longitude, jstring date);

int writePolygonShp(JNIEnv *env, SHPHandle hSHP, DBFHandle hDBF, int index, int numberVertices,
                    double *latitude, double *longitude, jstring date);

JNIEXPORT jintArray JNICALL
Java_com_example_position2shp_Util_PositionUtils_getFieldTypes(JNIEnv *env, jclass clazz,
                                                               jstring file_path) {
    const char *fileStr = (*env)->GetStringUTFChars(env, file_path, 0);
    DBFHandle hDBF = DBFOpen(fileStr, "rb+");
    (*env)->ReleaseStringUTFChars(env, file_path, fileStr);

    int fieldCount = DBFGetFieldCount(hDBF);

    jintArray ret;
    ret = (jobjectArray) (*env)->NewIntArray(env, fieldCount);

    jint fill[fieldCount];
    if (fieldCount > 0) {
        int i;
        char *pszFieldName = (int *) malloc(sizeof(char));
        int *pnWidth = (int *) malloc(sizeof(int));
        int *pnDecimals = (int *) malloc(sizeof(int));

        for (i = 0; i < fieldCount; i++) {
            DBFFieldType field = DBFGetFieldInfo(hDBF, i, pszFieldName, pnWidth, pnDecimals);
            int type = (int) field;
            fill[i] = type;

            /*jint *body = (*env)->GetIntArrayElements(env, ret, 0);
            body[0] = type;
            (*env)->ReleaseIntArrayElements(env, ret, body, 0);*/
        }
        (*env)->SetIntArrayRegion(env, ret, 0, fieldCount, fill);

        free(pszFieldName);
        free(pnWidth);
        free(pnDecimals);
    }

    DBFClose(hDBF);
    return (ret);
}

JNIEXPORT jobjectArray JNICALL
Java_com_example_position2shp_Util_PositionUtils_getFieldNames(JNIEnv *env, jclass clazz,
                                                               jstring file_path) {
    const char *fileStr = (*env)->GetStringUTFChars(env, file_path, 0);
    DBFHandle hDBF = DBFOpen(fileStr, "rb+");
    (*env)->ReleaseStringUTFChars(env, file_path, fileStr);

    int fieldCount = DBFGetFieldCount(hDBF);

    jobjectArray ret;
    ret = (jobjectArray) (*env)->NewObjectArray(env, fieldCount,
                                                (*env)->FindClass(env, "java/lang/String"),
                                                (*env)->NewStringUTF(env, ""));
    if (fieldCount > 0) {
        int i;
        char *pszFieldName = (int *) malloc(sizeof(char));
        int *pnWidth = (int *) malloc(sizeof(int));
        int *pnDecimals = (int *) malloc(sizeof(int));

        for (i = 0; i < fieldCount; i++) {
            DBFFieldType field = DBFGetFieldInfo(hDBF, i, pszFieldName, pnWidth, pnDecimals);
            (*env)->SetObjectArrayElement(env, ret, i, (*env)->NewStringUTF(env, pszFieldName));

        }

        free(pszFieldName);
        free(pnWidth);
        free(pnDecimals);
    }

    DBFClose(hDBF);
    return (ret);
}

JNIEXPORT jobjectArray JNICALL
Java_com_example_position2shp_Util_PositionUtils_getStringAttributes(JNIEnv *env, jclass clazz,
                                                                     jstring file_path,
                                                                     jint fieldID) {
    const char *fileStr = (*env)->GetStringUTFChars(env, file_path, 0);
    DBFHandle hDBF = DBFOpen(fileStr, "rb+");
    (*env)->ReleaseStringUTFChars(env, file_path, fileStr);

    int recordCount = DBFGetRecordCount(hDBF);
    jobjectArray ret;
    ret = (jobjectArray) (*env)->NewObjectArray(env, recordCount,
                                                (*env)->FindClass(env, "java/lang/String"),
                                                (*env)->NewStringUTF(env, ""));
    if (recordCount > 0) {
        int i;
        for (i = 0; i < recordCount; i++) {
            const char *attribute = DBFReadStringAttribute(hDBF, i, fieldID);
            (*env)->SetObjectArrayElement(env, ret, i, (*env)->NewStringUTF(env, attribute));
        }
    }

    DBFClose(hDBF);
    return (ret);
}

jdoubleArray
Java_com_example_position2shp_Util_PositionUtils_getDoubleAttributes(JNIEnv *env, jclass clazz,
                                                                     jstring file_path,
                                                                     jint fieldID) {
    const char *fileStr = (*env)->GetStringUTFChars(env, file_path, 0);
    DBFHandle hDBF = DBFOpen(fileStr, "rb+");
    (*env)->ReleaseStringUTFChars(env, file_path, fileStr);

    int recordCount = DBFGetRecordCount(hDBF);
    jdoubleArray ret;
    ret = (jobjectArray) (*env)->NewDoubleArray(env, recordCount);

    double *attributes;
    attributes = malloc(recordCount * sizeof(double));
    if (recordCount > 0) {
        int i;
        for (i = 0; i < recordCount; i++) {

            const double attribute = DBFReadDoubleAttribute(hDBF, i, fieldID);
            attributes[i] = attribute;

        }
    }
    (*env)->SetDoubleArrayRegion(env, ret, 0, recordCount, attributes);

    DBFClose(hDBF);
    free(attributes);
    return (ret);
}

jintArray
Java_com_example_position2shp_Util_PositionUtils_getIntAttributes(JNIEnv *env, jclass clazz,
                                                                  jstring file_path, jint fieldID) {
    const char *fileStr = (*env)->GetStringUTFChars(env, file_path, 0);
    DBFHandle hDBF = DBFOpen(fileStr, "rb+");
    (*env)->ReleaseStringUTFChars(env, file_path, fileStr);

    int recordCount = DBFGetRecordCount(hDBF);
    jintArray ret;
    ret = (jobjectArray) (*env)->NewIntArray(env, recordCount);

    int *attributes;
    attributes = malloc(recordCount * sizeof(int));
    if (recordCount > 0) {
        int i;
        for (i = 0; i < recordCount; i++) {
            const int attribute = DBFReadIntegerAttribute(hDBF, i, fieldID);
            attributes[i] = attribute;

        }
    }

    (*env)->SetIntArrayRegion(env, ret, 0, recordCount, attributes);
    DBFClose(hDBF);
    free(attributes);

    return (ret);
}


jint Java_com_example_position2shp_Util_PositionUtils_getRecordCount(JNIEnv *env, jclass clazz,
                                                                     jstring file_path) {
    const char *fileStr = (*env)->GetStringUTFChars(env, file_path, 0);
    DBFHandle hDBF = DBFOpen(fileStr, "rb+");
    (*env)->ReleaseStringUTFChars(env, file_path, fileStr);

    int recordCount = DBFGetRecordCount(hDBF);
    DBFClose(hDBF);

    return recordCount;
}

jint Java_com_example_position2shp_Util_PositionUtils_getShapefileType(JNIEnv *env, jclass clazz,
                                                                       jstring file) {
    // open an existing shapefile and dbf (e.g. /sdcard/foo/bar)
    const char *fileStr = (*env)->GetStringUTFChars(env, file, 0);
    SHPHandle hSHP = SHPOpen(fileStr, "rb+");
    (*env)->ReleaseStringUTFChars(env, file, fileStr);

    int *pnEntities = (int *) malloc(sizeof(int));
    int *pnShapeType = (int *) malloc(sizeof(int));
    double *padfMinBound = {0};
    double *padfMaxBound = {0};

    SHPGetInfo(hSHP, pnEntities, pnShapeType, padfMinBound, padfMaxBound);

    jint shapefileType = 0;
    if (pnShapeType) //Point
        shapefileType = *pnShapeType;

    free(pnEntities);
    free(pnShapeType);
    SHPClose(hSHP);

    return shapefileType;
}

jboolean Java_com_example_position2shp_Util_PositionUtils_updateShapefile(JNIEnv *env, jclass clazz,
                                                                          jstring file,
                                                                          jobject listObject) {
    // open an existing shapefile and dbf (e.g. /sdcard/foo/bar)
    const char *fileStr = (*env)->GetStringUTFChars(env, file, 0);
    SHPHandle hSHP = SHPOpen(fileStr, "rb+");
    DBFHandle hDBF = DBFOpen(fileStr, "rb+");
    (*env)->ReleaseStringUTFChars(env, file, fileStr);

    int *pnEntities = (int *) malloc(sizeof(int));
    int *pnShapeType = (int *) malloc(sizeof(int));
    double *padfMinBound = {0};
    double *padfMaxBound = {0};

    SHPGetInfo(hSHP, pnEntities, pnShapeType, padfMinBound, padfMaxBound);

    if (pnShapeType && *pnShapeType == 1) //Point
        processPointPosition(env, hSHP, hDBF, file, listObject);

    else if (pnShapeType && *pnShapeType == 3) //Line
        processLinePosition(env, hSHP, hDBF, file, listObject);

    else if (pnShapeType && *pnShapeType == 5) //Polygon
        processPolygonPosition(env, hSHP, hDBF, file, listObject);

    free(pnEntities);
    free(pnShapeType);
    SHPClose(hSHP);
    DBFClose(hDBF);
}

jboolean
Java_com_example_position2shp_Util_PositionUtils_createPointShapefile(JNIEnv *env, jclass clazz,
                                                                      jstring file,
                                                                      jobject listObject) {
    // create a shapefile and dbf (e.g. /sdcard/foo/bar)
    const char *fileStr = (*env)->GetStringUTFChars(env, file, 0);
    SHPHandle hSHP = SHPCreate(fileStr, SHPT_POINT);
    DBFHandle hDBF = DBFCreate(fileStr);

    DBFAddField(hDBF, "date", FTString, 150, 0);
    DBFAddField(hDBF, "Attribute1", FTString, 150, 0);

    SHPClose(hSHP);
    DBFClose(hDBF);
}

jboolean
Java_com_example_position2shp_Util_PositionUtils_createLineShapefile(JNIEnv *env, jclass clazz,
                                                                     jstring file,
                                                                     jobject listObject) {
    // create a shapefile and dbf (e.g. /sdcard/foo/bar)
    const char *fileStr = (*env)->GetStringUTFChars(env, file, 0);
    SHPHandle hSHP = SHPCreate(fileStr, SHPT_ARC);
    DBFHandle hDBF = DBFCreate(fileStr);

    DBFAddField(hDBF, "Date", FTString, 150, 0);
    DBFAddField(hDBF, "Attribute1", FTString, 150, 0);

    SHPClose(hSHP);
    DBFClose(hDBF);
}

jboolean
Java_com_example_position2shp_Util_PositionUtils_createPolygonShapefile(JNIEnv *env, jclass clazz,
                                                                        jstring file,
                                                                        jobject listObject) {
    // create a shapefile and dbf (e.g. /sdcard/foo/bar)
    const char *fileStr = (*env)->GetStringUTFChars(env, file, 0);
    SHPHandle hSHP = SHPCreate(fileStr, SHPT_POLYGON);
    DBFHandle hDBF = DBFCreate(fileStr);

    DBFAddField(hDBF, "Date", FTString, 150, 0);
    DBFAddField(hDBF, "Attribute1", FTString, 150, 0);

    SHPClose(hSHP);
    DBFClose(hDBF);
}

void processPointPosition(JNIEnv *env, SHPHandle hSHP, DBFHandle hDBF, jstring file,
                          jobject listObject) {
    jclass classAdapter = (*env)->FindClass(env,
                                            "com/example/position2shp/Util/PositionUtils$DataAdapter");
    jmethodID methodIdSize = (*env)->GetMethodID(env, classAdapter, "size", "()I");
    jmethodID methodIdGet = (*env)->GetMethodID(env, classAdapter, "get",
                                                "(I)Ljava/lang/Object;");    // see: http://docs.oracle.com/javase/6/docs/technotes/guides/jni/spec/types.html#wp16437

    int totalItems = (*env)->CallIntMethod(env, listObject, methodIdSize);

    // Position
    jclass classPosition = (*env)->FindClass(env,
                                             "com/example/position2shp/Util/PositionUtils$Position");
    jmethodID methodIdGetLatitude = (*env)->GetMethodID(env, classPosition, "getLatitude", "()D");
    jmethodID methodIdGetLongitude = (*env)->GetMethodID(env, classPosition, "getLongitude", "()D");
    jmethodID methodIdGetDate = (*env)->GetMethodID(env, classPosition, "getDate",
                                                    "()Ljava/lang/String;");

    // Let us print out those items
    const int recordCount = DBFGetRecordCount(hDBF);
    int i;
    for (i = 0; i < totalItems; i++) {
        jobject position = (*env)->CallObjectMethod(env, listObject, methodIdGet, i);
        jdouble latitude = (*env)->CallDoubleMethod(env, position, methodIdGetLatitude);
        jdouble longitude = (*env)->CallDoubleMethod(env, position, methodIdGetLongitude);
        jstring date = (*env)->CallObjectMethod(env, position, methodIdGetDate);

        writePointShp(env, hSHP, hDBF, i, latitude, longitude, date);

        // free reference and avoid a "JNI local reference table" overflow
        (*env)->DeleteLocalRef(env, date);
        (*env)->DeleteLocalRef(env, position);

    }
}

void
processLinePosition(JNIEnv *env, SHPHandle hSHP, DBFHandle hDBF, jstring file, jobject listObject) {
    jclass classAdapter = (*env)->FindClass(env,
                                            "com/example/position2shp/Util/PositionUtils$DataAdapter");
    jmethodID methodIdSize = (*env)->GetMethodID(env, classAdapter, "size", "()I");
    jmethodID methodIdGet = (*env)->GetMethodID(env, classAdapter, "get",
                                                "(I)Ljava/lang/Object;");    // see: http://docs.oracle.com/javase/6/docs/technotes/guides/jni/spec/types.html#wp16437

    int totalItems = (*env)->CallIntMethod(env, listObject, methodIdSize);

    // Position
    jclass classPosition = (*env)->FindClass(env,
                                             "com/example/position2shp/Util/PositionUtils$Position");
    jmethodID methodIdGetLatitude = (*env)->GetMethodID(env, classPosition, "getLatitude", "()D");
    jmethodID methodIdGetLongitude = (*env)->GetMethodID(env, classPosition, "getLongitude", "()D");
    jmethodID methodIdGetDate = (*env)->GetMethodID(env, classPosition, "getDate",
                                                    "()Ljava/lang/String;");

    // Let us print out those items
    double *latitudes;
    latitudes = malloc(totalItems * sizeof(double));

    double *longitudes;
    longitudes = malloc(totalItems * sizeof(double));

    jstring date = (*env)->NewStringUTF(env, "");
    const int recordCount = DBFGetRecordCount(hDBF);
    int i;
    for (i = 0; i < totalItems; i++) {
        jobject position = (*env)->CallObjectMethod(env, listObject, methodIdGet, i);
        jdouble latitude = (*env)->CallDoubleMethod(env, position, methodIdGetLatitude);
        latitudes[i] = latitude;
        jdouble longitude = (*env)->CallDoubleMethod(env, position, methodIdGetLongitude);
        longitudes[i] = longitude;
        date = (*env)->CallObjectMethod(env, position, methodIdGetDate);

        // free reference and avoid a "JNI local reference table" overflow

        (*env)->DeleteLocalRef(env, position);
    }

    writeLineShp(env, hSHP, hDBF, recordCount, totalItems, latitudes, longitudes, date);

    free(latitudes);
    free(longitudes);
}

void processPolygonPosition(JNIEnv *env, SHPHandle hSHP, DBFHandle hDBF, jstring file,
                            jobject listObject) {
    jclass classAdapter = (*env)->FindClass(env,
                                            "com/example/position2shp/Util/PositionUtils$DataAdapter");
    jmethodID methodIdSize = (*env)->GetMethodID(env, classAdapter, "size", "()I");
    jmethodID methodIdGet = (*env)->GetMethodID(env, classAdapter, "get",
                                                "(I)Ljava/lang/Object;");    // see: http://docs.oracle.com/javase/6/docs/technotes/guides/jni/spec/types.html#wp16437

    int totalItems = (*env)->CallIntMethod(env, listObject, methodIdSize);

    // Position
    jclass classPosition = (*env)->FindClass(env,
                                             "com/example/position2shp/Util/PositionUtils$Position");
    jmethodID methodIdGetLatitude = (*env)->GetMethodID(env, classPosition, "getLatitude", "()D");
    jmethodID methodIdGetLongitude = (*env)->GetMethodID(env, classPosition, "getLongitude", "()D");
    jmethodID methodIdGetDate = (*env)->GetMethodID(env, classPosition, "getDate",
                                                    "()Ljava/lang/String;");

    // Let us print out those items
    double *latitudes;
    latitudes = malloc(totalItems * sizeof(double));

    double *longitudes;
    longitudes = malloc(totalItems * sizeof(double));

    jstring date = (*env)->NewStringUTF(env, "");
    const int recordCount = DBFGetRecordCount(hDBF);
    int i;
    for (i = 0; i < totalItems; i++) {
        jobject position = (*env)->CallObjectMethod(env, listObject, methodIdGet, i);
        jdouble latitude = (*env)->CallDoubleMethod(env, position, methodIdGetLatitude);
        latitudes[i] = latitude;
        jdouble longitude = (*env)->CallDoubleMethod(env, position, methodIdGetLongitude);
        longitudes[i] = longitude;
        date = (*env)->CallObjectMethod(env, position, methodIdGetDate);

        // free reference and avoid a "JNI local reference table" overflow

        (*env)->DeleteLocalRef(env, position);
    }

    writePolygonShp(env, hSHP, hDBF, recordCount, totalItems, latitudes, longitudes, date);

    free(latitudes);
    free(longitudes);
}

int writePointShp(JNIEnv *env, SHPHandle hSHP, DBFHandle hDBF, int index, double latitude,
                  double longitude, jstring date) {
    SHPObject *oSHP = SHPCreateSimpleObject(SHPT_POINT, 1, &longitude, &latitude, NULL);
    SHPWriteObject(hSHP, -1, oSHP);

    const char *nativeDate = (*env)->GetStringUTFChars(env, date, 0);
    DBFWriteStringAttribute(hDBF, index, 0, nativeDate);
    (*env)->ReleaseStringUTFChars(env, date, nativeDate);

    const char *nativeAttribute = "Attribute1";
    DBFWriteStringAttribute(hDBF, index, 1, nativeAttribute);

    SHPDestroyObject(oSHP);

    return 0;
}

int writeLineShp(JNIEnv *env, SHPHandle hSHP, DBFHandle hDBF, int index, int numberVertices,
                 double *latitude, double *longitude, jstring date) {
    SHPObject *oSHP = SHPCreateSimpleObject(SHPT_ARC, numberVertices, longitude, latitude, NULL);
    SHPWriteObject(hSHP, -1, oSHP);

    const char *nativeDate = (*env)->GetStringUTFChars(env, date, 0);
    DBFWriteStringAttribute(hDBF, index, 0, nativeDate);
    (*env)->ReleaseStringUTFChars(env, date, nativeDate);

    const char *nativeAttribute = "Attribute1";
    DBFWriteStringAttribute(hDBF, index, 1, nativeAttribute);

    SHPDestroyObject(oSHP);

    return 0;
}

int writePolygonShp(JNIEnv *env, SHPHandle hSHP, DBFHandle hDBF, int index, int numberVertices,
                    double *latitude, double *longitude, jstring date) {
    SHPObject *oSHP = SHPCreateSimpleObject(SHPT_POLYGON, numberVertices, longitude, latitude,
                                            NULL);
    SHPWriteObject(hSHP, -1, oSHP);

    const char *nativeDate = (*env)->GetStringUTFChars(env, date, 0);
    DBFWriteStringAttribute(hDBF, index, 0, nativeDate);
    (*env)->ReleaseStringUTFChars(env, date, nativeDate);

    const char *nativeAttribute = "Attribute1";
    DBFWriteStringAttribute(hDBF, index, 1, nativeAttribute);

    SHPDestroyObject(oSHP);

    return 0;
}