from PIL import Image
import main
import base64
from os.path import dirname, join
from com.chaquo.python import Python
import glob
import os
files_dir = str(Python.getPlatform().getApplication().getFilesDir())
def sendPicture(imageData):
    imgdata = base64.b64decode(imageData)
    files_dir = str(Python.getPlatform().getApplication().getFilesDir())
    #dirname(__file__)
    filename = "/test2.png"
    path = "".join((files_dir, filename))
    with open(path, 'wb') as f:
        f.write(imgdata)
        f.close()
    result = main.recognizeTakenImage(path)
    image = cv2.imread(filename)
    return result
