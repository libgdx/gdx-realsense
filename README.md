# libGDX RealSense
Integration for libGDX RealSense (Windows 8+ only).

## Building
Install the RealSense SDK, Visual Studio Express 2013, premake 4 and SWIG (e.g. via Chocolatey).

Set RS_SDK_ROOT to your RealSense SDK installation directory.

Run premake4 to build the Visual Studio project files.

Open the Visual Studio project and build for release. This will place a dll file in src/main/resources which can then be loaded by the Java code.

To build