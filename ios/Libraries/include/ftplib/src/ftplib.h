/***************************************************************************/
/*									   */
/* ftplib.h - header file for callable ftp access routines                 */
/* Copyright (C) 1996-2001, 2013, 2016 Thomas Pfau, tfpfau@gmail.com	   */
/*	1407 Thomas Ave, North Brunswick, NJ, 08902			   */
/*									   */
/* This library is free software.  You can redistribute it and/or	   */
/* modify it under the terms of the Artistic License 2.0.		   */
/* 									   */
/* This library is distributed in the hope that it will be useful,	   */
/* but WITHOUT ANY WARRANTY; without even the implied warranty of	   */
/* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the	   */
/* Artistic License 2.0 for more details.				   */
/* 									   */
/* See the file LICENSE or 						   */
/* http://www.perlfoundation.org/artistic_license_2_0			   */
/*									   */
/***************************************************************************/

#if !defined(__FTPLIB_H)
#define __FTPLIB_H

#if defined(__unix__) || defined(VMS) || defined(__APPLE__)
#define GLOBALDEF
#define GLOBALREF extern
#elif defined(_WIN32)
#if defined BUILDING_LIBRARY
#define GLOBALDEF __declspec(dllexport)
#define GLOBALREF __declspec(dllexport)
#else
#define GLOBALREF __declspec(dllimport)
#endif
#endif

#include <limits.h>

#if defined(_MSC_VER) && _MSC_VER < 1900
/* MSVC headers define int32_t as int, but PRIx32 as "lx" instead of "x".
 * This triggers format warnings, so fix it up here. */
#undef PRId32
#undef PRIdLEAST32
#undef PRIdFAST32
#undef PRIi32
#undef PRIiLEAST32
#undef PRIiFAST32
#undef PRIo32
#undef PRIoLEAST32
#undef PRIoFAST32
#undef PRIu32
#undef PRIuLEAST32
#undef PRIuFAST32
#undef PRIx32
#undef PRIxLEAST32
#undef PRIxFAST32
#undef PRIX32
#undef PRIXLEAST32
#undef PRIXFAST32

#undef SCNd32
#undef SCNdLEAST32
#undef SCNdFAST32
#undef SCNi32
#undef SCNiLEAST32
#undef SCNiFAST32
#undef SCNo32
#undef SCNoLEAST32
#undef SCNoFAST32
#undef SCNu32
#undef SCNuLEAST32
#undef SCNuFAST32
#undef SCNx32
#undef SCNxLEAST32
#undef SCNxFAST32

#define PRId32 "d"
#define PRIdLEAST32 "d"
#define PRIdFAST32 "d"
#define PRIi32 "i"
#define PRIiLEAST32 "i"
#define PRIiFAST32 "i"
#define PRIo32 "o"
#define PRIoLEAST32 "o"
#define PRIoFAST32 "o"
#define PRIu32 "u"
#define PRIuLEAST32 "u"
#define PRIuFAST32 "u"
#define PRIx32 "x"
#define PRIxLEAST32 "x"
#define PRIxFAST32 "x"
#define PRIX32 "X"
#define PRIXLEAST32 "X"
#define PRIXFAST32 "X"

#define SCNd32 "d"
#define SCNdLEAST32 "d"
#define SCNdFAST32 "d"
#define SCNi32 "i"
#define SCNiLEAST32 "i"
#define SCNiFAST32 "i"
#define SCNo32 "o"
#define SCNoLEAST32 "o"
#define SCNoFAST32 "o"
#define SCNu32 "u"
#define SCNuLEAST32 "u"
#define SCNuFAST32 "u"
#define SCNx32 "x"
#define SCNxLEAST32 "x"
#define SCNxFAST32 "x"

#endif /* __CLANG_INTTYPES_H */

/* FtpAccess() type codes */
#define FTPLIB_DIR 1
#define FTPLIB_DIR_VERBOSE 2
#define FTPLIB_FILE_READ 3
#define FTPLIB_FILE_WRITE 4

