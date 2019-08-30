fastlane documentation
================
# Installation

Make sure you have the latest version of the Xcode command line tools installed:

```
xcode-select --install
```

Install _fastlane_ using
```
[sudo] gem install fastlane -NV
```
or alternatively using `brew cask install fastlane`

# Available Actions
## Android
### android buildMr
```
fastlane android buildMr
```
Build Bink MR
### android buildBeta
```
fastlane android buildBeta
```
Build Bink Beta
### android buildGamma
```
fastlane android buildGamma
```
Build Bink Gamma
### android buildExternal
```
fastlane android buildExternal
```
Build Bink External UAT
### android buildRelease
```
fastlane android buildRelease
```
Build Bink Release
### android buildNightly
```
fastlane android buildNightly
```
Build Bink Nightly

----

This README.md is auto-generated and will be re-generated every time [fastlane](https://fastlane.tools) is run.
More information about fastlane can be found on [fastlane.tools](https://fastlane.tools).
The documentation of fastlane can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
