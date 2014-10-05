RS_SDK_ROOT = os.getenv("RS_SDK_ROOT")
if not RS_SDK_ROOT then
	printf("ERROR: Environment variable RS_SDK_ROOT is not set.")
	printf("Set it to something like: C:\\Program Files (x86)\\Intel\\RSSDK")
	os.exit()
end

BUILD_DIR = "build"
if _ACTION == "clean" then
	os.rmdir(BUILD_DIR)
end

solution "fbx-conv"
	configurations { "Debug", "Release" }
	location (BUILD_DIR .. "/" .. _ACTION)
	
project "fbx-conv"
	--- GENERAL STUFF FOR ALL PLATFORMS --------------------------------
	kind "ConsoleApp"
	language "C++"
	location (BUILD_DIR .. "/" .. _ACTION)
	files {
		"./src/**.c*",
		"./src/**.h",
	}
	includedirs {
		(FBX_SDK_ROOT .. "/include"),
		"./libs/libpng/include",
		"./libs/zlib/include",
	}
	defines {
		"FBXSDK_NEW_API",
	}
	--- debugdir "."

	configuration "Debug"
		defines {
			"DEBUG",
		}
		flags { "Symbols" }
	
	configuration "Release"
		defines {
			"NDEBUG",
		}
		flags { "Optimize" }

	--- VISUAL STUDIO --------------------------------------------------
	configuration "vs*"
		flags {
			"NoPCH",
			"NoMinimalRebuild"
		}
		buildoptions { "/MP" }
		defines {
			"_CRT_SECURE_NO_WARNINGS",
			"_CRT_NONSTDC_NO_WARNINGS"
		}
		libdirs {
			(FBX_SDK_ROOT .. "/lib/vs2010/x86"),
			"./libs/libpng/lib/windows/x86",
			"./libs/zlib/lib/windows/x86",
		}
		links {
			"libpng14",
			"zlib",
			"libfbxsdk-md",
		}
		
	configuration { "vs*", "Debug" }
		libdirs {
			(FBX_SDK_ROOT .. "/lib/vs2010/x86/debug"),
		}
		
	configuration { "vs*", "Release" }
		libdirs {
			(FBX_SDK_ROOT .. "/lib/vs2010/x86/release"),
		}

	--- LINUX ----------------------------------------------------------
	configuration { "linux" }
		kind "ConsoleApp"
		buildoptions { "-Wall" }
		-- TODO: while using x64 will likely be fine for most people nowadays,
		--       we still need to make this configurable
		libdirs {
			"./libs/libpng/lib/linux/x64",
			"./libs/zlib/lib/linux/x64",
		}
		links {
			"png",
			"z",
			"pthread",
			"fbxsdk",
			"dl",
		}

	configuration { "linux", "Debug" }
		libdirs {
			(FBX_SDK_ROOT .. "/lib/gcc4/x64/debug"),
		}
		
	configuration { "linux", "Release" }
		libdirs {
			(FBX_SDK_ROOT .. "/lib/gcc4/x64/release"),
		}

	--- MAC ------------------------------------------------------------
	configuration { "macosx" }
		kind "ConsoleApp"
		buildoptions { "-Wall" }
		libdirs {
			(FBX_SDK_ROOT .. "/lib/gcc4/ub"),
			"./libs/libpng/lib/macosx",
			"./libs/zlib/lib/macosx",
		}
		links {
			"png",
			"z",
			"CoreFoundation.framework",
			"fbxsdk",
		}

	configuration { "macosx", "Debug" }
		libdirs {
			(FBX_SDK_ROOT .. "/lib/gcc4/ub/debug"),
		}
		
	configuration { "macosx", "Release" }
		libdirs {
			(FBX_SDK_ROOT .. "/lib/gcc4/ub/release"),
		}