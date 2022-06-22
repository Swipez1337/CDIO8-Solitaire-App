import cv2
import os.path as path
import screen
from os.path import dirname, join


def showImage(testImage, rois):
    cv2.namedWindow(testImage, cv2.WINDOW_AUTOSIZE)  # Create window with freedom of dimensions
    imS = cv2.resize(rois, (1080, 780))  # Resize image
    cv2.imshow(testImage, imS)  # Show image
    cv2.waitKey(0)  # Display the image infinitely until any keypres

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