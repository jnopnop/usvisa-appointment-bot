# Visa Appointment Bot
## Installation

1. Download and install Chrome Driver 
   2. Grab the Stable one here https://googlechromelabs.github.io/chrome-for-testing/
   3. Unzip somewhere and remember the path e.g. /usr/local/bin/chromedriver
   4. Make sure it's executable, I just did `chmod 777 /usr/local/bin/chromedriver`
2. Open application.yml and fill the missing properties. Note the comment for `telegram.statusMessageId`
3. If you have a group appointment, check the comment in `UserActions.tryBook`
4. Build and give it a shot in the IDE, let the bot send the first message, set the property from step #2
5. `mvn clean install`
6. Copy the jar file somewhere and remember the path as `{binaryPath}`
7. Now the **recurring** part, taken from here https://medium.com/@chetcorcos/a-simple-launchd-tutorial-9fecfcf2dbb3
8. Open `org.nop.usvisa.plist` and update following fields (use absolute paths):
   9. StartInterval (in seconds), set to 10m by default, I wouldn't go under 5m really but that's up to you
   10. StandardErrorPath and StandardOutPath to be able to read the app logs whenever it runs
   11. WorkingDirectory
   12. ProgramArguments. Set full path to your java binary and in the last argument - relative path from `{binaryPath}`
13. Copy the scheduled task definition to where the MacOS expects it `cp org.nop.usvisa.plist ~/Library/LaunchAgents/`
14. `launchctl load ~/Library/LaunchAgents/org.nop.usvisa.plist`
15. `tail -f {here's your StandardErrorPath or StandardOutPath}` to see logs in action
16. How to stop: `launchctl unload ~/Library/LaunchAgents/org.nop.usvisa.plist`

## Acknowledgment
Thanks to @dvalbuena1 and his work at https://github.com/dvalbuena1/visa_rescheduler_aws/tree/main