.PHONY: all test tests j2me java certs app clean jasmin aot shumway config-build package
BASIC_SRCS=$(shell find . -maxdepth 2 -name "*.ts" -not -path "./bld/*") config.ts
JIT_SRCS=$(shell find jit -name "*.ts" -not -path "./bld/*")
SHUMWAY_SRCS=$(shell find shumway -name "*.ts")
RELEASE ?= 0
PROFILE ?= 0
PROFILE_FORMAT ?= PLAIN
BENCHMARK ?= 0
CONSOLE ?= 1
JAVA_VER ?= 6
TESTS ?= 1

# The directory into which the *app* target should copy the files.
PACKAGE_DIR ?= output
export PACKAGE_DIR

# Whether or not to print certain verbose messages during the build process.
# We use this to keep the build log on Travis below its 10K line display limit.
VERBOSE ?= 0
export VERBOSE

# An extra configuration script to load.  Use this to configure the project
# to run a particular midlet.  By default, it runs the test midlet RunTestsMIDlet.
CONFIG ?= config/runtests.js
export CONFIG

# Whether or not to package test files like tests.jar and support scripts.
# Set this to 1 if running tests on a device or building an app for a midlet
# in the tests/ subdirectory, like Asteroids.
PACKAGE_TESTS ?= 0
export PACKAGE_TESTS

# If we're going to package tests, we need to make sure they've been made.
ifeq ($(PACKAGE_TESTS),1)
  TESTS_JAR = tests/tests.jar
endif

NAME ?= PluotSorbet
MIDLET_NAME ?= midlet
DESCRIPTION ?= a J2ME-compatible virtual machine written in JavaScript
ORIGIN ?= app://pluotsorbet.mozilla.org
VERSION ?= $(shell date +%s)

ICON_128 ?= img/default-icon-128.png
ICON_512 ?= img/default-icon-512.png

# Sensor support
JSR_256 ?= 1
export JSR_256

# Bluetooth support
JSR_082 ?= 1
export JSR_082

# Location service support
JSR_179 ?= 1
export JSR_179

# Content handler support
CONTENT_HANDLER_FILTER ?= '["image/*", "video/*", "audio/*"]'
export CONTENT_HANDLER_FILTER

ifeq ($(PROFILE),0)
# J2ME_OPTIMIZATIONS doesnt exist in google's closure compiler, mykmelez's fork of closure compiler is gone
#  J2ME_JS_OPTIMIZATION_LEVEL = J2ME_OPTIMIZATIONS
J2ME_JS_OPTIMIZATION_LEVEL = SIMPLE
else
  # Closure optimization level J2ME_OPTIMIZATIONS breaks the profiler somehow,
  # so we revert to level SIMPLE if the profiler is enabled.
  J2ME_JS_OPTIMIZATION_LEVEL = SIMPLE

  # Add dependency on shumway when the profiler is enabled.
  PROFILE_DEP = shumway
endif

# Closure is really chatty, so we shush it by default to reduce log lines
# for Travis.
ifeq ($(VERBOSE),1)
  CLOSURE_WARNING_LEVEL = VERBOSE
else
  CLOSURE_WARNING_LEVEL = QUIET
endif

ifeq ($(RELEASE),1)
	CLOSURE_FLAGS = 
else
	CLOSURE_FLAGS = --formatting PRETTY_PRINT
endif

MAIN_JS_SRCS = \
  polyfill/canvas-toblob.js \
  polyfill/fromcodepoint.js \
  polyfill/codepointat.js \
  polyfill/map.js \
  polyfill/contains.js \
  polyfill/find.js \
  polyfill/findIndex.js \
  polyfill/fround.js \
  blackBox.js \
  timer.js \
  util.js \
  native.js \
  string.js \
  libs/load.js \
  libs/zipfile.js \
  libs/jarstore.js \
  libs/long.js \
  libs/encoding.js \
  libs/fs.js \
  libs/fs-init.js \
  libs/forge/util.js \
  libs/forge/md5.js \
  libs/jsbn/jsbn.js \
  libs/jsbn/jsbn2.js \
  libs/contacts.js \
  libs/pipe.js \
  libs/contact2vcard.js \
  libs/emoji.js \
  libs/FileSaver/FileSaver.js \
  libs/webaudio-tinysynth.js \
  midp/midp.js \
  midp/frameanimator.js \
  midp/fs.js \
  midp/crypto.js \
  midp/gfx.js \
  midp/text_editor.js \
  midp/localmsg.js \
  midp/socket.js \
  midp/sms.js \
  midp/codec.js \
  midp/pim.js \
  midp/device_control.js \
  midp/background.js \
  midp/media.js \
  midp/content.js \
  game-ui.js \
  $(NULL)

