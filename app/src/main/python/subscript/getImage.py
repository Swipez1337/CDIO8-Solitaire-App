import base64
from PIL import Image
from io import BytesIO

def getImage(imageData: String)
    #code largely gotten from:
    #https://moonbooks.org/Articles/How-to-convert-a-base64-image-in-png-format-using-python-/
    image = Image.open(base64.b64decode(imageData))
    image.show()
    return

