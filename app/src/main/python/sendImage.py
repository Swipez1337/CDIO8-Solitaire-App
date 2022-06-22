from PIL import Image
import main
import base64
from os.path import dirname, join
from com.chaquo.python import Python
import glob
import os
files_dir = str(Python.getPlatform().getApplication().getFilesDir())
def sendPicture(path):
    result = main.recognizeTakenImage(path)
    return result
