# libGDX RealSense
Integration for libGDX RealSense (Windows 8+ only).

## Integration into your libGDX project
1) Create a desktop only libGDX project via the [GDX Setup UI](http://libgdx.badlogicgames.com/download.html
2) In your build.gradle file, modify the `project("core:")` section to look as follows
```
project(":core") {
    apply plugin: "java"


    dependencies {
        compile "com.badlogicgames.gdx:gdx:$gdxVersion"
        compile "com.badlogicgames.gdx:gdx-box2d:$gdxVersion"
        compile "com.badlogicgames.gdx:gdx-realsense:1.0.0-SNAPSHOT"
    }
}
```
3) [Import the project](https://github.com/libgdx/libgdx/wiki/Gradle-and-Eclipse) into your prefered IDE.
4) See the [examples](https://github.com/libgdx/gdx-realsense/tree/master/tests/com/badlogic/realsense) on how to call the RealSense SDK in your libGDX application. 
5) Check out the [RealSense SDK API documentation](https://software.intel.com/en-us/realsense/documentation). You can directly translate the examples from there to this project's JNI wrapper!

## Building the native library
1) Install the RealSense SDK, Visual Studio Express 2013, premake 4 and SWIG (e.g. via Chocolatey).
2) Set RS_SDK_ROOT to your RealSense SDK installation directory.
3) run `genbindings.bat` to generate the SWIG wrapper
4) Run `premake4 vs2010` to build the Visual Studio project files.
5) Open the Visual Studio project and build for release. This will place a dll file in src/main/resources which can then be loaded by the Java code.