ifeq ($(JSR_179),1)
	MAIN_JS_SRCS += midp/location.js
endif

ifeq ($(JSR_256),1)
	MAIN_JS_SRCS += midp/sensor.js
endif

ifeq ($(BENCHMARK),1)
	MAIN_JS_SRCS += benchmark.js libs/ttest.js
endif

ifeq ($(CONSOLE),1)
	MAIN_JS_SRCS += libs/console.js
endif

MAIN_JS_SRCS += keymap.js

# Add main.js last, as it depends on some of the other scripts.
MAIN_JS_SRCS += main.js

# Create a checksum file to monitor the changes of the Makefile configuration.
# If the configuration has changed, we update the checksum file to let the files
# which depend on it to regenerate.

CHECKSUM := "$(RELEASE)$(PROFILE)$(PROFILE_FORMAT)$(BENCHMARK)$(CONSOLE)$(JSR_256)$(JSR_082)$(JSR_179)$(CONFIG)$(NAME)$(DESCRIPTION)$(ORIGIN)"
OLD_CHECKSUM := "$(shell [ -f .checksum ] && cat .checksum)"
$(shell [ $(CHECKSUM) != $(OLD_CHECKSUM) ] && echo $(CHECKSUM) > .checksum)

toBool = $(if $(findstring 1,$(1)),true,false)
PREPROCESS = python2 tools/preprocess-1.1.0/lib/preprocess.py -s \
             -D RELEASE=$(call toBool,$(RELEASE)) \
             -D PROFILE=$(PROFILE) \
             -D PROFILE_FORMAT=$(PROFILE_FORMAT) \
             -D BENCHMARK=$(call toBool,$(BENCHMARK)) \
             -D CONSOLE=$(call toBool,$(CONSOLE)) \
             -D JSR_256=$(JSR_256) \
             -D JSR_179=$(JSR_179) \
             -D CONTENT_HANDLER_FILTER=$(CONTENT_HANDLER_FILTER) \
             -D CONFIG=$(CONFIG) \
             -D NAME="$(NAME)" \
             -D MIDLET_NAME="$(MIDLET_NAME)" \
             -D DESCRIPTION="$(DESCRIPTION)" \
             -D ORIGIN=$(ORIGIN) \
             -D VERSION=$(VERSION) \
             $(NULL)
PREPROCESS_SRCS = $(shell find . -name "*.in" -not -path config/build.js.in)
PREPROCESS_DESTS = $(PREPROCESS_SRCS:.in=)

all: config-build java jasmin tests j2me shumway aot bench/benchmark.jar bld/main-all.js

$(shell mkdir -p build_tools)

XULRUNNER_VERSION=31.0
OLD_XULRUNNER_VERSION := $(shell [ -f build_tools/.xulrunner_version ] && cat build_tools/.xulrunner_version)
$(shell [ "$(XULRUNNER_VERSION)" != "$(OLD_XULRUNNER_VERSION)" ] && echo $(XULRUNNER_VERSION) > build_tools/.xulrunner_version)

SLIMERJS_VERSION=0.10.0pre
OLD_SLIMERJS_VERSION := $(shell [ -f build_tools/.slimerjs_version ] && cat build_tools/.slimerjs_version)
$(shell [ "$(SLIMERJS_VERSION)" != "$(OLD_SLIMERJS_VERSION)" ] && echo $(SLIMERJS_VERSION) > build_tools/.slimerjs_version)

SOOT_VERSION=25Mar2015
OLD_SOOT_VERSION := $(shell [ -f build_tools/.soot_version ] && cat build_tools/.soot_version)
$(shell [ "$(SOOT_VERSION)" != "$(OLD_SOOT_VERSION)" ] && echo $(SOOT_VERSION) > build_tools/.soot_version)

