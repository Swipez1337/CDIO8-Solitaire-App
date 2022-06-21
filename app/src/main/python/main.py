# project is modified code from: https://github.com/naderchehab/card-detector
import sys
from com.chaquo.python import Python
import cv2
import layoutMatches
import numpy as np
import os.path as path
import columnsDividedDTO
from numpy import cos, sin
import imageModification
import templateMatching
import screen
import testSets
import testMethods
import time
from functools import cmp_to_key
from os.path import dirname, join
import settings
from settings import relXval, relYval
from Identity import Identity
from MatchCombination import MatchCombination
from displayAndFetch import getImage, showImage
from imageModification import addPadding

# when True displays image with detected areas
from matchOrganising import transformToCards
import sys
import settings

show = False
testMode = False
testImages = ['test2.png', 'test6.png', 'test8.png', 'test11.png', 'test12.png']
testImages = ['test27.jpg']
testIm = 'test27.jpg'
matchingThresholds = [.80, .81, .82, .83, .84, .85, .86]
matchingThresholds = [.80]
# range of rotation to be applied to source image
rotations = [-4,-2,0,2,4]
rotations = [0]
# dimensions of image
# dimensions = settings.dimensions
# chosen dimensions for image
imageDim = settings.imageDim
# dimensions of padding added
padDim = settings.padDim
# base dimensions that relative x, y distances are calculated from
baseDim = settings.baseDim
# NEEDS DONE, SET NECESSARY RELATIVE AREA TO SEARCH
areaToScanTopLeft = settings.areaToScanTopLeft
areaToScanBottomRight = settings.areaToScanBottomRight
# things we're looking for
suits = testSets.suits
ranks = testSets.values

allCards = {v + ' ' + s for s in suits for v in ranks}

# cards found so far
cardsDetected = set()

suitsDict = {}
for suit in suits:
    suitsDict[suit] = getImage(suit, True)

ranksDict = {}
for rank in ranks:
    ranksDict[rank] = getImage(rank, True)

backsideTemplate = getImage("backside", True)

def recognizeImage():
    for threshold in matchingThresholds:
        print('THRESHOLD: ' + str(threshold) + '\n')
        for test in testImages:
            start_time = time.time()
            result = watchAndDisplayCards(test, threshold)
            print("--- %s seconds ---" % (time.time() - start_time))
    return result

def recognizeTakenImage(path):
    result = watchAndDisplayCards(path, .80)
    return result

# get the coordinates of a point rotated minus 'degrees' around center of image
def rotationBacktrack(coordinates, degrees=0):
    x = coordinates[0]
    y = coordinates[1]
    radians = (0.0174532925199 * degrees)
    middleX = padDim[0] / 2
    middleY = padDim[1] / 2
    x = x - padDim[0] / 2
    y = y - padDim[1] / 2
    newX = x * cos(radians) - y * sin(radians) + middleX
    newY = x * sin(radians) + y * cos(radians) + middleY
    return int(newX), int(newY)

