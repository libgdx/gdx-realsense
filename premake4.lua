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

solution "gdx-realsense"
	configurations { "Debug", "Release" }
	location (BUILD_DIR .. "/" .. _ACTION)
	
project "gdx-realsense"
	--- GENERAL STUFF FOR ALL PLATFORMS --------------------------------
	kind "SharedLib"
	language "C++"
	location (BUILD_DIR .. "/" .. _ACTION)
	files {
		"./native/**.c*",
		"./native/**.h",
	}
	includedirs {
		(RS_SDK_ROOT .. "/include")
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
			(RS_SDK_ROOT .. "/lib/x64"),
		}
		links {
			"libpcx",
		}
		