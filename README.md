# libGDX RealSense SDK Integration
Intel's RealSense SDK let's you create new immersive experiences by combining smart algorithms and innovative hardware. The SDK provides functionality such as face tracking and analysis, hand and hand gesture detection, speech analysis and synthesis and many more features.

This project allows you to integrate Intel's RealSense SDK into your libGDX application. The integration exposes the full set of C++ APIs of the SDK in a Java-compatible way through a SWIG JNI wrapper. This makes it very easy to translate code samples of the [RealSense SDK API documentation](https://software.intel.com/en-us/realsense/documentation) to Java!

The rest of this document will walk you through the basic setup and demonstrate how to create a simple RealSense application with libGDX.

## Setting up the RealSense SDK
To use Intel's RealSense, you need to:

1. Have a Windows 8+ installation
2. Download an install the Intel RealSense SDK
3. Acquire a compatible 3D camera

That's it!

## Setting up a libGDX project with Intel RealSense SDK Integration
This guide assumes you are familiar with creating a standard libGDX project. If not, please head over to our [documentation page](http://libgdx.badlogicgames.com/documentation.html). You need to:

1. [Install the prerequisits](https://github.com/libgdx/libgdx/wiki/Setting-up-your-Development-Environment-%28Eclipse%2C-Intellij-IDEA%2C-NetBeans%29) (JDK, IDE)
2. [Create a libGDX application via the setup UI](https://github.com/libgdx/libgdx/wiki/Project-Setup-Gradle)
3. [Import the project into your IDE](https://github.com/libgdx/libgdx/wiki/Gradle-and-Eclipse)

This guide assumes that you are using Eclipse as your IDE. Once you have created and imported you project into Eclipse, you can add the libGDX RealSense integration to your project.

 * Open the build.gradle file in the root directory of your project
 * Add the dependency `compile "com.badlogicgames.gdx:gdx-realsense:1.0.0-SNAPSHOT"` to the dependencies section of the core project:
```
 project(":core") {
    ...
    dependencies {
        ...
        compile "com.badlogicgames.gdx:gdx-realsense:1.0.0-SNAPSHOT"
    }
}
```
 * Right click the core project in Eclipse, and click on `Gradle -> Refresh Dependencies`

This will add the `gdx-realsense.jar` to your project, which contains everything you need to use the Intel RealSense SDK!

## The `PXCSenseManager`
All operations of the SDK can be accessed in one way or another through the PXCSenseManager class. It is responsible for managing things like capturing streams from the camera, executing computer vision algorithms for face or hand recognition and so on. The first step is therefore to create and initialize this manager:

```java
PXCSenseManager senseManager = PXCSenseManager.CreateInstance();
... configuration of streams and modules ...
senseManager.Init();
```

You should use one `PXCSenseManager` throughout your entire application. Before you call the `PXCSenseManager#Init()` method, you can configure streams and modules (algorithms).

## Capturing and displaying camera streams
The Intel RealSense SDK allows you to directly access the different streams of your camera. A stream can be an RGB image captured by the color sensor or a depth image captured by the depth sensor of the camera. The first thing to do to access these streams is to tell the `PXCSenseManager` which streams you want to use. We do this before we initialize the manager:

```java
senseManager = PXCSenseManager.CreateInstance();
senseManager.EnableStream(StreamType.STREAM_TYPE_COLOR);
senseManager.EnableStream(StreamType.STREAM_TYPE_DEPTH);
senseManager.Init();
```

This will tell the manager to enable both the color and depth stream of the camera. Next, we want the image data to be accessible to us as OpenGL textures. The libGDX RealSense integration provides the `StreamTexture` class for this purpose:

```java
colorTexture = new StreamTexture(senseManager, StreamType.STREAM_TYPE_COLOR);
depthTexture = new StreamTexture(senseManager, StreamType.STREAM_TYPE_DEPTH);
```

The above code usually goes into your `ApplicationListener#create()` method.

Next, we want to capture the current stream data and display it on screen. We can do this in our `ApplicationListener#render()` method:

```java
pxcStatus status = senseManager.AcquireFrame();
if(status == pxcStatus.PXC_STATUS_NO_ERROR) {
 Sample sample = senseManager.QuerySample();
 colorTexture.updateFromFrame(sample);
 depthTexture.updateFromFrame(sample);
 senseManager.ReleaseFrame();
}
```

We first need to tell the sense manager to acquire the current frame. If there was no error, we can query for the sample (streams data) of the current frame, and update our `StreamTexture`s via calls to `StreamTexture#updateFromFrame`. This will take the image data from the stream and upload it to the GPU as an OpenGL texture. Once we are done with updating the textures, we tell the sense manager to release the current frame.

Now that our textures are updated, we can draw them to the screen, e.g. via libGDX's `SpriteBatch`:

```java
batch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
batch.begin();
batch.draw(colorTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
batch.draw(depthTexture, 0, 0, 320, 240);
batch.end();
```
You can use `StreamTexture`s like any other `Texture`!

## Building the native library
1. Install the RealSense SDK, Visual Studio Express 2013, premake 4 and SWIG (e.g. via Chocolatey).
2. Set RS_SDK_ROOT to your RealSense SDK installation directory.
3. run `genbindings.bat` to generate the SWIG wrapper
4. Run `premake4 vs2010` to build the Visual Studio project files.
5. Open the Visual Studio project and build for release. This will place a dll file in src/main/resources which can then be loaded by the Java code.
