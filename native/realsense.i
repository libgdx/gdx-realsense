/* File: example.i */
%module realsense
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


%include "native/include/pxcversion.h"