CLOSURE_COMPILER_VERSION=pluotsorbet-v20150814
OLD_CLOSURE_COMPILER_VERSION := $(shell [ -f build_tools/.closure_compiler_version ] && cat build_tools/.closure_compiler_version)
$(shell [ "$(CLOSURE_COMPILER_VERSION)" != "$(OLD_CLOSURE_COMPILER_VERSION)" ] && echo $(CLOSURE_COMPILER_VERSION) > build_tools/.closure_compiler_version)

SPIDERMONKEY_VERSION=38.1.0esr
OLD_SPIDERMONKEY_VERSION := $(shell [ -f build_tools/.spidermonkey_version ] && cat build_tools/.spidermonkey_version)
$(shell [ "$(SPIDERMONKEY_VERSION)" != "$(OLD_SPIDERMONKEY_VERSION)" ] && echo $(SPIDERMONKEY_VERSION) > build_tools/.spidermonkey_version)

PATH := build_tools/spidermonkey:build_tools/slimerjs-$(SLIMERJS_VERSION):${PATH}

UNAME_S := $(shell uname -s)
UNAME_M := $(shell uname -m)
ifeq ($(UNAME_S),Linux)
	PLATFORM=linux-$(UNAME_M)
	XULRUNNER_PATH=xulrunner/xulrunner
	XULRUNNER_EXT=tar.bz2
	BOOTCLASSPATH_SEPARATOR=:
endif
ifeq ($(UNAME_S),Darwin)
	PLATFORM=mac
	XULRUNNER_PATH=XUL.framework/Versions/Current/xulrunner
	XULRUNNER_EXT=tar.bz2
	BOOTCLASSPATH_SEPARATOR=:
endif
ifneq (,$(findstring MINGW,$(UNAME_S)))
	PLATFORM=win32
	XULRUNNER_PATH=xulrunner
	XULRUNNER_EXT=zip
	BOOTCLASSPATH_SEPARATOR=;
endif
ifneq (,$(findstring CYGWIN,$(UNAME_S)))
	PLATFORM=win32
	XULRUNNER_PATH=xulrunner
	XULRUNNER_EXT=zip
	BOOTCLASSPATH_SEPARATOR=;
endif

# The path to the XULRunner executable that is actually used by the test runner.
# Set this on the command line to run tests against a different version
# of XULRunner than the default (which is specified by XULRUNNER_PATH),
# so you can test changes for compatibility with older and newer versions
# of Gecko than the default.  For example:
#
#   make test XULRUNNER=/Applications/Firefox28.app/Contents/MacOS/firefox
#
XULRUNNER ?= build_tools/$(XULRUNNER_PATH)

test: all build_tools/slimerjs-$(SLIMERJS_VERSION) build_tools/$(XULRUNNER_PATH)
	SLIMERJSLAUNCHER=$(XULRUNNER) tests/runtests.py

build_tools/slimerjs-$(SLIMERJS_VERSION): build_tools/.slimerjs_version
	rm -rf build_tools/slimerjs*
	wget -P build_tools -N https://ftp.mozilla.org/pub/mozilla.org/labs/j2me.js/slimerjs-0.10.0pre-2014-12-17.zip
	unzip -o -d build_tools build_tools/slimerjs-0.10.0pre-2014-12-17.zip
	touch build_tools/slimerjs-$(SLIMERJS_VERSION)

build_tools/$(XULRUNNER_PATH): build_tools/.xulrunner_version
	#rm -rf build_tools/XUL* build_tools/xul*
	#wget -P build_tools -N https://ftp.mozilla.org/pub/mozilla.org/xulrunner/releases/$(XULRUNNER_VERSION)/runtimes/xulrunner-$(XULRUNNER_VERSION).en-US.$(PLATFORM).$(XULRUNNER_EXT)
#ifeq ($(XULRUNNER_EXT),tar.bz2)
#	tar x -C build_tools -f build_tools/xulrunner-$(XULRUNNER_VERSION).en-US.$(PLATFORM).$(XULRUNNER_EXT) -m
#else
#	unzip -o -d build_tools build_tools/xulrunner-$(XULRUNNER_VERSION).en-US.$(PLATFORM).$(XULRUNNER_EXT)
#endif