/* FtpAccess() mode codes */
#define FTPLIB_ASCII 'A'
#define FTPLIB_IMAGE 'I'
#define FTPLIB_TEXT FTPLIB_ASCII
#define FTPLIB_BINARY FTPLIB_IMAGE

/* connection modes */
#define FTPLIB_PASSIVE 1
#define FTPLIB_PORT 2

/* connection option names */
#define FTPLIB_CONNMODE 1
#define FTPLIB_CALLBACK 2
#define FTPLIB_IDLETIME 3
#define FTPLIB_CALLBACKARG 4
#define FTPLIB_CALLBACKBYTES 5

#ifdef __cplusplus
extern "C" {
#endif

#if defined(__UINT64_MAX)
typedef uint64_t fsz_t;
#else
typedef uint32_t fsz_t;
#endif

typedef struct NetBuf netbuf;
typedef int (*FtpCallback)(netbuf *nControl, fsz_t xfered, void *arg);

typedef struct FtpCallbackOptions {
    FtpCallback cbFunc;		/* function to call */
    void *cbArg;		/* argument to pass to function */
    unsigned int bytesXferred;	/* callback if this number of bytes transferred */
    unsigned int idleTime;	/* callback if this many milliseconds have elapsed */
} FtpCallbackOptions;

GLOBALREF int ftplib_debug;
GLOBALREF void FtpInit(void);
GLOBALREF char *FtpLastResponse(netbuf *nControl);
GLOBALREF int FtpConnect(const char *host, netbuf **nControl);
GLOBALREF int FtpOptions(int opt, long val, netbuf *nControl);
GLOBALREF int FtpSetCallback(const FtpCallbackOptions *opt, netbuf *nControl);
GLOBALREF int FtpClearCallback(netbuf *nControl);
GLOBALREF int FtpLogin(const char *user, const char *pass, netbuf *nControl);
GLOBALREF int FtpAccess(const char *path, int typ, int mode, netbuf *nControl, netbuf **nData);
GLOBALREF int FtpRead(void *buf, int max, netbuf *nData);
GLOBALREF int FtpWrite(const void *buf, int len, netbuf *nData);
GLOBALREF int FtpClose(netbuf *nData);
GLOBALREF int FtpSite(const char *cmd, netbuf *nControl);
GLOBALREF int FtpSysType(char *buf, int max, netbuf *nControl);
GLOBALREF int FtpMkdir(const char *path, netbuf *nControl);
GLOBALREF int FtpChdir(const char *path, netbuf *nControl);
GLOBALREF int FtpCDUp(netbuf *nControl);
GLOBALREF int FtpRmdir(const char *path, netbuf *nControl);
GLOBALREF int FtpPwd(char *path, int max, netbuf *nControl);
GLOBALREF int FtpNlst(const char *output, const char *path, netbuf *nControl);
GLOBALREF int FtpDir(const char *output, const char *path, netbuf *nControl);
GLOBALREF int FtpSize(const char *path, unsigned int *size, char mode, netbuf *nControl);
#if defined(__UINT64_MAX)
GLOBALREF int FtpSizeLong(const char *path, fsz_t *size, char mode, netbuf *nControl);
#endif
GLOBALREF int FtpModDate(const char *path, char *dt, int max, netbuf *nControl);
GLOBALREF int FtpGet(const char *output, const char *path, char mode,
	netbuf *nControl);
GLOBALREF int FtpPut(const char *input, const char *path, char mode,
	netbuf *nControl);
GLOBALREF int FtpRename(const char *src, const char *dst, netbuf *nControl);
GLOBALREF int FtpDelete(const char *fnm, netbuf *nControl);
GLOBALREF void FtpQuit(netbuf *nControl);

#ifdef __cplusplus
};
#endif

#endif /* __FTPLIB_H */
