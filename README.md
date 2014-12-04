# libGDX RealSense SDK Integration

[![RealSense & libGDX video](http://sht.tl/a3tIbi)](https://www.youtube.com/watch?v=ZybPe86W4ZI&feature=youtu.be)
Click on the image above to view the video acompanying this guide!

[Intel's RealSense SDK](https://software.intel.com/en-us/realsense/) let's you create new immersive experiences by combining smart algorithms and innovative hardware. The SDK provides functionality such as face tracking and analysis, hand and hand gesture detection, speech analysis and synthesis and many more features.

This project allows you to integrate Intel's RealSense SDK into your libGDX application. The integration exposes the full set of C++ APIs of the SDK in a Java-compatible way through a SWIG JNI wrapper. This makes it very easy to translate code samples of the [RealSense SDK API documentation](https://software.intel.com/en-us/realsense/documentation) to Java! 

We have prepared two examples that show you how to [captures camera streams](https://github.com/libgdx/gdx-realsense/blob/master/tests/com/badlogic/realsense/StreamViewer.java) and perform [hand and gesture recognition](https://github.com/libgdx/gdx-realsense/blob/master/tests/com/badlogic/realsense/HandTracking.java).

The rest of this document will walk you through the basic setup and demonstrate how to create a simple RealSense application with libGDX.

## Setting up the RealSense SDK
To use Intel's RealSense, you need to:

1. Get a PC with Windows 8+ 
2. Download an install the [Intel RealSense SDK](https://software.intel.com/en-us/realsense/intel-realsense-sdk-for-windows)
3. Acquire a [compatible 3D camera](https://software.intel.com/en-us/realsense/integrated-camera-and-supported-systems)

## Setting up a libGDX Project with Intel RealSense SDK Integration
This guide assumes you are familiar with creating a standard libGDX project. If not, please head over to our [documentation page](http://libgdx.badlogicgames.com/documentation.html). You need to:

1. [Install the prerequisits](https://github.com/libgdx/libgdx/wiki/Setting-up-your-Development-Environment-%28Eclipse%2C-Intellij-IDEA%2C-NetBeans%29) (JDK, IDE)
2. [Create a libGDX application via the setup UI](https://github.com/libgdx/libgdx/wiki/Project-Setup-Gradle)
3. [Import the project into your IDE](https://github.com/libgdx/libgdx/wiki/Gradle-and-Eclipse)

This guide assumes that you are using Eclipse as your IDE. Once you have created and imported you project into Eclipse, you can add the libGDX RealSense integration to your project.

 * Open the build.gradle file in the root directory of your project
 * Add the dependency `compile "com.badlogicgames.gdx:gdx-realsense:1.0.0"` to the dependencies section of the core project:
```
 project(":core") {
    ...
    dependencies {
        ...
        compile "com.badlogicgames.gdx:gdx-realsense:1.0.0"
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

## Capturing and Displaying Camera Streams
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

Find out more about [Raw Stream Capturing and Processing](https://software.intel.com/sites/landingpage/realsense/camera-sdk/2014gold/documentation/html/). View the [full source code for this example](https://github.com/libgdx/gdx-realsense/blob/master/tests/com/badlogic/realsense/StreamViewer.java)!

## Hand Tracking and Gesture Recognition
Intel's RealSense SDK provides you with a set of sophisticated algorithms to [detect and tracking hands, reconstruct full 3D hand skeletons and recognize a wide range of hand gestures](https://software.intel.com/sites/landingpage/realsense/camera-sdk/2014gold/documentation/html/). All of this functionality is encapsulated in the `PXCHandModule` which is managed by the sense manager.

### Setting up the Hand Module
Setting up the hand module is rather simple:
```java
senseManager = PXCSenseManager.CreateInstance();
... enable streams we want to capture ...
senseManager.EnableHand();
PXCHandModule handModule = senseManager.QueryHand();	
senseManager.Init();
```

In addition to enabling any streams, we also tell the sense manager to enable the hand module (`PXCSenseManager#EnableHand`). We get an instance of the hand module via a call to `PXCSenseManager#QueryHand()`.

Next we need to configure the hand module:

```java
handData = handModule.CreateOutput();
handConfig = handModule.CreateActiveConfiguration();
handConfig.EnableAllGestures();
handConfig.EnableAlert(AlertType.ALERT_HAND_DETECTED);
handConfig.EnableAlert(AlertType.ALERT_HAND_NOT_DETECTED);
handConfig.ApplyChanges();
```

The fist statement creates an instance of `PXCHandData`. Think of it as an analog to `StreamTexture` but for hand data (skeleton, gestures, etc.). Next we tell the SDK to enable the detection of all gestures. We also want to receive alerts when a hand is detected and when a hand is no longer detected. We apply the configuration via a call to `PXCHandModule#ApplyChanges()`

All of this configuration usually goes into your `ApplicationListener#Create()` function.

### Capturing Hand Data
Hand data is derived from the current frame, just as stream data. The basic mechanism works like this:

```java
pxcStatus status = senseManager.AcquireFrame();
if(status == pxcStatus.PXC_STATUS_NO_ERROR) {
 ... update your StreamTextures
 handData.Update();
 senseManager.ReleaseFrame();
}
```

We acquire the current frame (`PXCSenseManager#AcquireFrame()`), based on which we instruct the `PXCHandData` to update itself (`PXCHandData#Update()`). This will update the number of hands recognized, skeletal data and detect gestures.

### Processing Joint Data
The hand module recognizes 22 joints for each hand it detects in the video stream.

![joints](https://software.intel.com/sites/landingpage/realsense/camera-sdk/2014gold/documentation/html/manuals_clip0044.png)

Each joint has a unique id (`JointType`) which maps to an integer value (0-21). A joint contains data such as it's id, it's position in pixel coordinates (relative to the depth image coordinate space), it's position in world space, rotation and so on. For this guide, we are only interested in displaying the pixal space position of each joint. Here's how we can extract this data:

```java
renderer.getProjectionMatrix().setToOrtho2D(0, 0, 640, 480);
renderer.begin(ShapeType.Filled);
int numHands = handData.QueryNumberOfHands();
for(int i = 0; i < numHands; i++) {
   IHand hand = handData.QueryHandData(AccessOrderType.ACCESS_ORDER_BY_TIME, i);
   for(int j = 0; j < PXCHandData.NUMBER_OF_JOINTS; j++) {
      JointData jointData = new JointData();
      hand.QueryTrackedJoint(PXCHandData.JointType.swigToEnum(j), jointData);
      renderer.circle(jointData.getPositionImage().getX(), 480 - jointData.getPositionImage().getY(), 2);
   }
}
renderer.end();
```

We use libGDX's `ShapeRenderer` class to draw a filled circle at each joint's position. The first thing we need to know is the number of hands that was detected (`PXCHandData#QueryNumberOfHands`). We then loop over all hands and extract the joint data. A hand is represented by an instance of `IHand`, which we acquire via a call to `PXCHandData#QueryHandData()`. We provide a sort order and the index of the hand. Next, we iterate through all joints (0-`PXCHandData.NUMBER_OF_JOINTS`) and fetch their data (`JointData`) via a call to `IHand#QueryTrackedJoint`. Finally, we render a circle at the image position of the joint.

### Processing Alerts
We setup the hand module to inform us if a new hand is detected or a previously detected hand disappeared. To query these alerts, we do the following:

```java
for(int i = 0; i < handData.QueryFiredAlertsNumber(); i++) {
   PXCHandData.AlertData alertData = new PXCHandData.AlertData();
   handData.QueryFiredAlertData(i, alertData);
   if(alertData.getLabel() == AlertType.ALERT_HAND_DETECTED) {
      Gdx.app.log("HandTracking", "Detected hand, id: " + alertData.GetHandId());
   } else if(alertData.getLabel() == AlertType.ALERT_HAND_NOT_DETECTED) {
      Gdx.app.log("HandTracking", "Hand lost, id: " + alertData.GetHandId());
   }
}
```
We loop over all alerts (`PXCHandData#QueryFiredAlertsNumber()`). For each alert, we fetch the `PXCHandData.AlertData` via a call to `PXCHandData.QueryFiredAlertData()`. Next we check of which type the alert is (`AlertType`). We setup the hand module to tell us about new hands (`AlertType.ALERT_HAND_DETECTED`), and previously detected hands that disappeared (`AlertType.ALERT_HAND_NOT_DETECTED`). Every hand that gets detected is assigned a unique id (`AlertData#GetHandId`).

### Detecting Gestures
Finally, we can check if a gesture was detected for a specific hand:

```java
for(int i = 0; i < handData.QueryFiredGesturesNumber(); i++) {
   PXCHandData.GestureData gestureData = new PXCHandData.GestureData();
   handData.QueryFiredGestureData(i, gestureData);
   Gdx.app.log("HandTracking", "Gesture for hand id: " + gestureData.GetHandId() + ": " + gestureData.GetCName());
}
```

We loop over all gestures detected for this frame (`PXCHandData#QueryFiredGesturesNumber()`). To query the gesture data, we construct a `PXCHandData.GestureData()` which we fill via a call to `PXCHandData#QueryFiredGestureData()`. We can then inspect the name of the detected gesture (`GestureData#GetCName()`) and the id of the hand for which it was detected (`GestureData#getHandId()`).

Here's the [full list of supported gestures](https://software.intel.com/sites/landingpage/realsense/camera-sdk/2014gold/documentation/html/). View the [full source code for this example](https://github.com/libgdx/gdx-realsense/blob/master/tests/com/badlogic/realsense/HandTracking.java)!

## Where to go from here?
Intel RealSense SDK comes with a wide range of documentation. We suggest you study the following resources:

* [Intel RealSense Documentation Page](https://software.intel.com/en-us/realsense/documentation)
* [Intel RealSense Reference Manual](https://software.intel.com/sites/landingpage/realsense/camera-sdk/2014gold/documentation/html/)

## Building the native library
You can savely ignore this section :)
1. Install the RealSense SDK, Visual Studio Express 2013, premake 4 and SWIG (e.g. via Chocolatey).
2. Set RS_SDK_ROOT to your RealSense SDK installation directory.
3. run `genbindings.bat` to generate the SWIG wrapper
4. Run `premake4 vs2010` to build the Visual Studio project files.
5. Open the Visual Studio project and build for release. This will place a dll file in src/main/resources which can then be loaded by the Java code.