build_tools/soot-trunk.jar: build_tools/.soot_version
	rm -f build_tools/soot-trunk.jar
	# link not dead yet but better include it in the project before it dies
	#wget -P build_tools https://github.com/marco-c/soot/releases/download/soot-$(SOOT_VERSION)/soot-trunk.jar
	cp tools/soot-trunk.jar build_tools/soot-trunk.jar
	touch build_tools/soot-trunk.jar

build_tools/closure.jar: build_tools/.closure_compiler_version
	rm -f build_tools/closure.jar
	# NOTE : dead link :(, a replacement is now included
	# built from https://github.com/google/closure-compiler/tree/maven-v20150901
	# wget -P build_tools https://github.com/mykmelez/closure-compiler/releases/download/$(CLOSURE_COMPILER_VERSION)/closure.jar
	cp tools/closure.jar build_tools/closure.jar
	touch build_tools/closure.jar

JS=build_tools/spidermonkey/js

$(JS): build_tools/.spidermonkey_version
	# NOTE : dead link
	#rm -rf build_tools/spidermonkey build_tools/jsshell*
	#wget -P build_tools -N https://ftp.mozilla.org/pub/mozilla.org/firefox/nightly/$(SPIDERMONKEY_VERSION)-candidates/build1/jsshell-$(PLATFORM).zip
	#unzip -o -d build_tools/spidermonkey build_tools/jsshell-$(PLATFORM).zip
	#chmod +x build_tools/spidermonkey/*
	#touch $(JS)

$(PREPROCESS_DESTS): $(PREPROCESS_SRCS) .checksum
	$(foreach file,$(PREPROCESS_SRCS),$(PREPROCESS) -o $(file:.in=) $(file);)

jasmin:
	make -C tools/jasmin-2.4 JAVA_VER=$(JAVA_VER)

relooper:
	make -C jit/relooper/


bld/j2me.js: $(BASIC_SRCS) $(JIT_SRCS) build_tools/closure.jar .checksum
	@echo "Building J2ME"
	tsc --sourcemap --target ES5 references.ts -d --out bld/j2me.js
ifeq ($(RELEASE),1)
	java -jar build_tools/closure.jar --warning_level $(CLOSURE_WARNING_LEVEL) --language_in ECMASCRIPT5 -O $(J2ME_JS_OPTIMIZATION_LEVEL) bld/j2me.js > bld/j2me.cc.js \
		&& mv bld/j2me.cc.js bld/j2me.js
endif

bld/j2me-jsc.js: $(BASIC_SRCS) $(JIT_SRCS)
	@echo "Building J2ME AOT Compiler"
	tsc --sourcemap --target ES5 references-jsc.ts -d --out bld/j2me-jsc.js

bld/jsc.js: jsc.ts bld/j2me-jsc.js
	@echo "Building J2ME JSC CLI"
	tsc --sourcemap --target ES5 jsc.ts --out bld/jsc.js

# Some scripts use ES6 features, so we have to specify ES6 as the in-language
# in order for Closure to compile them, even though for now we're optimizing
# "WHITESPACE_ONLY".
bld/main-all.js: $(MAIN_JS_SRCS) build_tools/closure.jar .checksum
	java -jar build_tools/closure.jar $(CLOSURE_FLAGS) --warning_level $(CLOSURE_WARNING_LEVEL) --language_in ES6 --create_source_map bld/main-all.js.map --source_map_location_mapping "|../" -O WHITESPACE_ONLY $(MAIN_JS_SRCS) > bld/main-all.js
	echo '//# sourceMappingURL=main-all.js.map' >> bld/main-all.js

j2me: bld/j2me.js bld/jsc.js

aot: bld/classes.jar.js
bld/classes.jar.js: java/classes.jar bld/jsc.js aot-methods.txt build_tools/closure.jar $(JS) .checksum
	@echo "Compiling ..."
	js bld/jsc.js -cp java/classes.jar -d -jf java/classes.jar -mff aot-methods.txt > bld/classes.jar.js
