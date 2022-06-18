from PIL import Image
import base64
from com.chaquo.python import Python
def sendPicture(imageData):
    imgdata = base64.b64decode(imageData)
    files_dir = str(Python.getPlatform().getApplication().getFilesDir())
    filename = "/currentImage.png"
    path = "".join((files_dir,filename))
    with open(path, 'wb') as f:
        f.write(imgdata)
    return path

