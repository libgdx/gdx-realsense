/* File: example.i */
%module realsense
%include "typemaps.i"
%include "stdint.i"

%typemap(jni) unsigned char *NIOBUFFER "jobject"  
%typemap(jtype) unsigned char *NIOBUFFER "java.nio.ByteBuffer"  
%typemap(jstype) unsigned char *NIOBUFFER "java.nio.ByteBuffer"  
%typemap(javain,
  pre="  assert $javainput.isDirect() : \"Buffer must be allocated direct.\";") unsigned char *NIOBUFFER "$javainput"
%typemap(javaout) unsigned char *NIOBUFFER {  
  return $jnicall;  
}  
%typemap(in) unsigned char *NIOBUFFER {  
  $1 = (unsigned char *) JCALL1(GetDirectBufferAddress, jenv, $input); 
  if ($1 == NULL) {  
    SWIG_JavaThrowException(jenv, SWIG_JavaRuntimeException, "Unable to get address of a java.nio.ByteBuffer direct byte buffer. Buffer must be a direct buffer and not a non-direct buffer.");  
  }  
}  
%typemap(memberin) unsigned char *NIOBUFFER {  
  if ($input) {  
    $1 = $input;  
  } else {  
    $1 = 0;  
  }  
}  
%typemap(freearg) unsigned char *NIOBUFFER ""  
%apply unsigned char *NIOBUFFER { unsigned char *planeDataOutput};

%{
#include "wchar.h"

int wstrlen(const wchar_t *s)
{
	int cnt = 0;
	while(*s++)
		cnt++;
	return cnt;
}
%}

%typemap(jni) wchar_t *                                         "jstring"
%typemap(jtype) wchar_t *                                         "String"
%typemap(jstype) wchar_t *                                         "String"


/* char * - treat as String */
%typemap(in) wchar_t * { 
  $1 = 0;
  if ($input) {
    $1 = ($1_ltype)JCALL2(GetStringChars, jenv, $input, 0);
    if (!$1) return $null;
  }
}

%typemap(directorin, descriptor="Ljava/lang/String;") wchar_t * { 
  $input = 0;
  if ($1) {
    $input = JCALL1(NewString, jenv, $1);
    if (!$input) return $null;
  }
}
%typemap(freearg) wchar_t * { if ($1) JCALL2(ReleaseStringChars, jenv, $input, (const jchar *) $1); }
%typemap(out) wchar_t * { if($1) $result = JCALL2(NewString, jenv, (const jchar *) $1, wstrlen ($1)); }
%typemap(javadirectorin) wchar_t * "$jniinput"
%typemap(javadirectorout) wchar_t * "$javacall"

%typemap(throws) wchar_t * {
  SWIG_JavaThrowException(jenv, SWIG_JavaRuntimeException, $1);
  return $null;
}
%typemap(javain) wchar_t * "$javainput"
%typemap(javaout) wchar_t * {
    return $jnicall;
  }

%{
#include <pxc3dscan.h>
#include <pxc3dseg.h>
#include <pxcaddref.h>
#include <pxcaudio.h>
#include <pxcaudiosource.h>
#include <pxcbase.h>
#include <pxccapture.h>
#include <pxccapturemanager.h>
#include <pxcdefs.h>
#include <pxcemotion.h>
#include <pxcfaceconfiguration.h>
#include <pxcfacedata.h>
#include <pxcfacemodule.h>
#include <pxchandconfiguration.h>
#include <pxchanddata.h>
#include <pxchandmodule.h>
#include <pxcimage.h>
#include <pxcmetadata.h>
#include <pxcpowerstate.h>
#include <pxcprojection.h>
#include <pxcsensemanager.h>
#include <pxcsession.h>
#include <pxcspeechrecognition.h>
#include <pxcspeechsynthesis.h>
#include <pxcstatus.h>
#include <pxcsyncpoint.h>
#include <pxctouchlesscontroller.h>
#include <pxctracker.h>
#include <pxcversion.h>
#include <pxcvideomodule.h>
%}

%extend PXCHandData {
	IHand* QueryHandData(AccessOrderType accessOrder, pxcI32 index) {
		PXCHandData::IHand* handData = 0;
		self->QueryHandData(accessOrder, index, handData);
		return handData;
	}
}

%extend PXCHandData::GestureData {
	wchar_t* GetCName() {
		return self->name;
	}
}

%ignore operatordelete;

#define __inline
#define __stdcall
#define __declspec(s)

%nodefaultdtor PXCBase;
%nodefaultdtor PXC3DScan;
%nodefaultdtor PXC3DSeg;
%nodefaultdtor PXCAddRef;
%nodefaultdtor PXCAudio;
%nodefaultdtor PXCAudioSource;
%nodefaultdtor PXCCapture;
%nodefaultdtor PXCCapture::Device;
%nodefaultdtor PXCCaptureManager;
%nodefaultdtor PXCEmotion;
%nodefaultdtor PXCFaceConfiguration;
%nodefaultdtor PXCFaceData;
%nodefaultdtor PXCFaceModule;
%nodefaultdtor PXCHandConfiguration;
%nodefaultdtor PXCHandData;
%nodefaultdtor PXCHandModule;
%nodefaultdtor PXCImage;
%nodefaultdtor PXCMetadata;
%nodefaultdtor PXCPowerState;
%nodefaultdtor PXCProjection;
%nodefaultdtor PXCSenseManager;
%nodefaultdtor PXCSession;
%nodefaultdtor PXCSpeechRecognition;
%nodefaultdtor PXCSpeechSynthesis;
%nodefaultdtor PXCSyncPoint;
%nodefaultdtor PXCTouchlessController;
%nodefaultdtor PXCTracker;
%nodefaultdtor PXCVideoModule;

%include "native/include/pxcdefs.h"
%include "native/include/pxcbase.h"
%include "native/include/pxc3dscan.h"
%include "native/include/pxc3dseg.h"
%include "native/include/pxcaddref.h"
%include "native/include/pxcaudio.h"
%include "native/include/pxcaudiosource.h"
%include "native/include/pxccapture.h"
%include "native/include/pxccapturemanager.h"
%include "native/include/pxcemotion.h"
%include "native/include/pxcfaceconfiguration.h"
%include "native/include/pxcfacedata.h"
%include "native/include/pxcfacemodule.h"
%include "native/include/pxchandconfiguration.h"
%include "native/include/pxchanddata.h"
%include "native/include/pxchandmodule.h"
%include "native/include/pxcimage.h"
%include "native/include/pxcmetadata.h"
%include "native/include/pxcpowerstate.h"
%include "native/include/pxcprojection.h"
%include "native/include/pxcsensemanager.h"
%include "native/include/pxcsession.h"
%include "native/include/pxcspeechrecognition.h"
%include "native/include/pxcspeechsynthesis.h"
%include "native/include/pxcstatus.h"
%include "native/include/pxcsyncpoint.h"
%include "native/include/pxctouchlesscontroller.h"
%include "native/include/pxctracker.h"
%include "native/include/pxcversion.h"
%include "native/include/pxcvideomodule.h"

%pragma(java) jniclasscode=%{
	static {
		new com.badlogic.gdx.utils.SharedLibraryLoader().load("gdx-realsense");
	}
%}