ifeq ($(RELEASE),1)
	java -jar build_tools/closure.jar --warning_level $(CLOSURE_WARNING_LEVEL) --language_in ECMASCRIPT5 -O SIMPLE bld/classes.jar.js > bld/classes.jar.cc.js \
		&& mv bld/classes.jar.cc.js bld/classes.jar.js
endif

bld/tests.jar.js: tests/tests.jar bld/jsc.js $(JS) aot-methods.txt
	java -jar build_tools/closure.jar --warning_level $(CLOSURE_WARNING_LEVEL) --language_in ECMASCRIPT5 -O SIMPLE bld/jsc.js -cp java/classes.jar tests/tests.jar -d -jf tests/tests.jar -mff aot-methods.txt > bld/tests.jar.js

bld/program.jar.js: program.jar bld/jsc.js $(JS) aot-methods.txt
	java -jar build_tools/closure.jar --warning_level $(CLOSURE_WARNING_LEVEL) --language_in ECMASCRIPT5 -O SIMPLE bld/jsc.js -cp java/classes.jar program.jar -d -jf program.jar -mff aot-methods.txt > bld/program.jar.js

shumway: bld/shumway.js
bld/shumway.js: $(SHUMWAY_SRCS)
	tsc --sourcemap --target ES5 shumway/references.ts --out bld/shumway.js

# We should update config/build.js everytime to generate the new VERSION number
# based on current time.
config-build: config/build.js.in
	$(PREPROCESS) -o config/build.js config/build.js.in

tests/tests.jar: tests
tests: java jasmin
ifeq ($(TESTS),1)
	make -C tests JAVA_VER=$(JAVA_VER)
endif

LANG_FILES=$(shell find l10n -name "*.xml")
LANG_DESTS=$(LANG_FILES:%.xml=java/%.json) java/custom/com/sun/midp/i18n/ResourceConstants.java java/custom/com/sun/midp/l10n/LocalizedStringsBase.java

java/classes.jar: java
java: $(LANG_DESTS) build_tools/soot-trunk.jar
	make -C java JAVA_VER=$(JAVA_VER)

$(LANG_DESTS): $(LANG_FILES)
	rm -rf java/l10n/
	mkdir java/l10n/
	$(foreach file,$(LANG_FILES), tools/xml_to_json.py $(file) java/$(file:.xml=.json);)
	mkdir -p java/custom/com/sun/midp/i18n/ java/custom/com/sun/midp/l10n/
	tools/xml_to_java_classes.py l10n/en-US.xml

certs:
	make -C certs

img/icon-128.png: $(ICON_128)
	cp $(ICON_128) img/icon-128.png
img/icon-512.png: $(ICON_512)
	cp $(ICON_512) img/icon-512.png

icon: img/icon-128.png img/icon-512.png

# Makes an output/ directory containing the packaged open web app files.
app: config-build java certs j2me aot bld/main-all.js icon $(TESTS_JAR) $(PROFILE_DEP)
	tools/package.sh

package: app
	rm -f '$(NAME)-$(VERSION).zip'
	cd $(PACKAGE_DIR) && zip -r '../$(NAME)-$(VERSION).zip' *

BENCHMARK_SRCS=$(shell find bench -name "*.java")

bench/benchmark.jar: $(BENCHMARK_SRCS) java/classes.jar tests/tests.jar
ifeq ($(TESTS),1)
	rm -rf bench/build
	mkdir bench/build
	javac -source $(JAVA_VER) -target $(JAVA_VER) -encoding UTF-8 -bootclasspath "java/classes.jar$(BOOTCLASSPATH_SEPARATOR)tests/tests.jar" -extdirs "" -d bench/build $(BENCHMARK_SRCS) > /dev/null
	cd bench/build && jar cf0 ../benchmark.jar *
	rm -rf bench/build
endif

clean:
	rm -rf bld $(PACKAGE_DIR)
	rm -f $(PREPROCESS_DESTS)
	make -C tools/jasmin-2.4 clean
	make -C tests clean
	make -C java clean
	rm -rf java/l10n/
	rm -f java/custom/com/sun/midp/i18n/ResourceConstants.java java/custom/com/sun/midp/l10n/LocalizedStringsBase.java
	rm -f bench/benchmark.jar
	rm -f img/icon-128.png img/icon-512.png
	rm -f package.zip
