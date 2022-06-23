import cv2
import os.path as path
import screen
from os.path import dirname, join

# this code is modiefied code from https://github.com/naderchehab/card-detector
# @author https://github.com/naderchehab, @author s201729, @author s183925
# loads image and returns it
def getImage(name, template):
    # filename = name + '.png'
    filename = join(dirname(__file__), name + '.png')
    if template:
        image = cv2.imread(path.join(filename))
    else:
        filename = join(dirname(__file__), name)
        image = cv2.imread(path.join(filename))

    image = screen.imageToBw(image)
    return image

def getPath():
    # filename = name + '.png'
    filename = join(dirname(__file__), 'test2.png')
    return filename