# this code is modified from https://github.com/naderchehab/card-detector/blob/master/screen.py
# @author https://github.com/naderchehab
import numpy as np
from PIL import ImageGrab
import cv2

def imageToBw(image):
    imageGray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    return imageGray

def capture():
    screen = ImageGrab.grab()
    imageNumpy = np.array(screen)
    return imageNumpy

def showImage(image):
    cv2.imshow('result', image)
    cv2.waitKey(1) # for testing use 0


