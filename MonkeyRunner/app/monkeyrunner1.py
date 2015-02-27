#vogella link

__author__ = 'Rahul Kurup'
from com.android.monkeyrunner import MonkeyRunner, MonkeyDevice
import commands
import sys
import os

# starting the application and test
print ("Starting the monkeyrunner script")
if not os.path.exists("monkeyrunnerscreenshots"):
    print ("creating the screenshots directory")
    os.makedirs("monkeyrunnerscreenshots")

# connection to the current device, and return a MonkeyDevice object
device = MonkeyRunner.waitForConnection()

apk_path = device.shell('pm path com.game.accballlite')
if apk_path.startswith('package:'):
 print ("application installed.")
else:
 print ("not installed, install APK")
 device.installPackage('com.game.accballlite')

print ("starting application....")
device.startActivity(component='com.game.accballite.BaseActivity')
#screenshot
for x in range(0, 10):
 MonkeyRunner.sleep(1)
 result = device.takeSnapshot()
 output='/shot'+str(x)+'.png'
 result.writeToFile(output,'png')
 device.shell('screencap -p /sdcard/'+output+'.png')
print ("screenshot taken and stored on device")
print ("Finishing the test")