# This is the main function that is executed continuously to watch for new cards and display them
def watchAndDisplayCards(imagePath, matchingThreshold):
    cardsDetected.clear()
    allMatches = []
    allMatchSets = list()
    for rotation in rotations:
        # image = cv2.imread(path.join(testImage))
        if not testMode:
            filename = join(dirname(__file__), testIm)
            image = cv2.imread(path.join(filename))
        else:
            image  = cv2.imread(imagePath)



        image = cv2.resize(image, (imageDim[0], imageDim[1]))
        # adds padding to prevent going out of bounds when searching in rotated image
        image = addPadding(image, padDim)
        image = screen.imageToBw(image)
        # rotates image by given degrees
        image = imageModification.rotate(image, rotation)
        areaToScan = image[areaToScanTopLeft[1]:areaToScanBottomRight[1], areaToScanTopLeft[0]:areaToScanBottomRight[0]]

        backsideMatches = templateMatching.getMatches(areaToScan, backsideTemplate, matchingThreshold)
        backsideMatches = map(lambda match: {'actualLoc': match, 'name': 'backside'}, backsideMatches)

        # does this work with rotation?
        backsideList = list()
        for match in backsideMatches:
            result = rotationBacktrack(match['actualLoc'], rotation)
            match['actualLoc'] = (result[0], result[1])
            backsideList.append(match)


        for suit in suitsDict:

            suitTemplate = suitsDict[suit]
            suitMatchesOrigin = templateMatching.getMatches(areaToScan, suitTemplate, matchingThreshold)

            # find coordinates of matches in 0 degree rotated image
            permsuitMatchesOrigin = list(suitMatchesOrigin)
            suitActualLoc = []
            for suitMatch in permsuitMatchesOrigin:
                result = rotationBacktrack(suitMatch, rotation)
                suitActualLoc += result

            # map locations of given suit type
            suitMatches = map(lambda match: {'topLeft': match, 'actualLoc': (0, 0), 'name': suit}, permsuitMatchesOrigin)
            # insert actual locations of matches into map
            i = 0
            suitList = list()
            for match in suitMatches:
                match['actualLoc'] = (suitActualLoc[i], suitActualLoc[i + 1])
                suitList.append(match)
                i += 2

            # We found a suit, now find the associated rank above it (if any)
            allRankMatches = []
            for suitMatch in suitList:
                suitMatchTopLeft = suitMatch['topLeft']
                # define search area for ranks
                topLeft = (int(suitMatchTopLeft[0] - relXval(5)), int(suitMatchTopLeft[1] - relYval(50)))
                bottomRight = (int(suitMatchTopLeft[0] + relXval(50)), int(suitMatchTopLeft[1] + 0))
                searchArea = areaToScan[topLeft[1]:bottomRight[1], topLeft[0]:bottomRight[0]]

                # list of maps of ranks for a given suit match
                rankMatchSets = []
                for rank in ranksDict:

                    rankTemplate = ranksDict[rank]
                    rankMatch = templateMatching.getMatches(searchArea, rankTemplate, matchingThreshold)

                    # map locations of matches for given rank
                    rankMatch = map(
                        lambda match: {'actualLoc': (topLeft[0] + match[0], topLeft[1] + match[1]), 'name': rank},
                        rankMatch)

                    # calculate and insert coordinates of matches in 0 degree rotated image into map
                    rankList = list()
                    for match in rankMatch:
                        result = rotationBacktrack(match['actualLoc'], rotation)
                        match['actualLoc'] = (result[0], result[1])
                        rankList.append(match)

                    # save single instance of every card detected
                    if len(rankList) > 0:
                        rankMatchSets += rankList
                        cardsDetected.add(rank + ' ' + suit)
                    # store matches of a given rank
                    rankMatchList = list()
                    for match in rankList:
                        rankMatchList.append(match)
                    allRankMatches = allRankMatches + rankMatchList
                # add a suit match with its rank matches to list of all sets
                if len(rankMatchSets) > 0:
                    matchCombination = MatchCombination(suitMatch, rankMatchSets)
                    allMatchSets.append(matchCombination)
            # store all suit and rank matches
            suitMatchList = list()
            for match in suitList:
                suitMatchList.append(match)
            # allMatches = allMatches + suitMatchList + allRankMatches
            allMatches += suitList + allRankMatches

        if (len(backsideList) > 0):
            allMatches += backsideList

            for match in backsideList:
                backsideObj = MatchCombination(match)
                allMatchSets.append(backsideObj)


    finalList = transformToCards(allMatchSets)
    columnList = layoutMatches.divideIntoColumns(finalList)
    # layoutMatches.printColumnsDivided(columnList)
    if columnList == - 1:
        return - 1
    columnsDividedDTO.getJsonList(columnList)
    return columnsDividedDTO.getJsonList(columnList)
